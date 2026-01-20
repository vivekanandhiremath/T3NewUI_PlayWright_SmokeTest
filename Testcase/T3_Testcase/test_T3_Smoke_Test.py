import os
import time
from datetime import datetime

import allure
import pytest

from Testcase.BaseTest import BaseTest
from pageopject.landpage import LandingPage
from utilities.ReadCSV import get_all_urls
from utilities.customlogger import TestLogger


def get_test_urls():
    """Helper method to get test URLs with their indices"""
    return [(i, url) for i, url in enumerate(get_all_urls(), 1)]


class TestT3Smoke:
    @pytest.fixture(autouse=True)
    def setup(self, request):
        """Setup for the test class"""
        self.test_name = request.node.name
        self.start_time = datetime.now()
        self.urls = get_all_urls()
        self.url_count = len(self.urls)
        self.log = TestLogger(test_name=self.test_name)
        os.makedirs("results", exist_ok=True)
        yield
        end_time = datetime.now()
        duration = (end_time - self.start_time).total_seconds()
        self.log.log_info_to_both_file_and_allure(f"Test {self.test_name} completed in {duration:.2f} seconds")

    @pytest.mark.parametrize("index,url", get_test_urls(),
                             ids=[f"URL {i}: {url}" for i, url in enumerate(get_all_urls(), 1)])
    @allure.story("T3_Testcase Smoke Test Flow")
    @allure.severity(allure.severity_level.CRITICAL)
    @allure.description("""
    This test verifies the basic smoke flow of the T3_Testcase application.
    Test Data:
    - Environment: QA
    - Browser: Chrome
    Expected Results:
    - Application should launch successfully
    - All core functionalities should work as expected
    - Database should be updated correctly
    """)
    def test_smoke_flow_single_url(self, request, db_connection, headless, index, url):
        """Test the smoke flow for a single URL with retry logic"""
        test_name = f"URL {index}/{len(get_all_urls())}: {url}"
        allure.dynamic.title(test_name)

        max_retries = 1  # Retry once if initial attempt fails
        retry_count = 0

        while retry_count <= max_retries:
            test = None
            try:
                with allure.step(f"Testing {test_name} (Attempt {retry_count + 1})"):
                    test = BaseTest(
                        base_url=url,
                        project_type='t3',
                        headless=headless
                    )
                    test.log.log_step_to_both_file_and_allure(
                        f"Starting test for {test_name} (Attempt {retry_count + 1})", "INFO"
                    )
                    test.setup()

                    landing_page = LandingPage(test.driver)

                    # Check if URL loaded properly
                    if not landing_page.is_payment_option_displayed():
                        if retry_count < max_retries:
                            retry_count += 1
                            error_msg = f"Payment CTA not found for {url}. Retrying... (Attempt {retry_count + 1})"
                            test.log.log_warning_to_both_file_and_allure(error_msg)
                            test.teardown()
                            time.sleep(2)  # Brief pause before retry
                            continue
                        else:
                            skip_msg = f"Skipping URL {url} - Payment CTA not found after {max_retries + 1} attempts"
                            test.log.log_warning_to_both_file_and_allure(skip_msg)
                            pytest.skip(skip_msg)

                    result = test.run_test(db_connection=db_connection)
                    test.log.log_step_to_both_file_and_allure(
                        f"Test completed successfully for {url}", "PASS"
                    )
                    return result

            except Exception as e:
                if retry_count < max_retries:
                    retry_count += 1
                    error_msg = f"Test failed for {url}: {str(e)}. Retrying... (Attempt {retry_count + 1})"
                    if test and hasattr(test, 'log'):
                        test.log.log_warning_to_both_file_and_allure(error_msg)
                    test.teardown()
                    time.sleep(2)  # Brief pause before retry
                    continue
                else:
                    error_msg = f"Test failed for {url} after {max_retries + 1} attempts: {str(e)}"
                    if test and hasattr(test, 'log'):
                        test.log.log_error_to_both_file_and_allure(error_msg)
                    raise

            finally:
                if test:
                    try:
                        test.teardown()
                    except Exception as e:
                        if hasattr(self, 'log'):
                            self.log.log_error_to_both_file_and_allure(
                                f"Error during teardown for {url}: {str(e)}",
                                raise_exception=False
                            )
