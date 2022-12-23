package sample.model.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sample.EntityTestSupport;
import sample.ValidationException;
import sample.model.account.Account.AccountStatusType;

public class AccountTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Account.class);
    }

    @Test
    public void loadActive() {
        tx(() -> {
            // 通常時取得検証
            fixtures.acc("normal").save(rep);
            rep.flushAndClear(); // clear session cache
            Account account = Account.loadActive(rep, "normal");
            assertEquals("normal", account.getId());
            assertEquals(AccountStatusType.NORMAL, account.getStatusType());

            // 退会時取得検証
            Account withdrawal = fixtures.acc("withdraw");
            withdrawal.setStatusType(AccountStatusType.WITHDRAWAL);
            withdrawal.save(rep);
            rep.flushAndClear(); // clear session cache
            try {
                Account.loadActive(rep, "withdraw");
            } catch (ValidationException e) {
                assertEquals("error.Account.loadActive", e.getMessage());
            }
        });
    }
}
