package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;

import sample.*;
import sample.model.account.Account.AccountStatusType;

public class AccountTest extends EntityTestSupport {

    protected void setupPreset() {
        targetEntities(Account.class);
    }

    @Test
    public void loadActive() {
        tx(() -> {
            // 通常時取得検証
            fixtures.acc("normal").save(rep);
            rep.flushAndClear(); // clear session cache
            assertThat(Account.loadActive(rep, "normal"), allOf(
                    hasProperty("id", is("normal")),
                    hasProperty("statusType", is(AccountStatusType.NORMAL))));
            
            // 退会時取得検証
            Account withdrawal = fixtures.acc("withdraw");
            withdrawal.setStatusType(AccountStatusType.WITHDRAWAL);
            withdrawal.save(rep);
            rep.flushAndClear(); // clear session cache
            try {
                Account.loadActive(rep, "withdraw");
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.Account.loadActive"));
            }
        });
    }
}
