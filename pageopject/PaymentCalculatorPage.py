import re
from time import sleep

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from utilities.customlogger import get_test_logger
from .BasePage import BasePage


class PaymentCalculatorPage(BasePage):
    """Page object for Payment Calculator functionality."""

    def __init__(self, driver: WebDriver):
        """
        Initialize PaymentCalculatorPage.

        Args:
            driver: WebDriver instance
        """
        super().__init__(driver)
        self.logger = get_test_logger()

        # Payment type locators (updated)
        self.lease_button = (By.XPATH, "//span[contains(text(), 'Lease')]")
        self.finance_button = (By.XPATH, "//span[contains(text(), 'Finance')]")
        self.cash_button = (By.XPATH, "//span[contains(text(), 'Cash')]")

        # Month chip locators (updated)
        self.month_chips = (By.XPATH, "//mat-chip-option[contains(@class, 'CalculatorBtns')]")
        self.selected_month_chip = (By.XPATH, "//mat-chip-option[contains(@class, 'mat-mdc-chip-selected')]")

    def get_current_payment_type(self):
        """Get the currently selected payment type"""
        try:
            # Debug: Check what payment buttons exist
            payment_buttons = self.driver.find_elements(By.XPATH, "//button[@name='payType']")
            self.logger.log_debug_only_to_file(f"Found {len(payment_buttons)} payment buttons")

            for i, button in enumerate(payment_buttons):
                aria_checked = button.get_attribute("aria-checked")
                text = button.find_element(By.CLASS_NAME, "mat-button-toggle-label-content").text.strip()
                self.logger.log_debug_only_to_file(f"Button {i}: {text}, aria-checked: {aria_checked}")
                if aria_checked == "true":
                    return text
            return None
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error getting current payment type: {str(e)}")
            return None

    def get_current_selected_month(self):
        """Get the currently selected month value"""
        try:
            sleep(3)

            selected_chips = self.driver.find_elements(*self.selected_month_chip)
            if selected_chips:
                selected_chip = selected_chips[0]
                # Try multiple approaches to get text
                month_value = None

                # Approach 1: Try specific class
                text_elements = selected_chip.find_elements(By.CLASS_NAME, "mdc-evolution-chip__text-label")
                if text_elements and len(text_elements) > 0:
                    chip_text = text_elements[0].text
                    if chip_text and chip_text.strip():
                        month_value = chip_text.strip().split()[0]
                else:
                    # Approach 2: Try getting text directly from chip
                    chip_text = selected_chip.text.strip()
                    if chip_text:
                        # Extract number from text like "48 months"
                        match = re.search(r'\d+', chip_text)
                        if match:
                            month_value = match.group()

                if month_value:
                    self.logger.log_info_to_both_file_and_allure(f"Current selected month: {month_value}")
                    return month_value
                else:
                    self.logger.log_debug_only_to_file("Selected month chip found but couldn't extract month value")
                    return None
            else:
                self.logger.log_debug_only_to_file("No selected month chip found")
                return None
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error getting selected month: {str(e)}")
            return None

    def select_payment_type(self, payment_type):
        """Select payment type: 'Lease', 'Finance', or 'Cash'"""
        try:
            current_type = self.get_current_payment_type()
            self.logger.log_info_to_both_file_and_allure(f"Current payment type: {current_type}")

            if current_type == payment_type:
                self.logger.log_info_to_both_file_and_allure(f"{payment_type} is already selected")
                return

            # Wait for any overlay to disappear
            sleep(2)

            if payment_type.lower() == "lease":
                self.element_utils.click_on_element(self.lease_button)
            elif payment_type.lower() == "finance":
                self.element_utils.click_on_element(self.finance_button)
            elif payment_type.lower() == "cash":
                self.element_utils.click_on_element(self.cash_button)
            else:
                raise ValueError(f"Invalid payment type: {payment_type}. Use 'Lease', 'Finance', or 'Cash'")

            # Wait for selection to complete
            sleep(2)
            self.logger.log_info_to_both_file_and_allure(f"Selected {payment_type} payment type")

        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error selecting payment type {payment_type}: {str(e)}")
            raise

    def select_month_by_value(self, target_month):
        """Select month chip by its value (e.g., '42', '48', '60')"""
        try:
            current_month = self.get_current_selected_month()
            self.logger.log_info_to_both_file_and_allure(
                f"Current month: {current_month}, Target month: {target_month}")

            if current_month == target_month:
                self.logger.log_info_to_both_file_and_allure(f"Month {target_month} is already selected")
                return

            # Find all month chips
            month_chips = self.driver.find_elements(*self.month_chips)

            for chip in month_chips:
                chip_text = chip.find_element(By.CLASS_NAME, "mdc-evolution-chip__text-label").text
                chip_month = chip_text.strip().split()[0]

                if chip_month == target_month:
                    self.element_utils.click_on_element(
                        (By.XPATH, f"//mat-chip-option[.//span[contains(text(), '{target_month}')]]"))
                    self.logger.log_info_to_both_file_and_allure(f"Selected month: {target_month}")
                    return

            raise ValueError(f"Month {target_month} not found in available options")

        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error selecting month {target_month}: {str(e)}")
            raise

    def get_available_months(self):
        """Get list of all available month options"""
        try:
            sleep(3)

            month_chips = self.driver.find_elements(*self.month_chips)
            months = []

            self.logger.log_debug_only_to_file(f"Found {len(month_chips)} month chips")

            for i, chip in enumerate(month_chips):
                try:
                    month_value = None

                    # Approach 1: Try specific class
                    text_elements = chip.find_elements(By.CLASS_NAME, "mdc-evolution-chip__text-label")
                    if text_elements and len(text_elements) > 0:
                        chip_text = text_elements[0].text
                        if chip_text and chip_text.strip():
                            month_value = chip_text.strip().split()[0]
                    else:
                        # Approach 2: Try getting text directly from chip
                        chip_text = chip.text.strip()
                        if chip_text:
                            # Extract number from text like "48 months"
                            match = re.search(r'\d+', chip_text)
                            if match:
                                month_value = match.group()

                    if month_value:
                        months.append(month_value)
                        self.logger.log_debug_only_to_file(f"Month chip {i}: {month_value}")
                    else:
                        self.logger.log_debug_only_to_file(f"Month chip {i}: Could not extract month value")
                except Exception as e:
                    self.logger.log_debug_only_to_file(f"Error reading month chip {i}: {str(e)}")

            self.logger.log_info_to_both_file_and_allure(f"Available months: {', '.join(months) if months else 'None'}")
            return months

        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error getting available months: {str(e)}")
            return []

    def switch_to_lease_tab(self):
        """Switch to Lease payment tab"""
        try:
            tab_locator = (By.XPATH,
                           "//span[contains(@class, 'mat-button-toggle-label-content') and contains(., 'Lease')]")

            if self.driver.find_elements(*tab_locator):
                tab = self.driver.find_element(*tab_locator)
                self.driver.execute_script("arguments[0].scrollIntoView(true);", tab)
                self.driver.execute_script("arguments[0].click();", tab)
                sleep(3)

                self.logger.log_info_to_both_file_and_allure("Switched to Lease tab")
                return True
            else:
                self.logger.log_warning_to_both_file_and_allure("Lease tab not found")
                return False
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error switching to Lease: {str(e)}")
            return False

    def switch_to_finance_tab(self):
        """Switch to Finance payment tab"""
        try:
            tab_locator = (By.XPATH,
                           "//span[contains(@class, 'mat-button-toggle-label-content') and contains(., 'Finance')]")

            if self.driver.find_elements(*tab_locator):
                tab = self.driver.find_element(*tab_locator)
                self.driver.execute_script("arguments[0].scrollIntoView(true);", tab)
                self.driver.execute_script("arguments[0].click();", tab)
                sleep(3)

                self.logger.log_info_to_both_file_and_allure("Switched to Finance tab")
                return True
            else:
                self.logger.log_warning_to_both_file_and_allure("Finance tab not found")
                return False
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error switching to Finance: {str(e)}")
            return False

    def switch_to_cash_tab(self):
        """Switch to Cash payment tab"""
        try:
            tab_locator = (By.XPATH,
                           "//span[contains(@class, 'mat-button-toggle-label-content') and contains(., 'Cash')]")

            if self.driver.find_elements(*tab_locator):
                tab = self.driver.find_element(*tab_locator)
                self.driver.execute_script("arguments[0].scrollIntoView(true);", tab)
                self.driver.execute_script("arguments[0].click();", tab)
                sleep(3)

                self.logger.log_info_to_both_file_and_allure("Switched to Cash tab")
                return True
            else:
                self.logger.log_warning_to_both_file_and_allure("Cash tab not found")
                return False
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error switching to Cash: {str(e)}")
            return False

    def log_months_for_current_payment_type(self, payment_type):
        """Log available months for current payment type"""
        try:
            available_months = self.get_available_months()
            current_month = self.get_current_selected_month()

            if payment_type.lower() == "cash":
                self.logger.log_info_to_both_file_and_allure(f"{payment_type}: No months available (as expected)")
            else:
                month_info = f"Current: {current_month}, Available: {', '.join(available_months)}"
                self.logger.log_info_to_both_file_and_allure(f"{payment_type}: {month_info}")

            return available_months
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error logging months for {payment_type}: {str(e)}")
            return []

    def test_all_payment_types_and_months(self):
        """Test all payment types (Lease, Finance, Cash) and get their available months"""
        payment_types = ["Lease", "Finance", "Cash"]
        results = {}

        for payment_type in payment_types:
            try:
                # Switch to payment type using same successful logic from TradeInPage
                tab_locator = (By.XPATH,
                               f"//span[contains(@class, 'mat-button-toggle-label-content') and contains(., '{payment_type}')]")

                if self.driver.find_elements(*tab_locator):
                    tab = self.driver.find_element(*tab_locator)
                    self.driver.execute_script("arguments[0].scrollIntoView(true);", tab)
                    self.driver.execute_script("arguments[0].click();", tab)
                    sleep(3)  # Wait for UI to stabilize

                    self.logger.log_info_to_both_file_and_allure(f"Switched to {payment_type} tab")

                    # Get available months
                    available_months = self.get_available_months()
                    current_month = self.get_current_selected_month()

                    if payment_type.lower() == "cash":
                        self.logger.log_info_to_both_file_and_allure(
                            f"{payment_type}: No months available (as expected)")
                        results[payment_type] = "No months (Cash)"
                    else:
                        month_info = f"Current: {current_month}, Available: {', '.join(available_months)}"
                        self.logger.log_info_to_both_file_and_allure(f"{payment_type}: {month_info}")
                        results[payment_type] = available_months
                else:
                    self.logger.log_warning_to_both_file_and_allure(f"Tab {payment_type} not found")
                    results[payment_type] = []

            except Exception as e:
                self.logger.log_warning_to_both_file_and_allure(f"Error testing {payment_type}: {str(e)}")
                results[payment_type] = []

        return results

    def switch_payment_type_and_month(self, payment_type, target_month=None):
        """Main method to switch payment type and optionally select a specific month"""
        try:
            # Get current state
            current_payment = self.get_current_payment_type()
            current_month = self.get_current_selected_month()

            self.logger.log_info_to_both_file_and_allure(
                f"Current state - Payment: {current_payment}, Month: {current_month}")

            # Switch payment type if needed
            if current_payment != payment_type:
                self.select_payment_type(payment_type)

            # For Cash payment type, months are not applicable
            if payment_type.lower() == "cash":
                self.logger.log_info_to_both_file_and_allure("Cash payment selected - months are not applicable")
                return

            # Select month if specified and different from current
            if target_month and current_month != target_month:
                self.select_month_by_value(target_month)
            elif target_month:
                self.logger.log_info_to_both_file_and_allure(f"Month {target_month} is already selected")

            # Log final state
            final_payment = self.get_current_payment_type()
            final_month = self.get_current_selected_month() if payment_type.lower() != "cash" else "N/A"
            self.logger.log_info_to_both_file_and_allure(
                f"Final state - Payment: {final_payment}, Month: {final_month}")

        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error in switch_payment_type_and_month: {str(e)}")
            raise
