package projects;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.opencsv.exceptions.CsvException;
import pageobject.ApplyForCreditPage;
import pageobject.HomePage;
import pageobject.LandingPage;
import utilities.*;

import java.io.IOException;
import java.util.List;

public class DTCreditAppProject {

    private final Page page;
    private final CustomLogger logger;
    private final DatabaseConnection dbConnection;
    private final EncryptEmail encryptEmail;
    private final boolean leadVerificationFlag;
    private final Environment environment;
    private final boolean isProd;
    private ExtentTest extentTest;
    private DwspUtils dwspUtils;
    private CsvUtils csvUtils;
    private String csvFilePath;
    private int csvRowIndex;
    private String vehicleType;

    public DTCreditAppProject(Page page,
            CustomLogger logger,
            DatabaseConnection dbConnection,
            EncryptEmail encryptEmail,
            String csvFilePath,
            int csvRowIndex,
            String vehicleType,
            Environment environment,
            boolean leadVerificationFlag) {

        this.page = page;
        this.logger = logger;
        this.dbConnection = dbConnection;
        this.encryptEmail = encryptEmail;

        this.csvFilePath = csvFilePath;
        this.csvRowIndex = csvRowIndex;
        this.vehicleType = vehicleType;
        this.leadVerificationFlag = leadVerificationFlag;

        this.environment = environment;
        this.isProd = environment == Environment.PROD;

        this.dwspUtils = new DwspUtils(page, logger);
        this.csvUtils = new CsvUtils(logger);
    }

    public void setExtentTest(ExtentTest extentTest) {
        this.extentTest = extentTest;
    }

    private ExtentTest node(String stepName) {
        if (extentTest != null)
            return extentTest.createNode(stepName);
        return null;
    }

    private void nodePass(ExtentTest node, String msg) {
        if (node != null)
            node.pass(msg);
        logger.logInfo(msg);
    }

    private void nodeWarn(ExtentTest node, String msg) {
        if (node != null)
            node.warning(msg);
        logger.logWarning(msg);
    }

    private void nodeFail(ExtentTest node, String msg) {
        if (node != null)
            node.fail(msg);
        logger.logError(msg);
    }

    private void nodeInfo(ExtentTest node, String msg) {
        if (node != null)
            node.info(msg);
        logger.logInfo(msg);
    }

    private void nodeSkip(ExtentTest node, String msg) {
        if (node != null)
            node.skip(msg);
        logger.logWarning(msg);
    }

    private void navigateToCsvUrlAndDetectDwsp() throws IOException, CsvException {
        if (csvFilePath == null || csvRowIndex < 0) {
            logger.logInfo("CSV not enabled");
            return;
        }

        List<String[]> data = csvUtils.readCsv(csvFilePath);
        String url = csvUtils.getCell(data, csvRowIndex, 0);

        if (url == null || url.trim().isEmpty()) {
            logger.logWarning("Empty CSV URL");
            return;
        }

        url = url.replace("\uFEFF", "")
                .replace("\u200B", "")
                .replace("\u00A0", " ")
                .trim();

        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        logger.logInfo("Navigating to: " + url);
        page.navigate(url);
        page.waitForLoadState();

        if (!isProd) {
            logger.logInfo("Non-PROD, skipping DWSP");
            return;
        }

        String dwsp = dwspUtils.checkDwsp();
        logger.logInfo("DWSP detected: " + dwsp);

        String baseUrl = DwspUtils.extractBaseUrl(page.url());

        String finalUrl = "used".equalsIgnoreCase(vehicleType)
                ? dwspUtils.buildUsedVehiclesUrl(baseUrl, dwsp)
                : dwspUtils.buildNewVehiclesUrl(baseUrl, dwsp);

        logger.logInfo("Redirecting to inventory: " + finalUrl);

        page.navigate(finalUrl);
        page.waitForLoadState();
        page.waitForTimeout(2000);
    }

