package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.*;

import sample.UnitTestSupport;

//low: 簡易な検証が中心
public class AssetTest extends UnitTestSupport {

    @Test
    public void canWithdraw() {
        // 残高   +  未実現キャッシュフロー - 出金依頼拘束額 = 出金可能額 
        // 10000 + (1000 - 2000) - 8000 = 1000
        fixtures.acc("test").save(rep);
        fixtures.cb("test", "20141118", "JPY", "10000").save(rep);
        fixtures.cf("test", "1000", "20141118", "20141120").save(rep);
        fixtures.cf("test", "-2000", "20141119", "20141121").save(rep);
        fixtures.cio("test", "8000", true).save(rep);

        assertThat(
                Asset.by("test").canWithdraw(rep, "JPY", new BigDecimal("1000"), "20141121"),
                is(true));
        assertThat(
                Asset.by("test").canWithdraw(rep, "JPY", new BigDecimal("1001"), "20141121"),
                is(false));
    }

}
