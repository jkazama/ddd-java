package sample.controller;

import org.junit.Test;

import sample.WebTestSupport;

//low: 簡易な正常系検証が中心
public class AssetControllerTest extends WebTestSupport {

    @Override
    protected String prefix() {
        return "/asset";
    }

    @Test
    public void findUnprocessedCashOut() throws Exception {
        fixtures.cio("sample", "3000", true).save(rep);
        fixtures.cio("sample", "8000", true).save(rep);
        // low: JSONの値検証は省略
        logger.info(performGet("/cio/unprocessedOut").getResponse().getContentAsString());
    }

    @Test
    public void withdraw() throws Exception {
        String query = "accountId=sample&currency=JPY&absAmount=1000";
        // low: JSONの値検証は省略
        logger.info(performGet("/cio/withdraw?" + query).getResponse().getContentAsString());
    }

}
