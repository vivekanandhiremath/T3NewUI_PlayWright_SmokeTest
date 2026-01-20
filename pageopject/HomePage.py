import time
from time import sleep

from selenium.common import NoSuchElementException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from pageopject.BasePage import BasePage
from utilities.customlogger import get_test_logger


class HomePage(BasePage):
    def __init__(self, driver):
        super().__init__(driver)
        self.logger = get_test_logger()
        # Locators
        self.initial_lead_close_icon = (
            By.XPATH, "//button[contains(@class, 'closeIconMob') and @aria-label='Close']"
        )
        self.yes_button = (By.XPATH,
                           "//button[@class='widget-btn widget-btn-primary widget-btn-block credit_btn credit_btn_primary mdc-button mat-mdc-button mat-unthemed mat-mdc-button-base']//span[@class='mat-mdc-button-touch-target']")
        self.No_button = (By.XPATH, "//button[.//span[text()='NO']]")
        self.first_name = (By.XPATH, "//input[@id='firstname']")
        self.last_name = (By.XPATH, "//input[@id='lastname']")
        self.email = (By.XPATH, "//input[@id='email']")
        self.phone = (By.XPATH, "//input[@id='phone']")
        self.address = (By.XPATH, "//input[@id='address']")
        self.address_option = (By.XPATH, "(//input[@class='form-check-input'])[2]")
        self.use_selected = (By.XPATH, "//button[@id='useSelectedBtn']")
        self.terms_check_box = (By.XPATH, "//input[@id='landscapeCheck']")
        self.estimate_button = (By.XPATH, "//button[@id='estimateBtn']")
        self.proceed_button = (By.XPATH, "//button[@id='submitBtnmanual']")
        self.submit_to_dealer_button = (By.XPATH, "//button[.='Submit to Dealer']")
        self.sfirst_name_input = (By.XPATH, "//input[@placeholder='First Name']")
        self.slast_name_input = (By.XPATH, "//input[@placeholder='Last Name']")
        self.semail_input = (By.XPATH, "//input[@placeholder='Email']")
        self.sterms_and_conditions_checkbox = (By.XPATH, "//input[@id='Termsandcondition-input']")
        self.ssubmit_button = (By.XPATH, "//button[normalize-space()='Submit']")
        self.first_name_text_field = (By.XPATH, "//input[@placeholder='Enter your first name']")
        self.last_name_text_field = (By.XPATH, "//input[@placeholder='Enter your last name']")
        self.zipcode_text_field = (By.XPATH, "//input[@placeholder='Zip Code']")
        self.phone_number_text_field = (By.XPATH, "//input[@placeholder='+1']")
        self.email_text_field = (By.XPATH, "//input[@placeholder='i.e youremail@email.com']")
        self.terms_conditions_checkbox = (By.XPATH, "//input[@id='Termsandcondition-input']")
        self.submit_button = (By.XPATH, "//button[.=' Submit ']")
        self.thank_you_text = (By.XPATH, "//h6[@class='thanks-text']")
        self.continue_shopping_btn = (By.XPATH, "//button[normalize-space()='Continue shopping']")
        self.widget_exit_button = (By.XPATH, "//button[@id='mainwidgetCloseButton']")
        self.trade_in_button = (By.XPATH, "(//span[.='Trade-In'])[3]")
        self.protection_menu = (By.XPATH, "(//span[.='Protection'])[3]")
        self.accessories_menu = (By.XPATH, "(//span[.='Accessories'])[3]")
        self.review_button = (By.XPATH, "(//span[.='Review'])[3]")
        self.submit_to_dealer_submit_button = (By.XPATH, "//button[.='Submit']")
        self.payment_calculator_icon = (By.XPATH, "//div[@class='widget_price-details']//img[@alt='image']")
        self.payment_calculator_close_icon = (By.XPATH, "(//button[@id='closeCalculator'])[2]")
        self.protection_add_button = (By.XPATH, "(//span[.='Add'])[1]")
        self.submit_to_dealer_first_name = (By.CSS_SELECTOR, "input[formcontrolname='firstName']")
        self.submit_to_dealer_last_name = (By.CSS_SELECTOR, "input[formcontrolname='lastName']")
        self.submit_to_dealer_email = (By.CSS_SELECTOR, "input[formcontrolname='email']")
        self.vehicle_details_side_menu = (By.XPATH, "//img[@alt='vehicle details']")
        self.after_initial_lead_form_close_button = (By.XPATH,
                                                     "//button[@class='mD__close posiiton-absolute closeIconMob ng-star-inserted']")
        self.credit_estimator_no_button = (By.XPATH, "//span[normalize-space()='NO']")
        self.test_drive_button = (By.XPATH, "//button[normalize-space()='Test Drive']")
        self.trade_in_value_heading = (By.XPATH, "//h2[.=\"Let's Get Your Trade-In Value\"]")
        self.service_protection_heading = (By.XPATH, "//h3[normalize-space()='Service & Protection']")
        self.category_label = (By.XPATH, "//label[normalize-space()='Category']")
        self.purchase_options_label = (By.XPATH, "//mat-label[normalize-space()='Purchase Options']")
        self.submit_to_dealer_popup_heading = (By.XPATH, "//p[@class='submitDealerContent_title']")
        self.submit_to_dealer_form_heading = (By.XPATH, "//h3[@class='submit_to_dealer_dialog_title ng-star-inserted']")
        self.payment_calculator_heading = (By.XPATH,
                                           "//app-payment-calculator[@class='mat-mdc-dialog-component-host ng-star-inserted']//div[@class='payment-heading'][normalize-space()='Payment Estimator']")
        self.submit_to_dealer_thank_you_text = (By.XPATH,
                                                "//h6[contains(text(),'Thank you for your interest in acquiring one of ou')]")
        # self.apply_for_credit_button = (By.XPATH, "//button[@id='applyForCredit']/span[4]")
        self.apply_for_credit_button = (By.XPATH, "//button[contains(., 'Apply for Credit')]")

    def is_input_empty(self, input_field) -> bool:
        """Check if an input field is empty.

        Args:
            input_field: WebElement - The input field to check

        Returns:
            bool: True if the input is empty or contains only whitespace, False otherwise
        """
        value = input_field.get_attribute("value")
        return value is None or value.strip() == ""

    def fill_prequal_form_if_empty_and_submit(self, firstname: str, lastname: str, email: str, phone: str,
                                              zipcode: str) -> None:
        """Fill the prequalification form if fields are empty and submit.

        Args:
            firstname: First name to enter if field is empty
            lastname: Last name to enter if field is empty
            email: Email to enter if field is empty
            phone: Phone number to enter if field is empty
            zipcode: Zip code to enter if field is empty
        """

        def safe_send_keys(locator, value, field_name):
            """Helper function to safely send keys to a field with retries."""
            try:
                element = WebDriverWait(self.driver, 10).until(
                    EC.visibility_of_element_located(locator)
                )
                element.clear()
                element.send_keys(value)
                self.logger.log_info_to_both_file_and_allure(f"Entered {field_name}: {value}")
                return True
            except Exception as e:
                self.logger.log_error_to_both_file_and_allure(f"Failed to enter {field_name}: {str(e)}")
                return False

        try:
            # Wait for the form to be visible
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located((By.CSS_SELECTOR, "form"))
            )

            # Check if we need to switch to an iframe
            try:
                iframe = self.driver.find_element(By.CSS_SELECTOR, "iframe")
                if iframe:
                    self.driver.switch_to.frame(iframe)
                    self.logger.log_info_to_both_file_and_allure("Switched to iframe")
            except:
                pass  # No iframe found, continue with default content

            # Fill first name
            first_name_locator = (By.CSS_SELECTOR, "input[formcontrolname='firstName']")
            if not safe_send_keys(first_name_locator, firstname, "first name"):
                raise Exception("Failed to enter first name")

            # Fill last name
            last_name_locator = (By.CSS_SELECTOR, "input[formcontrolname='lastName']")
            if not safe_send_keys(last_name_locator, lastname, "last name"):
                raise Exception("Failed to enter last name")

            # Fill email
            email_locator = (By.CSS_SELECTOR, "input[formcontrolname='email']")
            if not safe_send_keys(email_locator, email, "email"):
                raise Exception("Failed to enter email")

            # Fill phone
            phone_locator = (By.CSS_SELECTOR, "input[formcontrolname='phone']")
            if not safe_send_keys(phone_locator, phone, "phone"):
                raise Exception("Failed to enter phone")

            # Fill zip code
            zip_locator = (By.CSS_SELECTOR, "input[formcontrolname='zipCode']")
            if not safe_send_keys(zip_locator, zipcode, "zip code"):
                raise Exception("Failed to enter zip code")

            # Submit the form
            self.click_prequal_submit_button()

        except Exception as e:
            error_msg = f"Failed to submit Prequalification Form: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            screenshot_path = f"prequal_form_error_{int(time.time())}.png"
            self.driver.save_screenshot(screenshot_path)
            self.logger.log_info_to_both_file_and_allure(f"Screenshot saved to: {screenshot_path}")
            # Switch back to default content in case we were in an iframe
            self.driver.switch_to.default_content()
            raise

    def click_prequal_submit_button(self):
        """Click the submit button on the prequalification form."""
        self.logger.log_info_to_both_file_and_allure("Attempting to submit the prequalification form")
        try:
            # Check terms and conditions if present
            try:
                terms_checkbox = WebDriverWait(self.driver, 5).until(
                    EC.element_to_be_clickable((By.CSS_SELECTOR, "input[formcontrolname='termsAccepted']"))
                )
                if not terms_checkbox.is_selected():
                    terms_checkbox.click()
                    self.logger.log_info_to_both_file_and_allure("Checked terms and conditions")
            except:
                self.logger.log_info_to_both_file_and_allure("No terms and conditions checkbox found, proceeding...")

            # Submit the form
            submit_btn = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, "button[type='submit']"))
            )
            submit_btn.click()
            self.logger.log_info_to_both_file_and_allure("Submitted the prequalification form")
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in click_prequal_submit_button: {str(e)}")
            raise

    def click_submittoForm_button(self):
        self.logger.log_info_to_both_file_and_allure("Attempting to check the Terms & Conditions checkbox")
        try:
            # Check terms and conditions if not already checked
            checkbox = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(self.terms_conditions_checkbox)
            )
            if not checkbox.is_selected():
                checkbox.click()
                self.logger.log_info_to_both_file_and_allure("Checked terms and conditions")

            # Submit the form
            submit_btn = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable((By.XPATH, "//button[.='Submit']"))
            )
            submit_btn.click()
            self.logger.log_info_to_both_file_and_allure("Submitted the dealer form")
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in click_submitToForm_button: {str(e)}")
            raise

    def some_method(self):
        self.logger.log_info_to_both_file_and_allure("This is a log message from HomePage class")

    def close_initial_lead_popup(self):
        self.logger.log_info_to_both_file_and_allure("Checking for lead popup close icon")
        try:
            self.element_utils.click_element(*self.initial_lead_close_icon)
            self.logger.log_info_to_both_file_and_allure("Closed the initial lead popup")
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Popup close icon not found or not clickable: {str(e)}")

    def handle_initial_lead_popup(self):
        """
        Checks if the initial lead popup is visible and handles it if present.
        Returns True if popup was found and handled, False otherwise.
        """
        try:
            close_icon = WebDriverWait(self.driver, 3).until(
                EC.visibility_of_element_located(self.initial_lead_close_icon)
            )
            if close_icon.is_displayed():
                self.logger.log_info_to_both_file_and_allure("Closing initial lead popup")
                self.close_initial_lead_popup()
                self.click_yes_button()
                return True
        except Exception as e:
            self.logger.log_info_to_both_file_and_allure(f"No initial lead popup found or error occurred: {str(e)}")
            return False

    def enter_text_field(self, locator, text, field_name=""):
        try:
            self.logger.log_info_to_both_file_and_allure(f"Attempting to enter '{text}' in {field_name}")

            # Wait for element to be present
            element = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(locator)
            )

            # Scroll into view
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", element)
            sleep(0.5)

            # Try normal send_keys first
            try:
                element.clear()
                element.send_keys(text)
                self.logger.log_info_to_both_file_and_allure(f"Entered '{text}' in {field_name} using normal send_keys")
            except:
                # JavaScript fallback
                self.driver.execute_script("""
                    var element = arguments[0];
                    var text = arguments[1];
                    element.value = text;
                    // Trigger necessary events
                    element.dispatchEvent(new Event('input', { bubbles: true }));
                    element.dispatchEvent(new Event('change', { bubbles: true }));
                """, element, text)
                self.logger.log_info_to_both_file_and_allure(f"Entered '{text}' in {field_name} using JavaScript")

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Failed to enter text in {field_name}: {str(e)}")
            raise

    def check_terms_checkbox(self):
        self.logger.log_info_to_both_file_and_allure("Attempting to check the Terms & Conditions checkbox")
        try:
            self.element_utils.click_on_element(*self.terms_check_box)
            self.logger.log_info_to_both_file_and_allure("Checkbox clicked successfully via WebDriver")
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(
                f"Checkbox click failed: {str(e)}. Trying JavaScript click...")
            try:
                element = self.driver.find_element(*self.terms_check_box)
                self.driver.execute_script("arguments[0].click();", element)
                self.logger.log_info_to_both_file_and_allure("Checkbox clicked via JavaScriptExecutor")
            except Exception as js_e:
                self.logger.log_error_to_both_file_and_allure(f"Checkbox JavaScript click failed: {str(js_e)}")
                raise

    def click_estimate_button(self):
        self.logger.info("Attempting to click the estimate button")
        try:
            element = self.driver.find_element(*self.estimate_button)
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element)
            self.element_utils.click_on_element(*self.estimate_button)
            self.logger.log_info_to_both_file_and_allure("estimate button clicked successfully via WebDriver")
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(
                f"Checkbox click failed: {str(e)}. Trying JavaScript click...")
            try:
                element = self.driver.find_element(*self.estimate_button)
                self.driver.execute_script("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element)
                self.driver.execute_script("arguments[0].click();", element)
                self.logger.log_info_to_both_file_and_allure("estimate button clicked via JavaScriptExecutor")
            except Exception as js_e:
                self.logger.log_error_to_both_file_and_allure(f"estimate button JavaScript click failed: {str(js_e)}")
                raise

    def click_proceed_button(self):
        self.logger.info("Attempting to click on the proceed button")
        try:
            sleep(2)
            element = self.driver.find_element(*self.proceed_button)
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element)
            self.element_utils.click_on_element(*self.proceed_button)
            self.logger.log_info_to_both_file_and_allure("proceed button clicked successfully via WebDriver")
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(
                f"Checkbox click failed: {str(e)}. Trying JavaScript click...")
            try:
                element = self.driver.find_element(*self.proceed_button)
                self.driver.execute_script("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element)
                self.driver.execute_script("arguments[0].click();", element)
                self.logger.log_info_to_both_file_and_allure("proceed button clicked via JavaScriptExecutor")
            except Exception as js_e:
                self.logger.log_error_to_both_file_and_allure(f"proceed button JavaScript click failed: {str(js_e)}")
                raise

    def click_address_option(self):
        sleep(3)
        self.element_utils.click_on_element(*self.address_option, description="Address option")
        sleep(2)
        self.element_utils.click_on_element(*self.use_selected, description="Use selected")

    def fill_pr_equal_lead_form(self):
        try:
            # Wait for iframe and switch to it
            iframe = WebDriverWait(self.driver, 20).until(
                EC.presence_of_element_located((By.ID, "prequalifyIframe"))
            )
            self.driver.switch_to.frame(iframe)
            self.logger.log_info_to_both_file_and_allure("Switched to iframe")

            # Try multiple approaches to set the first name
            first_name_value = "test"
            first_name_set = False

            # Try different locators for first name field
            first_name_locators = [
                (By.ID, "firstname"),  # Most specific
                (By.NAME, "firstName"),
                (By.CSS_SELECTOR, "input#firstname"),
                (By.CSS_SELECTOR, "input[name='firstName']"),
                (By.CSS_SELECTOR, "input[type='text']")
            ]

            for locator in first_name_locators:
                try:
                    # Wait for element to be present
                    first_name_field = WebDriverWait(self.driver, 5).until(
                        EC.presence_of_element_located(locator)
                    )

                    # Scroll into view
                    self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", first_name_field)
                    sleep(1)

                    # Try normal send_keys first
                    try:
                        first_name_field.clear()
                        first_name_field.send_keys(first_name_value)
                        self.logger.log_info_to_both_file_and_allure(
                            f"Set first name using normal send_keys with locator: {locator}")
                        first_name_set = True
                        break
                    except:
                        # Try JavaScript as fallback
                        try:
                            self.driver.execute_script("""
                                var field = arguments[0];
                                field.value = arguments[1];
                                // Trigger necessary events
                                field.dispatchEvent(new Event('input', { bubbles: true }));
                                field.dispatchEvent(new Event('change', { bubbles: true }));
                            """, first_name_field, first_name_value)
                            self.logger.log_info_to_both_file_and_allure(
                                f"Set first name using JavaScript with locator: {locator}")
                            first_name_set = True
                            break
                        except Exception as js_e:
                            self.logger.log_warning_to_both_file_and_allure(
                                f"JavaScript also failed for locator {locator}: {str(js_e)}")
                            continue

                except Exception as e:
                    self.logger.log_warning_to_both_file_and_allure(
                        f"First name field not found with locator {locator}: {str(e)}")
                    continue

            if not first_name_set:
                raise Exception("Failed to set first name with any locator")

            # Verify the value was set
            try:
                first_name_field = self.driver.find_element(By.ID, "firstname")
                value = first_name_field.get_attribute('value')
                self.logger.log_info_to_both_file_and_allure(f"First name field value after setting: '{value}'")
                if value.strip() != first_name_value:
                    raise Exception(f"First name not set correctly. Expected '{first_name_value}', got '{value}'")
            except Exception as e:
                self.logger.log_warning_to_both_file_and_allure(f"Could not verify first name value: {str(e)}")

            # Rest of your form filling code
            self.enter_text_field(self.last_name, "test", "Last Name")
            sleep(1)
            self.enter_text_field(self.email, "test@test.com", "Email")
            sleep(1)
            self.enter_text_field(self.phone, "4535674567", "Phone")
            sleep(1)
            self.enter_text_field(self.address, "2900 Southern Blvd, Bronx, NY 10458, USA", "Address")
            sleep(2)
            self.click_address_option()
            sleep(1)
            self.check_terms_checkbox()
            sleep(1)
            self.click_estimate_button()

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in fill_pr_equal_lead_form: {str(e)}")
            self.driver.save_screenshot("form_fill_error.png")
            raise
        finally:
            # Always switch back to default content
            try:
                self.driver.switch_to.default_content()
            except:
                pass

    def click_yes_button(self):
        self.element_utils.click_on_element(*self.yes_button, description="'Yes' button")

    def click_NO_button(self):
        self.element_utils.click_on_element(*self.No_button, description="'No' button")

    def click_termsandcondition_button(self):
        self.element_utils.click_on_element(*self.sterms_and_conditions_checkbox,
                                            description="Terms & Conditions checkbox")

    def click_Submit_to_dealer_button(self):
        self.element_utils.click_on_element(*self.ssubmit_button, description="Submit to dealer button")

    def is_initial_form_visible(self):
        try:
            sleep(6)
            # First find the element using the locator
            element = self.driver.find_element(*self.first_name_text_field)
            # Then check its visibility
            return element.is_displayed()
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error checking initial form visibility: {str(e)}")
            return False

    def is_close_icon_visible(self):
        try:
            return self.element_utils.visibility_of_an_element(self.initial_lead_close_icon, 10)
        except NoSuchElementException as e:
            self.logger.log_warning_to_both_file_and_allure(f"close icon is not visible:{str(e)}")
            return False

    def fill_and_submit_initial_lead_form(self, firstname, lastname, email):
        try:
            # if self.is_initial_form_visible():
            # Find elements first
            first_name_field = self.driver.find_element(*self.first_name_text_field)
            last_name_field = self.driver.find_element(*self.last_name_text_field)
            email_field = self.driver.find_element(*self.email_text_field)
            terms_checkbox = self.driver.find_element(*self.terms_conditions_checkbox)
            submit_button = self.driver.find_element(*self.submit_button)

            # Interact with found elements
            first_name_field.send_keys(firstname)
            last_name_field.send_keys(lastname)
            email_field.send_keys(email)

            # Scroll to the checkbox and click it
            self.driver.execute_script("arguments[0].scrollIntoView(true);", terms_checkbox)
            terms_checkbox.click()

            # Scroll to submit button and click it
            self.driver.execute_script("arguments[0].scrollIntoView(true);", submit_button)
            submit_button.click()

            self.logger.log_info_to_both_file_and_allure("Submitted initial lead form")
            return True
        # return False
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error submitting initial lead form: {str(e)}")
            return False

    def click_on_close_button_after_initial_lead_form(self):
        try:
            sleep(3)
            self.element_utils.click_on_element(self.after_initial_lead_form_close_button)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(
                f"Failed to click close button after initial lead form: {str(e)}")

    def click_trade_in_button(self):
        try:
            self.element_utils.click_on_element(self.trade_in_button)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click trade in button: {str(e)}")

    def click_payment_calculator_icon(self):
        try:
            self.element_utils.click_on_element(self.payment_calculator_icon)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click payment calculator icon: {str(e)}")

    def is_payment_calculator_loaded(self):
        sleep(3)
        try:
            return self.element_utils.visibility_of_an_element(self.payment_calculator_heading, 10)
        except Exception:
            return False

    def click_on_payment_calculator_close_icon(self):
        try:
            self.element_utils.click_on_element(self.payment_calculator_close_icon)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click payment calculator close icon: {str(e)}")

    def click_on_protection_menu(self):
        try:
            self.element_utils.click_on_element(self.protection_menu)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click protection menu: {str(e)}")

    def is_protection_add_button_visible(self):
        try:
            return self.element_utils.visibility_of_an_element(self.protection_add_button, 10)
        except Exception:
            return False

    def click_on_protection_add_button(self):
        try:
            self.element_utils.click_on_element(self.protection_add_button)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click protection add button: {str(e)}")

    def click_on_accessories_menu(self):
        try:
            self.element_utils.click_on_element(self.accessories_menu)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click accessories menu: {str(e)}")

    def click_on_review_button(self):
        try:
            self.element_utils.click_on_element(self.review_button)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click review button: {str(e)}")

    def click_on_submit_to_dealer_button(self):
        try:
            self.element_utils.click_on_element(self.submit_to_dealer_button)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Failed to click submit to dealer button: {str(e)}")

    def is_vehicle_details_side_menu_visible(self):
        """
        Check if the vehicle details side menu is visible.

        Returns:
            bool: True if visible, False otherwise
        """
        try:
            sleep(2)
            element = self.element_utils.visibility_of_an_element(self.vehicle_details_side_menu, 10)
            return element is not None
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(
                f"Error checking vehicle details side menu visibility: {str(e)}")
            return False

    def fill_submit_to_dealer_form_if_empty_and_submit(self, firstname: str, lastname: str, email: str) -> None:
        """Fill the submit to dealer form if fields are empty and submit."""

        def safe_send_keys(locator, value, field_name, clear_first=False):
            """Helper function to safely send keys to a field with retries."""
            try:
                element = WebDriverWait(self.driver, 5).until(
                    EC.visibility_of_element_located(locator)
                )

                # Get current value
                current_value = element.get_attribute('value') or ''

                # If field is not empty and we're not forcing a clear, skip filling
                if current_value.strip() and not clear_first:
                    self.logger.log_info_to_both_file_and_allure(
                        f"Field '{field_name}' already has value: '{current_value}'. Skipping...")
                    return True

                if clear_first:
                    element.clear()
                    time.sleep(0.5)  # Small delay after clear

                element.send_keys(value)
                self.logger.log_info_to_both_file_and_allure(f"Entered {field_name}: {value}")
                return True
            except Exception as e:
                self.logger.log_error_to_both_file_and_allure(f"Failed to enter {field_name}: {str(e)}")
                return False

        try:
            # Wait for the form to be interactive
            WebDriverWait(self.driver, 10).until(
                lambda d: d.execute_script('return document.readyState') == 'complete'
            )

            # Try to find the form container
            form_present = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, "form, [formgroup], [formgroupname]"))
            )
            self.logger.log_info_to_both_file_and_allure("Found form container")

            # Define locators based on the actual HTML structure
            field_locators = [
                # First name field
                (By.CSS_SELECTOR, "input[formcontrolname='firstName'], [formcontrolname='firstName'] input"),
                (By.CSS_SELECTOR, "input[placeholder='First Name'], input[placeholder*='First']"),

                # Last name field
                (By.CSS_SELECTOR, "input[formcontrolname='lastName'], [formcontrolname='lastName'] input"),
                (By.CSS_SELECTOR, "input[placeholder='Last Name'], input[placeholder*='Last']"),

                # Email field
                (By.CSS_SELECTOR,
                 "input[formcontrolname='email'], [formcontrolname='email'] input, input[type='email']"),
                (By.CSS_SELECTOR, "input[placeholder*='Email'], input[placeholder*='email']")
            ]

            # Fill first name
            first_name_found = safe_send_keys(
                field_locators[0],
                firstname,
                "first name",
                clear_first=False  # Don't clear if prefilled
            ) or safe_send_keys(
                field_locators[1],
                firstname,
                "first name (fallback)",
                clear_first=False
            )

            if not first_name_found:
                self.logger.log_warning_to_both_file_and_allure(
                    "Could not find first name field with standard locators, trying JavaScript")
                try:
                    self.driver.execute_script(f"""
                        const inputs = document.querySelectorAll('input');
                        for (let input of inputs) {{
                            if ((input.placeholder || '').toLowerCase().includes('first') || 
                                (input.id || '').toLowerCase().includes('first') ||
                                (input.name || '').toLowerCase().includes('first')) {{
                                input.value = '{firstname}';
                                input.dispatchEvent(new Event('input'));
                                return true;
                            }}
                        }}
                        return false;
                    """)
                    self.logger.log_info_to_both_file_and_allure("Set first name using JavaScript")
                    first_name_found = True
                except Exception as js_e:
                    self.logger.log_error_to_both_file_and_allure(f"JavaScript fallback failed: {str(js_e)}")

            if not first_name_found:
                raise Exception("Could not find first name field with any locator")

            # Fill last name
            last_name_found = safe_send_keys(
                field_locators[2],
                lastname,
                "last name",
                clear_first=False
            ) or safe_send_keys(
                field_locators[3],
                lastname,
                "last name (fallback)",
                clear_first=False
            )

            if not last_name_found:
                self.logger.log_warning_to_both_file_and_allure(
                    "Could not find last name field with standard locators, trying JavaScript")
                try:
                    self.driver.execute_script(f"""
                        const inputs = document.querySelectorAll('input');
                        for (let input of inputs) {{
                            if ((input.placeholder || '').toLowerCase().includes('last') || 
                                (input.id || '').toLowerCase().includes('last') ||
                                (input.name || '').toLowerCase().includes('last')) {{
                                input.value = '{lastname}';
                                input.dispatchEvent(new Event('input'));
                                return true;
                            }}
                        }}
                        return false;
                    """)
                    self.logger.log_info_to_both_file_and_allure("Set last name using JavaScript")
                    last_name_found = True
                except Exception as js_e:
                    self.logger.log_error_to_both_file_and_allure(f"JavaScript fallback failed: {str(js_e)}")

            if not last_name_found:
                raise Exception("Could not find last name field with any locator")

            # Fill email if provided
            if email:
                email_found = safe_send_keys(
                    field_locators[4],
                    email,
                    "email",
                    clear_first=False
                ) or safe_send_keys(
                    field_locators[5],
                    email,
                    "email (fallback)",
                    clear_first=False
                )

                if not email_found:
                    self.logger.log_warning_to_both_file_and_allure(
                        "Could not find email field with standard locators, trying JavaScript")
                    try:
                        self.driver.execute_script(f"""
                            const inputs = document.querySelectorAll('input[type="email"], input[placeholder*="mail"]');
                            for (let input of inputs) {{
                                input.value = '{email}';
                                input.dispatchEvent(new Event('input'));
                            }}
                        """)
                        self.logger.log_info_to_both_file_and_allure("Set email using JavaScript")
                        email_found = True
                    except Exception as js_e:
                        self.logger.log_error_to_both_file_and_allure(
                            f"JavaScript fallback for email failed: {str(js_e)}")

                if not email_found:
                    self.logger.log_warning_to_both_file_and_allure("Could not find email field, but continuing...")

            # Submit the form
            self.click_submittoForm_button()

        except Exception as e:
            error_msg = f"Failed to submit Submit to Dealer Form: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            screenshot_path = f"submit_dealer_form_error_{int(time.time())}.png"
            self.driver.save_screenshot(screenshot_path)
            self.logger.log_info_to_both_file_and_allure(f"Screenshot saved to: {screenshot_path}")
            raise

    def click_submittoForm_button(self):
        """Click the submit button on the submit to dealer form."""
        self.logger.log_info_to_both_file_and_allure("Attempting to submit the submit to dealer form")

        try:
            # Check terms and conditions if present
            try:
                terms_checkbox = WebDriverWait(self.driver, 5).until(
                    EC.element_to_be_clickable((By.CSS_SELECTOR, "input[formcontrolname='termsAccepted']"))
                )
                if not terms_checkbox.is_selected():
                    terms_checkbox.click()
                    self.logger.log_info_to_both_file_and_allure("Checked terms and conditions")
            except:
                self.logger.log_info_to_both_file_and_allure("No terms and conditions checkbox found, proceeding...")

            # Submit the form
            submit_btn = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable((By.XPATH, "//button[.='Submit']"))
            )
            submit_btn.click()

            sleep(3)
            self.logger.log_info_to_both_file_and_allure("Submitted the dealer form")
        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in click_submit_to_form_button: {str(e)}")
            raise

    def _is_lease_or_finance_selected(self):
        """Check if Lease or Finance payment option is selected"""
        try:
            # Check if Lease is selected (has checked state)
            lease_element = self.driver.find_element(By.XPATH,
                                                     "//span[contains(@class, 'mat-button-toggle-label-content')][contains(text(), 'Lease')]//mat-pseudo-checkbox[@state='checked']")
            if lease_element:
                return True
        except:
            pass

        try:
            # Check if Finance is selected
            finance_element = self.driver.find_element(By.XPATH,
                                                       "//span[contains(@class, 'mat-button-toggle-label-content')][contains(text(), 'Finance')]//mat-pseudo-checkbox[@state='checked']")
            if finance_element:
                return True
        except:
            pass

        return False

    def _switch_to_lease_or_finance(self):
        """Switch from Cash to Lease or Finance payment option"""
        try:
            # Try to click on Lease first
            lease_button = self.driver.find_element(By.XPATH,
                                                    "//span[contains(@class, 'mat-button-toggle-label-content')][contains(text(), 'Lease')]")
            lease_button.click()
            self.logger.log_info_to_both_file_and_allure("Switched to Lease payment option")
            return True
        except:
            try:
                # If Lease fails, try Finance
                finance_button = self.driver.find_element(By.XPATH,
                                                          "//span[contains(@class, 'mat-button-toggle-label-content')][contains(text(), 'Finance')]")
                finance_button.click()
                self.logger.log_info_to_both_file_and_allure("Switched to Finance payment option")
                return True
            except:
                self.logger.log_error_to_both_file_and_allure("Failed to switch to Lease or Finance payment option")
                return False

    def _visibility_of_apply_for_credit_button(self):
        """Check if Apply For Credit button is visible (only available for Lease/Finance)"""
        # First check if Lease or Finance is selected
        if not self._is_lease_or_finance_selected():
            self.logger.log_debug_only_to_file("Cash payment option is selected, switching to Lease/Finance")
            if not self._switch_to_lease_or_finance():
                return False

        # Now check if Apply For Credit button is visible
        return self.element_utils.visibility_of_an_element_to_be_clickable(self.apply_for_credit_button, 10)

    def click_apply_for_credit_button(self):
        """Click Apply For Credit button if visible. Returns True if clicked, False if not visible."""
        if self._visibility_of_apply_for_credit_button():
            try:
                self.element_utils.click_on_element(self.apply_for_credit_button)
                self.logger.log_info_to_both_file_and_allure("Successfully clicked Apply For Credit button")
                return True
            except Exception as e:
                self.logger.log_error_to_both_file_and_allure(f"Error clicking Apply For Credit button: {str(e)}")
                raise
        else:
            self.logger.log_skip_to_both_file_and_allure("Apply for credit button is not visible, skipping",
                                                         raise_skip_exception=False)
            return False
