package sample.model.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sample.context.ValidationException;
import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.DomainTester.DomainTesterBuilder;
import sample.model.account.Account.AccountStatusType;

public class AccountTest {
    private DomainTester tester;

    @BeforeEach
    public void before() {
        tester = DomainTesterBuilder.from(Account.class).build();
    }

    @AfterEach
    public void after() {
        tester.close();
    }

    @Test
    public void loadActive() {
        tester.tx(rep -> {
            rep.save(DataFixtures.acc("normal"));
            rep.flushAndClear(); // clear session cache
            Account account = Account.loadActive(rep, "normal");
            assertEquals("normal", account.getId());
            assertEquals(AccountStatusType.NORMAL, account.getStatusType());

            // 退会時取得検証
            Account withdrawal = DataFixtures.acc("withdraw");
            withdrawal.setStatusType(AccountStatusType.WITHDRAWAL);
            rep.save(withdrawal);
            rep.flushAndClear(); // clear session cache
            try {
                Account.loadActive(rep, "withdraw");
            } catch (ValidationException e) {
                assertEquals("error.Account.loadActive", e.getMessage());
            }
        });
    }
}
