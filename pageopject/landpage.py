from pageopject.BasePage import BasePage


class LandingPage(BasePage):
    def __init__(self, driver):
        super().__init__(driver)
        self.logger = get_test_logger()

    def some_method(self):
        self.logger.log_info_to_both_file_and_allure("This is a log message from Landingpage class")

    # Define locators
    # Landing page
    payment_xpath = "(//img[@alt='PAYMENT OPTIONS'])[1]"
    pre_Qualify_xpath = "//span[normalize-space()='Get pre-qualified']"


from typing import Optional
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from utilities.customlogger import get_test_logger
from utilities.VehicleDetails import VehicleDetails
import time


class LandingPage(BasePage):
    """Page object for the landing page of the application."""

    # Locators
    PAYMENT_OPTION_BUTTON = (By.XPATH, "(//img[contains(@src, 'https://d1jougtdqdwy1v')])[1]")
    NO_THANKS_BUTTON = (By.XPATH, "//button[@id='ip-no']")
    COOKIE_BANNER_BUTTONS = (By.XPATH, """
        //button[contains(translate(., 'ACCEPT', 'accept'), 'accept') or 
        contains(translate(., 'ALLOW', 'allow'), 'allow') or 
        contains(., 'Got it') or contains(., 'I Agree') or 
        contains(., 'Accept') or contains(., 'Allow') or
        contains(@class, 'cookie') or contains(@class, 'cc-') or
        contains(@id, 'cookie') or contains(@id, 'consent') or
        @id='onetrust-accept-btn-handler']""")

    def __init__(self, driver: WebDriver):
        """
        Initialize LandingPage by calling parent's __init__.

        Args:
            driver: WebDriver instance
        """
        super().__init__(driver)
        self.logger = get_test_logger()
        self.stage1_index = "1"
        self.stage2_index = "4"
        self.stage3_index = "6"

    def scroll_to_vehicle_image_if_available(self) -> None:
        """Scroll to the vehicle image if it's present on the page."""
        image_locator = (By.XPATH, "(//img[contains(@src, 'https://d1jougtdqdwy1v')])[1]")
        try:
            image = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(image_locator)
            )
            self.driver.execute_script("arguments[0].scrollIntoView(true);", image)
            print("✅ Scrolled to vehicle image.")
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure("❌ Vehicle image not found. Skipping scroll.")

    def click_on_payment_option(self) -> Optional[VehicleDetails]:
        """
        Click on the payment option and return vehicle details.

        Returns:
            Optional[VehicleDetails]: Vehicle details if successful, None otherwise

        Raises:
            AssertionError: If payment option button is not found or not clickable
        """
        try:
            self.handle_cookie_banner_if_present()

            if self.is_no_thanks_button_visible():
                self.click_on_no_thanks_button()
                self.logger.log_info_to_both_file_and_allure("Clicked on No Thanks button")

            # Wait for the payment button to be present and visible
            try:
                payment_button = WebDriverWait(self.driver, 15).until(
                    EC.presence_of_element_located(self.PAYMENT_OPTION_BUTTON)
                )
                payment_button = WebDriverWait(self.driver, 15).until(
                    EC.visibility_of_element_located(self.PAYMENT_OPTION_BUTTON)
                )
                payment_button = WebDriverWait(self.driver, 15).until(
                    EC.element_to_be_clickable(self.PAYMENT_OPTION_BUTTON)
                )
            except Exception as e:
                error_msg = f"Payment option button not ready: {str(e)}"
                self.logger.log_error_to_both_file_and_allure(
                    error_msg,
                    raise_exception=False
                )
                raise AssertionError(error_msg)

            # Scroll to the button to ensure it's in view
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", payment_button)
            time.sleep(0.5)  # Small delay for scroll to complete

            # Extract vehicle details before clicking
            details = self.extract_vehicle_details_from_button(payment_button)
            self.logger.log_info_to_both_file_and_allure(f"Vehicle Details Extracted: {details}")

            # Click using JavaScript as a fallback
            try:
                payment_button.click()
            except Exception as e:
                self.logger.log_warning_to_both_file_and_allure(
                    f"Standard click failed, trying JavaScript click: {str(e)}"
                )
                self.driver.execute_script("arguments[0].click();", payment_button)

            self.logger.log_step_to_both_file_and_allure("Successfully clicked on payment option button", "PASS")
            time.sleep(2)  # Allow page to update after click

            return details

        except Exception as e:
            error_msg = f"Failed to click on Payment Option: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise  # This will be caught by the test's try/except

    def is_payment_option_displayed(self) -> bool:
        """Check if the payment option is displayed."""
        try:
            time.sleep(2)  # Short wait for any animations
            self.scroll_to_vehicle_image_if_available()
            return WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(self.PAYMENT_OPTION_BUTTON)
            ).is_displayed()
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Payment Options CTA not visible: {str(e)}")
            return False

    def click_on_payment_option_for_stage1(self) -> Optional[VehicleDetails]:
        """Click payment option for stage 1 and return vehicle details."""
        return self.click_and_extract_vehicle_details(self.stage1_index)

    def click_on_payment_option_for_stage2(self) -> Optional[VehicleDetails]:
        """Click payment option for stage 2 and return vehicle details."""
        return self.click_and_extract_vehicle_details(self.stage2_index)

    def click_on_payment_option_for_stage3(self) -> Optional[VehicleDetails]:
        """Click payment option for stage 3 and return vehicle details."""
        return self.click_and_extract_vehicle_details(self.stage3_index)

    def click_and_extract_vehicle_details(self, index: str) -> Optional[VehicleDetails]:
        """
        Click on payment option and extract vehicle details.

        Args:
            index: Index of the payment option to click

        Returns:
            Optional[VehicleDetails]: Vehicle details if successful, None otherwise
        """
        try:
            payment_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(
                    (By.XPATH, f"(//img[contains(@src, 'https://d1jougtdqdwy1v')])[{index}]")
                )
            )

            details = self.extract_vehicle_details_from_button(payment_button)
            self.utils.hover_over_element(payment_button)
            time.sleep(2)
            payment_button.click()
            self.logger.log_info_to_both_file_and_allure("Clicked on payment option button")
            time.sleep(2)
            return details

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Failed to click and extract details: {str(e)}")
            return None

    def extract_vehicle_details_from_button(self, button: WebElement) -> Optional[VehicleDetails]:
        """
        Extract vehicle details from the button's parent element.

        Args:
            button: WebElement of the payment button

        Returns:
            Optional[VehicleDetails]: Extracted vehicle details or None if failed
        """
        try:
            parent = self.driver.execute_script(
                "return arguments[0].closest('app-root, app-certified, eshop-inventory');",
                button
            )

            if not parent:
                self.logger.log_warning_to_both_file_and_allure("Parent tag not found for CTA.")
                return None

            vin = parent.get_attribute("vin") or ""
            dealercode = parent.get_attribute("dealercode") or ""
            zipcode = parent.get_attribute("zipcode") or ""
            vehicletype = parent.get_attribute("vehicle_type") or ""

            return VehicleDetails(
                vin=vin,
                dealercode=dealercode,
                zipcode=zipcode,
                vehicletype=vehicletype
            )

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Unable to extract vehicle details: {str(e)}")
            return None

    def is_no_thanks_button_visible(self) -> bool:
        """Check if the 'No Thanks' button is visible."""
        try:
            return WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(self.NO_THANKS_BUTTON)
            ).is_displayed()
        except Exception:
            return False

    def click_on_no_thanks_button(self) -> None:
        """Click on the 'No Thanks' button if visible."""
        try:
            button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(self.NO_THANKS_BUTTON)
            )
            button.click()
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click on No Thanks button: {str(e)}")
            raise

    def handle_cookie_banner_if_present(self) -> bool:
        """Handle cookie banner if present on the page.

        Returns:
            bool: True if cookie banner was handled, False otherwise
        """
        self.logger.log_info_to_both_file_and_allure("🔍 Starting cookie banner check...")
        start_time = time.time()
        max_wait = 5  # seconds

        try:
            # Check main document first
            if self._handle_cookie_banner_in_main_document():
                self.logger.log_info_to_both_file_and_allure(
                    f"✅ Cookie banner handled in {time.time() - start_time:.2f}s")
                return True

            # If we still have time, check iframes
            if (time.time() - start_time) < (max_wait - 2):
                if self._handle_cookie_banner_in_iframes(start_time):
                    self.logger.log_info_to_both_file_and_allure(
                        f"✅ Cookie banner handled in iframe ({time.time() - start_time:.2f}s)")
                    return True

            self.logger.log_debug_only_to_file(f"ℹ️ No cookie banner found (took {time.time() - start_time:.2f}s)")
            return False

        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"⚠️ Error in handle_cookie_banner_if_present: {str(e)}")
            return False

    def _handle_cookie_banner_in_main_document(self) -> bool:
        """Handle cookie banner in the main document.

        Returns:
            bool: True if cookie banner was found and handled, False otherwise
        """
        try:
            buttons = self.driver.find_elements(*self.COOKIE_BANNER_BUTTONS)

            for button in buttons:
                try:
                    if button.is_displayed() and button.is_enabled():
                        self.logger.log_debug_only_to_file(f"Found visible cookie button: {button.text.strip()}")
                        self._click_element_with_retry(button, "cookie banner")
                        return True
                except Exception:
                    continue
            return False
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error in _handle_cookie_banner_in_main_document: {str(e)}")
            return False

    def _handle_cookie_banner_in_iframes(self, start_time: float) -> bool:
        """Handle cookie banner in iframes.

        Args:
            start_time: Timestamp when the check started

        Returns:
            bool: True if cookie banner was found and handled, False otherwise
        """
        try:
            iframes = self.driver.find_elements(By.TAG_NAME, "iframe")
            if not iframes:
                return False

            # Check only first 2 iframes to save time
            for iframe in iframes[:2]:
                try:
                    self.driver.switch_to.frame(iframe)

                    # Look for common cookie buttons
                    xpath = """
                        //button[
                            contains(translate(., 'ACCEPT', 'accept'), 'accept') or 
                            contains(translate(., 'ALLOW', 'allow'), 'allow') or 
                            contains(., 'Got it') or contains(., 'I Agree') or 
                            contains(., 'Accept') or contains(., 'Allow')
                        ] | 
                        //*[contains(@class, 'cc-')]//button[
                            contains(., 'Accept') or 
                            contains(., 'Allow') or 
                            contains(., 'Agree')
                        ]
                    """
                    buttons = self.driver.find_elements(By.XPATH, xpath)

                    for button in buttons:
                        try:
                            if button.is_displayed() and button.is_enabled():
                                self._click_element_with_retry(button, "iframe cookie button")
                                return True
                        except Exception:
                            continue

                except Exception:
                    continue

                finally:
                    self.driver.switch_to.default_content()

                # If we've spent more than 1.5 seconds, give up
                if time.time() - start_time > 1.5:
                    break

            return False

        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error in _handle_cookie_banner_in_iframes: {str(e)}")
            try:
                self.driver.switch_to.default_content()
            except Exception:
                pass
            return False

    def _click_element_with_retry(self, element: WebElement, element_description: str) -> None:
        """
        Click an element with retry logic.

        Args:
            element: WebElement to click
            element_description: Description for logging
        """
        try:
            # Scroll to the element
            self.driver.execute_script(
                "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                element
            )

            # Try JavaScript click first
            try:
                self.driver.execute_script("arguments[0].click();", element)
                self.logger.log_info_to_both_file_and_allure(f"Clicked {element_description} using JavaScript")
                time.sleep(1)  # Wait for any animations
                return
            except Exception:
                self.logger.log_debug_only_to_file("JavaScript click failed, trying regular click")

            # Fall back to regular click
            element.click()
            self.logger.log_info_to_both_file_and_allure(f"Clicked {element_description} using regular click")
            time.sleep(1)  # Wait for any animations

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Failed to click {element_description}: {str(e)}")
            raise
