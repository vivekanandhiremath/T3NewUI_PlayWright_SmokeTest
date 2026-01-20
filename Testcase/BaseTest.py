from datetime import datetime

import allure

from utilities.DriverUtils import DriverUtils
from utilities.customlogger import get_test_logger
from utilities.environment_info import EnvironmentInfo


class BaseTest:
    def __init__(self, base_url, project_type='t3', headless=False):
        self.baseURL = base_url
        self.project_type = project_type.lower()
        self.headless = headless
        self.driver = None
        self.driver_util = None
        self.project = None
        # Initialize logger without driver first
        self.log = get_test_logger()
        self.log.log_debug_only_to_file(f"Initializing BaseTest with base_url: {base_url}")

    def _add_environment_info(self):
        """Add environment and system information to Allure report"""
        config = {
            "base_url": self.baseURL,
            "browser": "Chrome",
            "headless": str(self.headless),
            "environment": "QA",
            "Tester": "vivek",
            "run_id": f"RUN-{datetime.now().strftime('%Y%m%d%H%M%S')}"
        }
        EnvironmentInfo.attach_environment_info(config)

    @allure.feature("Test Setup")
    @allure.story("Initialize Test Environment")
    def setup(self):
        try:
            self.log.log_step_to_both_file_and_allure("Starting test environment setup", "INFO")
            self._add_environment_info()

            # Initialize WebDriver
            self.log.log_info_to_both_file_and_allure("Initializing WebDriver...")
            self.driver_util = DriverUtils(headless=self.headless)
            self.driver = self.driver_util.initialize_driver()

            # Update logger with the driver instance
            self.log = get_test_logger(self.driver)
            self.log.log_debug_only_to_file("WebDriver initialized successfully")

            # Navigate to base URL
            self.driver.get(self.baseURL)
            self.log.log_info_to_both_file_and_allure(f"Navigated to {self.baseURL}")

            # Initialize the appropriate project
            self._initialize_project()
            self.project.setup_project()
            self.log.log_step_to_both_file_and_allure("Test setup completed", "PASS")

        except Exception as e:
            self.log.log_error_to_both_file_and_allure(f"Setup failed: {e}", True)
            if hasattr(self, 'driver') and self.driver:
                self._capture_screenshot("setup_failure")
            raise

    def _initialize_project(self):
        """Initialize the appropriate project based on project_type"""
        # if self.project_type == 't1':
        # from Testcase.Projects.T1Project import T1Project
        # self.project = T1Project(self.driver, self.log)
        # elif self.project_type == 't3':
        from Testcase.Projects.T3Project import T3Project
        self.project = T3Project(self.driver, self.log)
        # elif self.project_type == 'ore':
        # from Testcase.Projects.OREProject import OREProject
        # self.project = OREProject(self.driver, self.log)
        # else:
        #     error_msg = f"Unsupported project type: {self.project_type}"
        #     self.log.log_error_to_both_file_and_allure(error_msg)
        #     raise ValueError(error_msg)

    @allure.step("Running test flow")
    def run_test(self, db_connection=None):
        try:
            self.log.log_step_to_both_file_and_allure("Starting test execution", "INFO")
            result = self.project.run_flow(db_connection=db_connection)
            self.log.log_step_to_both_file_and_allure("Test execution completed", "PASS")
            return result
        except Exception as e:
            self.log.log_error_to_both_file_and_allure(f"Test failed: {e}")
            if hasattr(self, 'driver') and self.driver:
                self._capture_screenshot("test_execution_failure")
            raise

    def _capture_screenshot(self, name):
        """Capture screenshot and attach to Allure report"""
        try:
            if self.driver:
                screenshot = self.driver.get_screenshot_as_png()
                allure.attach(
                    screenshot,
                    name=f"screenshot_{name}_{datetime.now().strftime('%Y%m%d_%H%M%S')}",
                    attachment_type=allure.attachment_type.PNG
                )
                self.log.log_debug_only_to_file(f"Screenshot captured: {name}")
        except Exception as e:
            self.log.log_error_to_both_file_and_allure(f"Failed to capture screenshot: {e}")

    def teardown(self):
        try:
            self.log.log_step_to_both_file_and_allure("Starting test teardown", "INFO")
            if hasattr(self, 'project') and self.project:
                self.project.teardown_project()

            if hasattr(self, 'driver') and self.driver:
                # Log before closing the driver
                self.log.log_info_to_both_file_and_allure("Closing browser...")
                self.driver.quit()
                self.log.log_info_to_both_file_and_allure("Browser closed")
                self.driver = None  # Clear the driver reference

        except Exception as e:
            self.log.log_error_to_both_file_and_allure(f"Error during teardown: {e}")
            raise
        finally:
            # Don't log after closing the driver
            pass  # Removed the final log step
