package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import sample.ActionStatusType;
import sample.context.ValidationException;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;
import sample.model.asset.CashInOut.FindCashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.model.asset.Cashflow.CashflowType;
import sample.util.TimePoint;

@DataJdbcTest
@ActiveProfiles("test")
public class CashInOutTest {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcAggregateTemplate jdbcTemplate;

    private static final String ccy = "JPY";
    private static final String accId = "test";
    private static final LocalDate baseDay = LocalDate.of(2014, 11, 18);

    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTester.create(jdbcTemplate, dataSource);
        tester.txInitializeData(rep -> {
            rep.save(DataFixtures.acc(accId).build());
            rep.save(DataFixtures.fiAcc(rep.dh(), accId, Remarks.CASH_OUT, ccy).build());
            rep.save(DataFixtures.cb(rep.dh(), accId, baseDay, ccy, "1000").build());
        });
    }

    @Test
    public void find() {
        tester.tx(rep -> {
            TimePoint now = rep.dh().time().tp();
            CashInOut cio = DataFixtures.cio("1", accId, "300", true, now)
                    .eventDay(LocalDate.of(2014, 11, 18))
                    .build();
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
                assertEquals(AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT, e.warns().fieldError("absAmount").get().message());
            }

            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, BigDecimal.ZERO));
                fail();
            } catch (ValidationException e) {
                assertEquals("error.domain.AbsAmount.zero", e.warns().fieldError("absAmount").get().message());
            }

            CashInOut normal = CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("300")));
            assertEquals(accId, normal.accountId());
            assertEquals(ccy, normal.currency());
            assertEquals(new BigDecimal("300"), normal.absAmount().setScale(0));
            assertTrue(normal.withdrawal());
            assertEquals(baseDay, normal.requestDay());
            assertEquals(baseDay, normal.eventDay());
            assertEquals(LocalDate.of(2014, 11, 21), normal.valueDay());
            assertEquals(Remarks.CASH_OUT + "-" + ccy, normal.targetFiCode());
            assertEquals("FI" + accId, normal.targetFiAccountId());
            assertEquals(Remarks.CASH_OUT + "-" + ccy, normal.selfFiCode());
            assertEquals("xxxxxx", normal.selfFiAccountId());
            assertEquals(ActionStatusType.UNPROCESSED, normal.statusType());
            assertNull(normal.cashflowId());

            // Withdrawal request considering restricted amount [Exception]
            try {
                CashInOut.withdraw(rep, new RegCashOut(accId, ccy, new BigDecimal("701")));
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT, e.warns().fieldError("absAmount").get().message());
            }
        });
    }

    @Test
    public void cancel() {
        tester.tx(rep -> {
            TimePoint tp = rep.dh().time().tp();
            // Cancel a request of the CF having not yet processed
            CashInOut normal = rep.save(DataFixtures.cio("1", accId, "300", true, tp).build());
            assertEquals(ActionStatusType.CANCELLED, normal.cancel(rep).statusType());

            // When Reach an event day, I cannot cancel it. [ValidationException]
            CashInOut today = DataFixtures.cio("2", accId, "300", true, tp)
                    .eventDay(LocalDate.of(2014, 11, 18))
                    .build();
            rep.save(today);
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
            CashInOut normal = rep.save(DataFixtures.cio("1", accId, "300", true, tp).build());
            assertEquals(ActionStatusType.ERROR, normal.error(rep).statusType());

            // When it is processed, an error cannot do it. [ValidationException]
            CashInOut today = rep.save(DataFixtures.cio("2", accId, "300", true, tp)
                    .eventDay(LocalDate.of(2014, 11, 18))
                    .statusType(ActionStatusType.PROCESSED)
                    .build());
            rep.save(today);
            try {
                today.error(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.STATUS_PROCESSING, e.warns().fieldError("statusType").get().message());
            }
        });
    }

    @Test
    public void process() {
        tester.tx(rep -> {
            TimePoint tp = rep.dh().time().tp();
            // It is handled non-arrival on an event day [ValidationException]
            CashInOut future = rep.save(DataFixtures.cio("1", accId, "300", true, tp).build());
            try {
                future.process(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CIO_EVENT_DAY_AFTER_EQUALS_DAY, e.getMessage());
            }

            // Event day arrival processing.
            CashInOut normal = DataFixtures.cio("2", accId, "300", true, tp)
                    .eventDay(LocalDate.of(2014, 11, 18))
                    .build();
            rep.save(normal);
            CashInOut processed = normal.process(rep);
            assertEquals(ActionStatusType.PROCESSED, processed.statusType());
            assertNotNull(processed.cashflowId());

            // Check the Cashflow that CashInOut produced.
            Cashflow cf = Cashflow.load(rep, normal.cashflowId());
            assertEquals(accId, cf.accountId());
            assertEquals(ccy, cf.currency());
            assertEquals(new BigDecimal("-300"), cf.amount().setScale(0));
            assertEquals(CashflowType.CASH_OUT, cf.cashflowType());
            assertEquals(Remarks.CASH_OUT, cf.remark());
            assertEquals(LocalDate.of(2014, 11, 18), cf.eventDay());
            assertEquals(LocalDate.of(2014, 11, 21), cf.valueDay());
            assertEquals(ActionStatusType.UNPROCESSED, cf.statusType());
        });
    }

}
