package sample.model.account;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import javax.transaction.Transactional;

import lombok.val;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import sample.*;
import sample.context.orm.JpaRepository;
import sample.model.DataFixtures;
import sample.model.account.Account.AccountStatusType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@Transactional
public class AccountTest {

	@Autowired
	private JpaRepository rep;
	@Autowired
	private DataFixtures fixtures;

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
