package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import sample.ActionStatusType;
import sample.EntityTestSupport;
import sample.ValidationException;
import sample.model.DomainErrorKeys;

// low: 簡易な正常系検証が中心。依存するCashBalanceの単体検証パスを前提。
public class CashflowTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Cashflow.class);
    }

    @Test
    public void register() {
        tx(() -> {
            // 過去日付の受渡でキャッシュフロー発生 [例外]
            try {
                Cashflow.register(rep, fixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 17)));
                fail();
            } catch (ValidationException e) {
                assertEquals("error.Cashflow.beforeEqualsDay", e.getMessage());
            }
            // 翌日受渡でキャッシュフロー発生
            var cf = Cashflow.register(rep, fixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 19)));
            assertEquals(new BigDecimal("1000"), cf.getAmount());
            assertEquals(ActionStatusType.UNPROCESSED, cf.getStatusType());
            assertEquals(LocalDate.of(2014, 11, 18), cf.getEventDay());
            assertEquals(LocalDate.of(2014, 11, 19), cf.getValueDay());
        });
    }

    @Test
    public void realize() {
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");

            // 未到来の受渡日 [例外]
            var cfFuture = fixtures.cf("test1", "1000", LocalDate.of(2014, 11, 18),
                    LocalDate.of(2014, 11, 19)).save(rep);
            try {
                cfFuture.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CF_REALIZE_DAY, e.getMessage());
            }

            // キャッシュフローの残高反映検証。 0 + 1000 = 1000
            var cfNormal = fixtures.cf("test1", "1000", LocalDate.of(2014, 11, 17),
                    LocalDate.of(2014, 11, 18)).save(rep);
            assertEquals(ActionStatusType.PROCESSED, cfNormal.realize(rep).getStatusType());
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());

            // 処理済キャッシュフローの再実現 [例外]
            try {
                cfNormal.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.STATUS_PROCESSING, e.getMessage());
            }

            // 過日キャッシュフローの残高反映検証。 1000 + 2000 = 3000
            var cfPast = fixtures.cf("test1", "2000", LocalDate.of(2014, 11, 16), LocalDate.of(2014, 11, 17)).save(rep);
            assertEquals(ActionStatusType.PROCESSED, cfPast.realize(rep).getStatusType());
            assertEquals(new BigDecimal("3000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());
        });
    }

    @Test
    public void registerWithRealize() {
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");
            // 発生即実現
            Cashflow.register(rep, fixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 18)));
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());
        });
    }

}
