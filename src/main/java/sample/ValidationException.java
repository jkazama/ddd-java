package sample;

import java.io.Serializable;
import java.util.*;

import lombok.Value;

import org.springframework.util.Assert;

/**
 * 審査例外を表現します。
 * 
 * @author jkazama
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Warns warns;

    /**
     * フィールドに従属しないグローバルな審査例外を通知するケースで利用してください。
     * @param message
     */
    public ValidationException(String message) {
        super(message);
        warns = Warns.init(message);
    }

    /**
     * フィールドに従属する審査例外を通知するケースで利用してください。
     * @param field
     * @param message
     */
    public ValidationException(String field, String message) {
        super(message);
        warns = Warns.init(field, message);
    }

    /**
     * フィールドに従属する審査例外を通知するケースで利用してください。
     * @param field
     * @param message
     * @param messageArgs
     */
    public ValidationException(String field, String message, String[] messageArgs) {
        super(message);
        warns = Warns.init(field, message, messageArgs);
    }

    /**
     * 複数件の審査例外を通知するケースで利用してください。
     * @param warns
     */
    public ValidationException(final Warns warns) {
        super(warns.head().getMessage());
        this.warns = warns;
    }

    /**
     * @return 発生した審査例外一覧を返します。
     */
    public List<Warn> list() {
        return warns.list();
    }

    @Override
    public String getMessage() {
        return warns.head().getMessage();
    }

    /** 審査例外情報  */
    public static class Warns implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<Warn> list = new ArrayList<>();

        private Warns() {
        }

        public Warns add(String message) {
            list.add(new Warn(null, message, null));
            return this;
        }

        public Warns add(String field, String message) {
            list.add(new Warn(field, message, null));
            return this;
        }

        public Warns add(String field, String message, String[] messageArgs) {
            list.add(new Warn(field, message, messageArgs));
            return this;
        }

        public Warn head() {
            Assert.notEmpty(list);
            return list.get(0);
        }

        public List<Warn> list() {
            return list;
        }

        public boolean nonEmpty() {
            return !list.isEmpty();
        }

        public static Warns init() {
            return new Warns();
        }

        public static Warns init(String message) {
            return init().add(message);
        }

        public static Warns init(String field, String message) {
            return init().add(field, message);
        }

        public static Warns init(String field, String message, String[] messageArgs) {
            return init().add(field, message, messageArgs);
        }

    }

    /**
     * フィールドスコープの審査例外トークン。
     */
    @Value
    public static class Warn implements Serializable {
        private static final long serialVersionUID = 1L;
        private String field;
        private String message;
        private String[] messageArgs;

        /**
         * @return フィールドに従属しないグローバル例外時はtrue
         */
        public boolean global() {
            return field == null;
        }
    }

}
