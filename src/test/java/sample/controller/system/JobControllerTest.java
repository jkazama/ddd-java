package sample.controller.system;

import static org.mockito.BDDMockito.*;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import sample.*;
import sample.usecase.*;

//low: 簡易な正常系検証が中心
@WebMvcTest(JobController.class)
public class JobControllerTest extends WebTestSupport {

    @MockBean
    private AssetAdminService asset;
    @MockBean
    private MasterAdminService master;
    
    @Override
    protected String prefix() {
        return "/system/job";
    }

    @Test
    public void processDay() throws Exception {
        willDoNothing().given(master).processDay();
        performPost("/daily/processDay", JsonExpects.success());
    }

    @Test
    public void closingCashOut() throws Exception {
        willDoNothing().given(asset).closingCashOut();
        performPost("/daily/closingCashOut", JsonExpects.success());
    }

    @Test
    public void realizeCashflow() throws Exception {
        willDoNothing().given(asset).realizeCashflow();
        performPost("/daily/realizeCashflow", JsonExpects.success());
    }

}
