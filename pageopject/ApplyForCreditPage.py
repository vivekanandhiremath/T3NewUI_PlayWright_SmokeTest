from time import sleep

from selenium.common.exceptions import TimeoutException, NoSuchElementException
from selenium.webdriver.common.by import By

from pageopject.BasePage import BasePage
from utilities.customlogger import get_test_logger


class ApplyForCreditPage(BasePage):
    def __init__(self, driver):
        super().__init__(driver)
        self.logger = get_test_logger()
        # Locators
        self.first_name_input = (By.XPATH, "//input[@placeholder='First Name']")
        self.last_name_input = (By.XPATH, "//input[@placeholder='Last Name']")
        self.zip_code_input = (By.ID, "mat-input-11")
        self.phone_input = (By.ID, "mat-input-12")
        self.email_input = (By.XPATH, "//input[@placeholder='Email']")
        self.submit_button = (By.XPATH, "//button[normalize-space()='Submit']")
        self.back_button = (By.ID, "backBtn")
        self.apply_for_credit_checkbox = (By.XPATH, "//div[contains(@class,'mat-mdc-checkbox-touch-target')]")

    def type_into_first_name(self, first_name):
        try:
            self.logger.log_info_to_both_file_and_allure(f"Typing into First Name input: {first_name}")
            self.element_utils.type_into_an_element(self.first_name_input, first_name)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error typing into First Name input: {str(e)}")

    def type_into_last_name(self, last_name):
        try:
            self.logger.log_info_to_both_file_and_allure(f"Typing into Last Name input: {last_name}")
            self.element_utils.type_into_an_element(self.last_name_input, last_name)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error typing into Last Name input: {str(e)}")

    def type_into_zip_code(self, zip_code):
        try:
            self.logger.log_info_to_both_file_and_allure(f"Typing into Zip Code input: {zip_code}")
            self.element_utils.type_into_an_element(self.zip_code_input, zip_code)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error typing into Zip Code input: {str(e)}")

    def type_into_phone(self, phone):
        try:
            self.logger.log_info_to_both_file_and_allure(f"Typing into Phone input: {phone}")
            self.element_utils.type_into_an_element(self.phone_input, phone)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error typing into Phone input: {str(e)}")

    def type_into_email(self, email):
        try:
            self.logger.log_info_to_both_file_and_allure(f"Typing into Email input: {email}")
            self.element_utils.type_into_an_element(self.email_input, email)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error typing into Email input: {str(e)}")

    def click_submit_button(self):
        try:
            self.logger.log_debug_only_to_file("Clicking on Submit button")
            self.element_utils.click_on_element(self.submit_button)
            self.logger.log_debug_only_to_file("Clicked on Submit button")

        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error clicking Submit button: {str(e)}")

    def is_back_button_clickable(self, log_result=False):
        try:
            result = self.element_utils.wait_for_element_to_be_visible(self.back_button)
            if log_result:
                self.logger.log_info_to_both_file_and_allure("Back button is displayed and clickable")
            return result
        except (NoSuchElementException, TimeoutException):
            self.logger.log_warning_to_both_file_and_allure("Back button is not displayed or not clickable")
            return False

    def click_back_button(self):
        try:
            self.logger.log_info_to_both_file_and_allure("Clicking on Back button")
            self.element_utils.click_on_element(self.back_button)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error clicking Back button: {str(e)}")

    def _is_input_empty(self, locator):
        try:
            element = self.driver.find_element(*locator)
            value = element.get_attribute("value")
            return value is None or value.strip() == ""
        except NoSuchElementException:
            return True

    def _fill_apply_for_credit_form_if_empty_and_submit(self, firstname, lastname, email):
        try:
            self.logger.log_info_to_both_file_and_allure("Starting to fill Apply For Credit form")

            if self._is_input_empty(self.first_name_input):
                self.logger.log_info_to_both_file_and_allure(f"Entering first name: {firstname}")
                self.type_into_first_name(firstname)

            if self._is_input_empty(self.last_name_input):
                self.logger.log_info_to_both_file_and_allure(f"Entering last name: {lastname}")
                self.type_into_last_name(lastname)

            if self._is_input_empty(self.email_input):
                self.logger.log_info_to_both_file_and_allure(f"Entering email: {email}")
                self.type_into_email(email)

            self.logger.log_info_to_both_file_and_allure("Submitting Apply For Credit form")
            self.click_submit_button()
            self.logger.log_debug_only_to_file("clicked on submit button")
            sleep(3)
            # self.log_back_button_visibility()

        except (TimeoutException, NoSuchElementException) as e:
            error_msg = f"Failed to submit Apply For Credit Form: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise RuntimeError(error_msg) from e
        except Exception as e:
            error_msg = f"Unexpected error while submitting Apply For Credit form: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise RuntimeError(f"Failed to submit Apply For Credit Form: {str(e)}") from e

    def fill_apply_for_credit_form_and_submit(self, firstname, lastname, email):
        try:
            self._fill_apply_for_credit_form_if_empty_and_submit(firstname, lastname, email)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to submit to apply for credit form: {str(e)}")
