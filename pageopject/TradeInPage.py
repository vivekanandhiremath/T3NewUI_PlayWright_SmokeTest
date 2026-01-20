import time
from typing import Optional

from selenium.common.exceptions import TimeoutException, NoSuchElementException, StaleElementReferenceException
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from utilities.DriverUtils import DriverUtils
from utilities.ScreenShotUtils import ScreenshotUtils
from utilities.customlogger import get_test_logger
from .BasePage import BasePage


class TradeInPage(BasePage):
    """Page object for handling trade-in functionality."""

    def __init__(self, driver):
        """
        Initialize TradeInPage.

        Args:
            driver: WebDriver instance
        """
        super().__init__(driver)
        self.logger = get_test_logger()
        self.utils = DriverUtils(driver)
        self.screenshot_utils = ScreenshotUtils(driver)

        # Locators
        self.current_vehicle_field = (By.CSS_SELECTOR, "input[placeholder='Enter Vehicle Year, Brand, and Model']")
        self.autocomplete_items = (By.CSS_SELECTOR, "ul.aa-List li.aa-Item")
        self.mileage_field = (By.XPATH, "//input[@name='mileage']")
        self.zip_field = (By.XPATH, "//input[@name='zip']")
        self.next_button = (By.XPATH, "//button[@id='describe-your-vehicle-button']")
        self.get_estimate_button = (By.XPATH, "//button[.='Get Your Estimate']")
        self.trade_in_price_span = (By.CSS_SELECTOR, "span.fw-extrabold.mb-1.fs-px-42")
        self.no_idont_button = (By.XPATH, "//button[contains(translate(., \"'\", \"'\"), \"No, I don\")]")
        self.tab_labels = (By.CSS_SELECTOR, ".mat-button-toggle-label-content")
        self.active_tab = (By.CSS_SELECTOR,
                           ".mat-button-toggle.mat-button-toggle-checked .mat-button-toggle-label-content")
        self.overlays = (By.CSS_SELECTOR,
                         ".cdk-overlay-backdrop.cdk-overlay-dark-backdrop.cdk-overlay-backdrop-showing")

    def select_current_vehicle(self, search_text: str) -> None:
        """Select a vehicle from the autocomplete dropdown.

        Args:
            search_text: Text to search for (e.g., "2018 INFINITI QX30")

        Raises:
            NoSuchElementException: If no matching vehicle is found
        """
        try:
            time.sleep(1)
            self.element_utils.type_into_an_element(self.current_vehicle_field, search_text)

            try:
                # Wait for loading to complete
                WebDriverWait(self.driver, 10).until_not(
                    EC.presence_of_element_located(
                        (By.CSS_SELECTOR, ".aa-InputWrapperSuffix .aa-LoadingIndicator")
                    )
                )
            except TimeoutException:
                self.logger.log_info_to_both_file_and_allure("No loading indicator found or it didn't appear")

            # Wait for autocomplete items to be visible
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(self.autocomplete_items)
            )

            time.sleep(0.5)
            items = self.driver.find_elements(
                By.CSS_SELECTOR, "ul.aa-List li.aa-Item:not([hidden])"
            )

            if items:
                items[0].click()
                self.logger.log_info_to_both_file_and_allure(f"Selected vehicle from autocomplete: {search_text}")
                try:
                    WebDriverWait(self.driver, 10).until_not(
                        EC.visibility_of_element_located((By.CSS_SELECTOR, "ul.aa-List"))
                    )
                except TimeoutException:
                    self.logger.log_warning_to_both_file_and_allure(
                        "Autocomplete dropdown did not close after selection")
            else:
                try:
                    first_word = search_text.split()[0]
                    exact_match = self.driver.find_element(
                        By.XPATH, f"//li[contains(@class, 'aa-Item')]//span[contains(.,'{first_word}')]"
                    )
                    exact_match.click()
                    self.logger.log_info_to_both_file_and_allure(f"Selected exact match for: {search_text}")
                except NoSuchElementException:
                    raise NoSuchElementException(
                        f"No matching vehicles found in autocomplete for: {search_text}"
                    )

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error selecting vehicle: {str(e)}")
            raise

    def enter_mileage(self, mileage: str) -> None:
        """Enter mileage into the mileage field.

        Args:
            mileage: Mileage to enter
        """
        self.element_utils.type_into_an_element(self.mileage_field, mileage)

    def enter_zip(self, zip_code: str) -> None:
        """Enter zip code into the zip code field.

        Args:
            zip_code: Zip code to enter
        """
        self.element_utils.type_into_an_element(self.zip_field, zip_code)

    def click_next_button(self) -> None:
        """Click the Next button."""
        self.element_utils.click_on_element(self.next_button)

    def click_get_estimate_button(self) -> None:
        """Click the Get Estimate button."""
        try:
            self.element_utils.click_on_element(self.get_estimate_button)
        except:
            self.logger.log_error_to_both_file_and_allure("❌ Could not click Get Estimate button")

    def handle_trade_in(self) -> dict:
        """Handle the trade-in process.

        Returns:
            dict: Dictionary containing trade-in result with provider and price
        """
        wait = WebDriverWait(self.driver, 30)
        time.sleep(5)

        try:
            black_book_iframe = self.wait_for_iframe_by_src("https://app.blackbookinformation.com")

            if black_book_iframe:
                self.logger.log_info_to_both_file_and_allure("✅ BlackBook iframe found")
                self.driver.switch_to.frame(black_book_iframe)
                self.logger.log_debug_only_to_file("✅ Switched to Black Book iframe")

                # Wait for vehicle input to be visible
                wait.until(EC.visibility_of_element_located(
                    (By.CSS_SELECTOR, "input[placeholder='Enter Vehicle Year, Brand, and Model']")
                ))

                price = self.fill_black_book_iframe_form("123", "1000", "10001")
                self.logger.log_info_to_both_file_and_allure("Completed Trade-In flow for Black Book")

                self.driver.switch_to.default_content()
                return {"provider": "Black Book", "price": price}

            else:
                kbb_iframe = self.wait_for_iframe_by_src("https://tradeinadvisor.kbb.com")

                if kbb_iframe:
                    self.logger.log_info_to_both_file_and_allure("✅ KBB iframe found")
                    self.driver.switch_to.frame(kbb_iframe)
                    self.logger.log_debug_only_to_file("✅ Switched to KBB iframe")

                    # Wait for vehicle input to be visible
                    wait.until(EC.visibility_of_element_located(
                        (By.CSS_SELECTOR, "input[placeholder='Enter Vehicle Year, Brand, and Model']")
                    ))

                    price = None
                    self.logger.log_info_to_both_file_and_allure("Completed Trade-In flow for Kelly Blue Book")

                    self.driver.switch_to.default_content()
                    return {"provider": "Kelley Blue Book", "price": price}

                else:
                    self.logger.log_error_to_both_file_and_allure("❌ Neither Black Book nor KBB iframe was found.")
                    return {"provider": "None", "price": None}

        except Exception as e:
            self.driver.switch_to.default_content()
            self.logger.log_error_to_both_file_and_allure(f"❌ Error in Trade-In page iframe interaction: {str(e)}")
            return {"provider": "None", "price": None}

    def wait_for_iframe_by_src(self, src_substring: str, timeout: int = 30) -> Optional[WebElement]:
        """Wait for an iframe with the specified src substring to be present.

        Args:
            src_substring: Substring to match in the iframe's src attribute
            timeout: Maximum time to wait in seconds

        Returns:
            WebElement: The iframe element if found, None otherwise
        """
        wait = WebDriverWait(self.driver, timeout)
        iframes = wait.until(
            EC.presence_of_all_elements_located((By.TAG_NAME, "iframe"))
        )

        for iframe in iframes:
            iframe_src = iframe.get_attribute("src")
            if iframe_src and src_substring in iframe_src:
                return iframe
        return None

    def get_trade_in_price(self) -> str:
        """Get the trade-in price from the page.

        Returns:
            str: The trade-in price as a string
        """
        price_text = self.driver.find_element(
            *self.trade_in_price_span
        ).text.strip()

        clean_price = ''.join(c for c in price_text if c.isdigit() or c == '.')
        self.logger.log_info_to_both_file_and_allure(f"Clean numeric price from UI: {clean_price}")
        return clean_price

    def click_no_i_dont_button(self, max_attempts: int = 3) -> None:
        """Click the 'No, I don't' button with retries.

        Args:
            max_attempts: Maximum number of attempts to try clicking

        Raises:
            RuntimeError: If all attempts to click the button fail
        """
        attempts = 0

        while attempts < max_attempts:
            try:
                self.logger.log_debug_only_to_file(f"Attempt {attempts + 1} to click 'No, I don\\'t' button")

                button = WebDriverWait(self.driver, 10).until(
                    EC.element_to_be_clickable(self.no_idont_button)
                )

                self.driver.execute_script(
                    "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
                    button
                )
                time.sleep(0.3)
                self.driver.execute_script("arguments[0].click();", button)
                self.logger.log_debug_only_to_file("✅ Successfully clicked 'No, I don\\'t' button")
                time.sleep(0.5)
                return

            except StaleElementReferenceException:
                self.logger.log_debug_only_to_file("Stale element reference, retrying...")
                attempts += 1
            except NoSuchElementException:
                self.logger.log_debug_only_to_file("Button not found, retrying...")
                attempts += 1
            except Exception as e:
                self.logger.log_error_to_both_file_and_allure(f"Error clicking 'No, I don\\'t' button: {str(e)}")
                attempts += 1

            # Small delay between retries
            time.sleep(1)

        # If we get here, all attempts failed
        error_msg = f"❌ Failed to click 'No, I don\\'t' button after {max_attempts} attempts"
        self.logger.log_error_to_both_file_and_allure(error_msg)
        raise RuntimeError(error_msg)

    def compare_trade_in_price(self) -> None:
        """Compare the trade-in price with the expected value."""
        wait = WebDriverWait(self.driver, 10)

        lease_input = wait.until(
            EC.visibility_of_element_located((By.ID, "trade_in_lease"))
        )
        actual_value = lease_input.get_attribute("value").strip().replace("$", "").replace(",", "")
        expected_value = self.get_trade_in_price()

        self.logger.log_info_to_both_file_and_allure(
            f"🔍 Comparing Lease Input Value: {actual_value} with Expected Price: {expected_value}"
        )

        if actual_value == expected_value:
            self.logger.log_info_to_both_file_and_allure(f"✅ Lease amount matches expected value: {expected_value}")
        else:
            self.logger.log_error_to_both_file_and_allure(
                f"Lease amount mismatch. Expected: {expected_value}, but found: {actual_value}"
            )

    def switch_to_tab_by_label(self, tab_label: str) -> bool:
        """Switch to a tab by its label.

        Args:
            tab_label: Label of the tab to switch to

        Returns:
            bool: True if successful, False otherwise
        """
        try:
            # Try to close any overlays
            try:
                overlays = self.driver.find_elements(*self.overlays)
                if overlays:
                    self.logger.log_debug_only_to_file("Found overlay, attempting to close it")
                    self.driver.execute_script("""
                        var overlays = document.querySelectorAll('.cdk-overlay-backdrop');
                        overlays.forEach(function(overlay) { 
                            overlay.style.display = 'none'; 
                        });
                    """)
                    time.sleep(0.5)
            except Exception as e:
                self.logger.log_warning_to_both_file_and_allure(f"Could not close overlay: {str(e)}")

            tab_labels = self.driver.find_elements(*self.tab_labels)
            wait = WebDriverWait(self.driver, 5)

            for label in tab_labels:
                if label.text.strip().lower() == tab_label.lower():
                    self.driver.execute_script(
                        "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                        label
                    )

                    try:
                        self.driver.execute_script("arguments[0].click();", label)
                        self.logger.log_info_to_both_file_and_allure(f"✅ Switched to tab: {tab_label}")
                        time.sleep(1)
                        return True
                    except Exception as e:
                        self.logger.log_debug_only_to_file(f"JavaScript click failed, trying direct click: {str(e)}")
                        label.click()
                        self.logger.log_info_to_both_file_and_allure(f"✅ Switched to tab (direct click): {tab_label}")
                        time.sleep(1)
                        return True

            self.logger.log_error_to_both_file_and_allure(f"❌ Tab not found with label: {tab_label}")
            return False
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"❌ Error switching to tab '{tab_label}': {str(e)}")
            return False

    def get_trade_in_input_value(self, input_xpath: str) -> str:
        """Get the value from a trade-in input field.

        Args:
            input_xpath: XPath of the input field

        Returns:
            str: The value of the input field
        """
        try:
            self.logger.log_info_to_both_file_and_allure(
                f"Trying to get trade-in value from input field: {input_xpath}")

            # Try the provided XPath first
            locator = (By.XPATH, input_xpath)
            if not self.is_element_present(locator):
                self.logger.log_debug_only_to_file(
                    f"Input field with XPath '{input_xpath}' not found, trying alternative locators"
                )
                # Try to extract name from XPath and use it for alternative locators
                name = input_xpath.split("_")[-1].strip("']")
                locator = (By.CSS_SELECTOR, f"input[name='{name}'], input[data-test='{name}']")

                if not self.is_element_present(locator):
                    locator = (By.CSS_SELECTOR, "input[type='text'], input[type='number']")

            input_element = WebDriverWait(self.driver, 5).until(
                EC.visibility_of_element_located(locator)
            )

            # Scroll to the element
            self.driver.execute_script(
                "arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});",
                input_element
            )
            time.sleep(0.5)

            # Highlight the element
            self.highlight_element(input_element)

            # Try different ways to get the value
            value = self.driver.execute_script(
                "return arguments[0].value || '';", input_element
            )

            if not value:
                value = input_element.get_attribute("value") or ""

            if not value:
                value = self.driver.execute_script(
                    "return arguments[0].parentNode.textContent.trim() || '';",
                    input_element
                )

            clean_value = ''.join(c for c in value if c.isdigit() or c == '.')

            if clean_value:
                self.logger.log_info_to_both_file_and_allure(
                    f"✅ Successfully retrieved trade-in value from {input_xpath}: {clean_value}"
                )
            else:
                self.logger.log_warning_to_both_file_and_allure(
                    f"Found input field but couldn't extract value from {input_xpath}")
                if not clean_value:
                    screenshot_name = f"tradein_input_{input_xpath.replace('/', '_')}"
                    try:
                        screenshot_path = self.screenshot_utils.capture_screenshot(screenshot_name)
                        if screenshot_path:
                            self.logger.log_info_to_both_file_and_allure(f"Screenshot saved: {screenshot_path}")
                        else:
                            self.logger.log_warning_to_both_file_and_allure(
                                f"Failed to capture screenshot for: {screenshot_name}")
                    except Exception as e:
                        self.logger.log_warning_to_both_file_and_allure(f"Error taking screenshot: {str(e)}")

            return clean_value

        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(
                f"⚠️ Could not get trade-in value from {input_xpath}: {str(e)}")
            return ""

    def is_element_present(self, locator: tuple) -> bool:
        """Check if an element is present in the DOM.

        Args:
            locator: Tuple of (By, selector) to locate the element

        Returns:
            bool: True if element is present, False otherwise
        """
        try:
            return len(self.driver.find_elements(*locator)) > 0
        except Exception:
            return False

    def highlight_element(self, element: WebElement) -> None:
        """Highlight a WebElement with a red border.

        Args:
            element: WebElement to highlight
        """
        try:
            self.driver.execute_script(
                "arguments[0].style.border='2px solid red';", element
            )
        except Exception:
            # Ignore any errors during highlighting
            pass

    def get_active_tab_name(self) -> str:
        """Get the name of the currently active tab.

        Returns:
            str: Name of the active tab or empty string if not found
        """
        try:
            tabs = self.driver.find_elements(*self.active_tab)
            if tabs:
                active_tab = tabs[0].text.strip()
                self.logger.log_info_to_both_file_and_allure(f"Currently active tab: {active_tab}")
                return active_tab
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Could not determine active tab: {str(e)}")
        return ""

    def get_current_tab_trade_in_value(self) -> str:
        """Get the trade-in value from the current tab.

        Returns:
            str: The trade-in value or empty string if not found
        """
        active_tab = self.get_active_tab_name().lower()
        input_xpath = ""

        if "lease" in active_tab:
            input_xpath = "(//input[@id='trade_in_lease'])[2]"
        elif "finance" in active_tab:
            input_xpath = "(//input[@id='trade_in_finance'])[2]"
        elif "cash" in active_tab:
            input_xpath = "(//input[@id='trade_in_cash'])[2]"
        else:
            self.logger.log_warning_to_both_file_and_allure(
                f"Could not determine trade-in input field for active tab: {active_tab}")
            return ""

        return self.get_trade_in_input_value(input_xpath)

    def compare_trade_in_across_tabs(self) -> None:
        """Compare trade-in values across different tabs (Lease, Finance, Cash)."""
        tabs = ["Lease", "Finance", "Cash"]
        values = {}

        for tab_name in tabs:
            try:
                # Try to find and click the tab
                tab_locator = (By.XPATH,
                               f"//span[contains(@class, 'mat-button-toggle-label-content') and contains(., '{tab_name}')]")

                if self.is_element_present(tab_locator):
                    # Click the tab
                    tab = self.driver.find_element(*tab_locator)
                    self.driver.execute_script("arguments[0].scrollIntoView(true);", tab)
                    self.driver.execute_script("arguments[0].click();", tab)
                    time.sleep(1)  # Wait for tab to switch

                    self.logger.log_info_to_both_file_and_allure(f"Switched to {tab_name} tab")

                    # Try to get trade-in value using multiple strategies
                    value = self._get_trade_in_from_current_tab()
                    if value is not None:
                        values[tab_name] = value
                        self.logger.log_info_to_both_file_and_allure(
                            f"✅ Got trade-in value from {tab_name} tab: {value}")
                    else:
                        self.logger.log_warning_to_both_file_and_allure(
                            f"⚠️ Could not get trade-in value from {tab_name} tab")
                else:
                    self.logger.log_warning_to_both_file_and_allure(f"Tab {tab_name} not found")

            except Exception as e:
                self.logger.log_warning_to_both_file_and_allure(f"Error processing {tab_name} tab: {str(e)}")
                continue

        if not values:
            self.logger.log_error_to_both_file_and_allure("❌ Could not retrieve trade-in value from any tab")
            return

        # Compare all found values
        if len(set(values.values())) == 1:
            self.logger.log_info_to_both_file_and_allure(f"✅ All trade-in values match: {values}")
        else:
            self.logger.log_warning_to_both_file_and_allure(f"⚠️ Trade-in values differ across tabs: {values}")

    def compare_values(self, tab: str, actual: str, expected: str) -> None:
        """Compare actual and expected values and log the result.

        Args:
            tab: Name of the tab being checked
            actual: Actual value
            expected: Expected value
        """
        if actual == expected:
            self.logger.log_info_to_both_file_and_allure(f"✅ [{tab}] Trade-in value matches expected: {expected}")
        else:
            self.logger.log_error_to_both_file_and_allure(
                f"❌ [{tab}] Trade-in value mismatch. Expected: {expected}, Found: {actual}"
            )

    def _get_trade_in_from_current_tab(self) -> Optional[float]:
        """Helper method to get trade-in value from the current tab."""
        # Try different locators
        locators = [
            (By.XPATH, "//input[contains(@id, 'trade_in_')]"),
            (By.CSS_SELECTOR, "input[id*='trade_in_']"),
            (By.CLASS_NAME, "trade-in-value")
        ]

        for locator in locators:
            try:
                element = self.element_utils.wait_for_presence_of_element(locator, timeout=5)
                value = element.get_attribute("value") or element.text
                if value and value.strip():
                    return float(''.join(c for c in value if c.isdigit() or c == '.'))
            except:
                continue

        return None

    def get_active_tab(self) -> str:
        """Get the currently active tab (Lease, Finance, or Cash).
        
        Returns:
            str: The name of the active tab, or empty string if none found
        """
        try:
            # Find all tab elements
            tab_elements = self.driver.find_elements(
                By.CSS_SELECTOR,
                "span.mat-button-toggle-label-content"
            )

            # Check each tab to see which one is active
            for tab in tab_elements:
                # Get the tab text (Lease, Finance, or Cash)
                tab_text = tab.text.strip()
                if tab_text in ["Lease", "Finance", "Cash"]:
                    # Check if this tab has the checked pseudo-checkbox
                    checked_checkbox = tab.find_elements(
                        By.CSS_SELECTOR,
                        "mat-pseudo-checkbox-checked"
                    )
                    if checked_checkbox:
                        self.logger.log_info_to_both_file_and_allure(f"Active tab found: {tab_text}")
                        return tab_text

            # If we get here, no active tab was found
            self.logger.log_warning_to_both_file_and_allure("No active tab found")
            return ""

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error getting active tab: {str(e)}")
            return ""

    def switch_to_tab(self, tab_name: str) -> bool:
        """Switch to the specified tab (Lease, Finance, or Cash).

        Args:
            tab_name: Name of the tab to switch to (case-insensitive)

        Returns:
            bool: True if the tab was found and clicked, False otherwise
        """
        tab_name = tab_name.capitalize()
        if tab_name not in ["Lease", "Finance", "Cash"]:
            self.logger.log_error_to_both_file_and_allure(f"Invalid tab name: {tab_name}")
            return False

        try:
            # Try different locator strategies
            locators = [
                (By.XPATH,
                 f"//span[contains(@class, 'mat-button-toggle-label-content') and contains(., '{tab_name}')]"),
                (By.XPATH, f"//button[contains(., '{tab_name}')]"),
                (By.XPATH, f"//div[contains(@class, 'mat-button-toggle-label-content') and contains(., '{tab_name}')]")
            ]

            for locator in locators:
                try:
                    tab = self.element_utils.wait_for_element_to_be_clickable(locator, timeout=3)
                    if tab:
                        self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", tab)
                        time.sleep(0.5)
                        self.driver.execute_script("arguments[0].click();", tab)
                        time.sleep(1)  # Wait for tab to switch
                        self.logger.log_info_to_both_file_and_allure(f"Switched to {tab_name} tab")
                        return True
                except:
                    continue

            self.logger.log_warning_to_both_file_and_allure(f"Could not find {tab_name} tab with any locator")
            return False

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error switching to {tab_name} tab: {str(e)}")
            return False

    def switch_back_to_main_content(self) -> None:
        """Switch back to the main content from an iframe."""
        self.driver.switch_to.default_content()
        self.logger.log_info_to_both_file_and_allure("🔙 Switched back to main page content.")

    def fill_black_book_iframe_form(self, vehicle: str, mileage: str, zip_code: str) -> str:
        """Fill out the Black Book iframe form.

        Args:
            vehicle: Vehicle information
            mileage: Mileage of the vehicle
            zip_code: Zip code

        Returns:
            str: The trade-in price
        """
        try:
            self.select_current_vehicle(vehicle)
            self.logger.log_debug_only_to_file("selected vehicle")

            self.enter_mileage(mileage)
            self.logger.log_debug_only_to_file("entered mileage")

            self.enter_zip(zip_code)
            self.logger.log_debug_only_to_file("entered zip")

            time.sleep(2)
            self.click_next_button()
            self.logger.log_debug_only_to_file("clicked on next button")

            self.logger.log_debug_only_to_file("waiting for 5 second")
            time.sleep(5)

            self.click_get_estimate_button()
            self.logger.log_debug_only_to_file("clicked on get estimate button")

            time.sleep(5)
            self.logger.log_debug_only_to_file("trying to click on No I don't button")

            self.click_no_i_dont_button()
            self.logger.log_debug_only_to_file("clicked on No I Don't button")

            try:
                self.driver.execute_script(
                    "window.scrollTo({top: 0, behavior: 'smooth'})"
                )
                self.logger.log_debug_only_to_file("Scrolled to top of the iframe")
                time.sleep(1)
            except Exception as e:
                self.logger.log_debug_only_to_file(f"Could not scroll to top of iframe: {str(e)}")

            price = self.get_trade_in_price()
            self.logger.log_info_to_both_file_and_allure(f"Got the Trade In Price: {price}")
            self.logger.log_info_to_both_file_and_allure("🚗 Submitted trade-in form inside Black Book iframe.")

            self.switch_back_to_main_content()
            time.sleep(1)
            return price
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error filling out Black Book iframe form: {str(e)}")
            return ""
