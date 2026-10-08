import org.testng.annotations.Test;
import projects.DTCreditAppProject;

public class DT_CreditApp_Test extends BaseTest {

    @Test(description = "DT Credit App – Focused smoke test on Apply for Credit flow (Lead Form → Pre-Qual → Apply for Credit → DB Verification)")
    public void dtCreditAppTest() {
        DTCreditAppProject dtProject = new DTCreditAppProject(page, logger, dbConnection, encryptEmail, csvFilePath, csvRowIndex, vehicleType, environment, leadVerificationFlag);
        dtProject.setExtentTest(EshopTestListener.getTest());
        dtProject.runFlow();
    }
}
