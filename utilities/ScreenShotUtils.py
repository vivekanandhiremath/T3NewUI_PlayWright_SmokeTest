import os
from datetime import datetime
from typing import Optional

from selenium.common.exceptions import WebDriverException
from selenium.webdriver.remote.webdriver import WebDriver

from utilities.customlogger import get_test_logger


class ScreenshotUtils:
    """Utility class for taking and managing screenshots during test execution."""

    def __init__(self, driver: WebDriver):
        """
        Initialize ScreenshotUtils with WebDriver instance.

        Args:
            driver: WebDriver instance
        """
        self.driver = driver
        self.logger = get_test_logger()
        self.screenshot_dir = os.path.join(os.getcwd(), "screenshots")
        self._create_screenshot_dir()

    def _create_screenshot_dir(self) -> None:
        """Create the screenshots directory if it doesn't exist."""
        try:
            if not os.path.exists(self.screenshot_dir):
                os.makedirs(self.screenshot_dir)
                self.logger.log_debug_only_to_file(f"Created screenshot directory: {self.screenshot_dir}")
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Failed to create screenshot directory: {str(e)}")

    def capture_screenshot(self, test_name: str) -> Optional[str]:
        """
        Capture a screenshot and save it with a timestamp.

        Args:
            test_name: Name of the test for which screenshot is being taken

        Returns:
            str: Path to the saved screenshot or None if failed
        """
        try:
            if not test_name or not isinstance(test_name, str):
                test_name = "screenshot"

            # Clean the test name to be filesystem-safe
            safe_test_name = "".join(
                c if c.isalnum() or c in (' ', '-', '_') else '_'
                for c in test_name
            ).strip()

            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"{safe_test_name}_{timestamp}.png"
            filepath = os.path.join(self.screenshot_dir, filename)

            self.driver.save_screenshot(filepath)
            self.logger.log_debug_only_to_file(f"Screenshot saved to: {filepath}")
            return filepath

        except WebDriverException as wde:
            self.logger.log_debug_only_to_file(f"WebDriver error while taking screenshot: {str(wde)}")
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error taking screenshot: {str(e)}")

        return None

    def capture_screenshot_on_failure(self, test_name: str) -> Optional[str]:
        """
        Capture screenshot when a test fails.

        Args:
            test_name: Name of the failed test

        Returns:
            str: Path to the saved screenshot or None if failed
        """
        self.logger.log_debug_only_to_file(f"Test failed: {test_name}")
        return self.capture_screenshot(f"FAIL_{test_name}")

    def capture_screenshot_on_success(self, test_name: str) -> Optional[str]:
        """
        Capture screenshot when a test passes.

        Args:
            test_name: Name of the passed test

        Returns:
            str: Path to the saved screenshot or None if failed
        """
        self.logger.log_debug_only_to_file(f"Test passed: {test_name}")
        return self.capture_screenshot(f"PASS_{test_name}")

    def capture_full_page_screenshot(self, test_name: str) -> Optional[str]:
        """
        Capture full page screenshot (may not work with all browsers).

        Args:
            test_name: Name of the test

        Returns:
            str: Path to the saved screenshot or None if failed
        """
        try:
            # Save original size
            original_size = self.driver.get_window_size()

            # Get total page dimensions
            total_width = self.driver.execute_script("return document.body.scrollWidth")
            total_height = self.driver.execute_script("return document.body.scrollHeight")

            # Set window size to capture full page
            self.driver.set_window_size(total_width, total_height)

            # Take screenshot
            screenshot_path = self.capture_screenshot(f"FULL_{test_name}")

            # Restore original window size
            self.driver.set_window_size(original_size['width'], original_size['height'])

            return screenshot_path
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error capturing full page screenshot: {str(e)}")
            return None
