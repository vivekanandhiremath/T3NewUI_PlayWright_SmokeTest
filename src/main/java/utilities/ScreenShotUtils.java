package utilities;

import com.microsoft.playwright.Page;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenShotUtils {

    private final Page page;

    public ScreenShotUtils(Page page) {
        this.page = page;
    }

    public void captureScreenshot(String testName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "screenshots/" + testName + "_" + timestamp + ".png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filename)));
    }

    public void captureFullPageScreenshot(String testName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "screenshots/FULL_" + testName + "_" + timestamp + ".png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filename)).setFullPage(true));
    }
}
