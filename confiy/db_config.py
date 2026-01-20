# config/db_config.py
from utilities.database_utils import DatabaseConfig, DatabaseType

# MySQL Configuration
MYSQL_CONFIG = DatabaseConfig(
    db_type=DatabaseType.MYSQL,
    host="eshop-us-uat.cbh6amtvql51.us-east-1.rds.amazonaws.com",
    database="fca_ore",
    user="qa-ore",
    password="kbTzg6nVEmggXn5SzZNV",
    pool_size=5
)

# SQLite Configuration (for testing)
SQLITE_CONFIG = DatabaseConfig(
    db_type=DatabaseType.SQLITE,
    sqlite_file="test.db"
)

# Use this to switch between configurations
CURRENT_CONFIG = MYSQL_CONFIG  # or SQLITE_CONFIG
