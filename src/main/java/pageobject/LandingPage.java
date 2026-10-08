package pageobject;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import utilities.Environment;
import utilities.PopupHandler;
import utilities.VehicleDetails;

import java.util.Map;

public class LandingPage extends BasePage {

    private final boolean enablePopupHandler;
    private final PopupHandler popupHandler;
    private final Environment environment;

    private final Locator paymentOptionButton;
    private final Locator noThanksButton;
    private final Locator cookieBannerButton;

    public LandingPage(boolean enablePopupHandler,
                       Page page,
                       PopupHandler popupHandler,
                       Environment environment) {
        super(page);

        this.enablePopupHandler = enablePopupHandler;
        this.popupHandler = popupHandler;
        this.environment = environment;

        this.paymentOptionButton = page.locator(
                "img[src*='d1jougtdqdwy1v'], " +
                        "button[id$='_primaryCTA'], " +
                        "a[href*='fcac'], " +
                        "a[href*='fcafinancial']"
        );

        this.noThanksButton = page.locator("#ip-no");

        this.cookieBannerButton = page.locator(
                "//button[contains(translate(., 'ACCEPT', 'accept'), 'accept')" +
                        " or contains(translate(., 'ALLOW', 'allow'), 'allow')" +
                        " or contains(., 'Got it')" +
                        " or contains(., 'I Agree')" +
                        " or contains(., 'Accept')" +
                        " or contains(., 'Allow')]"
        );
    }

    public VehicleDetails clickOnPaymentOption() {

        if (enablePopupHandler && popupHandler != null) {
            popupHandler.dismissAll();
        }

        handleCookieBannerIfPresent();

        if (isNoThanksButtonVisible()) {
            clickOnNoThanksButton();
        }

        logger.logInfo("🚀 Starting SNI → VDP flow");

        // =========================
        // SNI FLOW ONLY
        // =========================
        if (isSniCtaAvailable()) {

            logger.logInfo("✅ CTA found on SNI");

            VehicleDetails sniDetails = extractWidgetDetails();
            logger.logInfo("📦 SNI Details: " + sniDetails);

            clickCTA();
            return sniDetails;
        }

        // =========================
        // VDP FLOW (ONLY PROD)
        // =========================
        logger.logInfo("➡️ CTA NOT found on SNI");

        if (environment != Environment.PROD) {
            logger.logInfo("🟡 UAT detected → skipping VDP navigation completely");
            return extractWidgetDetails();
        }

        navigateToVdp();

        VehicleDetails vdpDetails = extractWidgetDetails();
        logger.logInfo("📦 VDP Details: " + vdpDetails);

        clickCTAIfPresent();

        return vdpDetails;
    }

    private boolean isSniCtaAvailable() {
        try {
            Locator locator = paymentOptionButton.first();
            // Widget bootstraps asynchronously; give it a few seconds to render the CTA.
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(8000));
            return locator.count() > 0 && locator.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickCTA() {
        Locator cta = resolveCtaLocator();
        try {
            // Poll until the CTA is visible, then click once.
            cta.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(15000));
            cta.scrollIntoViewIfNeeded();
            // The widget CTA requires a force click - a normal click does not open the widget.
            cta.click(new Locator.ClickOptions().setForce(true).setTimeout(5000));
            logger.logInfo("✅ CTA clicked");
        } catch (Exception e) {
            logger.logWarning("⚠️ CTA click failed: " + e.getMessage());
        }
    }

    private Locator resolveCtaLocator() {
        // Prefer the CTA image - its src is populated only once the widget app has bootstrapped.
        // Clicking before the src loads (t<~3s) does not open the widget.
        try {
            Locator img = page.locator("img[src*='d1jougtdqdwy1v']").first();
            img.waitFor(new Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(15000));
            return img;
        } catch (Exception e) {
            return paymentOptionButton.first();
        }
    }

    private void clickCTAIfPresent() {
        try {
            if (paymentOptionButton.first().isVisible()) {
                clickCTA();
            } else {
                logger.logInfo("⚠️ CTA not present on VDP");
                throw new RuntimeException("CTA not present on VDP");
            }
        } catch (Exception e) {
            logger.logInfo("⚠️ CTA not present on VDP");
            throw new RuntimeException("CTA not present on VDP");
        }
    }

