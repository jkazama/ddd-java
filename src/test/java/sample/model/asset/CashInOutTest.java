package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import sample.ActionStatusType;
import sample.EntityTestSupport;
import sample.ValidationException;
import sample.model.DomainErrorKeys;
import sample.model.account.Account;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.master.SelfFiAccount;

// low: 簡易な正常系検証が中心。依存するCashflow/CashBalanceの単体検証パスを前提。
public class CashInOutTest extends EntityTestSupport {

    private static final String ccy = "JPY";
    private static final String accId = "test";
    private static final LocalDate baseDay = LocalDate.of(2014, 11, 18);

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
            cio.setEventDay(LocalDate.of(2014, 11, 18));
            cio.save(rep);
            // low: ちゃんとやると大変なので最低限の検証
            assertEquals(
                    1,
                    CashInOut.find(rep, findParam(LocalDate.of(2014, 11, 18), LocalDate.of(2014, 11, 19))).size());
            assertEquals(
                    1,
                    CashInOut.find(rep, findParam(LocalDate.of(2014, 11, 18),
                            LocalDate.of(2014, 11, 19), ActionStatusType.UNPROCESSED)).size());
            assertTrue(
                    CashInOut.find(rep, findParam(
                            LocalDate.of(2014, 11, 18),
                            LocalDate.of(2014, 11, 19), ActionStatusType.PROCESSED)).isEmpty());
            assertTrue(
                    CashInOut.find(rep, findParam(
                            LocalDate.of(2014, 11, 19),
                            LocalDate.of(2014, 11, 20), ActionStatusType.UNPROCESSED)).isEmpty());
        });
    }

    private FindCashInOut findParam(
            LocalDate fromDay, LocalDate toDay, ActionStatusType... statusTypes) {
        return FindCashInOut.builder()
                .currency(ccy)
                .statusTypes(statusTypes != null ? Set.of(statusTypes) : null)
                .updFromDay(fromDay)
                .updToDay(toDay)
                .build();
    }

    @Test
    public void withdrawal() {
        tx(() -> {
            // 超過の出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("1001")));
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT, e.getMessage());
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
            assertEquals(baseDay, normal.getRequestDay());
            assertEquals(baseDay, normal.getEventDay());
            assertEquals(LocalDate.of(2014, 11, 21), normal.getValueDay());
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
                assertEquals(AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT, e.getMessage());
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
            today.setEventDay(LocalDate.of(2014, 11, 18));
            today.save(rep);
            rep.flushAndClear();
            try {
                today.cancel(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CIO_EVENT_DAY_BEFORE_EQUALS_DAY, e.getMessage());
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
            today.setEventDay(LocalDate.of(2014, 11, 18));
            today.setStatusType(ActionStatusType.PROCESSED);
            today.save(rep);
            try {
                today.error(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.STATUS_PROCESSING, e.getMessage());
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
                assertEquals(AssetErrorKeys.CIO_EVENT_DAY_AFTER_EQUALS_DAY, e.getMessage());
            }

            // 発生日到来処理
            CashInOut normal = fixtures.cio(accId, "300", true);
            normal.setEventDay(LocalDate.of(2014, 11, 18));
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
            assertEquals(LocalDate.of(2014, 11, 18), cf.getEventDay());
            assertEquals(LocalDate.of(2014, 11, 21), cf.getValueDay());
            assertEquals(ActionStatusType.UNPROCESSED, cf.getStatusType());
        });
    }

}
