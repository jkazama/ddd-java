package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import sample.model.DataFixtures;
import sample.model.DomainTester;

@DataJdbcTest
@ActiveProfiles("test")
public class CashBalanceTest {
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
    public void add() {
        tester.tx(rep -> {
            CashBalance cb = rep
                    .save(DataFixtures.cb(rep.dh(), "test1", LocalDate.of(2014, 11, 18), "USD", "10.02").build());

            // 10.02 + 11.51 = 21.53
            cb = cb.add(rep, new BigDecimal("11.51"));
            assertEquals(new BigDecimal("21.53"), cb.amount().setScale(2));

            // 21.53 + 11.516 = 33.04 (check roundingMode)
            cb = cb.add(rep, new BigDecimal("11.516"));
            assertEquals(new BigDecimal("33.04"), cb.amount().setScale(2));

            // 33.04 - 41.51 = -8.47 (check minus)
            cb = cb.add(rep, new BigDecimal("-41.51"));
            assertEquals(new BigDecimal("-8.47"), cb.amount().setScale(2));
        });
    }

    @Test
    public void getOrNew() {
        tester.tx(rep -> {
            rep.save(DataFixtures.cb(rep.dh(), "test1", LocalDate.of(2014, 11, 18), "JPY", "1000").build());
            rep.save(DataFixtures.cb(rep.dh(), "test2", LocalDate.of(2014, 11, 17), "JPY", "3000").build());

            // Check the existing balance.
            CashBalance cbNormal = CashBalance.getOrNew(rep, "test1", "JPY");
            assertEquals("test1", cbNormal.accountId());
            assertEquals(LocalDate.of(2014, 11, 18), cbNormal.baseDay());
            assertEquals(new BigDecimal("1000"), cbNormal.amount().setScale(0));

            // Carrying forward inspection of the balance that does not exist in a basic
            // date.
            CashBalance cbRoll = CashBalance.getOrNew(rep, "test2", "JPY");
            assertEquals("test2", cbRoll.accountId());
            assertEquals(LocalDate.of(2014, 11, 18), cbRoll.baseDay());
            assertEquals(new BigDecimal("3000"), cbRoll.amount().setScale(0));

            // Create inspection of the account which does not hold the balance.
            CashBalance cbNew = CashBalance.getOrNew(rep, "test3", "JPY");
            assertEquals("test3", cbNew.accountId());
            assertEquals(LocalDate.of(2014, 11, 18), cbNew.baseDay());
            assertEquals(BigDecimal.ZERO, cbNew.amount());
        });
    }
}
