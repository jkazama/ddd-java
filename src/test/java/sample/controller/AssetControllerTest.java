package sample.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import sample.Application;
import sample.context.Timestamper;
import sample.context.orm.JpaRepository;
import sample.model.DataFixtures;

//low: 簡易な正常系検証が中心
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Transactional
public class AssetControllerTest {

	private final Logger logger = LoggerFactory.getLogger("ControllerTest");

	@Autowired
	private WebApplicationContext wac;
	@Autowired
	private DataFixtures fixtures;
	@Autowired
	private JpaRepository rep;
	@Autowired
	private Timestamper time;

	private MockMvc mockMvc;

	private String prefix = "/asset";

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	private String url(String path) {
		return prefix + path;
	}

	@Test
	public void findUnprocessedCashOut() throws Exception {
		fixtures.cio("sample", "3000", true).save(rep);
		fixtures.cio("sample", "8000", true).save(rep);
		// low: JSONの値検証は省略
		logger.info(perform("/cio/unprocessedOut").getResponse().getContentAsString());
	}

	private MvcResult perform(String path) throws Exception {
		return mockMvc.perform(get(url(path))).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void withdraw() throws Exception {
		String query = "accountId=sample&currency=JPY&absAmount=1000";
		// low: JSONの値検証は省略
		logger.info(perform("/cio/withdraw?" + query).getResponse().getContentAsString());
	}
	
}
