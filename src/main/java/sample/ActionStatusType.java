package sample;

import java.util.*;

/**
 * 何らかの行為に関わる処理ステータス概念。
 * 
 * @author jkazama
 */
public enum ActionStatusType {
    /** 未処理 */
    UNPROCESSED,
    /** 処理中 */
    PROCESSING,
    /** 処理済 */
    PROCESSED,
    /** 取消 */
    CANCELLED,
    /** エラー */
    ERROR;

    /** 完了済みのステータス一覧 */
    public static final List<ActionStatusType> finishTypes = Collections.unmodifiableList(
            Arrays.asList(PROCESSED, CANCELLED));

    /** 未完了のステータス一覧(処理中は含めない) */
    public static final List<ActionStatusType> unprocessingTypes = Collections.unmodifiableList(
            Arrays.asList(UNPROCESSED, ERROR));

    /** 未完了のステータス一覧(処理中も含める) */
    public static final List<ActionStatusType> unprocessedTypes = Collections.unmodifiableList(
            Arrays.asList(UNPROCESSED, PROCESSING, ERROR));

    /** 完了済みのステータスの時はtrue */
    public boolean isFinish() {
        return finishTypes.contains(this);
    }

    /** 未完了のステータス(処理中は含めない)の時はtrue */
    public boolean isUnprocessing() {
        return unprocessingTypes.contains(this);
    }

    /** 未完了のステータス(処理中も含める)の時はtrue */
    public boolean isUnprocessed() {
        return unprocessedTypes.contains(this);
    }
}
