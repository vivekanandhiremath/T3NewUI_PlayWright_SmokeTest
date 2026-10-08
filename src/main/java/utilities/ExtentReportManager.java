package utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton that owns the ExtentReports instance for the entire test run.
 * The report is written to target/extent-reports/T3WidgetReport_<timestamp>.html.
 */
public class ExtentReportManager {

    private static final CustomLogger logger = new CustomLogger();
    private static ExtentReports extent;
    private static String reportPath;

    private ExtentReportManager() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            reportPath = "target/extent-reports/T3WidgetReport_" + timestamp + ".html";

            // Ensure output directory exists
            new File("target/extent-reports").mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("T3 Widget Smoke Test Report");
            spark.config().setReportName("T3 Widget Smoke Test");
            spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");

            // Inject Custom JS to render Base64 images directly instead of the 'base64 img' badge
            String customJs = "$(document).ready(function() {" +
                    "    $('a[data-featherlight=\"image\"]').each(function() {" +
                    "        var href = $(this).attr('href');" +
                    "        if (href && href.startsWith('data:image')) {" +
                    "            $(this).html('<img src=\"' + href + '\" style=\"max-width: 100%; max-height: 250px; display: block; border-radius: 5px; margin-top: 10px; border: 1px solid #444;\"/>');" +
                    "        }" +
                    "    });" +
                    "});";
            spark.config().setJs(customJs);

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Environment", "UAT");
            extent.setSystemInfo("Browser", "Chromium (Playwright)");
            extent.setSystemInfo("Project", "T3 Widget Smoke Test");
            extent.setSystemInfo("Run Time", timestamp);

            logger.logInfo("ExtentReports initialised → " + reportPath);
        }
        return extent;
    }

    /**
     * Returns the absolute path of the generated report file.
     */
    public static String getReportPath() {
        return reportPath;
    }

    /**
     * Flushes buffered test results to disk. Must be called after all tests finish.
     */
    public static void flush() {
        if (extent != null) {
            extent.flush();
            logger.logInfo("ExtentReport flushed → " + reportPath);
        }
    }
}

