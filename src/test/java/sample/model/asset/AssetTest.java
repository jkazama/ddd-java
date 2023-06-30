package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.account.Account;

public class AssetTest {

    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Account.class, CashBalance.class, Cashflow.class, CashInOut.class).build();
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void canWithdraw() {
        tester.tx(rep -> {
            // 10000 + (1000 - 2000) - 8000 = 1000
            rep.save(DataFixtures.acc("test"));
            rep.save(DataFixtures.cb("test", LocalDate.of(2014, 11, 18), "JPY", "10000"));
            rep.save(DataFixtures.cf("test", "1000", LocalDate.of(2014, 11, 18), LocalDate.of(2014, 11, 20)));
            rep.save(DataFixtures.cf("test", "-2000", LocalDate.of(2014, 11, 19), LocalDate.of(2014, 11, 21)));
            rep.save(DataFixtures.cio("1", "test", "8000", true, rep.dh().time().tp()));

            assertTrue(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1000"), LocalDate.of(2014, 11, 21)));
            assertFalse(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1001"), LocalDate.of(2014, 11, 21)));
        });
    }

}
