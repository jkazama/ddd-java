package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.ActionStatusType;
import sample.context.ValidationException;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.account.Account;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.model.asset.Cashflow.CashflowType;
import sample.model.master.SelfFiAccount;
import sample.util.TimePoint;

public class CashInOutTest {

    private static final String ccy = "JPY";
    private static final String accId = "test";
    private static final LocalDate baseDay = LocalDate.of(2014, 11, 18);

    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Account.class, SelfFiAccount.class, CashInOut.class).build();
        tester.txInitializeData(rep -> {
            rep.save(DataFixtures.selfFiAcc(Remarks.CASH_OUT, ccy));
            rep.save(DataFixtures.acc(accId));
            rep.save(DataFixtures.fiAcc(accId, Remarks.CASH_OUT, ccy));
            rep.save(DataFixtures.cb(accId, baseDay, ccy, "1000"));
        });
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void find() {
        tester.tx(rep -> {
            TimePoint now = rep.dh().time().tp();
            CashInOut cio = DataFixtures.cio("1", accId, "300", true, now);
            cio.setEventDay(LocalDate.of(2014, 11, 18));
            rep.save(cio);
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
        tester.tx(rep -> {
            ActorSession.bind(Actor.builder().id(accId).build());
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("1001")));
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT, e.getMessage());
            }

            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, BigDecimal.ZERO));
                fail();
            } catch (ValidationException e) {
                assertEquals("error.domain.AbsAmount.zero", e.getMessage());
            }

            CashInOut normal = CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("300")));
            assertEquals(accId, normal.getAccountId());
            assertEquals(ccy, normal.getCurrency());
            assertEquals(new BigDecimal("300"), normal.getAbsAmount());
            assertTrue(normal.isWithdrawal());
            assertEquals(baseDay, normal.getRequestDay());
            assertEquals(baseDay, normal.getEventDay());
            assertEquals(LocalDate.of(2014, 11, 21), normal.getValueDay());
            assertEquals(Remarks.CASH_OUT + "-" + ccy, normal.getTargetFiCode());
            assertEquals("FI" + accId, normal.getTargetFiAccountId());
            assertEquals(Remarks.CASH_OUT + "-" + ccy, normal.getSelfFiCode());
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
        tester.tx(rep -> {
            TimePoint tp = rep.dh().time().tp();
            // Cancel a request of the CF having not yet processed
            CashInOut normal = rep.save(DataFixtures.cio("1", accId, "300", true, tp));
            assertEquals(ActionStatusType.CANCELLED, normal.cancel(rep).getStatusType());

            // When Reach an event day, I cannot cancel it. [ValidationException]
            CashInOut today = DataFixtures.cio("2", accId, "300", true, tp);
            today.setEventDay(LocalDate.of(2014, 11, 18));
            rep.save(today);
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
        tester.tx(rep -> {
            TimePoint tp = rep.dh().time().tp();
            CashInOut normal = rep.save(DataFixtures.cio("1", accId, "300", true, tp));
            assertEquals(ActionStatusType.ERROR, normal.error(rep).getStatusType());

            // When it is processed, an error cannot do it. [ValidationException]
            CashInOut today = rep.save(DataFixtures.cio("2", accId, "300", true, tp));
            today.setEventDay(LocalDate.of(2014, 11, 18));
            today.setStatusType(ActionStatusType.PROCESSED);
            rep.save(today);
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
        tester.tx(rep -> {
            TimePoint tp = rep.dh().time().tp();
            // It is handled non-arrival on an event day [ValidationException]
            CashInOut future = rep.save(DataFixtures.cio("1", accId, "300", true, tp));
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CIO_EVENT_DAY_AFTER_EQUALS_DAY, e.getMessage());
            }

            // Event day arrival processing.
            CashInOut normal = DataFixtures.cio("2", accId, "300", true, tp);
            normal.setEventDay(LocalDate.of(2014, 11, 18));
            rep.save(normal);
            CashInOut processed = normal.process(rep);
            assertEquals(ActionStatusType.PROCESSED, processed.getStatusType());
            assertNotNull(processed.getCashflowId());

            // Check the Cashflow that CashInOut produced.
            Cashflow cf = Cashflow.load(rep, normal.getCashflowId());
            assertEquals(accId, cf.getAccountId());
            assertEquals(ccy, cf.getCurrency());
            assertEquals(new BigDecimal("-300"), cf.getAmount());
            assertEquals(CashflowType.CASH_OUT, cf.getCashflowType());
            assertEquals(Remarks.CASH_OUT, cf.getRemark());
            assertEquals(LocalDate.of(2014, 11, 18), cf.getEventDay());
            assertEquals(LocalDate.of(2014, 11, 21), cf.getValueDay());
            assertEquals(ActionStatusType.UNPROCESSED, cf.getStatusType());
        });
    }

}
