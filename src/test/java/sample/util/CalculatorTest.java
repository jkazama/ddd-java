package sample.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;

public class CalculatorTest {

    @Test
    public void calculation() {

        // (10 + 2 - 4) * 4 / 8 = 4
        assertEquals(
                4,
                Calculator.init(10).add(2).subtract(4).multiply(4).divideBy(8).intValue());

        // (12.4 + 0.033 - 2.33) * 0.3 / 3.3 = 0.91 (RoundingMode.DOWN)
        assertEquals(
                new BigDecimal("0.91"),
                Calculator.init(12.4).scale(2).add(0.033).subtract(2.33).multiply(0.3).divideBy(3.3)
                        .decimal());
    }

    @Test
    public void roundingAlways() {

        // 3.333 -> 3.334 -> 3.335 (= 3.34)
        assertEquals(
                new BigDecimal("3.34"),
                Calculator.init(3.333).scale(2, RoundingMode.HALF_UP)
                        .add(0.001).add(0.001).decimal());

        // 3.333 -> 3.330 -> 3.330 (= 3.33)
        assertEquals(
                new BigDecimal("3.33"),
                Calculator.init(3.333).scale(2, RoundingMode.HALF_UP).roundingAlways(true)
                        .add(0.001).add(0.001).decimal());
    }

}
