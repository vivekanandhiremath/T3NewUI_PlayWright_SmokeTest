package pageobject;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import utilities.CustomLogger;
import utilities.ScreenShotUtils;

import java.util.ArrayList;
import java.util.List;

public class PreQualPage extends BasePage {
    private static final String PREQUAL_TRIGGER = "xpath=//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'get pre-qualified') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'get pre qualified') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pre-qualify now') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'prequalify now')] | //a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'get pre-qualified') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pre-qualify now') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'prequalify now')] | //span[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'get pre-qualified') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pre-qualify now') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'prequalify now')]";
    private static final String OVERLAY_BACKDROP = "div.cdk-overlay-backdrop.cdk-overlay-backdrop-showing";
    private static final String FIRST_NAME_FIELD = "#firstname, input[name='firstname'], input[formcontrolname='firstName'], input[placeholder*='First' i]";
    private static final String LAST_NAME_FIELD = "#lastname, input[name='lastname'], input[formcontrolname='lastName'], input[placeholder*='Last' i]";
    private static final String EMAIL_FIELD = "#email, input[name='email'], input[formcontrolname='email'], input[placeholder*='Email' i]";
    private static final String PHONE_FIELD = "#phone, input[name='phone'], input[formcontrolname='phone'], input[placeholder*='Phone' i]";
    private static final String ADDRESS_FIELD = "#address, input[name='address'], input[formcontrolname='address'], input[placeholder*='Address' i]";
    private static final String CITY_FIELD = "#city, input[name='city'], input[formcontrolname='city'], input[placeholder*='City' i]";
    private static final String STATE_FIELD = "#state, input[name='state'], input[formcontrolname='state'], input[placeholder*='State' i]";
    private static final String ZIP_FIELD = "#zipcode, #zipCode, input[name='zipcode'], input[name='zipCode'], input[formcontrolname='zipCode'], input[placeholder*='Zip' i]";
    private static final String TERMS_CHECKBOX = "#landscapeCheck, input[type='checkbox'][name*='term' i], input[type='checkbox'][id*='term' i]";
    private static final String ESTIMATE_BUTTON = "#estimateBtn, button:has-text('Estimate'), button:has-text('Get Estimate')";
    private static final String PROCEED_BUTTON = "#submitBtnmanual, button:has-text('Proceed'), button:has-text('Continue')";
    private static final String ADDRESS_SUGGESTIONS = "#suggestions div, .pac-item, [role='option']";
    private static final String ADDRESS_OPTION = "xpath=(//input[contains(@class,'form-check-input')])[2]";
    private static final String USE_SELECTED_BUTTON = "#useSelectedBtn, button:has-text('Use Selected')";
    private static final String ADDRESS_SUGGESTION_MODAL = ".addressSugggestModal, .addressSuggestModal";
    private static final String CONTINUE_BUTTON = "button:has-text('Continue')";
    private final CustomLogger logger = new CustomLogger();

    public PreQualPage(Page page) {
        super(page);
    }

    public void clickPreQualLink() {
        dismissBlockingOverlayIfPresent();
        Locator preQualTrigger = findVisibleInPageOrAnyFrame(PREQUAL_TRIGGER, 4000);
        if (preQualTrigger == null) {
            throw new IllegalStateException("Pre-Qualify trigger not found on page.");
        }
        if (!safeClick(preQualTrigger)) {
            throw new IllegalStateException("Pre-Qualify trigger found but click failed.");
        }
        dismissBlockingOverlayIfPresent();
        // page.pause();
    }

    public boolean isPreQualLinkDisplayed() {
        dismissBlockingOverlayIfPresent();
        return findVisibleInPageOrAnyFrame(PREQUAL_TRIGGER, 3500) != null;
    }

