package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import sample.*;
import sample.model.account.Account;
import sample.model.asset.CashInOut.*;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.master.SelfFiAccount;
import sample.util.DateUtils;

//low: 簡易な正常系検証が中心。依存するCashflow/CashBalanceの単体検証パスを前提。
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
            //low: ちゃんとやると大変なので最低限の検証
            assertThat(
                    CashInOut.find(rep, findParam("20141118", "20141119")),
                    hasSize(1));
            assertThat(
                    CashInOut.find(rep, findParam("20141118", "20141119", ActionStatusType.UNPROCESSED)),
                    hasSize(1));
            assertThat(
                    CashInOut.find(rep, findParam("20141118", "20141119", ActionStatusType.PROCESSED)),
                    empty());
            assertThat(
                    CashInOut.find(rep, findParam("20141119", "20141120", ActionStatusType.UNPROCESSED)),
                    empty());
        });
    }

    private FindCashInOut findParam(String fromDay, String toDay, ActionStatusType... statusTypes) {
        return new FindCashInOut(ccy, statusTypes, fromDay, toDay);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void withdrawal() {
        tx(() -> {
            // 超過の出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("1001")));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.CashInOut.withdrawAmount"));
            }

            // 0円出金の出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, BigDecimal.ZERO));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.domain.AbsAmount.zero"));
            }

            // 通常の出金依頼
            CashInOut normal = CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("300")));
            assertThat(normal, allOf(
                    hasProperty("accountId", is(accId)), hasProperty("currency", is(ccy)),
                    hasProperty("absAmount", is(new BigDecimal(300))), hasProperty("withdrawal", is(true)),
                    hasProperty("requestDate", hasProperty("day", is(baseDay))),
                    hasProperty("eventDay", is(baseDay)), hasProperty("valueDay", is("20141121")),
                    hasProperty("targetFiCode", is(Remarks.CashOut + "-" + ccy)),
                    hasProperty("targetFiAccountId", is("FI" + accId)),
                    hasProperty("selfFiCode", is(Remarks.CashOut + "-" + ccy)),
                    hasProperty("selfFiAccountId", is("xxxxxx")),
                    hasProperty("statusType", is(ActionStatusType.UNPROCESSED)),
                    hasProperty("cashflowId", is(nullValue()))));

            // 拘束額を考慮した出金依頼 [例外]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("701")));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.CashInOut.withdrawAmount"));
            }
        });
    }

    @Test
    public void cancel() {
        tx(() -> {
            // CF未発生の依頼を取消
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertThat(normal.cancel(rep), hasProperty("statusType", is(ActionStatusType.CANCELLED)));

            // 発生日を迎えた場合は取消できない [例外]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay("20141118");
            today.save(rep);
            rep.flushAndClear();
            try {
                today.cancel(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.CashInOut.beforeEqualsDay"));
            }
        });
    }

    @Test
    public void error() {
        tx(() -> {
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertThat(normal.error(rep), hasProperty("statusType", is(ActionStatusType.ERROR)));

            // 処理済の時はエラーにできない [例外]
            CashInOut today = fixtures.cio(accId, "300", true);
            today.setEventDay("20141118");
            today.setStatusType(ActionStatusType.PROCESSED);
            today.save(rep);
            try {
                today.error(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.ActionStatusType.unprocessing"));
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void process() {
        tx(() -> {
            // 発生日未到来の処理 [例外]
            CashInOut future = fixtures.cio(accId, "300", true).save(rep);
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.CashInOut.afterEqualsDay"));
            }

            // 発生日到来処理
            CashInOut normal = fixtures.cio(accId, "300", true);
            normal.setEventDay("20141118");
            normal.save(rep);
            assertThat(normal.process(rep), allOf(
                    hasProperty("statusType", is(ActionStatusType.PROCESSED)),
                    hasProperty("cashflowId", not(nullValue()))));
            // 発生させたキャッシュフローの検証
            assertThat(Cashflow.load(rep, normal.getCashflowId()), allOf(
                    hasProperty("accountId", is(accId)),
                    hasProperty("currency", is(ccy)),
                    hasProperty("amount", is(new BigDecimal("-300"))),
                    hasProperty("cashflowType", is(CashflowType.CashOut)),
                    hasProperty("remark", is(Remarks.CashOut)),
                    hasProperty("eventDate", hasProperty("day", is("20141118"))),
                    hasProperty("valueDay", is("20141121")),
                    hasProperty("statusType", is(ActionStatusType.UNPROCESSED))));
        });
    }

}
