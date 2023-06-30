package sample.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.client.HttpClientErrorException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import sample.context.ValidationException;
import sample.context.ValidationException.Warn;
import sample.context.ValidationException.Warns;

/**
 * Exception Map conversion support for RestController.
 * <p>
 * Insert an exception handling by AOP advice.
 */
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class RestErrorAdvice {

    private final MessageSource msg;

    public RestErrorAdvice(MessageSource msg) {
        this.msg = msg;
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Map<String, String[]>> handleServletRequestBinding(ServletRequestBindingException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, "error.ServletRequestBinding").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, "error.HttpMessageNotReadable").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMediaTypeException(
            HttpMediaTypeException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, "error.HttpMediaTypeException").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, "error.HttpMediaTypeNotAcceptable").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        var warns = Warns.init();
        for (ConstraintViolation<?> v : e.getConstraintViolations()) {
            warns.add(v.getPropertyPath().toString(), v.getMessage());
        }
        return new ErrorHolder(msg, warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String[]>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, convert(e.getBindingResult()).list())
                .result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String[]>> handleExchangeBind(WebExchangeBindException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, convert(e).list()).result(HttpStatus.BAD_REQUEST);
    }

    private Warns convert(BindingResult e) {
        Warns warns = Warns.init();
        e.getFieldErrors().forEach(err -> {
            String[] args = Arrays.stream(err.getArguments())
                    .filter((arg) -> !(arg instanceof MessageSourceResolvable))
                    .map(Object::toString)
                    .toArray(String[]::new);
            warns.add(err.getField(), err.getDefaultMessage(), args);
        });
        return warns;
    }

    @SuppressWarnings("null")
    public static Warns convert(List<ObjectError> errors) {
        Warns warns = Warns.init();
        errors.forEach((oe) -> {
            String field = "";
            if (1 == oe.getCodes().length) {
                field = bindField(oe.getCodes()[0]);
            } else if (1 < oe.getCodes().length) {
                field = bindField(oe.getCodes()[1]);
            }
            List<String> args = Arrays.stream(oe.getArguments())
                    .filter((arg) -> !(arg instanceof MessageSourceResolvable))
                    .map(Object::toString)
                    .collect(Collectors.toList());
            String message = oe.getDefaultMessage();
            if (0 <= oe.getCodes()[0].indexOf("typeMismatch")) {
                message = oe.getCodes()[2];
            }
            warns.add(field, message, args.toArray(new String[0]));
        });
        return warns;
    }

    public static String bindField(String field) {
        return Optional.ofNullable(field).map((v) -> v.substring(v.indexOf('.') + 1)).orElse("");
    }

    /** RestTemplate 例外時のブリッジサポート */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(e.getResponseBodyAsString(), headers, e.getStatusCode());
    }

    /** アプリケーション例外 */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String[]>> handleValidation(ValidationException e) {
        ErrorHolder error = new ErrorHolder(msg, e);
        log.warn(e.getMessage());
        return error.result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
        log.warn(e.getMessage());
        Warns warns = convert(e.getAllErrors());
        return new ErrorHolder(msg, warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String[]>> handleException(Exception e) {
        log.error("An unexpected exception occurred.", e);
        return new ErrorHolder(msg, "error.Exception")
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * The stack of the exception information.
     * <p>
     * can convert the exception information that I stacked into ResponseEntity
     * having Map by calling {@link #result(HttpStatus)}.
     * <p>
     * The key when You registered in {@link #errorGlobal} becomes the null.
     * <p>
     * The client-side receives a return value in [{"fieldA": "messageA"},
     * {"fieldB": "messageB"}].
     */
    public static class ErrorHolder {
        private Map<String, List<String>> errors = new HashMap<String, List<String>>();
        private MessageSource msg;

        public ErrorHolder(final MessageSource msg) {
            this.msg = msg;
        }

        public ErrorHolder(final MessageSource msg, final ValidationException e) {
            this(msg, e.list());
        }

        public ErrorHolder(final MessageSource msg, final List<Warn> warns) {
            this.msg = msg;
            for (Warn warn : warns) {
                if (warn.global()) {
                    this.errorGlobal(warn.message());
                } else {
                    this.error(warn.field(), warn.message());
                }
            }
        }

        public ErrorHolder(final MessageSource msg, String globalMsgKey, String... msgArgs) {
            this.msg = msg;
            this.errorGlobal(globalMsgKey, msgArgs);
        }

        public ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
            if (!errors.containsKey("")) {
                errors.put("", new ArrayList<String>());
            }
            errors.get("").add(msg.getMessage(msgKey, msgArgs, defaultMsg, Locale.getDefault()));
            return this;
        }

        public ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
            return this.errorGlobal(msgKey, msgKey, msgArgs);
        }

        public ErrorHolder error(String field, String msgKey, String... msgArgs) {
            if (!errors.containsKey(field)) {
                errors.put(field, new ArrayList<String>());
            }
            errors.get(field).add(msg.getMessage(msgKey, msgArgs, msgKey, Locale.getDefault()));
            return this;
        }

        public ResponseEntity<Map<String, String[]>> result(HttpStatus status) {
            return new ResponseEntity<Map<String, String[]>>(
                    errors.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey, (entry) -> entry.getValue().toArray(new String[0]))),
                    status);
        }
    }

}
