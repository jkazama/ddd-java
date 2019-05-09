package sample.util;

import java.util.function.Consumer;

import sample.*;
import sample.ValidationException.Warns;

/**
 * Construction concept of the examination exception.
 */
public class Validator {
    private Warns warns = Warns.init();

    public static void validate(Consumer<Validator> proc) {
        Validator validator = new Validator();
        proc.accept(validator);
        validator.verify();
    }

    /** An global exception stacks inside if valid is false. */
    public Validator check(boolean valid, String message) {
        if (!valid)
            warns.add(message);
        return this;
    }

    /** An field exception stacks inside if valid is false. */
    public Validator checkField(boolean valid, String field, String message) {
        if (!valid)
            warns.add(field, message);
        return this;
    }

    /** An global exception occurs if valid is false. */
    public Validator verify(boolean valid, String message) {
        return check(valid, message).verify();
    }

    /** An field exception occurs if valid is false. */
    public Validator verifyField(boolean valid, String field, String message) {
        return checkField(valid, field, message).verify();
    }

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
