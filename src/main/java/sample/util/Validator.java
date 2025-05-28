package sample.util;

import java.util.function.Consumer;

import sample.context.ValidationException;
import sample.util.Warns.WarnsBuilder;

/**
 * Construction concept for validation exceptions.
 */
public final class Validator {
    private final WarnsBuilder builder = Warns.builder();

    public static void validate(Consumer<Validator> proc) {
        var validator = new Validator();
        proc.accept(validator);
        validator.verify();
    }

    /** Stacks a global exception internally if valid is false. */
    public Validator check(boolean valid, String message) {
        if (!valid) {
            builder.add(message);
        }
        return this;
    }

    /** Stacks a field exception internally if valid is false. */
    public Validator checkField(boolean valid, String field, String message) {
        if (!valid) {
            builder.addField(field, message);
        }
        return this;
    }

    /** Throws a global exception if valid is false. */
    public Validator verify(boolean valid, String message) {
        return check(valid, message).verify();
    }

    /** Throws a field exception if valid is false. */
    public Validator verifyField(boolean valid, String field, String message) {
        return checkField(valid, field, message).verify();
    }

    public Validator verify() {
        if (hasWarn()) {
            throw ValidationException.of(builder);
        }
        return this;
    }

    public boolean hasWarn() {
        return builder.build().hasError();
    }

}
