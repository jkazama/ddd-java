package sample.context.orm;

/** LIKE 句で利用されるパターンを表現します。 */
public enum MatchMode {
    ANYWHERE,
    END,
    START;

    public String parse(String pattern) {
        switch (this) {
            case ANYWHERE:
                return "%" + pattern + "%";
            case END:
                return pattern + "%";
            case START:
                return "%" + pattern;
            default:
                return pattern;
        }
    }
}
