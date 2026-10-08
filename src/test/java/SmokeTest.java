import org.testng.annotations.Test;
import projects.T3Project;

public class SmokeTest extends BaseTest {

    @Test(description = "Eshop Smoke Test - T3 Widget Flow")
    public void t3WidgetSmokeTestFlow() {
        logger.logInfo("Setting up T3Project...");
        T3Project t3Project = new T3Project(page, logger, dbConnection, encryptEmail, csvFilePath, csvRowIndex,
                vehicleType, environment, leadVerificationFlag);

        // Wire in the Extent Report so each step gets its own child node
        t3Project.setExtentTest(EshopTestListener.getTest());

        logger.logInfo("Executing T3 Widget Flow...");
        t3Project.runFlow();

        logger.logInfo("T3 Widget Flow executed successfully. Verification is integrated within the runFlow method.");
    }
}
