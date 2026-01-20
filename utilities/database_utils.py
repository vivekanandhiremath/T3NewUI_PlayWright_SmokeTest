# utilities/database_utils.py
import logging
import sqlite3
from contextlib import contextmanager
from dataclasses import dataclass
from enum import Enum
from sqlite3 import Connection
from typing import List, Dict, Any, Union

import mysql.connector
from mysql.connector import Error
from mysql.connector.abstracts import MySQLConnectionAbstract
from mysql.connector.pooling import PooledMySQLConnection

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class DatabaseType(Enum):
    MYSQL = "mysql"
    SQLITE = "sqlite"
    POSTGRESQL = "postgresql"


@dataclass
class DatabaseConfig:
    """Configuration class for database connections"""
    db_type: DatabaseType
    host: str = "localhost"
    port: int = 3306
    database: str = ""
    user: str = ""
    password: str = ""
    sqlite_file: str = "test.db"
    pool_size: int = 5


class DatabaseConnection:
    def __init__(self, config: DatabaseConfig):
        self.config = config
        self.connection_pool = None
        self._initialize_connection_pool()

    def _initialize_connection_pool(self):
        """Initialize the appropriate connection pool based on database type"""
        if self.config.db_type == DatabaseType.SQLITE:
            # SQLite doesn't need a connection pool, but we'll implement a simple one
            self.connection_pool = []
        else:
            try:
                if self.config.db_type == DatabaseType.MYSQL:
                    self.connection_pool = mysql.connector.pooling.MySQLConnectionPool(
                        pool_name="mypool",
                        pool_size=self.config.pool_size,
                        host=self.config.host,
                        port=self.config.port,
                        user=self.config.user,
                        password=self.config.password,
                        database=self.config.database,
                        autocommit=True
                    )
                # Add other database types here (PostgreSQL, etc.)
            except Error as e:
                logger.error(f"Error creating connection pool: {e}")
                raise

    @contextmanager
    def get_connection(self) -> Union[MySQLConnectionAbstract, PooledMySQLConnection, Connection]:
        """Get a database connection from the pool"""
        conn = None
        try:
            if self.config.db_type == DatabaseType.SQLITE:
                conn = sqlite3.connect(self.config.sqlite_file)
                conn.row_factory = sqlite3.Row
                yield conn
            else:
                conn = self.connection_pool.get_connection()
                yield conn
        except Error as e:
            logger.error(f"Error getting database connection: {e}")
            raise
        finally:
            if conn and self.config.db_type != DatabaseType.SQLITE:
                conn.close()  # Returns connection to the pool

    def execute_query(self, query: str, params: tuple = None, fetch_all: bool = True) -> List[Dict[str, Any]]:
        """Execute a SELECT query and return results"""
        with self.get_connection() as conn:
            cursor = conn.cursor(dictionary=True) if self.config.db_type != DatabaseType.SQLITE else conn.cursor()

            try:
                cursor.execute(query, params or ())
                if fetch_all:
                    result = [dict(row) for row in cursor.fetchall()]
                else:
                    result = [dict(row) for row in cursor.fetchone() or []]
                return result
            except Error as e:
                logger.error(f"Error executing query: {e}")
                raise
            finally:
                cursor.close()

    def execute_update(self, query: str, params: tuple = None) -> int:
        """Execute an INSERT, UPDATE, or DELETE query and return row count"""
        with self.get_connection() as conn:
            cursor = conn.cursor()
            try:
                cursor.execute(query, params or ())
                conn.commit()
                return cursor.rowcount
            except Error as e:
                conn.rollback()
                logger.error(f"Error executing update: {e}")
                raise
            finally:
                cursor.close()

    def execute_many(self, query: str, params_list: List[tuple]) -> int:
        """Execute multiple parameterized queries in a transaction"""
        with self.get_connection() as conn:
            cursor = conn.cursor()
            try:
                cursor.executemany(query, params_list)
                conn.commit()
                return cursor.rowcount
            except Error as e:
                conn.rollback()
                logger.error(f"Error executing batch update: {e}")
                raise
            finally:
                cursor.close()

    def table_exists(self, table_name: str) -> bool:
        """Check if a table exists in the database"""
        if self.config.db_type == DatabaseType.SQLITE:
            query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?"
            result = self.execute_query(query, (table_name,))
        elif self.config.db_type == DatabaseType.MYSQL:
            query = "SHOW TABLES LIKE %s"
            result = self.execute_query(query, (table_name,))
        else:
            raise ValueError(f"Unsupported database type: {self.config.db_type}")

        return len(result) > 0


# Example usage:
if __name__ == "__main__":
    # Example configuration
    config = DatabaseConfig(
        db_type=DatabaseType.MYSQL,
        host="localhost",
        database="your_database",
        user="your_username",
        password="your_password",
        pool_size=3
    )

    # Or for SQLite:
    # config = DatabaseConfig(
    #     db_type=DatabaseType.SQLITE,
    #     sqlite_file="test.db"
    # )

    db = DatabaseConnection(config)

    # Example query
    try:
        # Select example
        users = db.execute_query("SELECT * FROM users WHERE status = %s", ("active",))
        for user in users:
            print(f"User: {user['username']}, Email: {user['email']}")

        # Update example
        updated = db.execute_update(
            "UPDATE users SET last_login = NOW() WHERE id = %s",
            (user_id,)
        )
        print(f"Updated {updated} rows")

    except Error as e:
        print(f"Database error: {e}")
