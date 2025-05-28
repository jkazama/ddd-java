package sample.model.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import sample.context.ValidationException;
import sample.model.DataFixtures;
import sample.model.DomainTester;
import sample.model.account.Account.AccountStatusType;

@DataJdbcTest
@ActiveProfiles("test")
public class AccountTest {
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
    public void loadActive() {
        tester.tx(rep -> {
            rep.save(DataFixtures.acc("normal").build());
            Account account = Account.loadActive(rep, "normal");
            assertEquals("normal", account.id());
            assertEquals(AccountStatusType.NORMAL, account.statusType());

            // Verification when withdrawal account is loaded
            Account withdrawal = DataFixtures.acc("withdraw").statusType(AccountStatusType.WITHDRAWAL).build();
            rep.save(withdrawal);
            try {
                Account.loadActive(rep, "withdraw");
            } catch (ValidationException e) {
                assertEquals("error.Account.loadActive", e.getMessage());
            }
        });
    }
}
