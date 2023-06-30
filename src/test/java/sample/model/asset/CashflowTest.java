package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.ActionStatusType;
import sample.context.ValidationException;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;

public class CashflowTest {

    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Cashflow.class).build();
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void register() {
        tester.tx(rep -> {
            // It is cashflow outbreak by the delivery of the past date.
            // [ValidationException]
            try {
                Cashflow.register(rep, DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 17)));
                fail();
            } catch (ValidationException e) {
                assertEquals("error.Cashflow.beforeEqualsDay", e.getMessage());
            }
            // Cashflow occurs by delivery the next day.
            var cf = Cashflow.register(rep, DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 19)));
            assertEquals(new BigDecimal("1000"), cf.getAmount());
            assertEquals(ActionStatusType.UNPROCESSED, cf.getStatusType());
            assertEquals(LocalDate.of(2014, 11, 18), cf.getEventDay());
            assertEquals(LocalDate.of(2014, 11, 19), cf.getValueDay());
        });
    }

    @Test
    public void realize() {
        tester.tx(rep -> {
            CashBalance.getOrNew(rep, "test1", "JPY");

            // Value day of non-arrival. [ValidationException]
            var cfFuture = rep.save(DataFixtures
                    .cf("test1", "1000", LocalDate.of(2014, 11, 18),
                            LocalDate.of(2014, 11, 19)));
            try {
                cfFuture.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CF_REALIZE_DAY, e.getMessage());
            }

            // Balance reflection inspection of the cashflow. 0 + 1000 = 1000
            var cfNormal = rep.save(DataFixtures
                    .cf("test1", "1000", LocalDate.of(2014, 11, 17),
                            LocalDate.of(2014, 11, 18)));
            assertEquals(ActionStatusType.PROCESSED, cfNormal.realize(rep).getStatusType());
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());

            // Re-realization of the treated cashflow. [ValidationException]
            try {
                cfNormal.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.STATUS_PROCESSING, e.getMessage());
            }

            // Balance reflection inspection of the other day cashflow. 1000 + 2000 = 3000
            var cfPast = rep.save(
                    DataFixtures.cf("test1", "2000", LocalDate.of(2014, 11, 16), LocalDate.of(2014, 11, 17)));
            assertEquals(ActionStatusType.PROCESSED, cfPast.realize(rep).getStatusType());
            assertEquals(new BigDecimal("3000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());
        });
    }

    @Test
    public void registerWithRealize() {
        tester.tx(rep -> {
            CashBalance.getOrNew(rep, "test1", "JPY");
            // Cashflow is realized immediately
            Cashflow.register(rep, DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 18)));
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").getAmount());
        });
    }

}
