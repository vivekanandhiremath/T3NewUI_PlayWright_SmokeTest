import inspect
import logging
import os
import sys
from datetime import datetime
from logging.handlers import RotatingFileHandler
from typing import Optional

import allure
import pytest
from selenium.webdriver.remote.webdriver import WebDriver

# Set up basic logging configuration
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class TestLogger:
    _instance = None
    _initialized = False

    def __new__(cls, test_name=None, driver: Optional[WebDriver] = None):
        if cls._instance is None:
            cls._instance = super(TestLogger, cls).__new__(cls)
            cls._instance._initialized = False
        if driver is not None:
            cls._instance.driver = driver
        return cls._instance

    def __init__(self, test_name=None, driver: Optional[WebDriver] = None):
        if not self._initialized:
            self.test_name = test_name or "test"
            self.driver = driver
            self.logger = logging.getLogger(self.test_name)
            self.logger.setLevel(logging.DEBUG)

            # Create logs directory if it doesn't exist
            os.makedirs('logs', exist_ok=True)

            # Create a unique log file name with timestamp
            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
            self.log_file = f'logs/automation_{self.test_name}_{timestamp}.log'

            # Clear existing handlers
            self.logger.handlers = []

            # File handler with rotation (10MB per file, keep 5 backups)
            file_handler = RotatingFileHandler(
                self.log_file,
                maxBytes=10 * 1024 * 1024,  # 10MB
                backupCount=5,
                encoding='utf-8'
            )
            file_handler.setLevel(logging.DEBUG)

            # Console handler
            console_handler = logging.StreamHandler(sys.stdout)
            console_handler.setLevel(logging.DEBUG)

            # Formatter
            formatter = logging.Formatter(
                '%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                datefmt='%Y-%m-%d %H:%M:%S'
            )

            file_handler.setFormatter(formatter)
            console_handler.setFormatter(formatter)

            # Add handlers
            self.logger.addHandler(file_handler)
            self.logger.addHandler(console_handler)

            self._initialized = True

    def set_driver(self, driver: WebDriver) -> None:
        """Update the WebDriver instance for taking screenshots"""
        self.driver = driver

    def _log_info(self, message):
        """Log info message"""
        self.logger.info(message)

    def _log_error(self, message, exc_info=False):
        """Log error message"""
        self.logger.error(message, exc_info=exc_info)

    def _log_warning(self, message):
        """Log warning message"""
        self.logger.warning(message)

    def _log_debug(self, message):
        """Log debug message"""
        self.logger.debug(message)

    def log_debug_only_to_file(self, message, *args, **kwargs):
        """Log a debug message to file only (not to console or Allure)"""
        self.logger.debug(message, *args, **kwargs)

    def log_step_to_file(self, message, level="INFO"):
        """Log step to file with specified level"""
        if level.upper() == "INFO":
            self.logger.info(f"STEP - {message}")
        elif level.upper() == "ERROR":
            self.logger.error(f"STEP - {message}")
        elif level.upper() == "WARNING":
            self.logger.warning(f"STEP - {message}")
        else:
            self.logger.info(f"STEP - {message}")

    def log_step_to_both_file_and_allure(self, message, level="INFO"):
        """Log step to both file and Allure report"""
        # Log to file
        self.log_step_to_file(message, level)

        # Log to Allure
        if level.upper() == "INFO":
            with allure.step(f"ℹ️ {message}"):
                pass
        elif level.upper() == "PASS":
            with allure.step(f"✅ {message}"):
                pass
        elif level.upper() == "WARNING":
            with allure.step(f"⚠️ {message}"):
                pass
        elif level.upper() == "ERROR":
            with allure.step(f"❌ {message}"):
                pass
        else:
            with allure.step(f"ℹ️ {message}"):
                pass

    def log_info_to_both_file_and_allure(self, message):
        """Log info message to both file and Allure"""
        self._log_info(message)
        with allure.step(f"ℹ️ {message}"):
            pass

    def log_warning_to_both_file_and_allure(self, message, capture_screenshot: bool = True):
        """Log warning message to both file and Allure"""
        self._log_warning(message)
        with allure.step(f"⚠️ WARNING: {message}"):
            if capture_screenshot and hasattr(self, 'driver') and self.driver:
                with allure.step("Capturing screenshot for warning"):
                    screenshot_taken = attach_screenshot(self.driver, "WARNING_")
                    if not screenshot_taken:
                        self._log_warning("Failed to capture screenshot for warning")

    def log_error_to_both_file_and_allure(self, message, raise_exception=True, exc_info=False):
        """Log error message to both file and Allure"""
        self._log_error(message, exc_info=exc_info)
        with allure.step(f"❌ ERROR: {message}"):
            if hasattr(self, 'driver') and self.driver:
                screenshot_taken = attach_screenshot(self.driver, "ERROR_")
                if not screenshot_taken:
                    self._log_error("Failed to capture screenshot for error")
        if raise_exception:
            raise AssertionError(message)

    def log_success_to_both_file_and_allure(self, message):
        """Log success message to both file and Allure"""
        self._log_info(f"✅ {message}")
        with allure.step(f"✅ SUCCESS: {message}"):
            pass

    def log_to_both_console_and_file(self, message: str) -> None:
        """Log a message to both console and file, but not to Allure"""
        self._log_info(message)

    def log_skip_to_both_file_and_allure(self, message, raise_skip_exception=True):
        """
        Log skip message to both file and Allure, with URL and calling method info

        Args:
            message (str): The skip message
            raise_skip_exception (bool): Whether to raise pytest.skip exception
        """
        # Get current URL
        current_url = "Unable to retrieve current URL"
        try:
            if hasattr(self, 'driver') and self.driver:
                current_url = self.driver.current_url
        except Exception as e:
            current_url = f"Unable to retrieve current URL: {str(e)}"

        # Get calling method name
        calling_method = "unknown"
        try:
            calling_method = inspect.stack()[1].function
        except Exception:
            calling_method = "unknown"

        # Log to file with enhanced info
        self._log_info(f"[{calling_method}] SKIP: {message}\nURL: {current_url}")

        # Log to Allure
        with allure.step(f"⏭️ SKIP: {message}"):
            allure.attach(f"Calling Method: {calling_method}\nCurrent URL: {current_url}",
                          name="Skip Details", attachment_type=allure.attachment_type.TEXT)

        # Raise skip exception if requested
        if raise_skip_exception:
            pytest.skip(message)


def attach_screenshot(driver, prefix=""):
    """Attach screenshot to Allure report"""
    try:
        if driver:
            # Add a small delay to ensure the page is ready
            import time
            time.sleep(0.5)  # Small delay to ensure page is ready

            # Take screenshot
            screenshot = driver.get_screenshot_as_png()
            if not screenshot:
                logger.error("Screenshot is empty")
                return False

            # Generate a unique name with timestamp
            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
            screenshot_name = f"{prefix}{timestamp}.png"

            # Attach to Allure
            allure.attach(
                screenshot,
                name=screenshot_name,
                attachment_type=allure.attachment_type.PNG
            )
            logger.debug(f"Screenshot attached: {screenshot_name}")
            return True
        return False
    except Exception as e:
        logger.error(f"Failed to capture screenshot: {str(e)}")
        return False


def get_test_logger(driver: Optional[WebDriver] = None) -> TestLogger:
    """
    Get or create the singleton TestLogger instance
    Args:
        driver: Optional WebDriver instance for screenshots
    Returns:
        TestLogger: The singleton logger instance
    """
    logger = TestLogger(driver=driver)
    if driver is not None:
        logger.set_driver(driver)
    return logger
