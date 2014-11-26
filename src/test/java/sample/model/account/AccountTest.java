package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import lombok.val;

import org.junit.Test;

import sample.*;
import sample.model.account.Account.AccountStatusType;

public class AccountTest extends UnitTestSupport {

	@Test
	public void loadActive() {
		// 通常時取得検証
		fixtures.acc("normal").save(rep);
		assertThat(Account.loadActive(rep, "normal"), allOf(
			hasProperty("id", is("normal")),
			hasProperty("statusType", is(AccountStatusType.NORMAL))));
		
		// 退会時取得検証
		val withdrawal = fixtures.acc("withdrawal");
		withdrawal.setStatusType(AccountStatusType.WITHDRAWAL);
		withdrawal.save(rep);
		try {
			Account.loadActive(rep, "withdrawal");
		} catch (ValidationException e) {
			assertThat(e.getMessage(), is("error.Account.loadActive"));
		}
	}
}
