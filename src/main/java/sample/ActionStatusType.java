package sample;

import java.util.*;

/**
 * Processing status concept about some kind of acts.
 */
public enum ActionStatusType {
    UNPROCESSED,
    PROCESSING,
    PROCESSED,
    CANCELLED,
    ERROR;

    public static final List<ActionStatusType> finishTypes = Collections.unmodifiableList(
            Arrays.asList(PROCESSED, CANCELLED));

    public static final List<ActionStatusType> unprocessingTypes = Collections.unmodifiableList(
            Arrays.asList(UNPROCESSED, ERROR));

    public static final List<ActionStatusType> unprocessedTypes = Collections.unmodifiableList(
            Arrays.asList(UNPROCESSED, PROCESSING, ERROR));

    public boolean isFinish() {
        return finishTypes.contains(this);
    }

    public boolean isUnprocessing() {
        return unprocessingTypes.contains(this);
    }

    public boolean isUnprocessed() {
        return unprocessedTypes.contains(this);
    }
}
