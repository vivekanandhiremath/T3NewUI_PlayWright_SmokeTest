package pageobject;

import com.microsoft.playwright.Page;
import utilities.CustomLogger;
import utilities.ElementUtils;

public abstract class BasePage {
    protected final CustomLogger logger;
    protected final Page page;

    ElementUtils elementUtils;

    public BasePage(Page page) {
        this.page = page;
        elementUtils = new ElementUtils(page);
        logger = new CustomLogger();

    }
}
