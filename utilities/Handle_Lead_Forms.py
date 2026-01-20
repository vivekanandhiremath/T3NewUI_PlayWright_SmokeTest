# utilities/Handle_Lead_Forms.py

from time import sleep

from utilities.DriverUtils import DriverUtils
from utilities.customlogger import get_test_logger


class LeadFormsHandler:
    logger = get_test_logger()

    @staticmethod
    def generate_new_email():
        """Generate a random email address with current date and time for testing"""
        from datetime import datetime
        current_time = datetime.now().strftime("%Y%m%d_%I%M%S%p").lower()
        return f"shankar{current_time}@gmail.com"

    @staticmethod
    def handle_initial_lead_form(home_page, lead_type="normal", vehicle_details=None):
        """Handle the initial lead form if present"""
        email = LeadFormsHandler.generate_new_email()
        print(f"generated email{email}")
        sleep(5)
        try:
            if home_page.is_initial_form_visible():
                sleep(3)
                LeadFormsHandler.logger.log_info_to_both_file_and_allure(
                    "Initial lead form is visible. Filling and submitting...")
                if home_page.is_close_icon_visible():
                    LeadFormsHandler.logger.log_info_to_both_file_and_allure(
                        "Since the initial lead close icon is visible, this initial lead form is optional."
                    )
                    home_page.fill_and_submit_initial_lead_form("Sendto", "All", email)
                    home_page.click_on_close_button_after_initial_lead_form()
                    LeadFormsHandler.logger.log_success_to_both_file_and_allure(
                        "Submitted initial lead form and closed thank you popup.")
                else:
                    LeadFormsHandler.logger.log_info_to_both_file_and_allure(
                        "Since the initial lead close icon is not visible, this initial lead form is mandatory."
                    )
                    home_page.fill_and_submit_initial_lead_form("Sendto", "All", email)
                    home_page.click_on_close_button_after_initial_lead_form()
                    LeadFormsHandler.logger.log_success_to_both_file_and_allure(
                        "Submitted initial lead form and closed thank you popup.")
            else:
                LeadFormsHandler.logger.log_info_to_both_file_and_allure("Initial lead form not displayed.")

            # Check vehicle details side menu visibility
            if not home_page.is_vehicle_details_side_menu_visible():
                LeadFormsHandler.logger.log_warning_to_both_file_and_allure(
                    "Vehicle Details Side Menu is not visible after handling lead form.")
                return None

            # Handle different lead types
            if isinstance(lead_type, str) and lead_type.lower() == "browserclose":
                DriverUtils.quit_driver()
                LeadFormsHandler.logger.log_info_to_both_file_and_allure(
                    "Browser closed as per lead type: browserClose")
                return None
            elif isinstance(lead_type, str) and lead_type.lower() == "widgetclose":
                home_page.click_on_widget_close()
                LeadFormsHandler.logger.log_info_to_both_file_and_allure("Widget closed as per lead type: widgetclose")
            elif isinstance(lead_type, str) and lead_type.lower() == "idle":
                LeadFormsHandler.logger.log_info_to_both_file_and_allure(
                    "Initial Lead form is submitted and kept idle.")
            elif isinstance(lead_type, str) and lead_type.lower() == "normal":
                LeadFormsHandler.logger.log_info_to_both_file_and_allure("Initial Lead form is submitted")
            else:
                LeadFormsHandler.logger.log_info_to_both_file_and_allure(
                    "Initial Lead form is submitted with vehicle details")

            return email

        except Exception as e:
            LeadFormsHandler.logger.log_error_to_both_file_and_allure(f"Error handling initial lead form: {str(e)}")
            return None

    @staticmethod
    def handle_test_drive_form(test_drive_page, home_page, vehicle_details, email, lead_type="normal"):
        """Handle the test drive form submission

        Args:
            test_drive_page: Instance of TestDrivePage
            home_page: Instance of HomePage
            vehicle_details: Dictionary containing vehicle details
            email: Email address to use
            lead_type: Type of lead (default: "normal")
        """
        try:
            if not test_drive_page.is_test_drive_button_displayed():
                LeadFormsHandler.logger.log_warning_to_both_file_and_allure("Test Drive button not displayed.")
                return False

            test_drive_page.click_on_test_drive_button()
            test_drive_page.fill_test_drive_form_and_submit(
                "test", "test", email, "9", "00", "am"
            )
            LeadFormsHandler.logger.log_info_to_both_file_and_allure("Submitted Test Drive form")

            if lead_type.lower() == "widgetclose":
                test_drive_page.click_on_test_drive_thank_you_popup_close_button()
                home_page.click_on_widget_close()
            elif lead_type.lower() == "browserclose":
                DriverUtils.quit_driver()
            elif lead_type.lower() == "idle":
                LeadFormsHandler.logger.log_info_to_both_file_and_allure("Test drive form is submitted and kept idle.")
            elif lead_type.lower() == "normal":
                test_drive_page.click_on_test_drive_thank_you_popup_close_button()

            return True

        except Exception as e:
            LeadFormsHandler.logger.log_error_to_both_file_and_allure(f"Error handling test drive form: {str(e)}")
            return False

    @staticmethod
    def handle_pre_qual_form(pre_qual_page, home_page, vehicle_details, email, lead_type="normal"):
        """Handle the pre-qualification form submission"""
        try:
            if not pre_qual_page.is_pre_qual_link_displayed():
                LeadFormsHandler.logger.log_warning_to_both_file_and_allure("Pre Qual button not displayed.")
                return False

            pre_qual_page.click_pre_qual_on_payment_section()
            pre_qual_page.fill_pre_qual_form_if_empty_and_submit(
                "test", "test", email, "4356467890",
                "1535 Broadway, New York, NY 10036, USA",
                "MANHATTAN", "New York", "10036"
            )
            LeadFormsHandler.logger.log_info_to_both_file_and_allure("Submitted Pre-qual Form")

            if lead_type.lower() == "widgetclose":
                home_page.click_on_widget_close()
            elif lead_type.lower() == "browserclose":
                DriverUtils.quit_driver()
            elif lead_type.lower() == "idle":
                LeadFormsHandler.logger.log_info_to_both_file_and_allure("Pre Qual form is submitted and kept idle.")

            return True

        except Exception as e:
            LeadFormsHandler.logger.log_error_to_both_file_and_allure(f"Error handling pre-qual form: {str(e)}")
            return False

    @staticmethod
    def handle_apply_form_credit_form(afcp, home_page, vehicle_details, email, lead_type):
        """
        Handles the Apply For Credit form submission and subsequent actions based on lead type.

        Args:
            afcp: ApplyForCreditPage object
            hp: HomePage object
            vehicle_details: VehicleDetails object
            email: Email address for form submission
            lead_type: Type of lead handling ("widgetclose", "browserclose", "idle", "normal")
        """

        home_page.click_apply_for_credit_button()
        afcp.fill_apply_for_credit_form_and_submit("test", "test", email)
        LeadFormsHandler.logger.log_info_to_both_file_and_allure("Submitted Apply For Credit form")

        lead_type_lower = lead_type.lower()
        if lead_type_lower == "widgetclose":
            try:
                afcp.click_back_button()
                home_page.click_on_widget_close()
                LeadFormsHandler.logger.log_info_to_both_file_and_allure("Widget closed successfully.")
            except Exception as e:
                LeadFormsHandler.logger.log_error_to_both_file_and_allure(f"Failed to close widget: {e}")

        elif lead_type_lower == "browserclose":
            try:
                DriverUtils.quit_driver()
                LeadFormsHandler.logger.log_info_to_both_file_and_allure("Browser closed successfully.")
            except Exception as e:
                LeadFormsHandler.logger.log_warning_to_both_file_and_allure(f"Browser closure failed: {e}")

        elif lead_type_lower == "idle":
            LeadFormsHandler.logger.log_info_to_both_file_and_allure(
                "Apply For Credit form is submitted and kept idle.")

        elif lead_type_lower == "normal":
            LeadFormsHandler.logger.log_info_to_both_file_and_allure("Apply For Credit form is submitted.")
            sleep(3)
            if (afcp.is_back_button_clickable()):
                LeadFormsHandler.logger.log_debug_only_to_file("Back button is displayed and clickable.")
                afcp.click_back_button()
                LeadFormsHandler.logger.log_debug_only_to_file("clicked on back button.")
            else:
                LeadFormsHandler.logger.log_warning_to_both_file_and_allure(
                    "Back button is not displayed or not clickable.")


        else:
            LeadFormsHandler.logger.log_warning_to_both_file_and_allure(
                f"Unknown lead type: {lead_type}. Proceeding without special handling.")
