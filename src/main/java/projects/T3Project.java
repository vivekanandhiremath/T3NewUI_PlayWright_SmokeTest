package projects;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import com.opencsv.exceptions.CsvException;
import pageobject.*;
import utilities.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.aventstack.extentreports.MediaEntityBuilder;

public class T3Project {

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
    private String vehicleType; // "new" or "used"

    // Constructor for CSV-based flow
    public T3Project(Page page,
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

    public void runFlow() {
        logger.logInfo("Running T3_Testcase specific flow");
        logger.logInfo("🚀 Starting T3 flow");

        List<String[]> csvData;
        try {
            csvData = csvUtils.readCsv(csvFilePath);
        } catch (Exception e) {
            logger.logError("Failed to read CSV: " + e.getMessage());
            ExtentTest errNode = node("CSV Read Error");
            if (errNode != null)
                errNode.fail("Failed to read CSV: " + e.getMessage());
            throw new RuntimeException("CSV read failed", e);
        }

        if (csvData == null || csvData.isEmpty()) {
            logger.logError("CSV file is empty.");
            ExtentTest errNode = node("CSV Empty");
            if (errNode != null)
                errNode.fail("CSV file is empty.");
            throw new RuntimeException("CSV is empty");
        }

        int totalUrls = csvData.size();
        int passed = 0;
        int failed = 0;
        List<String> urlFailures = new ArrayList<>();
        ExtentTest originalTest = extentTest;

        for (int i = 0; i < totalUrls; i++) {
            String rawUrl = csvUtils.getCell(csvData, i, 0);
            if (rawUrl == null) {
                logger.logWarning("Skipping null URL at row " + i);
                failed++;
                urlFailures.add("Row " + i + ": null URL");
                continue;
            }
            String url = rawUrl.replace("\uFEFF", "")
                    .replace("\u200B", "")
                    .replace("\u00A0", " ")
                    .trim();
            if (url.isEmpty()) {
                logger.logWarning("Skipping empty URL at row " + i);
                failed++;
                urlFailures.add("Row " + i + ": empty URL");
                continue;
            }
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }

            logger.logInfo("══════════════════════════════════════════");
            logger.logInfo("Processing URL " + (i + 1) + "/" + totalUrls + ": " + url);
            logger.logInfo("══════════════════════════════════════════");

            ExtentTest urlNode = node("URL " + (i + 1) + ": " + url);
            this.extentTest = urlNode;

            PopupHandler popupHandler = new PopupHandler(page);
            if (isProd) {
                popupHandler.dismissAll();
            }

            LandingPage landingPage = new LandingPage(isProd, page, popupHandler, environment);
            HomePage homePage = new HomePage(page);
            TradeInPage tradeInPage = new TradeInPage(page);
            TestDrivePage testDrivePage = new TestDrivePage(page);
            PreQualPage preQualPage = new PreQualPage(page);
            PaymentCalculatorPage calcPage = new PaymentCalculatorPage(page);
            ApplyForCreditPage applyForCreditPage = new ApplyForCreditPage(page);
            LeadFormsHandler leadFormsHandler = new LeadFormsHandler();

            Exception stepFailure = null;
            String email = null;
            boolean urlPassed = false;

            try {
                // Step 0: CSV Navigation and DWSP Detection
                ExtentTest step0 = node("Step 0 - CSV Navigation and DWSP Detection");
                try {
                    navigateToCsvUrlForUrl(url);
                    nodePass(step0, "CSV navigation and DWSP detection completed");
                } catch (Exception e) {
                    nodeWarn(step0, "Navigation failed: " + e.getMessage());
                }

                // Step 2: Lead Form
                ExtentTest step1 = node("Step 2 - Lead Form Submission");
                try {
                    VehicleDetails vehicleDetails = landingPage.clickOnPaymentOption();
                    email = leadFormsHandler.handleInitialLeadForm(homePage, "normal", vehicleDetails);
                    if (email == null) {
                        nodeWarn(step1, "Initial lead form not fully completed - email is null.");
                        stepFailure = new RuntimeException("Lead form returned null email");
                    } else {
                        boolean preQualSuccess = leadFormsHandler.handlePreQualForm(preQualPage, homePage,
                                vehicleDetails, email, "normal");
                        homePage.ensureDefaultContent();

                        if (!preQualSuccess) {
                            nodeWarn(step1, "Pre-qual form was skipped or failed.");
                            if (step1 != null) {
                                String base64 = Base64.getEncoder().encodeToString(
                                        page.screenshot(new Page.ScreenshotOptions().setFullPage(false)));
                                step1.warning("Screenshot for warning (Pre-qual form skipped/failed):",
                                        MediaEntityBuilder
                                                .createScreenCaptureFromBase64String(base64, "Pre-qual form warning")
                                                .build());
                            }
                        } else {
                            nodePass(step1, "Lead form submitted. Email: " + email);
                        }
                    }
                } catch (Exception e) {
                    nodeFail(step1, "Lead form failed: " + e.getMessage());
                    stepFailure = e;
                }

                // Step 3: Trade-in
                ExtentTest step2 = node("Step 3 - Trade-in Flow");
                if (stepFailure != null) {
                    nodeSkip(step2, "Skipped - earlier step failed.");
                } else {
                    try {
                        if (homePage.clickTradeInButton()) {
                            Map<String, String> tradeInResult = tradeInPage.handleTradeIn();
                            String provider = tradeInResult.getOrDefault("provider", "NONE");
                            if (!"None".equalsIgnoreCase(provider) && !"NONE".equalsIgnoreCase(provider)) {
                                nodeInfo(step2, "Trade-in provider: " + provider);
                                tradeInPage.compareTradeInAcrossTabs();
                                nodePass(step2, "Trade-in completed by " + provider);
                            } else {
                                nodeWarn(step2, "No BB/KBB iframe found - trade-in skipped gracefully.");
                            }
                        } else {
                            nodeWarn(step2, "Trade-In tab not found - skipping.");
                        }
                    } catch (Exception e) {
                        nodeWarn(step2, "Trade-in non-fatal: " + e.getMessage() + " - continuing.");
                    }
                }

                // Step 4: Payment Calculator
                ExtentTest step3 = node("Step 4 - Payment Calculator");
                if (stepFailure != null) {
                    nodeSkip(step3, "Skipped - earlier step failed.");
                } else {
                    try {
                        homePage.clickPaymentCalculatorIcon();
                        if (homePage.isPaymentCalculatorLoaded()) {
                            Map<String, Map<String, Object>> summary = calcPage.processAllPaymentTypes();
                            nodeInfo(step3, "Payment summary: " + summary);
                            nodePass(step3, "Payment calculator processed successfully.");
                        } else {
                            nodeWarn(step3, "Payment calculator did not load - skipping.");
                        }
                    } catch (Exception e) {
                        nodeWarn(step3, "Payment calculator non-fatal: " + e.getMessage() + " - continuing.");
                        try {
                            homePage.forceClosePaymentCalculator();
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        page.waitForTimeout(800);
                    } catch (Exception ignored) {
                    }
                }

                // Step 5: Test Drive
                ExtentTest step4 = node("Step 5 - Test Drive Form");
                if (stepFailure != null) {
                    nodeSkip(step4, "Skipped - earlier step failed.");
                } else {
                    try {
                        leadFormsHandler.handleTestDriveForm(testDrivePage, homePage, null, email, "normal");
                        nodePass(step4, "Test drive form submitted successfully.");
                    } catch (Exception e) {
                        nodeFail(step4, "Test drive failed: " + e.getMessage());
                        stepFailure = e;
                    }
                }

                // Step 6: Apply for Credit
                ExtentTest step5 = node("Step 6 - Apply for Credit");
                if (stepFailure != null) {
                    nodeSkip(step5, "Skipped - earlier step failed.");
                } else {
                    try {
                        leadFormsHandler.handleApplyFormCreditForm(applyForCreditPage, homePage, null, email, "normal");
                        nodePass(step5, "Apply for credit form handled.");
                    } catch (Exception e) {
                        nodeWarn(step5, "Apply for credit non-fatal: " + e.getMessage() + " - continuing.");
                    }
                }

                // Step 7: Protection / Accessories / Submit
                ExtentTest step6 = node("Step 7 - Protection, Accessories and Submit to Dealer");
                if (stepFailure != null) {
                    nodeSkip(step6, "Skipped - earlier step failed.");
                } else {
                    try {
                        try {
                            homePage.dismissBlockingOverlayIfPresent();
                        } catch (Exception ignored) {
                        }
                        try {
                            page.waitForTimeout(500);
                        } catch (Exception ignored) {
                        }
                        homePage.clickOnProtectionMenu();
                        if (homePage.isProtectionAddButtonVisible())
                            homePage.clickOnProtectionAddButton();
                        nodeInfo(step6, "Protection menu handled.");
                        homePage.clickOnAccessoriesMenu();
                        nodeInfo(step6, "Accessories menu handled.");
                        homePage.clickOnReviewButton();
                        nodeInfo(step6, "Review button clicked.");
                        homePage.clickOnSubmitToDealerButton();
                        try {
                            page.waitForTimeout(3000);
                        } catch (Exception ignored) {
                        }
                        homePage.fillSubmitToDealerFormIfEmptyAndSubmit("Sendto", "All", email);
                        nodePass(step6, "Submit to Dealer completed.");
                    } catch (Exception e) {
                        nodeFail(step6, "Submit to dealer failed: " + e.getMessage());
                        stepFailure = e;
                    }
                }

                // Step 8: DB Verification
                ExtentTest step7 = node("Step 8 - Database Lead Verification");
                if (stepFailure != null) {
                    nodeSkip(step7, "Skipped - earlier step failed.");
                } else {
                    if (leadVerificationFlag) {
                        try {
                            DBDataFetcher dbDataFetcher = new DBDataFetcher(dbConnection.getConnection());
                            String encryptedEmail = encryptEmail.getEncryptedEmail(email,
                                    ReadProperties.getApplicationEncryptedURL());
                            nodeInfo(step7, "Encrypted email resolved.");
                            nodeInfo(step7, "Waiting for database propagation (3 seconds)...");
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            if (!dbDataFetcher.testSpecificLeadByEmail(encryptedEmail)) {
                                nodeInfo(step7, "Not found in main table - checking private offers...");
                                if (!dbDataFetcher.testSpecificLeadInPrivateOffersByEmail(encryptedEmail)) {
                                    throw new AssertionError("Lead not found in main table or private offers");
                                }
                                nodePass(step7, "Lead found in private offers table.");
                            } else {
                                nodePass(step7, "Lead verification completed successfully.");
                            }
                        } catch (Exception e) {
                            nodeFail(step7, "DB verification failed: " + e.getMessage());
                            stepFailure = e;
                        }
                    } else {
                        nodeSkip(step7, "Skipped - lead verification is disabled.");
                    }
                }

                if (stepFailure != null) {
                    urlPassed = false;
                } else {
                    urlPassed = true;
                }
            } catch (Exception e) {
                logger.logError("Unexpected error for URL " + url + ": " + e.getMessage());
                urlPassed = false;
            }

            if (urlPassed) {
                passed++;
                nodePass(urlNode, "✅ Flow completed successfully for " + url);
            } else {
                failed++;
                urlFailures.add(url + " - flow failed");
                nodeFail(urlNode, "❌ Flow failed for " + url);

                try {
                    String base64 = Base64.getEncoder()
                            .encodeToString(page.screenshot(new Page.ScreenshotOptions().setFullPage(false)));
                    urlNode.fail("Screenshot at point of failure for " + url,
                            MediaEntityBuilder.createScreenCaptureFromBase64String(base64, "Failure Screenshot")
                                    .build());
                } catch (Exception e) {
                    nodeFail(urlNode, "Failed to capture screenshot: " + e.getMessage());
                }
            }
        }

        this.extentTest = originalTest;
        ExtentTest summaryNode = node("Summary");
        nodeInfo(summaryNode, "Total URLs: " + totalUrls + " | Passed: " + passed + " | Failed: " + failed);
        if (failed == 0) {
            nodePass(summaryNode, "All " + totalUrls + " URLs passed.");
        } else {
            for (String f : urlFailures) {
                nodeFail(summaryNode, "FAILED: " + f);
            }
            nodeFail(summaryNode, passed + "/" + totalUrls + " passed, " + failed + " failed.");
            throw new AssertionError(failed + " URL(s) failed out of " + totalUrls + ". See report for details.");
        }
    }

    private void navigateToCsvUrlForUrl(String url) throws IOException, CsvException {
        if (!isProd) {
            logger.logInfo("Non-PROD, skipping DWSP");
            page.navigate(url);
            page.waitForLoadState();
            return;
        }

        logger.logInfo("Navigating to: " + url);
        page.navigate(url);
        page.waitForLoadState();

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
}