package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import sample.ActionStatusType;
import sample.EntityTestSupport;
import sample.ValidationException;
import sample.model.account.Account;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.master.SelfFiAccount;
import sample.util.DateUtils;

// low: 簡易な正常系検証が中心。依存するCashflow/CashBalanceの単体検証パスを前提。
public class CashInOutTest extends EntityTestSupport {

    private static final String ccy = "JPY";
    private static final String accId = "test";
    private static final String baseDay = "20141118";

    @Override
    protected void setupPreset() {
        targetEntities(Account.class, SelfFiAccount.class, CashInOut.class);
    }

    @Override
    protected void before() {
        tx(() -> {
            fixtures.selfFiAcc(Remarks.CashOut, ccy).save(rep);
            // 残高1000円の口座(test)を用意
            fixtures.acc(accId).save(rep);
            fixtures.fiAcc(accId, Remarks.CashOut, ccy).save(rep);
            fixtures.cb(accId, baseDay, ccy, "1000").save(rep);
        });
    }

    @Test
    public void find() {
        tx(() -> {
            CashInOut cio = fixtures.cio(accId, "300", true);
            cio.setUpdateDate(DateUtils.date("20141118"));
            cio.save(rep);
            // low: ちゃんとやると大変なので最低限の検証
            assertEquals(
                    1,
                    CashInOut.find(rep, findParam("20141118", "20141119")).size());
            assertEquals(
                    1,
                    CashInOut.find(rep, findParam("20141118", "20141119", ActionStatusType.UNPROCESSED)).size());
            assertTrue(
                    CashInOut.find(rep, findParam("20141118", "20141119", ActionStatusType.PROCESSED)).isEmpty());
            assertTrue(
                    CashInOut.find(rep, findParam("20141119", "20141120", ActionStatusType.UNPROCESSED)).isEmpty());
        });
    }

    private FindCashInOut findParam(String fromDay, String toDay, ActionStatusType... statusTypes) {
        return new FindCashInOut(ccy, statusTypes, fromDay, toDay);
    }

    @Test
    public void withdrawal() {
        tx(() -> {
            // 超過の出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("1001")));
                fail();
            } catch (ValidationException e) {
                assertEquals("error.CashInOut.withdrawAmount", e.getMessage());
            }

            // 0円出金の出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, BigDecimal.ZERO));
                fail();
            } catch (ValidationException e) {
                assertEquals("error.domain.AbsAmount.zero", e.getMessage());
            }

            // 通常の出金依頼
            CashInOut normal = CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("300")));
            assertEquals(accId, normal.getAccountId());
            assertEquals(ccy, normal.getCurrency());
            assertEquals(new BigDecimal("300"), normal.getAbsAmount());
            assertTrue(normal.isWithdrawal());
            assertEquals(baseDay, normal.getRequestDate().getDay());
            assertEquals(baseDay, normal.getEventDay());
            assertEquals("20141121", normal.getValueDay());
            assertEquals(Remarks.CashOut + "-" + ccy, normal.getTargetFiCode());
            assertEquals("FI" + accId, normal.getTargetFiAccountId());
            assertEquals(Remarks.CashOut + "-" + ccy, normal.getSelfFiCode());
            assertEquals("xxxxxx", normal.getSelfFiAccountId());
            assertEquals(ActionStatusType.UNPROCESSED, normal.getStatusType());
            assertNull(normal.getCashflowId());

            // 拘束額を考慮した出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("701")));
                fail();
            } catch (ValidationException e) {
                assertEquals("error.CashInOut.withdrawAmount", e.getMessage());
            }
        });
    }

    @Test
    public void cancel() {
        tx(() -> {
            // CF未発生の依頼を取消
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertEquals(ActionStatusType.CANCELLED, normal.cancel(rep).getStatusType());

            // 発生日を迎えた場合は取消できない [例外]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay("20141118");
            today.save(rep);
            rep.flushAndClear();
            try {
                today.cancel(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals("error.CashInOut.beforeEqualsDay", e.getMessage());
            }
        });
    }

    @Test
    public void error() {
        tx(() -> {
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertEquals(ActionStatusType.ERROR, normal.error(rep).getStatusType());

            // 処理済の時はエラーにできない [例外]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay("20141118");
            today.setStatusType(ActionStatusType.PROCESSED);
            today.save(rep);
            try {
                today.error(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals("error.ActionStatusType.unprocessing", e.getMessage());
            }
        });
    }

    @Test
    public void process() {
        tx(() -> {
            // 発生日未到来の処理 [例外]
            CashInOut future = fixtures.cio(accId, "300", true).save(rep);
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals("error.CashInOut.afterEqualsDay", e.getMessage());
            }

            // 発生日到来処理
            CashInOut normal = fixtures.cio(accId, "300", true);
            normal.setEventDay("20141118");
            normal.save(rep);
            CashInOut processed = normal.process(rep);
            assertEquals(ActionStatusType.PROCESSED, processed.getStatusType());
            assertNotNull(processed.getCashflowId());

            // 発生させたキャッシュフローの検証
            Cashflow cf = Cashflow.load(rep, normal.getCashflowId());
            assertEquals(accId, cf.getAccountId());
            assertEquals(ccy, cf.getCurrency());
            assertEquals(new BigDecimal("-300"), cf.getAmount());
            assertEquals(CashflowType.CashOut, cf.getCashflowType());
            assertEquals(Remarks.CashOut, cf.getRemark());
            assertEquals("20141118", cf.getEventDate().getDay());
            assertEquals("20141121", cf.getValueDay());
            assertEquals(ActionStatusType.UNPROCESSED, cf.getStatusType());
        });
    }

}
