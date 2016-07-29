package sample.controller.admin;

import static org.mockito.BDDMockito.*;

import java.util.*;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import sample.WebTestSupport;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.usecase.AssetAdminService;

//low: 簡易な正常系検証が中心
@WebMvcTest(AssetAdminController.class)
public class AssetAdminControllerTest extends WebTestSupport {

    @MockBean
    private AssetAdminService service;

    @Override
    protected String prefix() {
        return "/admin/asset";
    }

    @Test
    public void findCashInOut() throws Exception {
        String day = time.day();
        given(service.findCashInOut(any(FindCashInOut.class))).willReturn(resultCashInOuts());
        performGet(
            uriBuilder("/cio/")
                .queryParam("updFromDay", day)
                .queryParam("updToDay", day)
                .build(),
            JsonExpects.success()
                .match("$[0].currency", "JPY")
                .match("$[0].absAmount", 3000)
                .match("$[1].absAmount", 8000));
    }

    private List<CashInOut> resultCashInOuts() {
        return Arrays.asList(
                fixtures.cio("sample", "3000", true),
                fixtures.cio("sample", "8000", false));
    }

}
