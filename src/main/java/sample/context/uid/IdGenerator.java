package sample.context.uid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Utility component for ID generation.
 * low: It is memory-based because it is a sample, but actually it depends on
 * resource because there is a need for persistence.
 */
@Component
public class IdGenerator {

    private Map<String, AtomicLong> values = new HashMap<>();

    public String generate(String key) {
        switch (key) {
            case "CashInOut":
                return formatCashInOut(nextValue(key));
            default:
                return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private String formatCashInOut(long v) {
        // low: Correct code formatting including fixed digitization and 0 padding is
        // required.
        return "CIO" + v;
    }

    private synchronized long nextValue(String key) {
        if (!values.containsKey(key)) {
            values.put(key, new AtomicLong(0));
        }
        return values.get(key).incrementAndGet();
    }

}
