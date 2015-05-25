package sample.controller.admin;

import org.junit.Test;

import sample.WebTestSupport;

//low: 簡易な正常系検証が中心
public class AssetAdminControllerTest extends WebTestSupport {

	@Override
	protected String prefix() {
		return "/admin/asset";
	}

	@Test
	public void findCashInOut() throws Exception {
		fixtures.cio("sample", "3000", true).save(rep);
		fixtures.cio("sample", "8000", true).save(rep);
		// low: JSONの値検証は省略
		String query = "updFromDay=20141118&updToDay=21141118";
		logger.info(performGet("/cio?" + query).getResponse().getContentAsString());
	}

}
