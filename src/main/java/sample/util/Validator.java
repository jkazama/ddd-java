package sample.util;

import sample.*;
import sample.ValidationException.Warns;

/**
 * 審査例外Builder。
 * 
 * @author jkazama
 */
public class Validator {
    private Warns warns = Warns.init();

    /** 審査を行います。validがfalseの時に例外を内部にスタックします。 */
    public Validator check(boolean valid, String message) {
        if (!valid)
            warns.add(message);
        return this;
    }

    /** 個別属性の審査を行います。validがfalseの時に例外を内部にスタックします。 */
    public Validator checkField(boolean valid, String field, String message) {
        if (!valid)
            warns.add(field, message);
        return this;
    }

    /** 審査を行います。失敗した時は即時に例外を発生させます。 */
    public Validator verify(boolean valid, String message) {
        return check(valid, message).verify();
    }

    /** 個別属性の審査を行います。失敗した時は即時に例外を発生させます。 */
    public Validator verifyField(boolean valid, String field, String message) {
        return checkField(valid, field, message).verify();
    }

    /** 検証します。事前に行ったcheckで例外が存在していた時は例外を発生させます。 */
    public Validator verify() {
        if (hasWarn())
            throw new ValidationException(warns);
        return clear();
    }

    public boolean hasWarn() {
        return warns.nonEmpty();
    }

    public Validator clear() {
        warns.list().clear();
        return this;
    }

}
