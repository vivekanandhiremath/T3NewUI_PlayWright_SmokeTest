package pageobject;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import utilities.CustomLogger;

public class HomePage extends BasePage {
    private static final String FIRST_NAME_INPUT = "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'first name') or @formcontrolname='firstName']";
    private static final String LAST_NAME_INPUT = "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'last name') or @formcontrolname='lastName']";
    private static final String EMAIL_INPUT = "//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email') or @formcontrolname='email']";
    private static final String TERMS_CHECKBOX = "//input[@id='Termsandcondition-input' or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'term') or contains(translate(@formcontrolname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'term')]";
    private static final String INITIAL_LEAD_SUBMIT_BUTTON = "//button[normalize-space()='Submit' or normalize-space()=' Submit ' or .//span[normalize-space()='Submit']]";
    private static final String POST_SUBMIT_DIALOG_CLOSE_BUTTON = "(//div[starts-with(@id,'mat-mdc-dialog-')]//button[@aria-label='Close' or normalize-space()='Close' or .//span[normalize-space()='Close'])[1]";
    private static final String POST_SUBMIT_CLOSE_ICON_ONLY = "//button[@aria-label='Close' and contains(@class,'closeIconMob') and not(@id='mainwidgetCloseButton')]";
    private static final String VEHICLE_DETAILS_SIDE_MENU = "//img[contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'vehicle details')] | //span[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'vehicle details')]";
    private static final String OVERLAY_BACKDROP = "div.cdk-overlay-backdrop.cdk-overlay-backdrop-showing";

    // IMPORTANT: must exclude #mainwidgetCloseButton – clicking it closes the
    // entire widget.
    // Only dismiss DIALOG/OVERLAY close buttons, not the widget itself.
    private static final String GENERIC_CLOSE_BUTTON = "//button[not(@id='mainwidgetCloseButton') and (@aria-label='Close' or normalize-space()='Close' or contains(@class,'closeIcon') or contains(@class,'close'))]";
    private final CustomLogger logger = new CustomLogger();

    public HomePage(Page page) {
        super(page);

    }

    // public void closeInitialLeadPopup() {
    // page.locator("button[aria-label='Close']").click();
    // }
    //
    // public void clickYesButton() {
    // page.locator("button:has-text('YES')").click();
    // }
    //
    // public void clickNoButton() {
    // page.locator("button:has-text('NO')").click();
    // }

