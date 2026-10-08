import org.testng.annotations.Test;
import projects.ESHOP_CA_Project;

public class ESHOP_CA_Test extends BaseTest {

    @Test(description = "ESHOP CA – Navigate to dealer page and detect environment (PROD/UAT) from loaded JS scripts")
    public void eshopCAEnvironmentDetectionTest() {
        ESHOP_CA_Project eshopProject = new ESHOP_CA_Project(page, logger, csvFilePath, environment);
        eshopProject.setExtentTest(EshopTestListener.getTest());
        eshopProject.runFlow();
    }
}
