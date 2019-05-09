package sample.model.asset;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import sample.*;

//low: Minimum test.
public class CashflowTest extends EntityTestSupport {

    @Override
    protected void setupPreset() {
        targetEntities(Cashflow.class);
    }

    @Test
    public void register() {
        tx(() -> {
            // It is cashflow outbreak by the delivery of the past date. [ValidationException]
            try {
                Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141117"));
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.Cashflow.beforeEqualsDay"));
            }
            // Cashflow occurs by delivery the next day.
            assertThat(Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141119")),
                    allOf(
                            hasProperty("amount", is(new BigDecimal("1000"))),
                            hasProperty("statusType", is(ActionStatusType.UNPROCESSED)),
                            hasProperty("eventDate", hasProperty("day", is("20141118"))),
                            hasProperty("valueDay", is("20141119"))));
        });
    }

    @Test
    public void realize() {
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");

            // Value day of non-arrival. [ValidationException]
            Cashflow cfFuture = fixtures.cf("test1", "1000", "20141118", "20141119").save(rep);
            try {
                cfFuture.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.Cashflow.realizeDay"));
            }

            // Balance reflection inspection of the cashflow.  0 + 1000 = 1000
            Cashflow cfNormal = fixtures.cf("test1", "1000", "20141117", "20141118").save(rep);
            assertThat(cfNormal.realize(rep), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("1000"))));

            // Re-realization of the treated cashflow. [ValidationException]
            try {
                cfNormal.realize(rep);
                fail();
            } catch (ValidationException e) {
                assertThat(e.getMessage(), is("error.ActionStatusType.unprocessing"));
            }

            // Balance reflection inspection of the other day cashflow. 1000 + 2000 = 3000
            Cashflow cfPast = fixtures.cf("test1", "2000", "20141116", "20141117").save(rep);
            assertThat(cfPast.realize(rep), hasProperty("statusType", is(ActionStatusType.PROCESSED)));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("3000"))));
        });
    }

    @Test
    public void registerWithRealize() {
        tx(() -> {
            CashBalance.getOrNew(rep, "test1", "JPY");
            // Cashflow is realized immediately
            Cashflow.register(rep, fixtures.cfReg("test1", "1000", "20141118"));
            assertThat(CashBalance.getOrNew(rep, "test1", "JPY"),
                    hasProperty("amount", is(new BigDecimal("1000"))));
        });
    }

}
