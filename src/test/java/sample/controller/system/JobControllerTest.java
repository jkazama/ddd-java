package sample.controller.system;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import lombok.val;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.*;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import sample.*;
import sample.context.Timestamper;
import sample.context.orm.JpaRepository;
import sample.model.DataFixtures;
import sample.model.asset.*;

//low: 簡易な正常系検証が中心。100万保有のsampleを前提としてしまっています。
//low: メソッド毎にコンテナ初期化を望む時はClassMode.AFTER_EACH_TEST_METHODを利用
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Transactional
public class JobControllerTest {

	@Autowired
	private WebApplicationContext wac;
	@Autowired
	private DataFixtures fixtures;
	@Autowired
	private JpaRepository rep;
	@Autowired
	private Timestamper time;

	private MockMvc mockMvc;

	private String prefix = "/system/job";

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	private String url(String path) {
		return prefix + path;
	}

	@Test
	public void processDay() throws Exception {
		String currentDay = time.day();
		assertThat(currentDay, is("20141118"));
		perform("/daily/processDay");
		assertThat(time.day(), is("20141119"));
		perform("/daily/processDay");
		assertThat(time.day(), is("20141120"));
		time.daySet(currentDay);
	}

	private MvcResult perform(String path) throws Exception {
		return mockMvc.perform(get(url(path))).andExpect(status().isOk()).andReturn();
	}

	@Test
	public void closingCashOut() throws Exception {
		// 当日発生の振込出金依頼を準備
		val co = fixtures.cio("sample", "3000", true);
		co.setEventDay(time.day());
		co.save(rep);
		assertThat(CashInOut.load(rep, co.getId()), hasProperty("statusType", is(ActionStatusType.UNPROCESSED)));
		// 実行検証
		perform("/daily/closingCashOut");
		assertThat(CashInOut.load(rep, co.getId()), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
	}

	@Test
	public void realizeCashflow() throws Exception {
		// 当日実現のキャッシュフローを準備
		val cf = fixtures.cf("sample", "3000", "20141117", "20141118").save(rep);
		assertThat(Cashflow.load(rep, cf.getId()), hasProperty("statusType", is(ActionStatusType.UNPROCESSED)));
		assertThat(CashBalance.getOrNew(rep, "sample", "JPY"),
				hasProperty("amount", is(new BigDecimal("1000000.0000"))));
		// 実行検証
		perform("/daily/realizeCashflow");
		assertThat(Cashflow.load(rep, cf.getId()), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
		assertThat(CashBalance.getOrNew(rep, "sample", "JPY"),
				hasProperty("amount", is(new BigDecimal("1003000.0000"))));
	}

}
