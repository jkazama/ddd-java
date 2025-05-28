package sample.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sample.context.ErrorKeys;
import sample.context.MessageAccessor;
import sample.context.ValidationException;
import sample.context.actor.ActorSession;
import sample.context.spring.ApiResponseError;
import sample.util.Warns;

/**
 * Exception mapping conversion support for RestController.
 * <p>
 * Inserts exception handling through AOP advice.
 */
@ControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor(staticName = "of")
@Slf4j
public class RestErrorAdvice {
    private final MessageAccessor msg;

    /** Servlet request binding exception. */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ProblemDetail handleServletRequestBinding(ServletRequestBindingException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .add("error.ServletRequestBinding")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    private Locale locale() {
        return ActorSession.actor().locale();
    }

    /** Message not readable exception. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .add("error.HttpMessageNotReadable")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    /** Media type exception. */
    @ExceptionHandler(HttpMediaTypeException.class)
    public ProblemDetail handleHttpMediaTypeException(
            HttpMediaTypeException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .add("error.HttpMediaTypeException")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    /** Media type not acceptable exception. */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ProblemDetail handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .add("error.HttpMediaTypeNotAcceptable")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    /** BeanValidation(JSR303) constraint exception. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .addAllConstraint(e.getConstraintViolations())
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    /** Controller request binding exception. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .addAll(convert(e.getBindingResult()))
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    /** Controller request binding exception. */
    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleExchangeBind(WebExchangeBindException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .addAll(convert(e))
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    /** Controller request binding exception. */
    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBind(BindException e) {
        log.warn(e.getMessage());
        return ApiResponseError.builder()
                .addAll(convert(e))
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    private Warns convert(BindingResult e) {
        var builder = Warns.builder();
        e.getFieldErrors().forEach(err -> {
            var messageArgs = Arrays.stream(err.getArguments())
                    .filter((arg) -> !(arg instanceof MessageSourceResolvable))
                    .map(Object::toString)
                    .toList();
            builder.addField(err.getField(), err.getDefaultMessage(), messageArgs);
        });
        return builder.build();
    }

    @SuppressWarnings("null")
    public static Warns convert(List<ObjectError> errors) {
        var builder = Warns.builder();
        errors.forEach((oe) -> {
            var field = Optional.ofNullable(oe.getCodes())
                    .filter(codes -> codes.length > 0)
                    .map(codes -> bindField(codes[0]))
                    .orElse("");
            var messageArgs = Arrays.stream(oe.getArguments())
                    .filter((arg) -> !(arg instanceof MessageSourceResolvable))
                    .map(Object::toString)
                    .toList();
            var message = oe.getDefaultMessage();
            if (field.contains("typeMismatch")) {
                message = oe.getCodes()[2];
            }
            builder.addField(field, message, messageArgs);
        });
        return builder.build();
    }

    public static String bindField(String field) {
        return Optional.ofNullable(field).map((v) -> v.substring(v.indexOf('.') + 1)).orElse("");
    }

    /** Application exception. */
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException e) {
        return ApiResponseError.builder()
                .addAll(e.warns())
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem();
    }

    /**
     * IO exception (Broken pipe by Tomcat is not the server's responsibility, so
     * it is excluded.)
     * <p>
     * If the client side is terminated by a broken pipe, it is handled as a
     * successful response.
     * </p>
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ProblemDetail> handleIOException(IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.info("Client-side processing was terminated.");
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return ResponseEntity.internalServerError()
                    .body(handleException(e));
        }
    }

    /** Generic exception. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        log.error("An unexpected exception occurred.", e);
        return ApiResponseError.builder()
                .add(ErrorKeys.Exception)
                .build(this.msg, HttpStatus.INTERNAL_SERVER_ERROR, this.locale())
                .problem();
    }

}
