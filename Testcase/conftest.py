# Testcase/conftest.py
import pytest

from utilities.database_utils import DatabaseConnection


@pytest.fixture(scope="session")
def db_connection():
    """
    Fixture that provides a database connection.
    Uses MYSQL_CONFIG from db_config for actual database operations.
    """
    from confiy.db_config import MYSQL_CONFIG

    # Initialize the database connection
    db = DatabaseConnection(MYSQL_CONFIG)

    # Verify the connection
    try:
        with db.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT 1")
            assert cursor.fetchone()[0] == 1, "Database connection test failed"
    except Exception as e:
        pytest.fail(f"Failed to connect to database: {str(e)}")

    yield db

    # Cleanup (connection pooling handles this automatically)
