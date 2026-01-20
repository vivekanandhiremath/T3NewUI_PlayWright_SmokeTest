import time

from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from utilities.DriverUtils import DriverUtils
from utilities.customlogger import get_test_logger


class EncryptEmail:
    def __init__(self, headless=True):
        self.driver_util = DriverUtils(headless=headless)
        self.driver = None
        self.wait = None
        self.logger = get_test_logger()
        self.initialize_driver()

    def initialize_driver(self):
        """Initialize and configure the WebDriver with retry logic"""
        max_retries = 3
        for attempt in range(max_retries):
            try:
                self.driver = self.driver_util.initialize_driver()
                self.driver.set_page_load_timeout(30)
                self.wait = WebDriverWait(self.driver, 15)
                return
            except Exception as e:
                self.logger.log_debug_only_to_file(f"Driver initialization attempt {attempt + 1} failed: {str(e)}")
                if attempt == max_retries - 1:
                    raise
                time.sleep(2)  # Wait before retry

    def get_encrypted_email(self, plain_email, base_url, max_retries=3):
        """Get encrypted email from the encryption service with retry logic"""
        for attempt in range(max_retries):
            try:
                self.logger.log_debug_only_to_file(f"Attempt {attempt + 1} to get encrypted email")

                # Navigate to the encryption service
                self.driver.get(base_url)
                self.logger.log_debug_only_to_file(f"Successfully navigated to {base_url}")

                # Wait for page to load completely
                time.sleep(2)

                # Input field interaction
                input_field = (By.XPATH, "//input[@id='plain_text']")
                self.wait.until(EC.presence_of_element_located(input_field))
                input_element = self.driver.find_element(*input_field)
                input_element.clear()
                input_element.send_keys(plain_email)
                self.logger.log_debug_only_to_file("Entered email in input field")

                # Click the encrypt button
                encrypt_btn = (By.XPATH, "//input[@value='encrypt']")
                self.wait.until(EC.element_to_be_clickable(encrypt_btn)).click()
                self.logger.log_debug_only_to_file("Clicked encrypt button")

                # Wait for the result to appear - using a more robust XPath
                result_locator = (By.XPATH, "//h3[contains(text(), 'Encrypted Value:')]/following-sibling::p")
                self.wait.until(EC.visibility_of_element_located(result_locator))

                # Get the encrypted value
                encrypted_element = self.driver.find_element(*result_locator)
                encrypted_value = encrypted_element.text.strip()

                if not encrypted_value:
                    raise ValueError("Empty encrypted value received")

                self.logger.log_debug_only_to_file(f"Successfully encrypted email: {encrypted_value}")
                return encrypted_value

            except TimeoutException as e:
                self.logger.log_debug_only_to_file(f"Timeout in attempt {attempt + 1}: {str(e)}")
                if attempt == max_retries - 1:
                    self.take_screenshot("encryption_timeout")
                    # Try one more approach before giving up
                    try:
                        encrypted_value = self.try_alternative_approach(plain_email)
                        if encrypted_value:
                            return encrypted_value
                    except Exception as alt_e:
                        self.logger.log_debug_only_to_file(f"Alternative approach also failed: {str(alt_e)}")
                    raise
            except Exception as e:
                self.logger.log_debug_only_to_file(f"Error in attempt {attempt + 1}: {str(e)}")
                if attempt == max_retries - 1:
                    self.take_screenshot("encryption_error")
                    raise

            # Wait before next attempt
            if attempt < max_retries - 1:
                time.sleep(2)

        raise Exception("All attempts to encrypt email failed")

    def try_alternative_approach(self, plain_email):
        """Try an alternative approach to get the encrypted value"""
        self.logger.log_debug_only_to_file("Trying alternative approach to get encrypted value")

        # Try to find any visible paragraph that might contain the encrypted value
        paragraphs = self.driver.find_elements(By.TAG_NAME, "p")
        for p in paragraphs:
            if p.is_displayed() and p.text and len(p.text) > 20:  # Encrypted values are usually long
                encrypted_value = p.text.strip()
                self.logger.log_debug_only_to_file(
                    f"Found potential encrypted value using alternative method: {encrypted_value}")
                return encrypted_value

        # If no paragraph found, try to find by text content
        page_source = self.driver.page_source
        if "Encrypted Value:" in page_source:
            start = page_source.find("Encrypted Value:") + len("Encrypted Value:")
            end = page_source.find("</p>", start)
            if end > start:
                encrypted_value = page_source[start:end].split(">")[-1].strip()
                if encrypted_value:
                    self.logger.log_debug_only_to_file(f"Found encrypted value in page source: {encrypted_value}")
                    return encrypted_value

        raise ValueError("Could not find encrypted value using alternative methods")

    def take_screenshot(self, name):
        """Take a screenshot for debugging"""
        try:
            timestamp = time.strftime("%Y%m%d_%H%M%S")
            filename = f"screenshot_{name}_{timestamp}.png"
            self.driver.save_screenshot(filename)
            self.logger.log_debug_only_to_file(f"Screenshot saved as {filename}")
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Failed to take screenshot: {str(e)}")

    def __del__(self):
        """Clean up resources"""
        try:
            if self.driver:
                self.driver.quit()
        except Exception as e:
            self.logger.log_debug_only_to_file(f"Error during cleanup: {str(e)}")


# Example usage
if __name__ == "__main__":
    encryptor = None
    try:
        encryptor = EncryptEmail(headless=False)
        encrypted = encryptor.get_encrypted_email(
            "test@example.com",
            "https://web-ihub.eshopdemo.net/"
        )
        print(f"Success! Encrypted value: {encrypted}")
    except Exception as e:
        print(f"Failed to encrypt email: {str(e)}")
    finally:
        if encryptor:
            encryptor.__del__()
