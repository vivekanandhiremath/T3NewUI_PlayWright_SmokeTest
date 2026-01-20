from time import sleep

from selenium.common import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

from pageopject.BasePage import BasePage
from utilities.ElementUtils import ElementUtils
from utilities.customlogger import get_test_logger


class PreQualPage(BasePage):
    # Locators
    PRE_QUAL_LINK = (By.XPATH, "//span[normalize-space()='Get pre-qualified']")
    FIRST_NAME_INPUT = (By.ID, "firstname")
    LAST_NAME_INPUT = (By.ID, "lastname")
    EMAIL_INPUT = (By.ID, "email")
    PHONE_INPUT = (By.ID, "phone")
    ADDRESS_INPUT = (By.ID, "address")
    ADDRESS_SUGGESTIONS = (By.XPATH, "//div[@id='suggestions']/div")
    CITY_INPUT = (By.ID, "city")
    STATE_INPUT = (By.ID, "state")
    ZIP_CODE_INPUT = (By.ID, "zipcode")
    TERMS_CHECKBOX = (By.ID, "landscapeCheck")
    ESTIMATE_BUTTON = (By.ID, "estimateBtn")
    PREQUAL_MODAL = (By.CLASS_NAME, "modalPrequal")
    MODAL_TITLE = (By.CLASS_NAME, "modal-title-prequal")
    PROCESSING_SYMBOL = (By.ID, "processing-symbol")
    PROCEED_BUTTON = (By.CSS_SELECTOR, "button#submitBtnmanual")
    ADDRESS_OPTION = (By.XPATH, "(//input[@class='form-check-input'])[2]")
    USE_SELECTED = (By.XPATH, "//button[@id='useSelectedBtn']")

    def __init__(self, driver):
        super().__init__(driver)
        self.element_utils = ElementUtils(driver)
        self.logger = get_test_logger()

    def click_address_option(self):
        """Click on the address option and confirm selection."""
        try:
            self.logger.log_debug_only_to_file("Attempting to select address option")

            # Wait for the address option to be clickable
            address_option = self.element_utils.wait_for_element_to_be_clickable(
                self.ADDRESS_OPTION,
                timeout=10
            )

            # Scroll to the element
            self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", address_option)
            sleep(1)  # Small delay for scroll

            # Click using JavaScript as a fallback
            self.driver.execute_script("arguments[0].click();", address_option)
            self.logger.log_debug_only_to_file("Clicked on address option")

            # Wait for the "Use Selected" button and click it
            use_selected_btn = self.element_utils.wait_for_element_to_be_clickable(
                self.USE_SELECTED,
                timeout=10
            )
            self.driver.execute_script("arguments[0].click();", use_selected_btn)
            self.logger.log_debug_only_to_file("Clicked on 'Use Selected' button")

            # Wait for the address suggestion modal to disappear
            try:
                WebDriverWait(self.driver, 10).until(
                    EC.invisibility_of_element_located((By.CLASS_NAME, "addressSugggestModal"))
                )
                self.logger.log_debug_only_to_file("Address suggestion modal closed")
            except:
                self.logger.log_warning_to_both_file_and_allure("Address suggestion modal might still be visible")

            return True

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in address selection: {str(e)}")
            self.driver.save_screenshot("address_selection_error.png")
            return False

    def is_pre_qual_link_displayed(self):
        """Check if the pre-qualification link is displayed with scroll fallback.

        Returns:
            bool: True if the pre-qual link is displayed after checking with scroll fallback
        """
        try:
            # First try normal visibility check
            if self.element_utils.visibility_of_an_element(self.PRE_QUAL_LINK, timeout=5):
                return True

            # If not visible, try scrolling to it
            self.logger.log_debug_only_to_file("Pre-qual link not initially visible, trying with scroll...")
            element = self.driver.find_element(*self.PRE_QUAL_LINK)

            # Scroll to the element with smooth behavior
            self.driver.execute_script(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});",
                element
            )

            # Small delay to allow scroll to complete
            sleep(1)

            # Check visibility again after scroll
            return element.is_displayed()

        except Exception as e:
            self.logger.log_debug_only_to_file(
                f"Error in is_pre_qual_link_displayed: {str(e)}. Trying with JavaScript click...")
            try:
                # As a last resort, try JavaScript click if element is found but not interactable
                element = self.driver.find_element(*self.PRE_QUAL_LINK)
                self.driver.execute_script("arguments[0].click();", element)
                self.logger.log_debug_only_to_file("Clicked pre-qual link using JavaScript")
                return True
            except Exception as js_e:
                self.logger.log_debug_only_to_file(f"Failed to interact with pre-qual link: {str(js_e)}")
                return False

    def click_pre_qual_on_payment_section(self):
        """Click the pre-qualification link in the payment section."""
        self.element_utils.click_on_element(self.PRE_QUAL_LINK)

    def _is_input_empty(self, element):
        """Check if an input field is empty."""
        try:
            value = element.get_attribute("value")
            return not bool(value and value.strip())
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error checking if input is empty: {str(e)}")
            return True

    def _fill_personal_info(self, first_name, last_name, email, phone):
        """Fill personal information fields."""
        self.logger.log_debug_only_to_file("Filling personal information")

        # Add a small delay to ensure the form is fully loaded
        sleep(5)

        # First Name
        try:
            first_name_field = self.element_utils.wait_for_element_to_be_clickable(
                self.FIRST_NAME_INPUT,
                timeout=10
            )
            # Clear the field only if it's not already filled
            current_value = first_name_field.get_attribute("value")
            if not current_value or current_value.strip() == "":
                first_name_field.clear()
                first_name_field.send_keys(first_name)
            else:
                self.logger.log_debug_only_to_file(f"First name field already has value: {current_value}")

            # Last Name
            last_name_field = self.element_utils.wait_for_element_to_be_clickable(
                self.LAST_NAME_INPUT,
                timeout=5
            )
            current_value = last_name_field.get_attribute("value")
            if not current_value or current_value.strip() == "":
                last_name_field.clear()
                last_name_field.send_keys(last_name)
            else:
                self.logger.log_debug_only_to_file(f"Last name field already has value: {current_value}")

            # Email
            email_field = self.element_utils.wait_for_element_to_be_clickable(
                self.EMAIL_INPUT,
                timeout=5
            )
            current_value = email_field.get_attribute("value")
            if not current_value or current_value.strip() == "":
                email_field.clear()
                email_field.send_keys(email)
            else:
                self.logger.log_debug_only_to_file(f"Email field already has value: {current_value}")

            # Phone
            phone_field = self.element_utils.wait_for_element_to_be_clickable(
                self.PHONE_INPUT,
                timeout=5
            )
            current_value = phone_field.get_attribute("value")
            if not current_value or current_value.strip() == "":
                phone_field.clear()
                phone_field.send_keys(phone)
            else:
                self.logger.log_debug_only_to_file(f"Phone field already has value: {current_value}")

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error filling personal info: {str(e)}")
            raise

    def _fill_address(self, address, city, state, zip_code):
        """Fill address information."""
        self.logger.log_debug_only_to_file("Filling address information")

        # Address with auto-suggestion handling
        if self._is_input_empty(self.driver.find_element(*self.ADDRESS_INPUT)):
            self.element_utils.type_into_an_element(self.ADDRESS_INPUT, address)
            # Handle address suggestions if they appear
            try:
                suggestions = self.element_utils.visibility_of_an_element(self.ADDRESS_SUGGESTIONS, timeout=2)
                if suggestions:
                    suggestions[0].click()
            except:
                self.logger.log_error_to_both_file_and_allure("No address suggestions found")

        # City
        city_field = self.driver.find_element(*self.CITY_INPUT)
        if city_field.is_enabled() and self._is_input_empty(city_field):
            self.element_utils.type_into_an_element(self.CITY_INPUT, city)

        # State
        state_field = self.driver.find_element(*self.STATE_INPUT)
        if state_field.is_enabled() and self._is_input_empty(state_field):
            self.element_utils.type_into_an_element(self.STATE_INPUT, state)

        # ZIP Code
        zip_field = self.driver.find_element(*self.ZIP_CODE_INPUT)
        if zip_field.is_enabled() and self._is_input_empty(zip_field):
            self.element_utils.type_into_an_element(self.ZIP_CODE_INPUT, zip_code)

    def _accept_terms_and_conditions(self):
        """Accept terms and conditions if not already accepted."""
        try:
            terms_checkbox = self.driver.find_element(*self.TERMS_CHECKBOX)
            if not terms_checkbox.is_selected():
                self.element_utils.click_on_element(self.TERMS_CHECKBOX)
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error accepting terms: {str(e)}")
            raise

    def _submit_form(self):
        """Submit the pre-qualification form."""
        try:
            self.logger.log_debug_only_to_file("Submitting the form")

            # First try normal click
            try:
                estimate_button = self.element_utils.wait_for_element_to_be_clickable(
                    self.ESTIMATE_BUTTON,
                    timeout=10
                )
                estimate_button.click()
                self.logger.log_debug_only_to_file("Clicked submit button using normal click")
                return
            except Exception as e:
                self.logger.log_debug_only_to_file(f"Normal click failed, trying with scroll: {str(e)}")

            # If normal click fails, try with scroll
            try:
                # Find the submit button
                estimate_button = self.element_utils.wait_for_presence_of_element(
                    self.ESTIMATE_BUTTON,
                    timeout=10
                )

                # Scroll to the button
                self.driver.execute_script(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                    estimate_button
                )
                sleep(1)  # Small delay for scroll animation

                # Try clicking with JavaScript
                self.driver.execute_script("arguments[0].click();", estimate_button)
                self.logger.log_debug_only_to_file("Clicked submit button using JavaScript after scroll")

            except Exception as e:
                self.logger.log_error_to_both_file_and_allure(f"Failed to click submit button: {str(e)}")
                raise

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in form submission: {str(e)}")
            self.driver.save_screenshot("form_submission_error.png")
            raise

    def click_proceed_button(self):
        """
        Click the proceed button after form submission.
        Switches to the prequalification iframe, finds and clicks the proceed button,
        then switches back to default content.
        """
        try:
            self.logger.log_debug_only_to_file("Attempting to click proceed button")

            # Switch to default content first
            self.driver.switch_to.default_content()

            # Try to find the iframe by ID first, then by title if that fails
            try:
                # First try with ID
                iframe = self.element_utils.wait_for_presence_of_element(
                    (By.ID, "prequalifyIframe"),
                    timeout=10
                )
                self.logger.log_debug_only_to_file("Found iframe by ID: prequalifyIframe")
            except TimeoutException:
                # If ID fails, try with title
                self.logger.log_debug_only_to_file("Iframe not found by ID, trying with title...")
                iframe = self.element_utils.wait_for_presence_of_element(
                    (By.CSS_SELECTOR, "iframe[title*='prequal']"),
                    timeout=10
                )
                self.logger.log_debug_only_to_file("Found iframe by title")

            # Switch to the iframe
            self.driver.switch_to.frame(iframe)
            self.logger.log_debug_only_to_file("Switched to prequalification iframe")

            try:
                # Wait for the proceed button to be clickable
                proceed_btn = self.element_utils.wait_for_element_to_be_clickable(
                    (By.ID, "submitBtnmanual"),
                    timeout=10
                )

                # Scroll into view with smooth behavior
                self.driver.execute_script(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                    proceed_btn
                )

                # Small delay to ensure scrolling is complete
                sleep(0.5)

                # Try normal click first, fall back to JavaScript click if needed
                try:
                    proceed_btn.click()
                    self.logger.log_debug_only_to_file("Clicked on Proceed button using normal click")
                except Exception as e:
                    self.driver.execute_script("arguments[0].click();", proceed_btn)
                    self.logger.log_debug_only_to_file("Clicked on Proceed button using JavaScript")

            finally:
                # Always switch back to default content
                self.driver.switch_to.default_content()

        except TimeoutException as e:
            error_msg = f"Timeout waiting for proceed button: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            self.driver.save_screenshot("proceed_button_timeout.png")
            raise Exception(error_msg) from e

        except Exception as e:
            error_msg = f"Error clicking proceed button: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            self.driver.save_screenshot("proceed_button_error.png")
            raise Exception(error_msg) from e

        finally:
            # Final safety check to ensure we're back to default content
            try:
                self.driver.switch_to.default_content()
            except Exception as e:
                self.logger.log_debug_only_to_file(f"Error switching back to default content: {str(e)}")

    def fill_pre_qual_form_if_empty_and_submit(self, first_name, last_name, email, phone, address, city, state,
                                               zip_code):
        """Fill out and submit the pre-qualification form."""
        try:
            self.logger.log_debug_only_to_file("Starting to switch to Pre-qual form iframe")

            # Print all available iframes for debugging
            iframes = self.driver.find_elements(By.TAG_NAME, "iframe")
            self.logger.log_debug_only_to_file(f"Found {len(iframes)} iframes on the page")
            for i, iframe in enumerate(iframes):
                try:
                    self.logger.log_debug_only_to_file(
                        f"Iframe {i}: {iframe.get_attribute('id')} - {iframe.get_attribute('name')} - {iframe.get_attribute('src')}")
                except:
                    self.logger.log_debug_only_to_file(f"Iframe {i}: Could not get details")

            # Switch to the iframe first
            if not self.switch_to_adobe_id_syncing_iframe():
                self.logger.log_debug_only_to_file("Failed to switch to pre-qual iframe")
                self.driver.save_screenshot("iframe_switch_failed.png")
                return False

            # Small delay to ensure iframe content is fully loaded
            sleep(2)

            # Since we already verified firstname is visible in switch_to_adobe_id_syncing_iframe,
            # we can proceed directly to filling the form
            self.logger.log_debug_only_to_file("Proceeding to fill pre-qual form as firstname field is visible")

            try:
                # Fill the form
                self._fill_personal_info(first_name, last_name, email, phone)
                self._fill_address(address, city, state, zip_code)
                self.click_address_option()
                sleep(2)
                self._accept_terms_and_conditions()
                sleep(2)

                # Submit the form
                self._submit_form()

                self.click_proceed_button()

                self.logger.log_info_to_both_file_and_allure("Successfully submitted pre-qual form")
                return True

            except Exception as e:
                self.logger.log_error_to_both_file_and_allure(f"Error while filling form: {str(e)}", exc_info=True)
                self.driver.save_screenshot("form_fill_error.png")
                return False

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Error in fill_pre_qual_form_if_empty_and_submit: {str(e)}",
                                                          exc_info=True)
            self.driver.save_screenshot("pre_qual_form_error.png")
            return False
        finally:
            # Always switch back to default content
            try:
                self.driver.switch_to.default_content()
                self.logger.log_debug_only_to_file("Switched back to default content")
            except Exception as e:
                self.logger.log_debug_only_to_file(f"Error switching back to default content: {str(e)}")

    def is_pre_qual_form_displayed(self):
        """Check if the pre-qualification form is displayed."""
        try:
            # Check for the modal title
            title_visible = self.element_utils.visibility_of_an_element(self.MODAL_TITLE)
            # Also check for first name field as additional confirmation
            first_name_visible = self.element_utils.visibility_of_an_element(self.FIRST_NAME_INPUT)
            return title_visible and first_name_visible
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error checking if pre-qual form is displayed: {str(e)}")
            return False

    def switch_to_adobe_id_syncing_iframe(self, timeout=20):
        """Switch to the Adobe ID Syncing iframe and wait for content to be ready."""
        try:
            # Wait for the iframe to be present and switch to it
            iframe = self.element_utils.wait_for_presence_of_element(
                (By.ID, "prequalifyIframe"),
                timeout=timeout
            )
            self.logger.log_debug_only_to_file("Found prequalifyIframe, attempting to switch to it")

            # Switch to the iframe
            self.driver.switch_to.frame(iframe)
            self.logger.log_debug_only_to_file("Successfully switched to prequalifyIframe")

            # Wait for the document.readyState to be complete within the iframe
            WebDriverWait(self.driver, timeout).until(
                lambda d: d.execute_script("return document.readyState") == "complete"
            )
            self.logger.log_debug_only_to_file("Iframe document is fully loaded")

            # Wait for the firstname field to be present and visible
            try:
                WebDriverWait(self.driver, timeout).until(
                    EC.presence_of_element_located((By.ID, "firstname"))
                )
                WebDriverWait(self.driver, timeout).until(
                    EC.visibility_of_element_located((By.ID, "firstname"))
                )
                self.logger.log_debug_only_to_file("Firstname field is present and visible in iframe")
                return True
            except Exception as e:
                self.logger.log_error_to_both_file_and_allure(f"Firstname field not found or visible: {str(e)}")
                # Take a screenshot of the current iframe content
                self.driver.save_screenshot("iframe_content.png")
                return False

        except Exception as e:
            self.logger.log_error_to_both_file_and_allure(f"Failed to switch to prequalification iframe: {str(e)}")
            # Take a screenshot of the main page
            self.driver.save_screenshot("iframe_switch_error.png")
            return False

    def switch_to_default_content(self):
        """Switch back to the default content."""
        try:
            self.driver.switch_to.default_content()
            self.logger.log_debug_only_to_file("Switched back to default content")
        except Exception as e:
            self.logger.log_warning_to_both_file_and_allure(f"Error switching to default content: {str(e)}")
