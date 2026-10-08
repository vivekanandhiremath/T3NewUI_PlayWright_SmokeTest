package pageobject;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utilities.CustomLogger;


public class ApplyForCreditPage extends BasePage {
    private final CustomLogger logger = new CustomLogger();


    public ApplyForCreditPage(Page page) {
        super(page);
    }

    public void fillApplyForCreditForm(String firstName, String lastName, String email) {
        page.locator("//input[@placeholder='First Name']").fill(firstName);
        page.locator("//input[@placeholder='Last Name']").fill(lastName);
        page.locator("//input[@placeholder='Email']").fill(email);
    }

    public void clickSubmitButton() {
        page.locator("//button[normalize-space()='Submit']").click();
    }

    public void clickBackButton() {

        page.locator("#backBtn").click();
    }

    public boolean isBackButtonVisible() throws InterruptedException {
//        page.pause();
        page.waitForTimeout(1000); // wait for 1 second to ensure the page has loaded
        boolean isBackButton = page.locator("#backBtn").isVisible();

        logger.logInfo("Back button visibility: " + isBackButton);

        return isBackButton;
    }

    public boolean isCloseButtonVisible() {
        try {
            // Poll until the close icon has loaded; isVisible() alone returns
            // false immediately when the element has not rendered yet.
            page.locator("#closeCreditApplication")
                    .waitFor(new Locator.WaitForOptions()
                            .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                            .setTimeout(15000));
            return true;
        } catch (Exception e) {
            logger.logWarning("Close button (#closeCreditApplication) not visible after wait.");
            return false;
        }
    }

    public void clickCloseButton() {
        page.locator("#closeCreditApplication").click();
        logger.logInfo("Clicked on close button (#closeCreditApplication).");
    }

    /**
     * Prefers the credit app close button; falls back to the back button only
     * when the close element is not available.
     */
    public void closeCreditAppForm() {
        if (isCloseButtonVisible()) {
            clickCloseButton();
            return;
        }
        try {
            if (isBackButtonVisible()) {
                clickBackButton();
                logger.logInfo("Clicked on Back button.");
            } else {
                logger.logWarning("Neither close nor back button is available.");
            }
        } catch (InterruptedException e) {
            logger.logWarning("Back button visibility check interrupted: " + e.getMessage());
        }
    }

    public void fillApplyForCreditFormAndSubmit(String firstName, String lastName, String email) {
        fillApplyForCreditForm(firstName, lastName, email);
        clickSubmitButton();
    }


}
