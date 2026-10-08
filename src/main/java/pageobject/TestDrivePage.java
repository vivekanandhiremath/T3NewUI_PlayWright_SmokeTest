package pageobject;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import utilities.CustomLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TestDrivePage extends BasePage {

    private final CustomLogger logger = new CustomLogger();

    public TestDrivePage(Page page) {
        super(page);
    }

    // -------------------------------------------------------
    // public API
    // -------------------------------------------------------

    public boolean isTestDriveButtonDisplayed() {
        try {
            Locator btn = page.locator("#testDriveBtn");
            btn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(10_000));
            return true;
        } catch (Exception e) {
            logger.logWarning("Test Drive button not visible: " + e.getMessage());
            // scroll fallback
            try {
                Locator btn = page.locator("#testDriveBtn");
                btn.scrollIntoViewIfNeeded();
                page.waitForTimeout(500);
                return btn.isVisible();
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    public void clickOnTestDriveButton() {
        page.locator("#testDriveBtn").click();
    }

    /**
     * Fills and submits the test drive form.
     * Matches Python fill_test_drive_form_and_submit():
     * - only fills fields that are currently empty
     * - selects tomorrow's date
     * - opens time picker, waits, selects time (normalises am→AM)
     * - unchecks drivers-license checkbox if it is checked
     * - scrolls to and clicks Proceed/Submit
     *
     * @param hour   hour string, e.g. "9"
     * @param minute minute string, e.g. "00"
     * @param amOrPm "am", "AM", "pm", or "PM" – normalised to uppercase internally
     */
    public void fillTestDriveFormAndSubmit(String firstName, String lastName, String email,
                                           String hour, String minute, String amOrPm) {
        logger.logInfo("Starting to fill test drive form");

        // Fill basic fields only when empty (Python: _fill_field_if_empty)
        fillFieldIfEmpty("//input[@formcontrolname='firstName']", firstName, "First name");
        fillFieldIfEmpty("//input[@formcontrolname='lastName']", lastName, "Last name");
        fillFieldIfEmpty("//input[@formcontrolname='email']", email, "Email");

        // Date selection
        selectDate();
        page.waitForTimeout(2000); // Python: sleep(2)

        // Time picker
        openTimePicker();
        page.waitForTimeout(2000); // Python: sleep(2)
        selectTime(hour, minute, amOrPm);

        // Driver's licence checkbox
        handleDriversLicenseCheckbox();

        // Submit
        clickProceedButton();
        logger.logInfo("Test drive form submitted successfully");
    }

    public void clickOnTestDriveThankYouPopupCloseButton() {
        try {
            page.locator("//button[.//span[contains(text(),'Close')]]")
                    .click(new Locator.ClickOptions().setTimeout(10_000));
            logger.logInfo("Closed test drive thank you popup");
        } catch (Exception e) {
            logger.logWarning("Could not close test drive thank you popup: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // private helpers
    // -------------------------------------------------------

    /**
     * Only type into a field when it currently has no value (Python: _fill_field_if_empty).
     */
    private void fillFieldIfEmpty(String xpath, String value, String fieldName) {
        try {
            Locator input = page.locator(xpath).first();
            String current = input.inputValue();
            if (current == null || current.trim().isEmpty()) {
                logger.logInfo("Entering " + fieldName.toLowerCase() + ": " + value);
                input.fill(value);
            } else {
                logger.logInfo(fieldName + " already filled: " + current);
            }
        } catch (Exception e) {
            logger.logWarning("fillFieldIfEmpty(" + fieldName + ") error: " + e.getMessage());
        }
    }

    /**
     * Selects tomorrow's date from the calendar.
     */
//    private void selectDate() {
//        LocalDate tomorrow = LocalDate.now().plusDays(1);
//        String formattedDate = tomorrow.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
//        logger.logInfo("Selecting date: " + formattedDate);
//
//        // Open calendar – try the touch-target button first (Python: JS click on span inside button)
//        boolean calendarOpened = false;
//        String[] calendarOpeners = {
//                "//button[@aria-label='Open calendar']//span[contains(@class,'mat-mdc-button-touch-target')]",
//                "//button[@aria-label='Open calendar']"
//        };
//        for (String sel : calendarOpeners) {
//            try {
//                Locator opener = page.locator(sel).first();
//                opener.waitFor(new Locator.WaitForOptions()
//                        .setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
//                opener.evaluate("el => el.click()");   // JS click, matching Python
//                calendarOpened = true;
//                break;
//            } catch (Exception ignored) {
//            }
//        }
//        if (!calendarOpened) {
//            // fallback: direct click on date input
//            try {
//                page.locator("//input[@formcontrolname='date']").first().click();
//            } catch (Exception e) {
//                throw new IllegalStateException("Failed to open date picker: " + e.getMessage());
//            }
//        }
//
//        // Wait for calendar
//        try {
//            page.locator(".mat-calendar").first()
//                    .waitFor(new Locator.WaitForOptions()
//                            .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
//        } catch (Exception e) {
//            logger.logWarning("Calendar visibility wait timed out, continuing anyway.");
//        }
//
//        // Click the date cell (Python: JS click)
//        try {
//            Locator dateCell = page.locator(
//                    "//button[@aria-label='" + formattedDate + "'][not(@disabled)]").first();
//            dateCell.waitFor(new Locator.WaitForOptions()
//                    .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
//            page.waitForTimeout(2000); // Python: sleep(2) before clicking date
//            dateCell.evaluate("el => { el.scrollIntoView(true); el.click(); }");
//            logger.logInfo("Selected date: " + formattedDate);
//        } catch (Exception e) {
//            throw new IllegalStateException("Failed to click date " + formattedDate + ": " + e.getMessage());
//        }
//
//        // Verify date was applied
//        try {
//            page.locator("//input[@formcontrolname='date']").first()
//                    .waitFor(new Locator.WaitForOptions()
//                            .setState(WaitForSelectorState.ATTACHED).setTimeout(5000));
//        } catch (Exception ignored) {
//        }
//    }
    private void selectDate() {
        logger.logInfo("Selecting next available date");

        // Open calendar
        boolean calendarOpened = false;
        String[] calendarOpeners = {
                "//button[@aria-label='Open calendar']//span[contains(@class,'mat-mdc-button-touch-target')]",
                "//button[@aria-label='Open calendar']"
        };

        for (String sel : calendarOpeners) {
            try {
                Locator opener = page.locator(sel).first();
                opener.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(5000));

                opener.evaluate("el => el.click()");
                calendarOpened = true;
                break;
            } catch (Exception ignored) {
            }
        }

        if (!calendarOpened) {
            try {
                page.locator("//input[@formcontrolname='date']").first().click();
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to open date picker: " + e.getMessage());
            }
        }

        // Wait for calendar
        try {
            page.locator(".mat-calendar").first()
                    .waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(10000));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Calendar visibility wait failed: " + e.getMessage());
        }

        // Start from tomorrow
        LocalDate dateToCheck = LocalDate.now().plusDays(1);

        // Search next 30 days
        for (int i = 0; i < 30; i++) {

            String formattedDate = dateToCheck.format(
                    DateTimeFormatter.ofPattern("MMMM d, yyyy"));

            logger.logInfo("Checking date: " + formattedDate);

            try {
                Locator dateCell = page.locator(
                        "//button[@aria-label='" + formattedDate +
                                "' and not(@aria-disabled='true')]"
                ).first();

                if (dateCell.count() > 0) {

                    dateCell.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(2000));

                    page.waitForTimeout(500);

                    dateCell.evaluate(
                            "el => { el.scrollIntoView({block:'center'}); el.click(); }"
                    );

                    logger.logInfo(
                            "Successfully selected date: " + formattedDate);

                    return;
                }

            } catch (Exception ignored) {
            }

            logger.logInfo("Date unavailable: " + formattedDate);

            dateToCheck = dateToCheck.plusDays(1);
        }

        throw new IllegalStateException(
                "No available date found in the next 30 days.");
    }

    /**
     * Opens the time picker dropdown.
     * Matches Python click_on_time_icon().
     */
    public void openTimePicker() {
        String[] selectors = {
                "//mat-icon[normalize-space()='schedule' or normalize-space()='access_time']",
                "//button[contains(@aria-label,'time') or contains(@aria-label,'Time') or contains(@aria-label,'clock')]",
                "//mat-datepicker-toggle[contains(@aria-label,'time') or contains(@aria-label,'Time')]//button",
                "//ngx-mat-timepicker-toggle//button",
                "//*[contains(@class,'timepicker-toggle') or contains(@class,'time-picker')]//button",
                "//button[@aria-label='Open time picker']",
                "//mat-icon[contains(text(),'schedule') or contains(text(),'access_time') or contains(text(),'query_builder')]"
        };
        for (String sel : selectors) {
            try {
                Locator btn = page.locator(sel).first();
                btn.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
                btn.click();
                logger.logInfo("openTimePicker: opened via " + sel);
                return;
            } catch (Exception ignored) {
            }
        }
        throw new IllegalStateException("Could not open time picker – no matching toggle found");
    }

    /**
     * Selects a time from the open dropdown.
     * Normalises am_or_pm to uppercase (Python: normalized_period = am_or_pm.strip().upper()).
     * Tries exact match first, then contains-class variant.
     *
     * @param hour   "9"
     * @param minute "00"
     * @param amOrPm "am" / "AM" / "pm" / "PM"
     */
    public void selectTime(String hour, String minute, String amOrPm) {
        // Python: normalized_period = am_or_pm.strip().upper()
        String period = amOrPm.trim().toUpperCase();
        if (!period.equals("AM") && !period.equals("PM")) {
            throw new IllegalArgumentException("Invalid period: " + amOrPm + ". Expected AM or PM.");
        }

        // Python: time_str = f"{hour.strip()}:{minute.strip()} {normalized_period}"
        String timeStr = hour.trim() + ":" + minute.trim() + " " + period;
        logger.logInfo("Attempting to select time: " + timeStr);

        // Python selector: "//span[@class='mdc-list-item__primary-text' and normalize-space()='{time_str}']"
        // Java: also try contains(@class) variant for robustness
        String[] candidates = {
                "//span[@class='mdc-list-item__primary-text' and normalize-space()='" + timeStr + "']",
                "//span[contains(@class,'mdc-list-item__primary-text') and normalize-space()='" + timeStr + "']",
                "//mat-option[normalize-space()='" + timeStr + "']",
                "//li[normalize-space()='" + timeStr + "']",
                "//*[contains(@class,'mdc-list-item') and normalize-space()='" + timeStr + "']",
                "//*[contains(@class,'timepicker') and normalize-space()='" + timeStr + "']"
        };

        for (String sel : candidates) {
            try {
                Locator el = page.locator(sel).first();
                el.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(4000));
                el.click();
                logger.logInfo("selectTime: selected '" + timeStr + "' via " + sel);
                return;
            } catch (Exception ignored) {
            }
        }

        throw new IllegalStateException(
                "selectTime: could not find time option matching '" + timeStr + "'");
    }

    /**
     * Convenience overload used by LeadFormsHandler (passes pre-combined "9:00 am" string).
     * Splits into hour/minute/period and delegates to the main selectTime().
     */
    public void selectTime(String timeString) {
        // Parse "9:00 am" or "9:00 AM"
        try {
            String[] parts = timeString.trim().split("\\s+", 2);
            String[] hm = parts[0].split(":", 2);
            String hour = hm[0].trim();
            String minute = hm.length > 1 ? hm[1].trim() : "00";
            String period = parts.length > 1 ? parts[1].trim() : "AM";
            selectTime(hour, minute, period);
        } catch (Exception e) {
            throw new IllegalStateException("selectTime(String) could not parse '" + timeString + "': " + e.getMessage());
        }
    }

    /**
     * Handles the driver's licence checkbox.
     * Python: if checkbox.is_selected() → uncheck it (JS click).
     */
    private void handleDriversLicenseCheckbox() {
        try {
            Locator checkbox = page.locator(
                    "//input[contains(@class,'mdc-checkbox__native-control')]").first();
            checkbox.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.ATTACHED).setTimeout(10_000));

            boolean isChecked = checkbox.isChecked();
            if (isChecked) {
                logger.logInfo("Driver's licence checkbox is checked – unchecking it.");
                checkbox.evaluate("el => { el.scrollIntoView({behavior:'smooth',block:'center'}); el.click(); }");
                page.waitForTimeout(2000); // Python: sleep(2)
                logger.logInfo("Driver's licence checkbox unchecked.");
            } else {
                logger.logInfo("Driver's licence checkbox is already unchecked.");
            }
        } catch (Exception e) {
            logger.logWarning("Could not handle driver's licence checkbox: " + e.getMessage());
        }
    }

    /**
     * Scrolls the Proceed/Submit button into view and clicks it (Python: _click_proceed_button).
     */
    private void clickProceedButton() {
        try {
            Locator btn = page.locator(
                    "//button[.//span[contains(text(),'Proceed') or contains(text(),'Submit')]]").first();
            btn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
            btn.evaluate("el => { el.scrollIntoView({block:'center'}); el.click(); }");
            logger.logInfo("Clicked on Proceed button");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to click Proceed button: " + e.getMessage());
        }
    }
}