    public void runFlow() {
        logger.logInfo("Running DT Credit App flow");
        PopupHandler popupHandler = new PopupHandler(page);

        if (isProd) {
            popupHandler.dismissAll();
        }

        LandingPage landingPage = new LandingPage(isProd, page, popupHandler, environment);
        HomePage homePage = new HomePage(page);
        ApplyForCreditPage applyForCreditPage = new ApplyForCreditPage(page);
        LeadFormsHandler leadFormsHandler = new LeadFormsHandler();

        Exception stepFailure = null;
        String email = null;

        // Step 1: CSV Navigation and DWSP Detection
        ExtentTest step1 = node("Step 1 - CSV Navigation and DWSP Detection");
        try {
            navigateToCsvUrlAndDetectDwsp();
            nodePass(step1, "CSV navigation and DWSP detection completed");
        } catch (Exception e) {
            nodeWarn(step1, "CSV mode not enabled or navigation failed: " + e.getMessage());
        }

        // Step 2: Click CTA and validate photos_gallary API (credit_flag)
        ExtentTest step2 = node("Step 2 - CTA Click and API Validation");
        VehicleDetails vehicleDetails = null;
        PhotosGallaryApiHandler apiHandler = new PhotosGallaryApiHandler(page, logger);
        PhotosGallaryApiHandler.PhotosGallaryResult apiResult = null;
        if (stepFailure != null) {
            nodeSkip(step2, "Skipped - earlier step failed.");
        } else {
            try {
                final VehicleDetails[] vdBox = new VehicleDetails[1];
                apiResult = apiHandler.captureAndValidate(
                        () -> vdBox[0] = landingPage.clickOnPaymentOption());
                vehicleDetails = vdBox[0];

                if (!apiResult.captured) {
                    nodeWarn(step2, "photos_gallary API response not captured - continuing.");
                } else {
                    nodeInfo(step2, "photos_gallary API response received (HTTP " + apiResult.httpStatus + ")");
                    boolean apiValid = apiHandler.validateApiResponse(apiResult);

                    if (!apiValid) {
                        nodeWarn(step2, "API validation warnings - continuing.");
                    } else {
                        nodeInfo(step2, "API response validated: status=" + apiResult.apiStatus
                                + ", code=" + apiResult.apiCode);
                    }

                    if (apiHandler.isDt2026CreditFlag(apiResult)) {
                        nodePass(step2, "credit_flag validated from API: dt2026");
                    } else {
                        nodeFail(step2, "credit_flag mismatch. Expected: dt2026, Found: " + apiResult.creditFlag);
                        stepFailure = new RuntimeException("credit_flag validation failed: " + apiResult.creditFlag);
                    }

                    nodeInfo(step2, "Vehicle: " + apiResult.make + " | Stock: " + apiResult.stockNumber);
                }
            } catch (Exception e) {
                nodeWarn(step2, "API validation: " + e.getMessage() + " - continuing.");
            }
        }

        // Step 3: Initial Lead Form Submission (unlocks widget & gets email)
        ExtentTest step3 = node("Step 3 - Initial Lead Form Submission");
        if (stepFailure != null) {
            nodeSkip(step3, "Skipped - earlier step failed.");
        } else {
            try {
                logger.logInfo("Submitting initial lead form to unlock the credit app...");
                email = leadFormsHandler.handleInitialLeadForm(homePage, "normal", vehicleDetails);
                if (email == null) {
                    nodeFail(step3, "Initial lead form failed - returned null email");
                    stepFailure = new RuntimeException("Lead form returned null email");
                } else {
                    nodePass(step3, "Initial lead form submitted. Email generated: " + email);
                    homePage.ensureDefaultContent();
                }
            } catch (Exception e) {
                nodeFail(step3, "Initial lead form submission failed: " + e.getMessage());
                stepFailure = e;
            }
        }

        // Step 4: Apply for Credit (clicks Apply for Credit, verifies UI, fills and
        // submits form)
        ExtentTest step4 = node("Step 4 - Apply for Credit");
        if (stepFailure != null) {
            nodeSkip(step4, "Skipped - earlier step failed.");
        } else {
            try {
                logger.logInfo("========== APPLY FOR CREDIT CHECK STARTED ==========");
                if (!homePage.clickApplyForCreditButton()) {
                    throw new RuntimeException("Apply for Credit button not found or not clickable");
                }

                // Fill and submit the simple credit form
                applyForCreditPage.fillApplyForCreditFormAndSubmit("Sendto", "All", email);

                // After submission, the full dt2026 credit application form loads — verify it
                if (apiResult != null && apiResult.captured && "dt2026".equals(apiResult.creditFlag)) {
                    nodeInfo(step4, "Verifying dt2026 credit form loaded on front end...");
                    boolean frontendVerified = apiHandler.verifyDt2026CreditFormLoaded();
                    if (frontendVerified) {
                        nodePass(step4, "dt2026 credit application form confirmed on front end.");
                    } else {
                        nodeWarn(step4, "dt2026 credit application form not loaded in front-end UI - continuing.");
                    }
                } else {
                    nodeInfo(step4, "No credit_flag from API to verify on front end.");
                }

                applyForCreditPage.closeCreditAppForm();

                nodePass(step4, "Apply for credit form handled successfully.");
            } catch (Exception e) {
                nodeFail(step4, "Apply for credit failed: " + e.getMessage());
                stepFailure = e;
            }
        }

        // Step 5: DB Verification
        ExtentTest step5 = node("Step 5 - Database Lead Verification");
        if (stepFailure != null) {
            nodeSkip(step5, "Skipped - earlier step failed.");
        } else {
            if (leadVerificationFlag) {
                try {
                    DBDataFetcher dbDataFetcher = new DBDataFetcher(dbConnection.getConnection());
                    String encryptedEmail = encryptEmail.getEncryptedEmail(email,
                            ReadProperties.getApplicationEncryptedURL());
                    nodeInfo(step5, "Encrypted email resolved.");

                    nodeInfo(step5, "Waiting for database propagation (3 seconds)...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    if (!dbDataFetcher.testSpecificLeadByEmail(encryptedEmail)) {
                        nodeInfo(step5, "Not found in main table - checking private offers...");
                        if (!dbDataFetcher.testSpecificLeadInPrivateOffersByEmail(encryptedEmail)) {
                            throw new AssertionError("Lead not found in main table or private offers");
                        }
                        nodePass(step5, "Lead found in private offers table.");
                    } else {
                        nodePass(step5, "Lead verification completed successfully.");
                    }
                } catch (Exception e) {
                    nodeFail(step5, "DB verification failed: " + e.getMessage());
                    stepFailure = e;
                }
            } else {
                nodeSkip(step5, "Skipped - lead verification is disabled.");
            }
        }

        if (stepFailure != null) {
            throw new AssertionError("DT Credit App flow failed. See report nodes for details.", stepFailure);
        }
    }
}
