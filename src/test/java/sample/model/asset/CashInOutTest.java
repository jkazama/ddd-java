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

//low: Minimum test.
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
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("1001")));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.CashInOut.withdrawAmount"));
            }

            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, BigDecimal.ZERO));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.domain.AbsAmount.zero"));
            }

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
            // Cancel a request of the CF having not yet processed
            CashInOut normal = fixtures.cio(accId, "300", true).save(rep);
            assertThat(normal.cancel(rep), hasProperty("statusType", is(ActionStatusType.CANCELLED)));

            // When Reach an event day, I cannot cancel it. [ValidationException]
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

            // When it is processed, an error cannot do it. [ValidationException]
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
            // It is handled non-arrival on an event day [ValidationException]
            CashInOut future = fixtures.cio(accId, "300", true).save(rep);
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.CashInOut.afterEqualsDay"));
            }

            // Event day arrival processing.
            CashInOut normal = fixtures.cio(accId, "300", true);
            normal.setEventDay("20141118");
            normal.save(rep);
            assertThat(normal.process(rep), allOf(
                    hasProperty("statusType", is(ActionStatusType.PROCESSED)),
                    hasProperty("cashflowId", not(nullValue()))));
            // Check the Cashflow that CashInOut produced.
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
