package sample;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sample.context.Timestamper;
import sample.context.orm.JpaRepository;
import sample.model.DataFixtures;

//low: メソッド毎にコンテナ初期化を望む時はDirtiesContextでClassMode.AFTER_EACH_TEST_METHODを利用
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext
@Transactional
public abstract class UnitTestSupport {

	@Autowired
	protected JpaRepository rep;
	@Autowired
	protected DataFixtures fixtures;
	@Autowired
	protected Timestamper time;
	
}
