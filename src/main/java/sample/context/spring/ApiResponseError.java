package sample.context.spring;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import jakarta.validation.ConstraintViolation;
import sample.context.MessageAccessor;
import sample.util.Warns;
import sample.util.Warns.WarnsBuilder;

/**
 * Represents an API response error.
 * <p>
 * Can include global errors and field errors in ProblemDetail format.
 * </p>
 */
public record ApiResponseError(ProblemDetail problem) {

    /**
     * Returns an API response error builder.
     *
     * @param msg message accessor
     * @return response error builder
     */
    public static ApiResponseErrorBuilder builder() {
        return new ApiResponseErrorBuilder();
    }

    /**
     * API response error builder.
     */
    public static class ApiResponseErrorBuilder {
        private final WarnsBuilder warnsBuilder = Warns.builder();

        /** Adds validation exception information. */
        public ApiResponseErrorBuilder addAll(Warns warns) {
            this.warnsBuilder.addAll(warns);
            return this;
        }

        /** Adds validation exception information. */
        public ApiResponseErrorBuilder addAllConstraint(Set<ConstraintViolation<?>> errors) {
            this.warnsBuilder.addAllConstraint(errors);
            return this;
        }

        /** Adds a global error. */
        public ApiResponseErrorBuilder add(String message, String... messageArgs) {
            this.warnsBuilder.add(message, messageArgs);
            return this;
        }

        /** Adds a global error. */
        public ApiResponseErrorBuilder add(String message, List<String> messageArgs) {
            this.warnsBuilder.add(message, messageArgs);
            return this;
        }

        /** Adds a field error. */
        public ApiResponseErrorBuilder addField(String field, String message, String... messageArgs) {
            this.warnsBuilder.addField(field, message, messageArgs);
            return this;
        }

        /** Adds a field error. */
        public ApiResponseErrorBuilder addField(String field, String message, List<String> messageArgs) {
            this.warnsBuilder.addField(field, message, messageArgs);
            return this;
        }

        /** Builds the response error. */
        public ApiResponseError build(MessageAccessor msg, HttpStatus status) {
            return this.build(msg, status, Locale.getDefault());
        }

        /** Builds the response error. */
        public ApiResponseError build(MessageAccessor msg, HttpStatus status, Locale locale) {
            var warns = this.warnsBuilder.build();
            var message = msg.load(locale, warns.globalError());
            var problrem = ProblemDetail.forStatusAndDetail(status, message);
            if (warns.hasFieldError()) {
                problrem.setProperty("errors", warns.fieldErrors().stream()
                        .map(v -> Map.of(
                                "field", v.field(),
                                "messageKey", v.message(),
                                "messageArgs", v.messageArgs(),
                                "message", msg.load(locale, v)))
                        .toList());
            }
            return new ApiResponseError(problrem);
        }

    }
}