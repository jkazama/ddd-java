package sample.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.math.*;

import org.junit.Test;

public class CalculatorTest {

    @Test
    public void calculation() {

        // (10 + 2 - 4) * 4 / 8 = 4
        assertThat(
                Calculator.init(10).add(2).subtract(4).multiply(4).divideBy(8).intValue(),
                is(4));

        // (12.4 + 0.033 - 2.33) * 0.3 / 3.3 = 0.91 (RoundingMode.DOWN)
        assertThat(
                Calculator.init(12.4).scale(2).add(0.033).subtract(2.33).multiply(0.3).divideBy(3.3).decimal(),
                is(new BigDecimal("0.91")));
    }

    @Test
    public void roundingAlways() {

        // 3.333 -> 3.334 -> 3.335 (= 3.34)
        assertThat(
                Calculator.init(3.333).scale(2, RoundingMode.HALF_UP)
                        .add(0.001).add(0.001).decimal(),
                is(new BigDecimal("3.34")));

        // 3.333 -> 3.330 -> 3.330 (= 3.33)
        assertThat(
                Calculator.init(3.333).scale(2, RoundingMode.HALF_UP).roundingAlways(true)
                        .add(0.001).add(0.001).decimal(),
                is(new BigDecimal("3.33")));
    }

}
