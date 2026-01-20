from utilities.customlogger import get_test_logger

logger = get_test_logger()


def test_specific_lead_by_email(db_connection, test_email):
    """Test querying a specific lead by email."""
    logger.log_to_both_console_and_file(f"Querying lead with specific email: {test_email}")

    try:
        results = db_connection.execute_query("""
            SELECT 
                created_at,
                session_id,
                vehicle_vin,
                dealer_code,
                zip,
                email,
                comments,
                lead_id,
                lead_source,
                source_id,
                additional_details 
            FROM leads 
            WHERE email = %s
            ORDER BY created_at DESC
        """, (test_email,))

        logger.log_to_both_console_and_file(f"Found {len(results)} records for email: {test_email}")

        if not results:
            logger.log_to_both_console_and_file(f"No records found for email: {test_email}")
            raise AssertionError(f"No lead found with email: {test_email}")  # This will trigger the fallback

        latest_lead = results[0]
        logger.log_to_both_console_and_file("Lead details:")
        for key, value in latest_lead.items():
            logger.log_to_both_console_and_file(f"  {key}: {value}")

        # Basic validations
        assert latest_lead['email'] == test_email, "Email doesn't match"
        assert latest_lead.get('lead_id'), "Lead ID is missing"
        assert latest_lead.get('created_at'), "Created at timestamp is missing"

        # Additional validations
        if latest_lead.get('vehicle_vin'):
            assert len(latest_lead['vehicle_vin']) == 17, "Invalid VIN length"
        if latest_lead.get('dealer_code'):
            assert latest_lead['dealer_code'].isdigit(), "Dealer code should be numeric"

        logger.log_to_both_console_and_file("✅ Specific lead test passed")
        return True

    except Exception as e:
        error_msg = f"Error in test_specific_lead_by_email: {str(e)}"
        logger.log_to_both_console_and_file(error_msg)
        raise


def test_specific_lead_in_private_offers_by_email(db_connection, test_email):
    try:
        # Get all column names from information_schema
        column_result = db_connection.execute_query("""
            SELECT column_name 
            FROM information_schema.columns 
            WHERE table_name = 'fca_ore_private_offers_details'
        """)

        # Handle both dictionary and tuple row formats
        if column_result and len(column_result) > 0:
            # Check if rows are dictionaries
            if hasattr(column_result[0], 'get'):
                columns = [row.get('column_name') for row in column_result]
            else:
                # Fallback to numeric index if not a dictionary
                columns = [row[0] for row in column_result if len(row) > 0]
        else:
            columns = []
            logger.log_to_both_console_and_file("No columns found for table fca_ore_private_offers_details")

        if not columns:
            logger.log_to_both_console_and_file("❌ Could not retrieve column information")
            return False

        # Build the SELECT query with all columns
        select_columns = ", ".join(columns)
        query = f"SELECT {select_columns} FROM fca_ore_private_offers_details WHERE email = %s"

        results = db_connection.execute_query(query, (test_email,))
        logger.log_to_both_console_and_file(
            f"ℹ️ Found {len(results)} records in private offers for email: {test_email}")

        if not results:
            logger.log_to_both_console_and_file("❌ No records found in private offers")
            return False

        latest_offer = results[0]

        # Log all columns and values
        logger.log_to_both_console_and_file("\n📋 Private Offer Details:")

        # Check if results are dictionaries
        is_dict_result = hasattr(latest_offer, 'get')

        for column in columns:
            if is_dict_result:
                value = latest_offer.get(column, "N/A")
            else:
                # Fallback to numeric index if not a dictionary
                try:
                    col_index = columns.index(column)
                    value = latest_offer[col_index] if col_index < len(latest_offer) else "N/A"
                except (ValueError, IndexError):
                    value = "N/A"

            logger.log_to_both_console_and_file(f"   {column}: {value}")

        # Basic validations
        if 'email' in columns:
            if is_dict_result:
                if latest_offer.get('email') != test_email:
                    logger.log_to_both_console_and_file(
                        f"❌ Email doesn't match: expected {test_email}, got {latest_offer.get('email')}")
                    return False
            else:
                try:
                    email_idx = columns.index('email')
                    if latest_offer[email_idx] != test_email:
                        logger.log_to_console_only(
                            f"❌ Email doesn't match: expected {test_email}, got {latest_offer[email_idx]}")
                        return False
                except (ValueError, IndexError):
                    logger.log_to_both_console_and_file("❌ Email column not found in results")
                    return False

        logger.log_to_both_console_and_file("✅ Private offer verification passed")
        return True

    except Exception as e:
        error_msg = f"❌ Error in test_specific_lead_in_private_offers_by_email: {str(e)}"
        logger.log_to_both_console_and_file(error_msg)
        return False
