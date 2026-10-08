package pageobject;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ReservePage extends BasePage {

    public ReservePage(Page page) {
        super(page);
    }

    public boolean clickReserveButton() {
        try {
            page.getByText("Reserve").first().click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void fillReserveFormFields(String firstName, String lastName, String phone, String email) {
        page.locator("input[formcontrolname='firstName']").fill(firstName);
        page.locator("input[formcontrolname='lastName']").fill(lastName);
        page.locator("input[formcontrolname='phonenumber']").fill(phone);
        page.locator("input[formcontrolname='email']").fill(email);

    }

    public void clickReserveCheckbox() {
        page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Terms and Conditions")).check();

//        page.locator("input[type='checkbox']").first().check();
    }

    public void clickDebitCardButton() {
        page.locator("div[data-funding-source='card']").click();
    }

    public void clickReserveBackButton() {
        page.locator("#backBtn.widget_back_icon").click();
    }

    public boolean checkReserveButtonVisibility() {
        return page.getByText("Reserve").first().isVisible();
    }

    public boolean completeReserveForm(String firstName, String lastName, String phone, String email) {
        fillReserveFormFields(firstName, lastName, phone, email);
        clickReserveCheckbox();
        clickDebitCardButton();
        return true;
    }
}
