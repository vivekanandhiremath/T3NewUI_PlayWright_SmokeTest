package utilities;

import pageobject.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LeadFormsHandler {

    private final CustomLogger logger = new CustomLogger();

    public String generateNewEmail() {
        String current_time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "shankar" + current_time + "@gmail.com";
    }

    public String handleInitialLeadForm(HomePage homePage, String leadType, VehicleDetails vehicleDetails) {
        String email = generateNewEmail();
        logger.logInfo("Generated email: " + email);

        try {
            if (homePage.isInitialFormVisible()) {
                logger.logInfo("Initial lead form is visible. Filling and submitting...");
                if (homePage.isCloseIconVisible()) {
                    logger.logInfo("Since the initial lead close icon is visible, this initial lead form is optional.");
                } else {
                    logger.logInfo("Since the initial lead close icon is not visible, this initial lead form is mandatory.");
                }

                homePage.fillAndSubmitInitialLeadForm("Sendto", "All", email);
                if (homePage.clickOnPostSubmitCloseButton()) {
                    logger.logStep("Submitted initial lead form and clicked post-submit close button.");
                } else {
                    logger.logWarning("Submitted initial lead form; post-submit close button was not clicked. Skipping downstream pre-qual flow.");
                    return null;
                }
            } else {
                logger.logInfo("Initial lead form not displayed.");
            }

            if (!homePage.isVehicleDetailsSideMenuVisible()) {
                logger.logWarning("Vehicle Details Side Menu is not visible after handling lead form.");
                return null;
            }

            switch (leadType.toLowerCase()) {
                case "browserclose":
                    // This will be handled in the main test
                    break;
                case "widgetclose":
                    homePage.clickOnWidgetClose();
                    logger.logInfo("Widget closed as per lead type: widgetclose");
                    break;
                case "idle":
                    logger.logInfo("Initial Lead form is submitted and kept idle.");
                    break;
                case "normal":
                    logger.logInfo("Initial Lead form is submitted");
                    break;
                default:
                    logger.logInfo("Initial Lead form is submitted with vehicle details");
                    break;
            }
            return email;
        } catch (Exception e) {
            logger.logError("Error handling initial lead form: " + e.getMessage());
            return null;
        }
    }

    public boolean handleTestDriveForm(TestDrivePage testDrivePage, HomePage homePage, VehicleDetails vehicleDetails, String email, String leadType) {
        try {
            if (!testDrivePage.isTestDriveButtonDisplayed()) {
                logger.logWarning("Test Drive button not displayed.");
                return false;
            }

            testDrivePage.clickOnTestDriveButton();
            // Python: fill_test_drive_form_and_submit("test","test", email, hour="9", minute="00", am_or_pm="AM")
            testDrivePage.fillTestDriveFormAndSubmit("test", "test", email, "9", "00", "AM");
            logger.logInfo("Submitted Test Drive form");

            switch (leadType.toLowerCase()) {
                case "widgetclose":
                    testDrivePage.clickOnTestDriveThankYouPopupCloseButton();
                    homePage.clickOnWidgetClose();
                    break;
                case "browserclose":
                    // This will be handled in the main test
                    break;
                case "idle":
                    logger.logInfo("Test drive form is submitted and kept idle.");
                    break;
                case "normal":
                    testDrivePage.clickOnTestDriveThankYouPopupCloseButton();
                    break;
            }
            return true;
        } catch (Exception e) {
            logger.logError("Error handling test drive form: " + e.getMessage());
            return false;
        }
    }

    public boolean handlePreQualForm(PreQualPage preQualPage, HomePage homePage, VehicleDetails vehicleDetails, String email, String leadType) {
        try {
            if (!preQualPage.isPreQualLinkDisplayed()) {
                logger.logWarning("Pre Qual button not displayed.");
                return false;
            }

            preQualPage.clickPreQualLink();
            boolean formSubmitted = preQualPage.fillPreQualFormWithJs("Sendto", "All", email, "4356467890", "1535 Broadway", "New York", "NY", "10036");
            if (!formSubmitted) {
                logger.logError("Pre-qual form submission failed.");
                return false;
            }
            logger.logInfo("Successfully submitted Pre-qual Form");

            switch (leadType.toLowerCase()) {
                case "widgetclose":
                    homePage.clickOnWidgetClose();
                    break;
                case "browserclose":
                    // This will be handled in the main test
                    break;
                case "idle":
                    logger.logInfo("Pre Qual form is submitted and kept idle.");
                    break;
            }
            return true;
        } catch (Exception e) {
            logger.logError("Error handling pre-qual form: " + e.getMessage());
            return false;
        }
    }

    public void handleApplyFormCreditForm(ApplyForCreditPage afcp, HomePage homePage, VehicleDetails vehicleDetails, String email, String leadType) {
        if (!homePage.clickApplyForCreditButton()) {
            logger.logInfo("Apply for Credit button not found or not clickable, skipping credit application");
            return;
        }

        try {
            afcp.fillApplyForCreditFormAndSubmit("Sendto", "All", email);
            logger.logInfo("Submitted Apply For Credit form");

            switch (leadType.toLowerCase()) {
                case "widgetclose":
                    if (afcp.isCloseButtonVisible()) {
                        afcp.clickCloseButton();
                    } else if (afcp.isBackButtonVisible()) {
                        afcp.clickBackButton();
                    } else {
                        logger.logWarning("Close/back button not clickable, cannot close widget");
                        break;
                    }
                    homePage.clickOnWidgetClose();
                    logger.logInfo("Widget closed successfully.");
                    break;
                case "browserclose":
                    // This will be handled in the main test
                    break;
                case "idle":
                    logger.logInfo("Apply For Credit form is submitted and kept idle.");
                    break;
                case "normal":
                    afcp.closeCreditAppForm();
                    logger.logInfo("Apply For Credit form is submitted.");

                    break;
            }
        } catch (Exception e) {
            logger.logError("Error in Apply For Credit form handling: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean handleReserveForm(ReservePage reservePage, HomePage homePage, VehicleDetails vehicleDetails, String email, String leadType) {
        try {
            logger.logInfo("Starting reserve form handling...");
            if (reservePage.checkReserveButtonVisibility()) {
                logger.logInfo("Reserve button found, clicking it...");
                if (!reservePage.clickReserveButton()) {
                    logger.logWarning("Failed to click Reserve button, but continuing...");
                }
            } else {
                logger.logInfo("Reserve button not found, skipping reserve form");
                return true;
            }

            if (!reservePage.completeReserveForm("Sendto", "All", "5634567890", email)) {
                logger.logError("Failed to complete reserve form.");
                return false;
            }

            logger.logInfo("Successfully submitted Reserve form");

            switch (leadType.toLowerCase()) {
                case "widgetclose":
                    homePage.clickOnWidgetClose();
                    logger.logInfo("Widget closed after reserve form submission.");
                    break;
                case "browserclose":
                    // This will be handled in the main test
                    break;
                case "idle":
                    logger.logInfo("Reserve form is submitted and kept idle.");
                    break;
                case "normal":
                    reservePage.clickReserveBackButton();
                    logger.logInfo("Reserve form submitted successfully.");
                    break;
                default:
                    logger.logWarning("Unknown lead type: " + leadType + ". Proceeding with normal handling.");
                    break;
            }
            return true;
        } catch (Exception e) {
            logger.logError("Error handling reserve form: " + e.getMessage());
            return false;
        }
    }
}
