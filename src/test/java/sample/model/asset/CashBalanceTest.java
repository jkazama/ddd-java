package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

import sample.EntityTestSupport;

//low: Minimum test.
public class CashBalanceTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(CashBalance.class);
    }
    
    @Test
    public void add() {
        tx(() -> {
            CashBalance cb = fixtures.cb("test1", "20141118", "USD", "10.02").save(rep);
    
            // 10.02 + 11.51 = 21.53
            assertThat(cb.add(rep, new BigDecimal("11.51")).getAmount(), is(new BigDecimal("21.53")));
    
            // 21.53 + 11.516 = 33.04 (check roundingMode)
            assertThat(cb.add(rep, new BigDecimal("11.516")).getAmount(), is(new BigDecimal("33.04")));
    
            // 33.04 - 41.51 = -8.47 (check minus)
            assertThat(cb.add(rep, new BigDecimal("-41.51")).getAmount(), is(new BigDecimal("-8.47")));
        });
    }

    @Test
    public void getOrNew() {
        tx(() -> {
            fixtures.cb("test1", "20141118", "JPY", "1000").save(rep);
            fixtures.cb("test2", "20141117", "JPY", "3000").save(rep);
    
            // Check the existing balance.
            CashBalance cbNormal = CashBalance.getOrNew(rep, "test1", "JPY");
            assertThat(cbNormal, allOf(
                    hasProperty("accountId", is("test1")),
                    hasProperty("baseDay", is("20141118")),
                    hasProperty("amount", is(new BigDecimal("1000")))));
    
            // Carrying forward inspection of the balance that does not exist in a basic date.
            CashBalance cbRoll = CashBalance.getOrNew(rep, "test2", "JPY");
            assertThat(cbRoll, allOf(
                    hasProperty("accountId", is("test2")),
                    hasProperty("baseDay", is("20141118")),
                    hasProperty("amount", is(new BigDecimal("3000")))));
    
            // Create inspection of the account which does not hold the balance.
            CashBalance cbNew = CashBalance.getOrNew(rep, "test3", "JPY");
            assertThat(cbNew, allOf(
                    hasProperty("accountId", is("test3")),
                    hasProperty("baseDay", is("20141118")),
                    hasProperty("amount", is(BigDecimal.ZERO))));
        });
    }
}
