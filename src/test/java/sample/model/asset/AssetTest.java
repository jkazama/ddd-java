package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import sample.EntityTestSupport;
import sample.model.account.Account;

// low: 簡易な検証が中心
public class AssetTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Account.class, CashBalance.class, Cashflow.class, CashInOut.class);
    }

    @Test
    public void canWithdraw() {
        tx(() -> {
            // 残高 + 未実現キャッシュフロー - 出金依頼拘束額 = 出金可能額
            // 10000 + (1000 - 2000) - 8000 = 1000
            fixtures.acc("test").save(rep);
            fixtures.cb("test", "20141118", "JPY", "10000").save(rep);
            fixtures.cf("test", "1000", "20141118", "20141120").save(rep);
            fixtures.cf("test", "-2000", "20141119", "20141121").save(rep);
            fixtures.cio("test", "8000", true).save(rep);

            assertTrue(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1000"), "20141121"));
            assertFalse(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1001"), "20141121"));
        });
    }

}
