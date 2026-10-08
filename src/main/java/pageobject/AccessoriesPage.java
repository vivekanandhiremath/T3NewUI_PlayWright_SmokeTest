package pageobject;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class AccessoriesPage extends BasePage {

    public AccessoriesPage(Page page) {
        super(page);
    }

    public void clickOnCategoryDropdown() {
        page.getByRole(AriaRole.COMBOBOX).first().click();
    }
}
