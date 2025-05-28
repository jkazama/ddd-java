package sample.context;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import sample.util.Warns;
import sample.util.Warns.Warn;
import sample.util.Warns.WarnsBuilder;

/**
 * Application's validation exception.
 * <p>
 * ValidationException represents validation exceptions that can be recovered
 * from,
 * such as input exceptions or invalid state transition exceptions.
 * The output in the log is performed at WARN level.
 * <p>
 * The validation exception can hold multiple errors in global/field scope.
 * When you handle multiple validation errors, please initialize it using
 * Warns.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Warns warns;

    /** Use this when notifying multiple validation exceptions. */
    public ValidationException(final Warns warns) {
        super(warns.globalError().message());
        this.warns = warns;
    }

    /** Returns the list of validation exceptions that occurred. */
    public Warns warns() {
        return this.warns;
    }

    public static ValidationException of(final Warn warn) {
        return new ValidationException(Warns.builder().add(warn).build());
    }

    public static ValidationException of(final Warns warns) {
        return new ValidationException(warns);
    }

    public static ValidationException of(final WarnsBuilder warnsBuilder) {
        return of(warnsBuilder.build());
    }

    public static ValidationException of(final Set<ConstraintViolation<Object>> errors) {
        var builder = Warns.builder();
        errors.forEach((v) -> builder.addField(v.getPropertyPath().toString(), v.getMessage()));
        return of(builder);
    }

    public static ValidationException of(String message, String... messageArgs) {
        return of(Warns.builder()
                .add(message, messageArgs)
                .build());
    }

    public static ValidationException of(String message, List<String> messageArgs) {
        return of(Warns.builder()
                .add(message, messageArgs)
                .build());
    }

    public static ValidationException ofField(String field, String message, String... messageArgs) {
        return of(Warns.builder()
                .addField(field, message, messageArgs)
                .build());
    }

    public static ValidationException ofField(String field, String message, List<String> messageArgs) {
        return of(Warns.builder()
                .addField(field, message, messageArgs)
                .build());
    }

}
