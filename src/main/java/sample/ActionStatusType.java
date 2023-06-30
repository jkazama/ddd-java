package sample;

import java.util.List;

/**
 * Processing status concept about some kind of acts.
 */
public enum ActionStatusType {
    UNPROCESSED,
    PROCESSING,
    PROCESSED,
    CANCELLED,
    ERROR;

    public static final List<ActionStatusType> FINISH_TYPES = List.of(PROCESSED, CANCELLED);

    public static final List<ActionStatusType> UNPROCESSING_TYPES = List.of(UNPROCESSED, ERROR);

    public static final List<ActionStatusType> UNPROCESSED_TYPES = List.of(UNPROCESSED, PROCESSING, ERROR);

    public boolean isFinish() {
        return FINISH_TYPES.contains(this);
    }

    public boolean isUnprocessing() {
        return UNPROCESSING_TYPES.contains(this);
    }

    public boolean isUnprocessed() {
        return UNPROCESSED_TYPES.contains(this);
    }
}