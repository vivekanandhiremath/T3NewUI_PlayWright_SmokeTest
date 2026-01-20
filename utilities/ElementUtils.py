import re
import time

from selenium.common.exceptions import StaleElementReferenceException
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from utilities.customlogger import get_test_logger


class ElementUtils:
    MAX_ATTEMPTS = 3

    def __init__(self, driver):
        self.driver = driver
        self.logger = get_test_logger()

    @staticmethod
    def sleep(millis: int) -> None:
        """Pause execution for the specified number of milliseconds."""
        time.sleep(millis / 1000.0)

    def is_selected(self, element: WebElement, timeout: int = 10) -> bool:
        """
        Check if an element is selected.

        Args:
            element: The WebElement to check
            timeout: Maximum time to wait for the element to be present (default: 10 seconds)

        Returns:
            bool: True if the element is selected, False otherwise
        """
        try:
            # First ensure the element is present and visible
            visible_element = self.visibility_of_an_element(element, timeout)
            return visible_element.is_selected() if visible_element else False
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Failed to check if element is selected: {e}")
            return False

    def click_on_element(self, element: WebElement, timeout: int = 10) -> None:
        """Click on a web element with retry logic and fallback to JavaScript click."""
        attempts = 0
        while attempts < self.MAX_ATTEMPTS:
            try:
                clickable_element = self.wait_for_element_to_be_clickable(element, timeout)
                element_info = self._clean_element_info(str(element))
                clickable_element.click()
                self.logger.log_debug_only_to_file(f"Successfully clicked on element: {element_info}")
                return
            except StaleElementReferenceException as e:
                attempts += 1
                clean_message = self._clean_error_message(str(e))
                self.logger.log_debug_only_to_file(
                    f"StaleElementReferenceException occurred (attempt {attempts} of {self.MAX_ATTEMPTS}): {clean_message}")
                if attempts == self.MAX_ATTEMPTS:
                    self.logger.log_debug_only_to_file("Max attempts reached for handling stale element")
                    break
                self.sleep(1000)
            except Exception as e:
                clean_message = self._clean_error_message(str(e))
                self.logger.log_debug_only_to_file(
                    f"[click_on_element] Normal click failed, attempting JavaScript click: {clean_message}")
                try:
                    self.javascript_click(element)
                    return
                except Exception as js_ex:
                    js_clean_message = self._clean_error_message(str(js_ex))
                    self.logger.log_debug_only_to_file(
                        f"[click_on_element] JavaScript click also failed: {js_clean_message}")
                    break
        self.logger.log_debug_only_to_file(f"Failed to click on element after {self.MAX_ATTEMPTS} attempts")

    def visibility_of_an_element(self, locator, timeout=10):
        """
        Wait for an element to be visible on the page.

        Args:
            locator: Tuple of (By, value)
            timeout: Maximum time to wait in seconds

        Returns:
            WebElement: The visible element or None if not found
        """
        try:
            wait = WebDriverWait(self.driver, timeout)
            return wait.until(EC.visibility_of_element_located(locator))
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Element not visible - Locator: {locator}, Error: {str(e)}")
            return None

    # Remove wait_for_element_to_be_visible since it's redundant

    def visibility_of_an_element_to_be_clickable(self, element: WebElement, timeout: int = 10) -> bool:
        """Check if an element is visible and clickable."""
        try:
            return self.wait_for_element_to_be_clickable(element, timeout).is_displayed()
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Visibility check failed: {e}")
            return False

    def presence_of_an_element(self, locator: tuple[str, str], timeout: int = 10) -> bool:
        """Check if an element is present in the DOM."""
        try:
            return self.wait_for_presence_of_element(locator, timeout).is_displayed()
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Presence check failed: {e}")
            return False

    def type_into_an_element(self, element: WebElement, text: str, timeout: int = 10) -> None:
        """Type text into an input field."""
        try:
            ele = self.wait_for_element_to_be_clickable(element, timeout)
            ele.clear()
            ele.send_keys(text)
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Typing into element failed: {e}")

    def javascript_click(self, element: WebElement) -> None:
        """Click an element using JavaScript."""
        try:
            self.driver.execute_script("arguments[0].click();", element)
        except Exception as e:
            self.logger.log_debug_only_to_file(f"JavaScript click failed: {e}")

    def scroll_till_element(self, element: WebElement, timeout: int = 10) -> None:
        """Scroll to make an element visible in the viewport."""
        try:
            self.driver.execute_script("arguments[0].scrollIntoView(true);", element)
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Scrolling to element failed: {e}")

    def scroll_to_bottom(self) -> None:
        """Scroll to the bottom of the page."""
        self.driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")

    def scroll_to_middle(self) -> None:
        """Scroll to the middle of the page."""
        height = self.driver.execute_script("return document.body.scrollHeight")
        self.driver.execute_script(f"window.scrollTo(0, {height / 2});")

    def get_attribute_value(self, element, attribute, timeout=10):
        """Get the value of an element's attribute."""
        try:
            visible_element = self.visibility_of_an_element(element, timeout)
            return visible_element.get_attribute(attribute) if visible_element else None
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Get attribute failed: {e}")
            return None

    def change_attribute_value(self, element: WebElement, attribute: str, value: str, timeout: int = 10) -> None:
        """Change an element's attribute value using JavaScript."""
        try:
            self.driver.execute_script(
                "arguments[0].setAttribute(arguments[1], arguments[2]);",
                element, attribute, value
            )
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Changing attribute failed: {e}")

    # ----- PRIVATE HELPER METHODS -----

    def wait_for_element_to_be_clickable(self, element: WebElement, timeout: int) -> WebElement:
        """Wait for an element to be clickable."""
        return WebDriverWait(self.driver, timeout).until(
            EC.element_to_be_clickable(element)
        )

    def wait_for_element_to_be_visible(self, locator, timeout=10):
        """
        Wait for an element to be visible on the page.

        Args:
            locator: Tuple of (By, value)
            timeout: Maximum time to wait in seconds

        Returns:
            WebElement: The visible element or None if not found
        """
        try:
            # Just call visibility_of_an_element which already handles the waiting
            element = self.visibility_of_an_element(locator, timeout)
            if element:
                return element
            self.logger.log_debug_only_to_file(f"Element with locator {locator} not visible after {timeout} seconds")
            return None
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error waiting for element {locator} to be visible: {str(e)}")
            return None

    def wait_for_presence_of_element(self, locator: tuple[str, str], timeout: int) -> WebElement:
        """Wait for an element to be present in the DOM."""
        return WebDriverWait(self.driver, timeout).until(
            EC.presence_of_element_located(locator)
        )

    def _clean_error_message(self, error_message: str) -> str:
        """Clean up error messages to remove redundant information."""
        if not error_message:
            return "[no error details]"

        try:
            cleaned = re.sub(r"^Expected condition failed: ", "", error_message)
            cleaned = re.sub(r"waiting for element to be clickable: .*?->", "", cleaned)
            cleaned = re.sub(r"tried for \d+ second\(s\) with \d+ milliseconds interval", "", cleaned)
            return re.sub(r"\s+", " ", cleaned).strip()
        except Exception:
            return error_message

    def _clean_element_info(self, element_string: str) -> str:
        """Clean up the element string to show only relevant locator information."""
        if not element_string:
            return "[unknown element]"

        try:
            # Handle Proxy elements
            if element_string.startswith("Proxy"):
                start = element_string.find("By.")
                if start > 0:
                    return element_string[start:]

            # Handle Decorated elements
            arrow_index = element_string.rfind("->")
            if arrow_index > 0:
                locator_part = element_string[arrow_index + 2:].strip()
                return re.sub(r"[)\]]+$", "", locator_part).strip()

            return element_string
        except Exception as e:
            print(f"Error cleaning element info: {e}")
            return element_string

    def clear_and_type(self, locator, text, timeout: int = 10) -> bool:
        """
        Clear the input field and type the specified text.

        Args:
            locator: Tuple of (By, selector) to locate the element
            text: Text to type into the element
            timeout: Maximum time to wait for the element to be visible and interactable

        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # Find the element
            element = self.wait_for_element_to_be_clickable(locator, timeout)
            if not element:
                self.logger.log_debug_only_to_file(f"Element not visible: {locator}")
                return False

            # Clear the field
            element.clear()

            # Type the text
            element.send_keys(text)
            return True

        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error in clear_and_type for {locator}: {str(e)}")
            return False
