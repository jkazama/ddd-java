package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import sample.ActionStatusType;
import sample.context.ValidationException;
import sample.model.DataFixtures;
import sample.model.DomainErrorKeys;
import sample.model.DomainTester;

@DataJdbcTest
@ActiveProfiles("test")
public class CashflowTest {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcAggregateTemplate jdbcTemplate;

    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTester.create(jdbcTemplate, dataSource);
    }

    @Test
    public void register() {
        tester.tx(rep -> {
            // It is cashflow outbreak by the delivery of the past date.
            // [ValidationException]
            try {
                Cashflow.register(rep, DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 17)).build());
                fail();
            } catch (ValidationException e) {
                assertEquals("error.Cashflow.beforeEqualsDay", e.warns().fieldError("valueDay").get().message());
            }
            // Cashflow occurs by delivery the next day.
            var cf = Cashflow.register(rep, DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 19)).build());
            assertEquals(new BigDecimal("1000"), cf.amount().setScale(0));
            assertEquals(ActionStatusType.UNPROCESSED, cf.statusType());
            assertEquals(LocalDate.of(2014, 11, 18), cf.eventDay());
            assertEquals(LocalDate.of(2014, 11, 19), cf.valueDay());
        });
    }

    @Test
    public void realize() {
        tester.tx(rep -> {
            CashBalance.getOrNew(rep, "test1", "JPY");

            // Value day of non-arrival. [ValidationException]
            var cfFuture = rep.save(DataFixtures.cf(rep.dh(), "test1", "1000", LocalDate.of(2014, 11, 18),
                    LocalDate.of(2014, 11, 19)).build());
            try {
                cfFuture.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(AssetErrorKeys.CF_REALIZE_DAY, e.getMessage());
            }

            // Balance reflection inspection of the cashflow. 0 + 1000 = 1000
            var cfNormal = rep.save(DataFixtures.cf(rep.dh(), "test1", "1000", LocalDate.of(2014, 11, 17),
                    LocalDate.of(2014, 11, 18)).build());
            assertEquals(ActionStatusType.PROCESSED, cfNormal.realize(rep).statusType());
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").amount().setScale(0));

            // Re-realization of the treated cashflow. [ValidationException]
            try {
                cfNormal.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertEquals(DomainErrorKeys.STATUS_PROCESSING, e.getMessage());
            }

            // Balance reflection inspection of the other day cashflow. 1000 + 2000 = 3000
            var cfPast = rep.save(
                    DataFixtures.cf(rep.dh(), "test1", "2000", LocalDate.of(2014, 11, 16),
                            LocalDate.of(2014, 11, 17)).build());
            assertEquals(ActionStatusType.PROCESSED, cfPast.realize(rep).statusType());
            assertEquals(new BigDecimal("3000"), CashBalance.getOrNew(rep, "test1", "JPY").amount().setScale(0));
        });
    }

    @Test
    public void registerWithRealize() {
        tester.tx(rep -> {
            CashBalance.getOrNew(rep, "test1", "JPY");
            // Cashflow is realized immediately
            Cashflow.register(rep, DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 18)).build());
            assertEquals(new BigDecimal("1000"), CashBalance.getOrNew(rep, "test1", "JPY").amount().setScale(0));
        });
    }

}
