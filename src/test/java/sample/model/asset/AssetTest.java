package sample.model.asset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class AssetTest {
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
    public void canWithdraw() {
        tester.tx(rep -> {
            // 10000 + (1000 - 2000) - 8000 = 1000
            rep.save(DataFixtures.acc("test").build());
            rep.save(DataFixtures.cb(rep.dh(), "test", LocalDate.of(2014, 11, 18), "JPY", "10000").build());
            rep.save(DataFixtures.cf(rep.dh(), "test", "1000", LocalDate.of(2014, 11, 18), LocalDate.of(2014, 11, 20))
                    .build());
            rep.save(DataFixtures.cf(rep.dh(), "test", "-2000", LocalDate.of(2014, 11, 19), LocalDate.of(2014, 11, 21))
                    .build());
            rep.save(DataFixtures.cio("1", "test", "8000", true, rep.dh().time().tp()).build());

            assertTrue(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1000"), LocalDate.of(2014, 11, 21)));
            assertFalse(
                    Asset.of("test").canWithdraw(rep, "JPY", new BigDecimal("1001"), LocalDate.of(2014, 11, 21)));
        });
    }

}
