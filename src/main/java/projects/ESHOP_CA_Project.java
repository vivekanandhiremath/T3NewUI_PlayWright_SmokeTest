package projects;

import com.aventstack.extentreports.ExtentTest;
import com.microsoft.playwright.Page;
import utilities.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ESHOP_CA_Project {

    private static final String RESULT_CSV_PATH = "testData/ESHOP_CA_PROD_URLS_RESULTS.csv";
    private final Page page;
    private final CustomLogger logger;
    private final boolean isProductionEnv;
    private ExtentTest extentTest;
    private CsvUtils csvUtils;
    private String csvFilePath;

    public ESHOP_CA_Project(Page page,
            CustomLogger logger,
            String csvFilePath,
            Environment environment) {
        this.page = page;
        this.logger = logger;
        this.csvFilePath = csvFilePath;
        this.isProductionEnv = environment != Environment.UAT;
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

    private String cleanUrl(String raw) {
        if (raw == null)
            return null;
        return raw.replace("\uFEFF", "")
                .replace("\u200B", "")
                .replace("\u00A0", " ")
                .trim();
    }

    private String getUrlFromRow(List<String[]> csvData, int rowIndex) {
        String[] row = csvData.get(rowIndex);
        if (row == null || row.length == 0)
            return null;
        String col0 = row[0];
        if (col0 != null && !col0.trim().isEmpty()) {
            String cleaned = cleanUrl(col0);
            if (cleaned != null && !cleaned.isEmpty()) {
                return cleaned;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < row.length; j++) {
            if (j > 0)
                sb.append(",");
            sb.append(row[j] != null ? row[j] : "");
        }
        String raw = cleanUrl(sb.toString()).replaceAll("^,+|,+$", "").trim();
        return raw.isEmpty() ? null : raw;
    }

    private boolean isBlockedByCloudflare() {
        try {
            String title = page.title();
            return title != null && title.toLowerCase().contains("just a moment");
        } catch (Exception e) {
            return false;
        }
    }

    public void runFlow() {
        logger.logInfo("Running ESHOP CA Environment Detection flow for ALL URLs in CSV");
        PopupHandler popupHandler = new PopupHandler(page);
        EShopCAEnvironmentDetector detector = new EShopCAEnvironmentDetector(page, logger);

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

        int total = csvData.size();
        int passed = 0;
        int failed = 0;
        List<String> failures = new ArrayList<>();
        List<String[]> results = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            String url = getUrlFromRow(csvData, i);

            if (url == null || url.isEmpty()) {
                logger.logWarning("Skipping empty URL at row " + i);
                failed++;
                failures.add("Row " + i + ": empty URL");
                String[] row = csvData.get(i);
                String fallbackUrl = (row != null && row.length > 0 && row[0] != null) ? row[0] : "";
                results.add(new String[] { fallbackUrl, "skipped" });
                try {
                    csvUtils.writeCsv(RESULT_CSV_PATH, results);
                } catch (IOException e) {
                    logger.logError("Failed to write results CSV: " + e.getMessage());
                }
                continue;
            }

            if (!url.startsWith("http")) {
                url = "https://" + url;
            }

            ExtentTest urlNode = node("URL " + (i + 1) + ": " + url);
            logger.logInfo("--- Processing URL " + (i + 1) + "/" + total + ": " + url + " ---");

            String resultStatus = "skipped";

            try {
                if (isProductionEnv) {
                    popupHandler.dismissAll();
                }

                page.navigate(url, new Page.NavigateOptions().setTimeout(120000));
                page.waitForLoadState();
                page.waitForTimeout(2000);

                if (isBlockedByCloudflare()) {
                    nodeFail(urlNode, "Blocked by Cloudflare on " + url);
                    failed++;
                    failures.add(url + ": Cloudflare blocked");
                    results.add(new String[] { url, "skipped" });
                    try {
                        csvUtils.writeCsv(RESULT_CSV_PATH, results);
                    } catch (IOException e) {
                        logger.logError("Failed to write results CSV: " + e.getMessage());
                    }
                    continue;
                }

                EShopCAEnvironmentDetector.DetectedEnvironment detected = detector.detect();

                if (urlNode != null) {
                    urlNode.info("Config environment: " + (isProductionEnv ? "PROD/CA_PROD" : "UAT"));
                    urlNode.info("Detected from scripts: " + detected.name());
                }

                boolean ok = false;
                switch (detected) {
                    case PROD:
                        if (isProductionEnv) {
                            nodePass(urlNode, "PRODUCTION confirmed on " + url);
                            resultStatus = "production is loaded";
                        } else {
                            nodeWarn(urlNode, "Scripts say PROD but config says UAT on " + url);
                        }
                        ok = true;
                        break;
                    case UAT:
                        if (!isProductionEnv) {
                            nodePass(urlNode, "UAT confirmed on " + url);
                        } else {
                            nodeWarn(urlNode, "Scripts say UAT but config says PROD on " + url);
                        }
                        ok = true;
                        break;
                    default:
                        nodeFail(urlNode, "Could not determine environment on " + url);
                        break;
                }

                if (ok) {
                    passed++;
                } else {
                    failed++;
                    failures.add(url + ": " + detected.name());
                }

            } catch (Exception e) {
                nodeFail(urlNode, "Failed on " + url + ": " + e.getMessage());
                failed++;
                failures.add(url + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }

            results.add(new String[] { url, resultStatus });

            try {
                csvUtils.writeCsv(RESULT_CSV_PATH, results);
                logger.logInfo("Results written to " + RESULT_CSV_PATH + " after URL " + (i + 1));
            } catch (IOException e) {
                logger.logError("Failed to write results CSV: " + e.getMessage());
            }
        }

        ExtentTest summaryNode = node("Summary");
        nodeInfo(summaryNode, "Total URLs: " + total + " | Passed: " + passed + " | Failed: " + failed);

        if (failed == 0) {
            nodePass(summaryNode, "All " + total + " URLs passed.");
        } else {
            for (String f : failures) {
                nodeFail(summaryNode, "FAILED: " + f);
            }
            nodeFail(summaryNode, passed + "/" + total + " passed, " + failed + " failed.");
            throw new AssertionError(failed + " URL(s) failed out of " + total + ". See report for details.");
        }
    }
}
