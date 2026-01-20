# Testcase/Projects/BaseProject.py
from abc import ABC, abstractmethod

from utilities.customlogger import get_test_logger  # Add this import


class BaseProject(ABC):
    def __init__(self, driver, logger=None):
        self.driver = driver
        # Initialize logger if not provided
        if logger is None:
            self.logger = get_test_logger(driver)
        else:
            self.logger = logger

    @abstractmethod
    def setup_project(self):
        """Project-specific setup"""
        self.logger.log_info_to_both_file_and_allure("Setting up project...")

    @abstractmethod
    def run_flow(self, *args, **kwargs):
        """Main test flow for the project"""
        self.logger.log_info_to_both_file_and_allure("Starting test flow...")

    @abstractmethod
    def teardown_project(self):
        """Project-specific teardown"""
        self.logger.log_info_to_both_file_and_allure("Tearing down project...")
