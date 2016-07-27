package sample;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.*;
import org.springframework.transaction.annotation.Transactional;

import sample.context.Timestamper;
import sample.context.orm.JpaRepository;
import sample.model.DataFixtures;

//low: メソッド毎にコンテナ初期化を望む時はDirtiesContextでClassMode.AFTER_EACH_TEST_METHODを利用
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Transactional
public abstract class UnitTestSupport {

    @Autowired
    protected JpaRepository rep;
    @Autowired
    protected DataFixtures fixtures;
    @Autowired
    protected Timestamper time;

}
