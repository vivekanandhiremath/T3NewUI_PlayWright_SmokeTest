# driver_utils.py

# Suppress undetected_chromedriver's logger
import logging

import undetected_chromedriver as uc

logging.getLogger('undetected_chromedriver').setLevel(logging.WARNING)
from utilities.customlogger import get_test_logger


class DriverUtils:
    logger = get_test_logger()

    def __init__(self, version_main=143, headless=False, use_subprocess=False):
        self.version_main = version_main
        self.headless = headless
        self.use_subprocess = use_subprocess
        self.driver = None

    def initialize_driver(self):
        """
        Initializes and returns an undetected Chrome driver.
        """
        PROXY = "11.456.448.110:8080"
        options = uc.ChromeOptions()
        # options.add_argument("--blink-settings=imagesEnabled=false")
        # options.add_argument(f"--proxy-server={PROXY}")
        prefs = {
            "credentials_enable_service": False,
            "profile.password_manager_enabled": False,
            "profile.default_content_setting_values.notifications": 2,  # Disable notifications
            "profile.managed_default_content_settings.popups": 2,  # Disable popups
            "profile.managed_default_content_settings.geolocation": 2,  # Disable geolocation
        }
        if self.headless:
            options.add_argument("--headless")
            options.add_argument("--disable-gpu")
            options.add_argument('--disable-dev-shm-usage')
            options.add_argument('--window-size=1920,1080')
            options.add_experimental_option("prefs", prefs)
            options.add_argument("--disable-extensions")
            options.add_argument("--disable-popup-blocking")
            options.add_argument("--disable-notifications")
            # Common options for both modes
            options.add_argument('--disable-blink-features=AutomationControlled')
            options.add_argument('--disable-notifications')
            options.add_argument('--disable-popup-blocking')
            options.add_argument('--start-maximized')

        self.driver = uc.Chrome(
            version_main=self.version_main,
            use_subprocess=self.use_subprocess,
            options=options
        )
        self.driver.execute_cdp_cmd(
            'Network.setUserAgentOverride',
            {
                "userAgent": 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'}
        )
        self.driver.execute_script("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})")
        self.driver.maximize_window()
        return self.driver

    def quit_driver(self):
        """
        Quits the driver if it exists.
        """
        if self.driver:
            self.logger.log_debug_only_to_file("Attempting to driver")
            self.driver.quit()
            self.logger.log_debug_only_to_file("Quit the driver successfully")
