package sample.context.uid;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * ID生成用のユーティリティコンポーネント。
 * <p>IDフォーマットが必要なケースで利用してください。
 * low: サンプルなのでメモリベースですが、実際は永続化の必要性があるためDBに依存します。
 * 
 * @author jkazama
 */
@Component
public class IdGenerator {

    private Map<String, AtomicLong> values = new HashMap<>();

    /** IDキーに応じたIDを自動生成します。 */
    public String generate(String key) {
        switch (key) {
        case "CashInOut":
            return formatCashInOut(nextValue(key));
        default:
            throw new IllegalArgumentException("サポートされない生成キーです [" + key + "]");
        }
    }

    private String formatCashInOut(long v) {
        //low: 実際は固定桁数化や0パディング含むちゃんとしたコード整形が必要です。
        return "CIO" + v;
    }

    private synchronized long nextValue(String key) {
        if (!values.containsKey(key)) {
            values.put(key, new AtomicLong(0));
        }
        return values.get(key).incrementAndGet();
    }

}
