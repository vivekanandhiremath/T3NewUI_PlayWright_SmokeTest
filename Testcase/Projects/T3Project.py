# Testcase/Projects/T3Project.py

from time import sleep

from Testcase.Projects.BaseProject import BaseProject
from Testcase.conftest import db_connection
from pageopject.ApplyForCreditPage import ApplyForCreditPage
from pageopject.HomePage import HomePage
from pageopject.PaymentCalculatorPage import PaymentCalculatorPage
# from pageopject.PaymentCalculatorPage import PaymentCalculatorPage
from pageopject.PrequalPage import PreQualPage
from pageopject.TestDrivePage import TestDrivePage
from pageopject.TradeInPage import TradeInPage
from pageopject.landpage import LandingPage
from utilities.Handle_Lead_Forms import LeadFormsHandler
from utilities.encryptEmail import EncryptEmail
from utilities.getDBData import test_specific_lead_in_private_offers_by_email
from utilities.readproperties import readconfig


class T3Project(BaseProject):
    def setup_project(self):
        self.logger.log_debug_only_to_file("Setting up T3_Testcase project")
        # T3_Testcase specific setup code here

    def run_flow(self, db_connection=None):
        self.logger.log_info_to_both_file_and_allure("Running T3_Testcase specific flow")
        landing_page = LandingPage(self.driver)
        home_page = HomePage(self.driver)
        trade_in_page = TradeInPage(self.driver)
        test_drive_page = TestDrivePage(self.driver)
        prequal_page = PreQualPage(self.driver)
        payment_calculator_page = PaymentCalculatorPage(self.driver)
        # T3_Testcase specific test steps
        sleep(2)
        vehicle_details = landing_page.click_on_payment_option()
        email = LeadFormsHandler.handle_initial_lead_form(home_page, vehicle_details)
        LeadFormsHandler.handle_pre_qual_form(prequal_page, home_page, vehicle_details, email, "normal")
        home_page.click_trade_in_button()
        trade_in_page.handle_trade_in()
        home_page.click_payment_calculator_icon()
        if home_page.is_payment_calculator_loaded():
            self.logger.log_info_to_both_file_and_allure("Calculator loaded")

        trade_in_page.compare_trade_in_across_tabs()

        payment_calculator_page.switch_to_lease_tab()
        lease_months = payment_calculator_page.log_months_for_current_payment_type("Lease")

        payment_calculator_page.switch_to_finance_tab()
        finance_months = payment_calculator_page.log_months_for_current_payment_type("Finance")

        payment_calculator_page.switch_to_cash_tab()
        cash_months = payment_calculator_page.log_months_for_current_payment_type("Cash")

        # Optional: Store results if needed
        payment_results = {
            "Lease": lease_months,
            "Finance": finance_months,
            "Cash": cash_months
        }

        home_page.click_on_payment_calculator_close_icon()

        LeadFormsHandler.handle_test_drive_form(test_drive_page, home_page, vehicle_details, email, "normal")

        apply_for_credit_page = ApplyForCreditPage(self.driver)
        LeadFormsHandler.handle_apply_form_credit_form(apply_for_credit_page, home_page, vehicle_details, email,
                                                       "normal")

        home_page.click_on_protection_menu()
        if home_page.is_protection_add_button_visible():
            home_page.click_on_protection_add_button()

        home_page.click_on_accessories_menu()
        home_page.click_on_review_button()
        home_page.click_on_submit_to_dealer_button()
        sleep(3)
        home_page.fill_submit_to_dealer_form_if_empty_and_submit("Sendto", "All", email)
        # Now handle the lead verification
        encryptor = None
        try:
            encryptor = EncryptEmail()
            encrypted_email = encryptor.get_encrypted_email(email, readconfig.getApplicationEncryptedURL())
            sleep(3)

            self.logger.log_info_to_both_file_and_allure("Starting lead verification in database...")
            try:
                from utilities.getDBData import test_specific_lead_by_email
                test_specific_lead_by_email(db_connection, encrypted_email)
                self.logger.log_info_to_both_file_and_allure("✅ Lead verification completed successfully")
            except AssertionError as e:
                self.logger.log_warning_to_both_file_and_allure(
                    "Lead not found in main table, checking private offers..."
                )
                if not test_specific_lead_in_private_offers_by_email(db_connection, encrypted_email):
                    raise AssertionError("Lead not found in main table or private offers")
                self.logger.log_info_to_both_file_and_allure("✅ Lead found in private offers table")
            return {
                "original_email": email,
                "encrypted_email": encrypted_email
            }

        except Exception as e:
            error_msg = f"Lead verification failed: {str(e)}"
            self.logger.log_error_to_both_file_and_allure(error_msg)
            raise AssertionError(error_msg)
        finally:
            if encryptor:
                encryptor.__del__()

    def teardown_project(self):
        self.logger.log_info_to_both_file_and_allure("Tearing down T3_Testcase project")
        # T3_Testcase specific teardown

    def _common_steps(self):
        # Common steps that might be shared
        pass