    private Locator findVisibleInPageOrAnyFrame(String selector, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            try {
                Locator onPage = firstVisible(page.locator(selector));
                if (onPage != null) {
                    return onPage;
                }
            } catch (Exception ignored) {
                // Continue checking frames.
            }

            for (Frame frame : getFramesForLookup()) {
                try {
                    Locator inFrame = firstVisible(frame.locator(selector));
                    if (inFrame != null) {
                        return inFrame;
                    }
                } catch (Exception ignored) {
                    // Try next frame.
                }
            }

            page.waitForTimeout(150);
        }
        return null;
    }

    private boolean safeClick(Locator locator) {
        try {
            locator.scrollIntoViewIfNeeded();
            locator.click(new Locator.ClickOptions().setTimeout(5000));
            return true;
        } catch (Exception ignored) {
            try {
                locator.click(new Locator.ClickOptions().setForce(true).setTimeout(5000));
                return true;
            } catch (Exception ignoredAgain) {
                return false;
            }
        }
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

    private void dismissBlockingOverlayIfPresent() {
        Locator backdrop = page.locator(OVERLAY_BACKDROP).first();
        try {
            if (!backdrop.isVisible()) {
                return;
            }
            page.keyboard().press("Escape");
            backdrop.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(1200));
        } catch (Exception ignored) {
            try {
                if (backdrop.isVisible()) {
                    backdrop.click(new Locator.ClickOptions().setForce(true));
                    backdrop.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(1500));
                }
            } catch (Exception ignoredAgain) {
                // Continue; call site will attempt safe click anyway.
            }
        }
    }

    public void fillPersonalInfo(String firstName, String lastName, String email, String phone) {
        logger.logInfo("Pre-qual: Starting to fill personal info");
        fillIfEditableAndEmptyOrOverwrite(FIRST_NAME_FIELD, firstName, false);
        fillIfEditableAndEmptyOrOverwrite(LAST_NAME_FIELD, lastName, false);
        fillIfEditableAndEmptyOrOverwrite(EMAIL_FIELD, email, false);
        fillIfEditableAndEmptyOrOverwrite(PHONE_FIELD, phone, false);
        logger.logInfo("Pre-qual: Completed filling personal info");
    }

    public void fillAddressInfo(String address, String city, String state, String zipCode) {
        logger.logInfo("Pre-qual: Starting to fill address info");
        fillIfEditableAndEmptyOrOverwrite(ADDRESS_FIELD, address, false);
        page.waitForTimeout(1500);
        clickAddressSuggestionIfPresent();
        page.waitForTimeout(2000);
        boolean addressConfirmed = clickAddressOptionAndUseSelectedIfPresent();
        logger.logInfo("Pre-qual: Address confirmation result: " + addressConfirmed);
        if (!addressConfirmed) {
            logger.logWarning("Pre-qual: Address not confirmed via modal, city/state may be auto-filled incorrectly");
        }
        fillIfEditableAndEmptyOrOverwrite(CITY_FIELD, city, false);
        fillIfEditableAndEmptyOrOverwrite(STATE_FIELD, state, false);
        fillIfEditableAndEmptyOrOverwrite(ZIP_FIELD, zipCode, false);
        logger.logInfo("Pre-qual: Completed filling address info");
    }

    public void acceptTerms() {
        logger.logInfo("Pre-qual: Accepting terms");
        Locator checkbox = locateVisibleInPageOrAnyFrame(TERMS_CHECKBOX, 3000);
        if (checkbox == null) {
            throw new IllegalStateException("Terms checkbox not found in pre-qual form.");
        }
        try {
            if (!checkbox.isChecked()) {
                checkbox.check(new Locator.CheckOptions().setForce(true));
            }
        } catch (Exception ignored) {
            checkbox.click(new Locator.ClickOptions().setForce(true));
        }
    }

    public void clickEstimateButton() {
        logger.logInfo("Pre-qual: Clicking estimate button");
        dismissBlockingOverlayIfPresent();
        Locator estimateButton = locateVisibleInPageOrAnyFrame(ESTIMATE_BUTTON, 4000);
        if (estimateButton == null) {
            throw new IllegalStateException("Estimate button not found in pre-qual form.");
        }
        if (!safeClick(estimateButton) && !safeJsClick(estimateButton)) {
            throw new IllegalStateException("Estimate button click failed in pre-qual form.");
        }
        logger.logInfo("Pre-qual: Estimate button clicked successfully");

        // Some pre-qual flows require an immediate proceed click.
        page.waitForTimeout(1000);
        clickProceedButton();
    }

    public void clickProceedButton() {
        logger.logInfo("Pre-qual: Looking for proceed button");
        Locator proceedButton = locateVisibleInPageOrAnyFrame(PROCEED_BUTTON, 3000);
        if (proceedButton != null) {
            logger.logInfo("Pre-qual: Clicking proceed button");
            if (!safeClick(proceedButton)) {
                safeJsClick(proceedButton);
            }
        } else {
            logger.logInfo("Pre-qual: Proceed button not found (may not be required)");
        }
    }

    public boolean fillPreQualFormWithJs(String firstName, String lastName, String email, String phone, String address,
            String city, String state, String zipCode) {
        try {
            logger.logInfo("Pre-qual: Starting form fill sequence");
            dismissBlockingOverlayIfPresent();
            ensurePreQualIframeReady();
            logger.logInfo("Pre-qual: Iframe ready check completed");

            // Check if Continue button is already present to skip pre-qual flow
            try {
                Locator continueBtn = page.frameLocator("#prequalifyIframe")
                        .getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Continue"));

                // Wait up to 5 seconds for the button to be visible
                continueBtn.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));

                logger.logInfo("Pre-qual: Continue button found early (pre-qual failed to load). Taking screenshot and skipping pre-qual flow.");
                new ScreenShotUtils(page).captureScreenshot("PreQual_Continue_Button_Found");
                if (!safeClick(continueBtn)) {
                    safeJsClick(continueBtn);
                }
                return false;
            } catch (Exception e) {
                logger.logInfo("Pre-qual: Continue button not found early, proceeding with normal flow.");
            }

            fillPersonalInfo(firstName, lastName, email, phone);
            fillAddressInfo(address, city, state, zipCode);
            acceptTerms();
            clickEstimateButton();
            logger.logInfo("Pre-qual: Form fill sequence completed successfully");
            return true;
        } catch (Exception e) {
            logger.logError("Pre-qual: Form fill failed with exception", e);
            return false;
        }
    }

    private void fillIfEditableAndEmptyOrOverwrite(String selector, String value, boolean overwriteIfAlreadyFilled) {
        Locator field = locateVisibleInPageOrAnyFrame(selector, 5000);
        if (field == null) {
            logger.logWarning("Pre-qual: Field not found for selector: "
                    + selector.substring(0, Math.min(50, selector.length())));
            if (isPrefilledPreQualMode()) {
                // Prefilled journey: editable field can be absent.
                logger.logInfo("Pre-qual: Skipping missing field (prefilled mode detected)");
                return;
            }
            logger.logWarning("Pre-qual: Required field not found, but continuing: "
                    + selector.substring(0, Math.min(50, selector.length())));
            return;
        }

        try {
            String existing = field.inputValue();
            if (!overwriteIfAlreadyFilled && existing != null && !existing.trim().isEmpty()) {
                logger.logInfo("Pre-qual: Field already has value, skipping: " + existing);
                return;
            }
            field.fill(value);
            logger.logInfo("Pre-qual: Filled field with value: " + value);
        } catch (Exception ignored) {
            try {
                field.click(new Locator.ClickOptions().setForce(true));
                field.fill(value);
                logger.logInfo("Pre-qual: Filled field with click fallback: " + value);
            } catch (Exception ignoredAgain) {
                logger.logWarning("Pre-qual: Failed to fill field even with fallback");
                // Keep flow resilient for dynamic field widgets.
            }
        }
    }

    private Locator locateVisibleInPageOrAnyFrame(String selector, long timeoutMs) {
        return findVisibleInPageOrAnyFrame(selector, timeoutMs);
    }

    private boolean isPrefilledPreQualMode() {
        boolean estimateVisible = locateVisibleInPageOrAnyFrame(ESTIMATE_BUTTON, 1200) != null;
        boolean editableInputVisible = locateVisibleInPageOrAnyFrame(
                FIRST_NAME_FIELD + ", " + LAST_NAME_FIELD + ", " + EMAIL_FIELD + ", " + PHONE_FIELD,
                1200) != null;
        return estimateVisible && !editableInputVisible;
    }

    private void clickAddressSuggestionIfPresent() {
        logger.logInfo("Pre-qual: Looking for address suggestions");
        Locator suggestion = locateVisibleInPageOrAnyFrame(ADDRESS_SUGGESTIONS, 2500);
        if (suggestion != null) {
            logger.logInfo("Pre-qual: Address suggestion found, clicking first suggestion");
            safeClick(suggestion);
        } else {
            logger.logInfo("Pre-qual: No address suggestions found");
        }
    }

    private boolean clickAddressOptionAndUseSelectedIfPresent() {
        logger.logInfo("Pre-qual: Looking for address option");
        Locator addressOption = locateVisibleInPageOrAnyFrame(ADDRESS_OPTION, 10000);
        if (addressOption != null) {
            logger.logInfo("Pre-qual: Address option found, clicking");
            addressOption.scrollIntoViewIfNeeded();
            page.waitForTimeout(1000);
            if (!safeClick(addressOption)) {
                if (!safeJsClick(addressOption)) {
                    logger.logWarning("Pre-qual: Failed to click address option");
                    return false;
                }
            }
            logger.logInfo("Pre-qual: Address option clicked successfully");
            page.waitForTimeout(1000);

            logger.logInfo("Pre-qual: Looking for Use Selected button");
            Locator useSelected = locateVisibleInPageOrAnyFrame(USE_SELECTED_BUTTON, 10000);
            if (useSelected != null) {
                logger.logInfo("Pre-qual: Use Selected button found, clicking");
                useSelected.scrollIntoViewIfNeeded();
                page.waitForTimeout(500);
                if (!safeClick(useSelected) && !safeJsClick(useSelected)) {
                    logger.logWarning("Pre-qual: Failed to click Use Selected button");
                    return false;
                }
                logger.logInfo("Pre-qual: Waiting for address modal to close");
                waitForAddressSuggestionModalToClose();
                logger.logInfo("Pre-qual: Address modal closed successfully");
                return true;
            }
            logger.logWarning("Pre-qual: Use Selected button not found after address option click");
            return false;
        }
        logger.logWarning("Pre-qual: Address option not found - address confirmation modal did not appear");
        return false;
    }

    private void waitForAddressSuggestionModalToClose() {
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            Locator modal = findVisibleInPageOrAnyFrame(ADDRESS_SUGGESTION_MODAL, 300);
            if (modal == null) {
                logger.logInfo("Pre-qual: Address modal successfully closed");
                return;
            }
            page.waitForTimeout(200);
        }
        logger.logWarning("Pre-qual: Address modal still visible after 10s wait, forcing dismissal");
        // Force dismiss any leftover modal/backdrop so the flow isn't blocked.
        dismissBlockingOverlayIfPresent();
        try {
            Locator modal = findVisibleInPageOrAnyFrame(ADDRESS_SUGGESTION_MODAL, 1500);
            if (modal != null) {
                safeClick(modal);
                modal.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(2000));
            }
        } catch (Exception ignored) {
            // Ignore - flow continues regardless.
        }
    }

    private void ensurePreQualIframeReady() {
        logger.logInfo("Pre-qual: Ensuring iframe is ready");
        List<Frame> frames = getFramesForLookup();
        logger.logInfo("Pre-qual: Found " + frames.size() + " frames to check");
        for (Frame frame : frames) {
            String name = frame.name() == null ? "" : frame.name().toLowerCase();
            String url = frame.url() == null ? "" : frame.url().toLowerCase();
            if (name.contains("prequal") || url.contains("prequal") || url.contains("apicarzato")) {
                logger.logInfo("Pre-qual: Found prequal iframe: " + name + " | " + url);
                try {
                    frame.locator("#firstname, #lastname, #email").first().waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.ATTACHED)
                            .setTimeout(8000));
                    logger.logInfo("Pre-qual: Iframe content is ready");
                    return;
                } catch (Exception ignored) {
                    logger.logWarning("Pre-qual: Iframe not ready, trying next");
                    // Try next frame or continue.
                }
            }
        }
        logger.logWarning("Pre-qual: No ready prequal iframe found, proceeding anyway");
    }

    private boolean safeJsClick(Locator locator) {
        try {
            if (locator == null || locator.count() == 0 || !locator.isVisible()) {
                return false;
            }
            locator.evaluate("el => { el.scrollIntoView({block:'center', inline:'center'}); el.click(); }");
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private List<Frame> getFramesForLookup() {
        List<Frame> prioritized = new ArrayList<>();
        List<Frame> remaining = new ArrayList<>();

        for (Frame frame : page.frames()) {
            String name = frame.name() == null ? "" : frame.name().toLowerCase();
            String url = frame.url() == null ? "" : frame.url().toLowerCase();
            if (name.contains("prequal") || url.contains("prequal")) {
                prioritized.add(frame);
            } else {
                remaining.add(frame);
            }
        }

        prioritized.addAll(remaining);
        return prioritized;
    }
}