    // =========================
    // VDP NAVIGATION (SAFE)
    // =========================
//    private void navigateToVdp() {
//        page.waitForTimeout(2000);
//        try {
//
//            String[] vehicleImageSelectors = {
//                    ".vehicle-image__image-wrapper img",
//                    ".vehicle-card__image img",
//                    ".hit-image img",
//                    ".inventory-card img",
//                    ".vehicle-listing img",
//                    "a[href*='/vehicle/'] img",
//                    "a[href*='vdp'] img",
//                    ".vehicle-card a img",
//                    ".inventory-item a img"
//            };
//
//            for (String selector : vehicleImageSelectors) {
//                try {
//                    Locator loc = page.locator(selector).first();
//                    if (loc.count() > 0 && loc.isVisible()) {
//
//                        logger.logInfo("🖱️ Clicking VDP selector: " + selector);
//
//                        loc.scrollIntoViewIfNeeded();
//                        loc.click();
//
//                        page.waitForLoadState();
//                        logger.logInfo("✅ Navigated to VDP");
//                        return;
//                    }
//                } catch (Exception ignored) {
//                }
//            }
//
//            logger.logInfo("⚠️ No VDP element found");
//
//        } catch (Exception e) {
//            logger.logInfo("❌ VDP navigation failed: " + e.getMessage());
//        }
//    }

    private void navigateToVdp() {

        String[] vehicleImageSelectors = {
                ".vehicle-image__image-wrapper img",
                ".vehicle-card__image img",
                ".hit-image.thumbnail-carousel img",
                ".hit-image img",
                ".vehicle-card-image img",
                ".inventory-listing-image img",
                "[class*='vehicle-image'] img",
                "[class*='vehicleImage'] img",
                "[class*='vehicle_image'] img",
                ".inventory-card img",
                ".vehicle-listing img",
                ".ddc-image-responsive img",
                "[class*='vehicle-media'] img",
                "[class*='vehiclebox-image']",
                "[class*='vehicle-box-image']",
                ".vehicle_loopslider",
                "[class*='vlpm3VehicleImage']",
                ".hero-carousel__single-image",
                "a[href*='/used/'] img",
                "a[href*='/new/'] img",
                "a[href*='/certified/'] img",
                "a[href*='vdp'] img",
                "a[href*='/vehicle/'] img",
                ".vehicle-card a img",
                ".inventory-item a img",
                ".srp-listing a img",
                ".inventory a img"
        };

        boolean clickedVehicle = false;

        for (String sel : vehicleImageSelectors) {

            try {
                Locator vehicleImg = page.locator(sel).first();

                vehicleImg.waitFor(new Locator.WaitForOptions().setTimeout(1000));

                if (vehicleImg.isVisible()) {

                    System.out.println("🖱️ Clicking vehicle using selector: " + sel);

                    String beforeClickUrl = page.url();

                    vehicleImg.scrollIntoViewIfNeeded();
                    page.waitForTimeout(500);

                    try {
                        vehicleImg.click(new Locator.ClickOptions().setTimeout(5000));

                        page.waitForLoadState();

                    } catch (Exception e) {
                        System.out.println("⏰ Click issue: " + e.getMessage());
                    }

                    String afterClickUrl = page.url();

                    if (!afterClickUrl.equals(beforeClickUrl)) {
                        clickedVehicle = true;
                        System.out.println("🔗 Navigated to VDP: " + afterClickUrl);
                        break;
                    }

                    System.out.println("⚠️ Click did not navigate, trying next selector...");
                }

            } catch (Exception ignored) {
            }
        }

        if (!clickedVehicle) {
            System.out.println("❌ No vehicle found for VDP navigation");
        }
    }

    // =========================
    // EXTRACT DATA
    // =========================
    private VehicleDetails extractWidgetDetails() {
        try {
            Map<String, Object> data = (Map<String, Object>) page.evaluate("""
                        () => {
                            const el = document.querySelector('app-root, app-certified, eshop-inventory');
                            if (!el) return {};
                            return {
                                vin: el.getAttribute('vin') || '',
                                dealerCode: el.getAttribute('dealercode') || '',
                                zipCode: el.getAttribute('zipcode') || '',
                                vehicleType: el.getAttribute('vehicle_type') || ''
                            };
                        }
                    """);

            logger.logInfo("✅ Widget extracted");

            return new VehicleDetails(
                    (String) data.getOrDefault("vin", ""),
                    (String) data.getOrDefault("dealerCode", ""),
                    (String) data.getOrDefault("zipCode", ""),
                    (String) data.getOrDefault("vehicleType", "")
            );

        } catch (Exception e) {
            logger.logInfo("⚠️ Extraction failed: " + e.getMessage());
            return new VehicleDetails("", "", "", "");
        }
    }

    // =========================
    // POPUPS
    // =========================
    private void handleCookieBannerIfPresent() {
        try {
            if (cookieBannerButton.count() > 0 && cookieBannerButton.first().isVisible()) {
                cookieBannerButton.first().click();
                logger.logInfo("✅ Cookie accepted");
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isNoThanksButtonVisible() {
        try {
            return noThanksButton.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickOnNoThanksButton() {
        try {
            elementUtils.clickOnElement(noThanksButton);
        } catch (Exception ignored) {
        }
    }
}