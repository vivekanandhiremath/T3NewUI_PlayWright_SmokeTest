import com.microsoft.playwright.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import utilities.*;

import java.util.Arrays;

@Listeners(EshopTestListener.class)
public abstract class BaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected CustomLogger logger;
    protected DatabaseConnection dbConnection;
    protected EncryptEmail encryptEmail;
    //    protected String csvFilePath = "testData/urls.csv";
    protected String csvFilePath;
    protected int csvRowIndex = 0; // Default to first row, can be overridden by specific tests
    protected String vehicleType;
    protected Environment environment;
    protected boolean leadVerificationFlag;


    @BeforeClass
    public void setUp() {
        String browserType = ReadProperties.getBrowser();
        logger = new CustomLogger();
        logger.logInfo("Initializing test with browser: " + browserType);

        playwright = Playwright.create();

        BrowserType selected;
        switch (browserType) {
            case "firefox":
                logger.logInfo("Launching Firefox browser");
                selected = playwright.firefox();
                break;
            case "edge":
                logger.logInfo("Launching Edge browser");
                selected = playwright.chromium();  // Edge uses Chromium
                break;
            case "safari":
                logger.logInfo("Launching Safari browser (webkit)");
                selected = playwright.webkit();
                break;
            case "chrome":
            default:
                logger.logInfo("Launching Chrome browser");
                selected = playwright.chromium();
                break;
        }

        // Launch browser with window maximization args (Chromium only)
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(false);

        // Only add --start-maximized for Chromium browsers
        if ("chrome".equalsIgnoreCase(browserType) || "edge".equalsIgnoreCase(browserType)) {
            launchOptions.setArgs(Arrays.asList("--start-maximized"));
            logger.logInfo("Using --start-maximized flag for Chromium browser");
        }
        browser = selected.launch(launchOptions);

        // Create context with appropriate viewport settings for each browser
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        if ("safari".equalsIgnoreCase(browserType)) {
            // Safari: Use large viewport size for maximization
            contextOptions.setViewportSize(1920, 1080);
            logger.logInfo("Safari: Setting viewport to 1920x1080 for maximization");
        } else if ("firefox".equalsIgnoreCase(browserType)) {
            // Firefox: Use large viewport size for maximization
            contextOptions.setViewportSize(1920, 1080);
            logger.logInfo("Firefox: Setting viewport to 1920x1080 for maximization");
        } else {
            // Chrome/Edge: No viewport constraint
            contextOptions.setViewportSize(null);
            logger.logInfo("Chrome/Edge: No viewport constraints");
        }
        context = browser.newContext(contextOptions);

        // Create page from context
        page = context.newPage();
        logger.logInfo("Browser window maximized successfully");
        dbConnection = new DatabaseConnection();
        // encrypt email should run headless; main test browser runs headed
        encryptEmail = new EncryptEmail(true);

        vehicleType = ReadProperties.getVehicleType();

        String envValue = ReadProperties.getEnv();

        if ("UAT".equalsIgnoreCase(envValue)) {
            environment = Environment.UAT;
            csvFilePath = "testData/Uat_urls.csv";
        } else if ("PROD".equalsIgnoreCase(envValue)) {
            environment = Environment.PROD;
            csvFilePath = "testData/Prod_urls.csv";
        } else {
            environment = Environment.CA_PROD;
            csvFilePath = "testData/ESHOP_CA_PROD_URLS.csv";
        }

        leadVerificationFlag = ReadProperties.isLeadVerificationEnabled();

        logger.logInfo("Running tests in environment: " + environment);
        logger.logInfo("Using CSV file: " + csvFilePath);
        logger.logInfo("Lead Verification Enabled: " + leadVerificationFlag);
    }


    @AfterClass
    public void tearDown() {
        if (encryptEmail != null) {
            encryptEmail.close();
        }
        if (dbConnection != null) {
            dbConnection.closeConnection();
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}