    public boolean clickTradeInButton() {
        try {
            // Using a more robust locator based on the python code
            page.locator("//div[@role='tab']//span[text()='Trade-In']").click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickPaymentCalculatorIcon() {
        dismissBlockingOverlayIfPresent();
        page.locator("//div[@class='widget_price-details']//img[@alt='image']").click();
    }

    // public void clickOnProtectionMenu() {
    // dismissBlockingOverlayIfPresent();
    // Locator protection = page.locator("(//span[.='Protection'])[3]");
    //
    // try {
    //
    // if (!protection.isVisible()) {
    // logger.logInfo("Protection menu is not visible");
    // }
    // protection.click();
    // } catch (Exception e) {
    // // Overlay may have just reappeared – dismiss and retry once
    // logger.logInfo("clickOnProtectionMenu first attempt failed (" +
    // e.getMessage() + "), retrying after overlay dismiss…");
    // page.waitForTimeout(800);
    // dismissBlockingOverlayIfPresent();
    // page.waitForTimeout(400);
    // try {
    // protection.click();
    // } catch (Exception e2) {
    // // Final fallback – force click
    // logger.logInfo("clickOnProtectionMenu retry failed, using force click.");
    // try {
    // protection.click(new Locator.ClickOptions().setForce(true));
    //
    // } catch (Exception ignored) {
    // logger.logInfo("clickOnProtectionMenu retry failed, using force click.");
    //
    // }
    // }
    // }
    // }

    // public void clickOnProtectionMenu() {
    // Locator protection =
    // page.locator("(//span[normalize-space()='Protection'])[3]");
    //
    // dismissBlockingOverlayIfPresent();
    //
    // try {
    // protection.waitFor(new Locator.WaitForOptions()
    // .setState(WaitForSelectorState.VISIBLE));
    //
    // protection.scrollIntoViewIfNeeded();
    // protection.click(new Locator.ClickOptions().setTimeout(10000));
    //
    // } catch (Exception e) {
    // dismissBlockingOverlayIfPresent();
    // protection.scrollIntoViewIfNeeded();
    // protection.click(new Locator.ClickOptions().setForce(true));
    // }
    // }

    public void clickOnProtectionMenu() {
        dismissBlockingOverlayIfPresent();

        Locator protection = page.locator("(//span[normalize-space()='Protection'])[3]");

        try {
            if (protection.isVisible(new Locator.IsVisibleOptions().setTimeout(3000))) {
                protection.click();
                logger.logInfo("Protection menu clicked.");
            } else {
                logger.logInfo("Protection menu not found. Continuing.");
            }
        } catch (Exception e) {
            logger.logInfo("Protection menu unavailable. Continuing execution.");
        }
    }

    public void clickOnAccessoriesMenu() {
        dismissBlockingOverlayIfPresent();
        Locator accessoriesMenu = page.locator("(//span[.='Accessories'])[3]");

        try {
            if (accessoriesMenu.isVisible(new Locator.IsVisibleOptions().setTimeout(3000))) {
                accessoriesMenu.click();
                logger.logInfo("Accessories menu clicked.");
            } else {
                logger.logInfo("Accessories menu not found. Continuing.");
            }
        } catch (Exception e) {
            logger.logInfo("Accessories menu unavailable. Continuing execution.");
        }
    }

    public void clickOnReviewButton() {
        dismissBlockingOverlayIfPresent();
        page.locator("(//span[.='Review'])[3]").click();
    }

    public void clickOnSubmitToDealerButton() {
        dismissBlockingOverlayIfPresent();
        page.locator("//button[.='Submit to Dealer']").click();
    }

    public boolean clickApplyForCreditButton() {
        // First check if the button is visible (handles payment mode switching)
        if (!isApplyForCreditButtonVisible(10)) {
            System.out.println(
                    "Apply for Credit button is not visible or clickable");
            return false;
        }

        try {
            // Locator applyButton = page.locator("//button[contains(., 'Apply for
            // Credit')]");
            Locator applyButton = page.locator(
                    "//button[.//span[normalize-space()='Apply For Credit']]");
            // Wait for button to be visible and enabled
            applyButton.waitFor(new Locator.WaitForOptions().setTimeout(10000));

            if (applyButton.count() > 0) {

                // Scroll element into view
                applyButton.scrollIntoViewIfNeeded();

                // Small delay after scrolling (optional)
                page.waitForTimeout(500);

                try {
                    // Standard Playwright click
                    applyButton.click();
                    System.out.println(
                            "Clicked on Apply for Credit button");
                    return true;

                } catch (Exception clickError) {

                    try {
                        // Fallback: JavaScript click
                        applyButton.evaluate("element => element.click()");

                        System.out.println(
                                "Clicked on Apply for Credit button using JavaScript");
                        return true;

                    } catch (Exception jsError) {

                        System.out.println(
                                "Failed to click Apply for Credit button. JS click error: "
                                        + jsError.getMessage());
                        return false;
                    }
                }

            } else {
                System.out.println(
                        "Apply for Credit button not found after visibility check");
                return false;
            }

        } catch (Exception e) {
            System.out.println(
                    "Error in clickApplyForCreditButton: " + e.getMessage());
            return false;
        }
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isApplyForCreditButtonVisible(int timeout) {

        logger.logInfo("========== APPLY FOR CREDIT CHECK STARTED ==========");

        if (!isLeaseOrFinanceSelected()) {

            logger.logInfo("Lease/Finance not selected. Attempting switch...");

            if (!switchToLeaseOrFinance()) {

                logger.logWarning("Failed to switch payment option.");
                return false;
            }

            page.waitForTimeout(3000);
        }

        try {

            logger.logInfo(
                    "Scrolling down to reveal Apply For Credit button...");

            // Scroll multiple times because the button is below
            for (int i = 1; i <= 5; i++) {

                page.mouse().wheel(0, 1000);
                page.waitForTimeout(1000);

                Locator applyButton = page.locator(
                        "//button[.//span[normalize-space()='Apply For Credit']]");

                logger.logInfo(
                        "Apply button count after scroll "
                                + i + ": "
                                + applyButton.count());

                if (applyButton.count() > 0
                        && applyButton.first().isVisible()) {

                    logger.logInfo(
                            "Apply For Credit button found.");

                    return true;
                }
            }

        } catch (Exception e) {

            logger.logWarning(
                    "Error while searching for Apply For Credit button: "
                            + e.getMessage());
        }

        logger.logInfo(
                "========== APPLY FOR CREDIT CHECK FAILED ==========");

        return false;
    }

    private boolean isLeaseOrFinanceSelected() {

        logger.logInfo("Checking whether Lease or Finance is selected...");

        try {
            Locator leaseRadio = page.getByRole(
                    AriaRole.RADIO,
                    new Page.GetByRoleOptions().setName("Lease"));

            logger.logInfo("Lease radio count: " + leaseRadio.count());

            if (leaseRadio.count() > 0) {

                String ariaChecked = leaseRadio.first().getAttribute("aria-checked");

                logger.logInfo("Lease aria-checked: " + ariaChecked);

                if ("true".equalsIgnoreCase(ariaChecked)) {
                    logger.logInfo("Lease is currently selected.");
                    return true;
                }
            }

        } catch (Exception e) {
            logger.logWarning("Error checking Lease selection: " + e.getMessage());
        }

        try {
            Locator financeRadio = page.getByRole(
                    AriaRole.RADIO,
                    new Page.GetByRoleOptions().setName("Finance"));

            logger.logInfo("Finance radio count: " + financeRadio.count());

            if (financeRadio.count() > 0) {

                String ariaChecked = financeRadio.first().getAttribute("aria-checked");

                logger.logInfo("Finance aria-checked: " + ariaChecked);

                if ("true".equalsIgnoreCase(ariaChecked)) {
                    logger.logInfo("Finance is currently selected.");
                    return true;
                }
            }

        } catch (Exception e) {
            logger.logWarning("Error checking Finance selection: " + e.getMessage());
        }

        logger.logInfo("Neither Lease nor Finance is selected.");
        return false;
    }

    private boolean switchToLeaseOrFinance() {

        logger.logInfo("Attempting to switch payment option...");
        page.waitForTimeout(300);

        try {

            Locator leaseRadio = page.getByRole(
                    AriaRole.RADIO,
                    new Page.GetByRoleOptions().setName("Lease"));

            logger.logInfo("Lease radio found count: " + leaseRadio.count());

            if (leaseRadio.count() > 0) {

                leaseRadio.first().scrollIntoViewIfNeeded();

                logger.logInfo("Clicking Lease option...");

                leaseRadio.first().click(
                        new Locator.ClickOptions().setForce(true));

                page.waitForTimeout(2000);

                logger.logInfo("Successfully clicked Lease.");

                return true;
            }

        } catch (Exception e) {

            logger.logWarning(
                    "Failed to switch to Lease: " + e.getMessage());
        }

        try {

            Locator financeRadio = page.getByRole(
                    AriaRole.RADIO,
                    new Page.GetByRoleOptions().setName("Finance"));

            logger.logInfo("Finance radio found count: " + financeRadio.count());

            if (financeRadio.count() > 0) {

                financeRadio.first().scrollIntoViewIfNeeded();

                logger.logInfo("Clicking Finance option...");

                financeRadio.first().click(
                        new Locator.ClickOptions().setForce(true));

                page.waitForTimeout(2000);

                logger.logInfo("Successfully clicked Finance.");

                return true;
            }

        } catch (Exception e) {

            logger.logWarning(
                    "Failed to switch to Finance: " + e.getMessage());
        }

        logger.logWarning("Unable to switch to either Lease or Finance.");

        return false;
    }

    /// ///////////////////////////////////////////////
    public boolean isInitialFormVisible() {
        if (isVisibleInPageOrAnyFrame(FIRST_NAME_INPUT, 4000)) {
            return true;
        }
        if (isVisibleInPageOrAnyFrame(EMAIL_INPUT, 2000)) {
            return true;
        }
        return false;
    }

    public boolean isCloseIconVisible() {
        return page.locator("//button[contains(@class, 'closeIconMob') and @aria-label='Close']").isVisible();
    }

    public void fillAndSubmitInitialLeadForm(String firstName, String lastName, String email) {
        Locator firstNameField = locateInPageOrAnyFrame(FIRST_NAME_INPUT);
        firstNameField.fill(firstName);
        locateInPageOrAnyFrame(LAST_NAME_INPUT).fill(lastName);
        locateInPageOrAnyFrame(EMAIL_INPUT).fill(email);
        ensureTermsCheckboxChecked();
        clickInitialLeadSubmit(firstNameField);
        dismissBlockingOverlayIfPresent();
    }

    private void clickInitialLeadSubmit(Locator firstNameField) {
        Locator submitInSameForm = firstNameField.locator(
                "xpath=ancestor::form[1]//button[not(@disabled) and (normalize-space()='Submit' or .//span[normalize-space()='Submit'])]")
                .first();
        if (safeClick(submitInSameForm)) {
            return;
        }

        Locator submitInSameDialog = firstNameField.locator(
                "xpath=ancestor::*[@role='dialog' or contains(@class,'dialog') or contains(@class,'modal')][1]//button[not(@disabled) and (normalize-space()='Submit' or .//span[normalize-space()='Submit'])]")
                .first();
        if (safeClick(submitInSameDialog)) {
            return;
        }

        Locator fallbackSubmit = locateInPageOrAnyFrame(INITIAL_LEAD_SUBMIT_BUTTON);
        if (!safeClick(fallbackSubmit)) {
            throw new IllegalStateException("Initial lead Submit button was found but could not be clicked.");
        }
    }

    private boolean safeClick(Locator locator) {
        try {
            if (locator == null || locator.count() == 0 || !locator.isVisible()) {
                return false;
            }
            locator.scrollIntoViewIfNeeded();
            locator.click();
            return true;
        } catch (Exception ignored) {
            try {
                locator.click(new Locator.ClickOptions().setForce(true));
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        }
    }

    private void ensureTermsCheckboxChecked() {
        Locator termsCheckbox = locateInPageOrAnyFrame(TERMS_CHECKBOX);

        try {
            if (!termsCheckbox.isChecked()) {
                termsCheckbox.check(new Locator.CheckOptions().setForce(true));
            }
        } catch (Exception ignored) {
            // Fallback for custom checkbox implementations that reject .check().
            termsCheckbox.click(new Locator.ClickOptions().setForce(true));
        }

        if (!termsCheckbox.isChecked()) {
            throw new IllegalStateException("Terms and Conditions checkbox was not checked.");
        }
    }

    private boolean isVisibleInPageOrAnyFrame(String selector, double timeoutMs) {
        try {
            Locator pageLocator = page.locator(selector).first();
            pageLocator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
            return true;
        } catch (Exception ignored) {
            // Continue checking iframes.
        }

        for (Frame frame : page.frames()) {
            try {
                Locator frameLocator = frame.locator(selector).first();
                frameLocator.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(Math.max(1000, timeoutMs / 2)));
                return true;
            } catch (Exception ignored) {
                // Try next frame.
            }
        }
        return false;
    }

    private Locator locateInPageOrAnyFrame(String selector) {
        try {
            Locator visibleOnPage = firstVisible(page.locator(selector));
            if (visibleOnPage != null) {
                return visibleOnPage;
            }

            Locator pageLocator = page.locator(selector).first();
            if (pageLocator.count() > 0) {
                return pageLocator;
            }
        } catch (Exception ignored) {
            // Try frames.
        }

        for (Frame frame : page.frames()) {
            try {
                Locator visibleInFrame = firstVisible(frame.locator(selector));
                if (visibleInFrame != null) {
                    return visibleInFrame;
                }

                Locator frameLocator = frame.locator(selector).first();
                if (frameLocator.count() > 0) {
                    return frameLocator;
                }
            } catch (Exception ignored) {
                // Try next frame.
            }
        }

        throw new IllegalStateException("Could not locate element for selector: " + selector);
    }

    private Locator firstVisible(Locator candidates) {
        int count = candidates.count();
        for (int i = 0; i < count; i++) {
            Locator candidate = candidates.nth(i);
            if (candidate.isVisible()) {
                return candidate;
            }
        }
        return null;
    }

    public boolean clickOnPostSubmitCloseButton() {
        logger.logInfo("Post-submit close detection started. Frames available: " + page.frames().size());
        logger.logInfo("Post-submit close candidates (initial): dialog="
                + countMatchesInPageAndFrames(POST_SUBMIT_DIALOG_CLOSE_BUTTON)
                + ", closeIcon=" + countMatchesInPageAndFrames(POST_SUBMIT_CLOSE_ICON_ONLY));

        Locator closeBtn = findVisibleInPageOrAnyFrame(POST_SUBMIT_DIALOG_CLOSE_BUTTON, 2500);

        // Recorder showed some journeys need a second submit click before close dialog
        // appears.
        if (closeBtn == null) {
            logger.logInfo("Post-submit dialog close not visible. Retrying submit once.");
            clickSubmitAgainIfVisible();
            closeBtn = findVisibleInPageOrAnyFrame(POST_SUBMIT_DIALOG_CLOSE_BUTTON, 3500);
            logger.logInfo("Post-submit close candidates (after submit retry): dialog="
                    + countMatchesInPageAndFrames(POST_SUBMIT_DIALOG_CLOSE_BUTTON)
                    + ", closeIcon=" + countMatchesInPageAndFrames(POST_SUBMIT_CLOSE_ICON_ONLY));
        }

        // Strict fallback to post-submit close icon only (no widget close usage).
        if (closeBtn == null) {
            logger.logInfo("Falling back to post-submit close icon selector (non-widget).");
            closeBtn = findVisibleInPageOrAnyFrame(POST_SUBMIT_CLOSE_ICON_ONLY, 2000);
        }

        if (closeBtn == null) {
            logger.logWarning("No post-submit close control became visible in page/frames.");
            return false;
        }

        if (!safeClick(closeBtn) && !safeJsClick(closeBtn)) {
            logger.logWarning("Post-submit close control found but click failed.");
            return false;
        }

        Locator stillVisibleClose = findVisibleInPageOrAnyFrame(POST_SUBMIT_DIALOG_CLOSE_BUTTON, 1000);
        if (stillVisibleClose == null) {
            stillVisibleClose = findVisibleInPageOrAnyFrame(POST_SUBMIT_CLOSE_ICON_ONLY, 1000);
        }
        if (stillVisibleClose != null) {
            if (!safeClick(stillVisibleClose)) {
                safeJsClick(stillVisibleClose);
            }
        }

        dismissBlockingOverlayIfPresent();
        Locator stillVisibleAfterClose = findVisibleInPageOrAnyFrame(POST_SUBMIT_DIALOG_CLOSE_BUTTON, 2000);
        if (stillVisibleAfterClose == null) {
            stillVisibleAfterClose = findVisibleInPageOrAnyFrame(POST_SUBMIT_CLOSE_ICON_ONLY, 2000);
        }

        if (stillVisibleAfterClose == null) {
            logger.logInfo("Post-submit close control dismissed successfully.");
        } else {
            logger.logWarning("Post-submit close control still visible after click attempts.");
        }
        return stillVisibleAfterClose == null;
    }

    private void clickSubmitAgainIfVisible() {
        try {
            Locator submitButton = findVisibleInPageOrAnyFrame(INITIAL_LEAD_SUBMIT_BUTTON, 1200);
            if (submitButton != null) {
                safeClick(submitButton);
            }
        } catch (Exception ignored) {
            // No second-click submit path available.
        }
    }

    private int countMatchesInPageAndFrames(String selector) {
        int total = 0;
        try {
            total += page.locator(selector).count();
        } catch (Exception ignored) {
            // Ignore and continue with frames.
        }

        for (Frame frame : page.frames()) {
            try {
                total += frame.locator(selector).count();
            } catch (Exception ignored) {
                // Try next frame.
            }
        }
        return total;
    }

    private boolean safeJsClick(Locator locator) {
        try {
            if (locator == null || locator.count() == 0 || !locator.isVisible()) {
                return false;
            }
            locator.evaluate("el => { el.scrollIntoView({block:'center', inline:'center'}); el.click(); }");
            return true;
        } catch (Exception ignored) {
            try {
                locator.dispatchEvent("click");
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        }
    }

    private Locator findVisibleInPageOrAnyFrame(String selector, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            try {
                Locator pageMatch = firstVisible(page.locator(selector));
                if (pageMatch != null) {
                    return pageMatch;
                }
            } catch (Exception ignored) {
                // Continue checking frames.
            }

            for (Frame frame : page.frames()) {
                try {
                    Locator frameMatch = firstVisible(frame.locator(selector));
                    if (frameMatch != null) {
                        return frameMatch;
                    }
                } catch (Exception ignored) {
                    // Try next frame.
                }
            }

            page.waitForTimeout(150);
        }
        return null;
    }

    public void dismissBlockingOverlayIfPresent() {
        Locator backdrop = page.locator(OVERLAY_BACKDROP).first();
        try {
            if (!backdrop.isVisible()) {
                return;
            }
            // Do NOT use Escape – it propagates globally and can close the main widget.
            // Try clicking a generic close button first.
            try {
                Locator closeBtn = page.locator(GENERIC_CLOSE_BUTTON).first();
                if (closeBtn.isVisible()) {
                    closeBtn.click(new Locator.ClickOptions().setForce(true));
                    backdrop.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(1200));
                    return;
                }
            } catch (Exception ignored) {
            }

            // Fallback – click the backdrop itself to dismiss
            try {
                if (backdrop.isVisible()) {
                    backdrop.click(new Locator.ClickOptions().setForce(true));
                    backdrop.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(1500));
                }
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isVehicleDetailsSideMenuVisible() {
        return isVisibleInPageOrAnyFrame(VEHICLE_DETAILS_SIDE_MENU, 6000);
    }

    public void clickOnWidgetClose() {
        page.locator("//button[@id='mainwidgetCloseButton']").click();
    }

    public void ensureDefaultContent() {
        // Playwright handles frames automatically, but we can reset if needed
        page.mainFrame();
    }

    public boolean isPaymentCalculatorLoaded() {
        return elementUtils.waitForVisible(
                "//app-payment-calculator[@class='mat-mdc-dialog-component-host ng-star-inserted']//div[@class='payment-heading'][normalize-space()='Payment Estimator']",
                8000) != null;
    }

    public void clickOnPaymentCalculatorCloseIcon() {
        forceClosePaymentCalculator();
    }

    /**
     * Closes the Payment Calculator dialog.
     * Matches Python: payment_calculator_close_icon = (By.XPATH,
     * "(//button[@id='closeCalculator'])[2]")
     * The [2] index targets the DIALOG close button (not the mini-widget button).
     * NO Escape key – it propagates globally and closes the main widget.
     */
    public void forceClosePaymentCalculator() {
        // Python: self.element_utils.click_on_element((By.XPATH,
        // "(//button[@id='closeCalculator'])[2]"))
        try {
            page.locator("(//button[@id='closeCalculator'])[2]").click();
            logger.logInfo("forceClosePaymentCalculator: closed via (//button[@id='closeCalculator'])[2]");
        } catch (Exception e) {
            logger.logWarning(
                    "forceClosePaymentCalculator: failed to click payment calculator close icon: " + e.getMessage());
        }
    }

    public boolean isProtectionAddButtonVisible() {
        return page.locator("(//span[.='Add'])[1]").isVisible();
    }

    public void clickOnProtectionAddButton() {
        page.locator("(//span[.='Add'])[1]").click();
    }

    public void fillSubmitToDealerFormIfEmptyAndSubmit(String firstName, String lastName, String email) {
        page.locator("input[formcontrolname='firstName']").fill(firstName);
        page.locator("input[formcontrolname='lastName']").fill(lastName);
        page.locator("input[formcontrolname='email']").fill(email);
        page.locator("//button[.='Submit']").click();
    }

}
