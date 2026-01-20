from selenium.webdriver.remote.webdriver import WebDriver

from utilities.DriverUtils import DriverUtils
from utilities.ElementUtils import ElementUtils


class BasePage:
    def __init__(self, driver: WebDriver):
        """
        Base page class that all page objects can inherit from.

        Args:
            driver: WebDriver instance (required)

        Raises:
            ValueError: If driver is None
        """
        if driver is None:
            raise ValueError("WebDriver cannot be None")

        self.driver = driver
        self.utils = DriverUtils(driver)
        self.element_utils = ElementUtils(driver)
