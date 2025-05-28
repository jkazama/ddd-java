package sample.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.util.StringUtils;

import jakarta.validation.ConstraintViolation;
import lombok.Builder;
import sample.context.ErrorKeys;

/** Validation exception information. */
public record Warns(List<Warn> errors) {

    /** Returns the global validation exception. */
    public Warn globalError() {
        return this.globalErrorOpt()
                .orElse(Warn.ofGlobal(ErrorKeys.Exception));
    }

    /** Returns the global validation exception. */
    public Optional<Warn> globalErrorOpt() {
        return errors.stream()
                .filter(Warn::isGlobal)
                .findFirst();
    }

    /** Returns the field validation exception list. */
    public List<Warn> fieldErrors() {
        return errors.stream()
                .filter(warn -> !warn.isGlobal())
                .toList();
    }

    /** Returns the field validation exception. */
    public Optional<Warn> fieldError(String field) {
        return errors.stream()
                .filter(warn -> !warn.isGlobal() && warn.field().equals(field))
                .findFirst();
    }

    /** Returns whether a validation exception exists. */
    public boolean hasError() {
        return !this.errors().isEmpty();
    }

    /** Returns whether a field validation exception exists. */
    public boolean hasFieldError() {
        return !fieldErrors().isEmpty();
    }

    /** Returns the validation exception information builder. */
    public static WarnsBuilder builder() {
        return new WarnsBuilder();
    }

    /** Validation exception information builder. */
    public static class WarnsBuilder {
        private List<Warn> warns = new ArrayList<>();

        /** Adds a validation exception message. */
        public WarnsBuilder add(String message, String... messageArgs) {
            warns.add(Warn.ofGlobal(message, messageArgs));
            return this;
        }

        /** Adds a validation exception message. */
        public WarnsBuilder add(String message, List<String> messageArgs) {
            warns.add(Warn.ofGlobal(message, messageArgs));
            return this;
        }

        /** Adds a validation exception message. */
        public WarnsBuilder add(Warn warn) {
            this.warns.add(warn);
            return this;
        }

        /** Adds a validation exception message to the field. */
        public WarnsBuilder addField(String field, String message, List<String> messageArgs) {
            warns.add(Warn.ofField(field, message, messageArgs));
            return this;
        }

        /** Adds a validation exception message to the field. */
        public WarnsBuilder addField(String field, String message, String... messageArgs) {
            warns.add(Warn.ofField(field, message, messageArgs));
            return this;
        }

        /** Adds a validation exception message. */
        public WarnsBuilder addConstraint(ConstraintViolation<?> error) {
            return this.addField(error.getPropertyPath().toString(), error.getMessage());
        }

        /** Adds a validation exception message. */
        public WarnsBuilder addAll(List<Warn> errors) {
            this.warns.addAll(errors);
            return this;
        }

        /** Adds a validation exception message. */
        public WarnsBuilder addAll(Warns warns) {
            this.warns.addAll(warns.errors);
            return this;
        }

        /** Adds a validation exception message. */
        public WarnsBuilder addAllConstraint(Set<ConstraintViolation<?>> errors) {
            errors.forEach(this::addConstraint);
            return this;
        }

        /** Returns the validation exception information. */
        public Warns build() {
            return new Warns(warns);
        }
    }

    /**
     * Represents a validation exception token in a field scope.
     * <p>
     * If the field is null, it is treated as a global exception.
     * </p>
     */
    @Builder
    public static record Warn(
            /** Validation exception field key */
            String field,
            /** Validation exception message */
            String message,
            /** Validation exception message arguments */
            List<String> messageArgs) {

        /** When the field is not associated, it is true for a global exception. */
        public boolean isGlobal() {
            return !StringUtils.hasText(field);
        }

        public static Warn ofGlobal(String message, String... messageArgs) {
            return Warn.ofGlobal(message, messageArgs != null ? List.of(messageArgs) : List.of());
        }

        public static Warn ofGlobal(String message, List<String> messageArgs) {
            return Warn.builder()
                    .message(message)
                    .messageArgs(messageArgs)
                    .build();
        }

        public static Warn ofField(String field, String message, String... messageArgs) {
            return Warn.ofField(field, message, messageArgs != null ? List.of(messageArgs) : List.of());
        }

        public static Warn ofField(String field, String message, List<String> messageArgs) {
            return Warn.builder()
                    .field(field)
                    .message(message)
                    .messageArgs(messageArgs)
                    .build();
        }

    }

}
