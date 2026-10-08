import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.microsoft.playwright.Page;
import org.testng.*;
import utilities.CustomLogger;
import utilities.EmailConfigManager;
import utilities.EmailReportSharer;
import utilities.ExtentReportManager;

import java.util.Base64;
import java.util.List;

/**
 * TestNG listener that:
 * 1. Creates an Extent Report entry for every test method.
 * 2. Captures a full-page screenshot on failure (embedded in the report).
 * 3. Flushes the report and emails it after the suite finishes.
 * <p>
 * Registered via @Listeners(EshopTestListener.class) on BaseTest.
 */
public class EshopTestListener implements ITestListener, ISuiteListener {

    private static final ExtentReports extent = ExtentReportManager.getInstance();
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final CustomLogger log = new CustomLogger();

    /**
     * Returns the ExtentTest node for the currently running test (for child nodes).
     */
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    // ── ITestListener ─────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        log.logInfo("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.logInfo("▶ TEST STARTED: " + methodName);
        if (description != null && !description.isEmpty()) {
            log.logInfo("  Description : " + description);
        }
        log.logInfo("  Class       : " + result.getTestClass().getName());
        ExtentTest test = extent.createTest(
                methodName,
                (description != null && !description.isEmpty()) ? description : methodName);
        extentTest.set(test);
        extentTest.get().info("Test started: " + methodName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long durationMs = result.getEndMillis() - result.getStartMillis();
        log.logInfo("✅ TEST PASSED : " + result.getMethod().getMethodName()
                + "  [" + durationMs + " ms]");
        log.logInfo("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        extentTest.get().pass("✅ Test passed successfully in " + durationMs + " ms.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long durationMs = result.getEndMillis() - result.getStartMillis();
        log.logError("❌ TEST FAILED : " + result.getMethod().getMethodName()
                + "  [" + durationMs + " ms]");
        if (result.getThrowable() != null) {
            log.logError("  Reason      : " + result.getThrowable().getMessage());
        }
        log.logInfo("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        ExtentTest test = extentTest.get();
        Throwable t = result.getThrowable();
        
        boolean isCustomAssertion = (t != null && t instanceof AssertionError && t.getMessage() != null && t.getMessage().contains("URL(s) failed out of"));
        
        if (isCustomAssertion) {
            test.fail("❌ " + t.getMessage());
        } else {
            test.fail(t);
            // Capture screenshot and embed it in the report for unexpected framework errors
            // (URL-level failures are already captured in T3Project)
            try {
                Object instance = result.getInstance();
                if (instance instanceof BaseTest) {
                    Page page = ((BaseTest) instance).page;
                    if (page != null) {
                        log.logInfo("  Capturing failure screenshot...");
                        byte[] screenshotBytes = page.screenshot(
                                new Page.ScreenshotOptions().setFullPage(false));
                        String base64 = Base64.getEncoder().encodeToString(screenshotBytes);
                        String testName = result.getMethod().getMethodName();
                        test.fail("Screenshot for failed test: " + testName,
                                MediaEntityBuilder.createScreenCaptureFromBase64String(base64, testName).build());
                        log.logInfo("  Screenshot embedded in Extent Report.");
                    }
                }
            } catch (Exception screenshotError) {
                log.logWarning("  Could not capture screenshot: " + screenshotError.getMessage());
                test.warning("Could not capture screenshot: " + screenshotError.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Throwable t = result.getThrowable();
        log.logWarning("⏭️  TEST SKIPPED: " + result.getMethod().getMethodName()
                + (t != null ? " – " + t.getMessage() : ""));
        log.logInfo("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        extentTest.get().skip("⏭️ Test skipped" + (t != null ? ": " + t.getMessage() : ""));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.logWarning("⚠️  TEST FAILED (within success %): " + result.getMethod().getMethodName());
        extentTest.get().warning("Test failed but within success percentage.");
    }

    @Override
    public void onStart(ITestContext context) {
        log.logInfo("═══════════════════════════════════════════════════");
        log.logInfo("🚀 SUITE CONTEXT STARTED: " + context.getName());
        log.logInfo("═══════════════════════════════════════════════════");
    }

    @Override
    public void onFinish(ITestContext context) {
        log.logInfo("═══════════════════════════════════════════════════");
        log.logInfo("🏁 SUITE CONTEXT FINISHED: " + context.getName());
        log.logInfo("   Passed  : " + context.getPassedTests().size());
        log.logInfo("   Failed  : " + context.getFailedTests().size());
        log.logInfo("   Skipped : " + context.getSkippedTests().size());
        log.logInfo("═══════════════════════════════════════════════════");
    }

    // ── ISuiteListener ────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        log.logInfo("╔═══════════════════════════════════════════════════╗");
        log.logInfo("║  SUITE STARTED : " + suite.getName());
        log.logInfo("╚═══════════════════════════════════════════════════╝");
    }

    /**
     * Called once after the entire suite finishes.
     * Flushes the Extent Report to disk then sends the report email.
     */
    @Override
    public void onFinish(ISuite suite) {
        // Count results across all test contexts in the suite
        int pass = 0, fail = 0, skip = 0;
        for (ISuiteResult sr : suite.getResults().values()) {
            ITestContext ctx = sr.getTestContext();
            pass += ctx.getPassedTests().size();
            fail += ctx.getFailedTests().size();
            skip += ctx.getSkippedTests().size();
        }
        int total = pass + fail + skip;

        log.logInfo("╔═══════════════════════════════════════════════════╗");
        log.logInfo("║  SUITE FINISHED: " + suite.getName());
        log.logInfo("║  Total   : " + total);
        log.logInfo("║  ✅ Passed : " + pass);
        log.logInfo("║  ❌ Failed : " + fail);
        log.logInfo("║  ⏭️  Skipped: " + skip);
        log.logInfo("╚═══════════════════════════════════════════════════╝");

        // Flush report to disk
        log.logInfo("📄 Flushing Extent Report to disk...");
        ExtentReportManager.flush();
        String reportPath = ExtentReportManager.getReportPath();
        log.logInfo("📄 Extent Report saved → " + new java.io.File(reportPath).getAbsolutePath());

        // Send email
        String status = fail > 0 ? "FAILED" : "PASSED";
        log.logInfo("📧 Preparing to send report email  [Status: " + status + "]...");

        try {
            EmailConfigManager cfg = new EmailConfigManager();

            if (!cfg.isEmailEnabled()) {
                log.logWarning("📧 Email sending is DISABLED in email_config.properties – skipping.");
                return;
            }

            boolean shouldSend = "PASSED".equals(status) ? cfg.isSendOnPass() : cfg.isSendOnFail();
            if (!shouldSend) {
                log.logWarning("📧 Email suppressed for status [" + status + "] by config (send_on_pass / send_on_fail).");
                return;
            }

            List<String> toList = cfg.getRecipientsToForStatus(status.toLowerCase());
            List<String> ccList = cfg.getRecipientsCcForStatus(status.toLowerCase());

            if (toList == null || toList.isEmpty()) {
                log.logWarning("📧 No TO recipients configured – skipping email.");
                return;
            }

            log.logInfo("📧 TO  : " + toList);
            if (ccList != null && !ccList.isEmpty()) {
                log.logInfo("📧 CC  : " + ccList);
            }
            log.logInfo("📧 Sending via SMTP (provider: " + cfg.getProvider() + ")...");

            EmailReportSharer sharer = new EmailReportSharer(cfg);
            boolean sent = sharer.sendReport(reportPath, toList, ccList, status, pass, fail, skip);

            if (sent) {
                log.logInfo("✅ Report email [" + status + "] sent successfully to: " + toList);
            } else {
                log.logError("❌ Report email was NOT sent. Check email_config.properties and SMTP credentials.");
            }
        } catch (Exception e) {
            log.logError("❌ Exception while sending report email: " + e.getMessage());
        }
    }
}

