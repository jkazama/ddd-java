package sample.context.orm;

import java.util.function.Function;

/** MatchMode corresponding to LIKE clause */
public enum SqlMatchMode {
    START(v -> v + "%"),
    END(v -> "%" + v),
    ANYWHERE(v -> "%" + v + "%");

    private final Function<String, String> parser;

    private SqlMatchMode(Function<String, String> parser) {
        this.parser = parser;
    }

    public String toMatchString(String pattern) {
        return this.parser.apply(pattern);
    }
}
