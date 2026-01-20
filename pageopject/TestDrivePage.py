import time
from datetime import datetime, timedelta
from time import sleep

from selenium.common import NoSuchElementException
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from utilities.customlogger import get_test_logger
from .BasePage import BasePage


class TestDrivePage(BasePage):
    """Page object for Test Drive scheduling functionality."""

    def __init__(self, driver: WebDriver):
        """
        Initialize TestDrivePage.

        Args:
            driver: WebDriver instance
        """
        super().__init__(driver)
        self.logger = get_test_logger()

        # Locators
        self.test_drive_button = (By.ID, "testDriveBtn")
        self.first_name_input = (By.XPATH, "//input[@formcontrolname='firstName']")
        self.last_name_input = (By.XPATH, "//input[@formcontrolname='lastName']")
        self.zipcode_input = (By.XPATH, "//input[@formcontrolname='zipCode']")
        self.phone_number_input = (By.XPATH, "//input[@formcontrolname='phonenumber']")
        self.email_input = (By.XPATH, "//input[@formcontrolname='email']")
        self.upload_driver_license_checkbox = (By.XPATH, "//input[contains(@class, 'mdc-checkbox__native-control')]")
        self.date_input = (By.XPATH, "//button[@aria-label='Open calendar']")
        self.time_icon = (By.XPATH, "//mat-icon[contains(text(),'schedule') or contains(text(),'access_time')]")
        self.time_option = (By.XPATH, "//div[contains(@class, 'mat-time-period')]//button[contains(.,'{}')]")
        self.proceed_button = (By.XPATH, "//button[.//span[contains(text(),'Proceed') or contains(text(),'Submit')]]")
        self.test_drive_thank_you_popup_close_button = (By.XPATH, "//button[.//span[contains(text(),'Close')]]")

    def is_test_drive_button_displayed(self) -> bool:
        """Check if the Test Drive button is displayed with scroll fallback."""
        try:
            button = WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(self.test_drive_button)
            )
            return True
        except TimeoutException:
            self.logger.log_warning_to_both_file_and_allure("Test Drive button not visible, trying with scroll...")
            button = self.driver.find_element(*self.test_drive_button)
            self.driver.execute_script(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                button
            )
            time.sleep(0.5)
            return button.is_displayed()
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error checking Test Drive button: {str(e)}")
            return False

    def click_on_test_drive_button(self) -> None:
        """Click on the Test Drive button."""
        self.element_utils.click_on_element(self.test_drive_button)

    def fill_test_drive_form_and_submit(
            self,
            firstname: str,
            lastname: str,
            email: str,
            hour: str = "10",
            minute: str = "00",
            am_or_pm: str = "AM"
    ) -> None:
        """Fill and submit the test drive form."""
        try:
            self._fill_test_drive_form(firstname, lastname, email, hour, minute, am_or_pm)
            self.logger.log_info_to_both_file_and_allure("Test drive form submitted successfully")
        except Exception as e:
            error_msg = f"Failed to submit test drive form: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise RuntimeError(error_msg) from e

    def _fill_test_drive_form(
            self,
            firstname: str,
            lastname: str,
            email: str,
            hour: str,
            minute: str,
            am_or_pm: str
    ) -> None:
        """Internal method to fill the test drive form."""
        self.logger.log_info_to_both_file_and_allure("Starting to fill test drive form")

        # Fill in basic information
        self._fill_field_if_empty(self.first_name_input, firstname, "First name")
        self._fill_field_if_empty(self.last_name_input, lastname, "Last name")
        self._fill_field_if_empty(self.email_input, email, "Email")

        # Select date and time
        self._select_date()
        sleep(2)
        self.click_on_time_icon()
        sleep(2)
        self.select_time(hour, minute, am_or_pm)

        # Handle driver's license checkbox
        self._handle_drivers_license_checkbox()

        # Submit the form
        self._click_proceed_button()

    def _fill_field_if_empty(self, locator: tuple, value: str, field_name: str) -> None:
        """Fill a field if it's empty."""
        if self._is_input_empty(locator):
            self.logger.log_info_to_both_file_and_allure(f"Entering {field_name.lower()}: {value}")
            self.element_utils.type_into_an_element(locator, value)

    def _is_input_empty(self, locator: tuple) -> bool:
        """Check if an input field is empty."""
        try:
            element = self.driver.find_element(*locator)
            value = element.get_attribute("value")
            return not value or value.strip() == ""
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error checking if input is empty: {str(e)}")
            return True

    def _select_date(self) -> None:
        """Select tomorrow's date from the date picker."""
        tomorrow = datetime.now() + timedelta(days=1)
        month = tomorrow.strftime("%B")
        day = str(tomorrow.day)
        year = str(tomorrow.year)

        self.logger.log_info_to_both_file_and_allure(f"Selecting date: {month} {day}, {year}")
        self._enter_date(month, day, year)

    def _enter_date(self, month: str, day: str, year: str) -> None:
        """Select a date from the date picker calendar using Java-style locators."""
        try:
            target_date = f"{month} {int(day)}, {year}"
            self.logger.log_debug_only_to_file(f"Selecting date: {target_date}")

            # Click on the calendar icon to open date picker
            try:
                calendar_icon = WebDriverWait(self.driver, 10).until(
                    EC.element_to_be_clickable(
                        (By.XPATH,
                         "//button[@aria-label='Open calendar']//span[contains(@class, 'mat-mdc-button-touch-target')]")
                    )
                )
                self.driver.execute_script("arguments[0].click();", calendar_icon)
                self.logger.log_debug_only_to_file("Clicked calendar icon")
            except Exception as e:
                self.logger.warning(f"Could not click calendar icon: {str(e)}")
                # Fallback to direct date input click
                try:
                    date_input = WebDriverWait(self.driver, 5).until(
                        EC.element_to_be_clickable((By.XPATH, "//input[@formcontrolname='date']"))
                    )
                    date_input.click()
                    self.logger.log_debug_only_to_file("Clicked date input field directly")
                except Exception as e2:
                    self.logger.log_debug_only_to_file(f"Could not open date picker: {str(e2)}")
                    raise Exception("Failed to open date picker")
            sleep(2)
            # Wait for calendar to be visible
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located((By.CLASS_NAME, "mat-calendar"))
            )
            self.logger.log_debug_only_to_file("Calendar is visible")

            # Format the target date as it appears in the calendar (e.g., "January 2, 2026")
            formatted_date = f"{month} {int(day)}, {year}"

            # Find and click the date cell
            date_cell = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(
                    (By.XPATH, f"//button[@aria-label='{formatted_date}'][not(@disabled)]")
                )
            )
            sleep(2)
            self.driver.execute_script("arguments[0].scrollIntoView(true);", date_cell)
            self.driver.execute_script("arguments[0].click();", date_cell)
            self.logger.log_debug_only_to_file(f"Selected date: {formatted_date}")

            # Verify the date was set in the input
            try:
                WebDriverWait(self.driver, 5).until(
                    lambda d: d.find_element(By.XPATH, "//input[@formcontrolname='date']").get_attribute('value')
                )
                self.logger.log_info_to_both_file_and_allure("Date was successfully set")
            except TimeoutException:
                self.logger.log_warning_to_both_file_and_allure("Could not verify date was set in the input field")

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in _enter_date: {str(e)}")
            raise Exception(f"Failed to select date {target_date}: {str(e)}")

    def select_time(self, hour: str, minute: str, am_or_pm: str) -> None:
        """Select a time from the time picker.

        Args:
            hour: The hour to select (e.g., '9')
            minute: The minute to select (e.g., '00')
            am_or_pm: Either 'AM' or 'PM' (case-insensitive)

        Raises:
            ValueError: If am_or_pm is not 'AM' or 'PM'
            RuntimeError: If there's an error selecting the time
        """
        try:
            # Validate and normalize the period (AM/PM)
            normalized_period = am_or_pm.strip().upper()
            if normalized_period not in ('AM', 'PM'):
                error_msg = f"Invalid period: {am_or_pm}. Expected AM or PM."
                self.logger.log_debug_only_to_file(error_msg)
                raise ValueError(error_msg)

            # Format the time string (e.g., "9:00 AM")
            time_str = f"{hour.strip()}:{minute.strip()} {normalized_period}"
            self.logger.log_debug_only_to_file(f"Attempting to select time: {time_str}")

            # Create XPath for the time element
            time_xpath = f"//span[@class='mdc-list-item__primary-text' and normalize-space()='{time_str}']"

            # Wait for and click the time element
            time_element = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable((By.XPATH, time_xpath))
            )
            time_element.click()
            self.logger.log_info_to_both_file_and_allure(f"Successfully selected time: {time_str}")

        except (TimeoutException, NoSuchElementException) as e:
            error_msg = f"Failed to select time: {hour}:{minute} {am_or_pm} - {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise RuntimeError(error_msg) from e
        except Exception as e:
            error_msg = f"Unexpected error while selecting time {hour}:{minute} {am_or_pm} - {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise RuntimeError(error_msg) from e

    def _handle_drivers_license_checkbox(self) -> None:
        """Handle the driver's license checkbox with scroll and state check."""
        try:
            # Find the checkbox element
            checkbox = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(self.upload_driver_license_checkbox)
            )

            # Scroll the checkbox into view
            self.driver.execute_script("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", checkbox)
            sleep(1)  # Small delay for scroll to complete

            # Check if the checkbox is selected
            if checkbox.is_selected():
                self.logger.log_debug_only_to_file("Checkbox is selected, unchecking it...")
                # Scroll to the element again before clicking (in case page moved)
                self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", checkbox)
                # Click using JavaScript to avoid any potential overlay issues
                self.driver.execute_script("arguments[0].click();", checkbox)
                self.logger.log_debug_only_to_file("Successfully unchecked driver's license checkbox")
                sleep(2)
            else:
                self.logger.log_debug_only_to_file("Checkbox is already unchecked")

        except TimeoutException:
            self.logger.log_warning_to_both_file_and_allure(
                "Timed out waiting for driver's license checkbox to be present")
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Could not handle driver's license checkbox: {str(e)}")
            # Take screenshot for debugging
            screenshot_path = f"checkbox_error_{int(time.time())}.png"
            self.driver.save_screenshot(screenshot_path)
            self.logger.log_debug_only_to_file(f"Screenshot saved to: {screenshot_path}")

    def _click_proceed_button(self) -> None:
        """Click the proceed/submit button."""
        try:
            proceed_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(self.proceed_button)
            )
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", proceed_button)
            proceed_button.click()
            self.logger.log_debug_only_to_file("Clicked on Proceed button")
        except Exception as e:
            error_msg = f"Failed to click Proceed button: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise RuntimeError(error_msg) from e

    def click_on_test_drive_thank_you_popup_close_button(self) -> None:
        """Click the close button on the thank you popup."""
        try:
            close_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(self.test_drive_thank_you_popup_close_button)
            )
            close_button.click()
            self.logger.log_debug_only_to_file("Closed test drive thank you popup")
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Could not close test drive thank you popup: {str(e)}")

    def click_on_time_icon(self):
        try:
            self.element_utils.click_on_element(self.time_icon, 10)
        except (TimeoutException, NoSuchElementException) as e:
            self.logger.log_error_to_both_file_and_allure(f"Failed to click on Time Icon: {str(e)}")
            raise RuntimeError(f"Failed to click on Time Icon: {str(e)}") from e
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Failed to click on Time Icon: {str(e)}")
            raise RuntimeError(f"Failed to click on Time Icon: {str(e)}") from e
