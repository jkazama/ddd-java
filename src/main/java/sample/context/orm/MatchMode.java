package sample.context.orm;

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
