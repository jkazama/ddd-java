package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;

public class CashBalanceTest {

    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(CashBalance.class).build();
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void add() {
        tester.tx(rep -> {
            CashBalance cb = rep.save(DataFixtures.cb("test1", LocalDate.of(2014, 11, 18), "USD", "10.02"));

            // 10.02 + 11.51 = 21.53
            assertEquals(new BigDecimal("21.53"), cb.add(rep, new BigDecimal("11.51")).getAmount());

            // 21.53 + 11.516 = 33.04 (check roundingMode)
            assertEquals(new BigDecimal("33.04"), cb.add(rep, new BigDecimal("11.516")).getAmount());

            // 33.04 - 41.51 = -8.47 (check minus)
            assertEquals(new BigDecimal("-8.47"), cb.add(rep, new BigDecimal("-41.51")).getAmount());
        });
    }

    @Test
    public void getOrNew() {
        tester.tx(rep -> {
            rep.save(DataFixtures.cb("test1", LocalDate.of(2014, 11, 18), "JPY", "1000"));
            rep.save(DataFixtures.cb("test2", LocalDate.of(2014, 11, 17), "JPY", "3000"));

            // Check the existing balance.
            CashBalance cbNormal = CashBalance.getOrNew(rep, "test1", "JPY");
            assertEquals("test1", cbNormal.getAccountId());
            assertEquals(LocalDate.of(2014, 11, 18), cbNormal.getBaseDay());
            assertEquals(new BigDecimal("1000"), cbNormal.getAmount());

            // Carrying forward inspection of the balance that does not exist in a basic
            // date.
            CashBalance cbRoll = CashBalance.getOrNew(rep, "test2", "JPY");
            assertEquals("test2", cbRoll.getAccountId());
            assertEquals(LocalDate.of(2014, 11, 18), cbRoll.getBaseDay());
            assertEquals(new BigDecimal("3000"), cbRoll.getAmount());

            // Create inspection of the account which does not hold the balance.
            CashBalance cbNew = CashBalance.getOrNew(rep, "test3", "JPY");
            assertEquals("test3", cbNew.getAccountId());
            assertEquals(LocalDate.of(2014, 11, 18), cbNew.getBaseDay());
            assertEquals(BigDecimal.ZERO, cbNew.getAmount());
        });
    }
}
