package sample.context.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import sample.ValidationException;
import sample.ValidationException.Warn;
import sample.ValidationException.Warns;

/**
 * REST用の例外Map変換サポート。
 * <p>
 * AOPアドバイスで全てのRestControllerに対して例外処理を当て込みます。
 * 
 * @author jkazama
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

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, String[]>> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, "error.HttpMediaTypeNotAcceptable").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String[]>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, "error.EntityNotFoundException").result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String[]>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        for (ConstraintViolation<?> v : e.getConstraintViolations()) {
            warns.add(v.getPropertyPath().toString(), v.getMessage());
        }
        return new ErrorHolder(msg, warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    @SuppressWarnings("null")
    public ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        for (ObjectError oe : e.getAllErrors()) {
            String field = "";
            var codes = oe.getCodes();
            if (1 == codes.length) {
                field = bindField(codes[0]);
            } else if (1 < codes.length) {
                // low: プリフィックスは冗長なので外してます
                field = bindField(codes[1]);
            }
            List<String> args = new ArrayList<String>();
            for (Object arg : oe.getArguments()) {
                if (arg instanceof MessageSourceResolvable) {
                    continue;
                }
                args.add(arg.toString());
            }
            String message = oe.getDefaultMessage();
            if (0 <= codes[0].indexOf("typeMismatch")) {
                message = codes[2];
            }
            warns.add(field, message, args.toArray(new String[0]));
        }
        return new ErrorHolder(msg, warns.list()).result(HttpStatus.BAD_REQUEST);
    }

    protected String bindField(String field) {
        if (field == null) {
            return "";
        }
        return field.substring(field.indexOf('.') + 1);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String[]>> handleValidation(ValidationException e) {
        log.warn(e.getMessage());
        return new ErrorHolder(msg, e).result(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String[]>> handleException(Exception e) {
        log.error("予期せぬ例外が発生しました。", e);
        return new ErrorHolder(msg, "error.Exception", "サーバー側で問題が発生した可能性があります。")
                .result(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /** 例外情報のスタックを表現します。 */
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
                    errorGlobal(warn.message());
                } else {
                    error(warn.field(), warn.message());
                }
            }
        }

        public ErrorHolder(final MessageSource msg, String globalMsgKey, String... msgArgs) {
            this.msg = msg;
            errorGlobal(globalMsgKey, msgArgs);
        }

        public ErrorHolder errorGlobal(String msgKey, String defaultMsg, String... msgArgs) {
            if (!errors.containsKey("")) {
                errors.put("", new ArrayList<String>());
            }
            errors.get("").add(msg.getMessage(msgKey, msgArgs, defaultMsg, Locale.getDefault()));
            return this;
        }

        public ErrorHolder errorGlobal(String msgKey, String... msgArgs) {
            return errorGlobal(msgKey, msgKey, msgArgs);
        }

        public ErrorHolder error(String field, String msgKey, String... msgArgs) {
            if (!errors.containsKey(field)) {
                errors.put(field, new ArrayList<String>());
            }
            errors.get(field).add(msg.getMessage(msgKey, msgArgs, msgKey, Locale.getDefault()));
            return this;
        }

        public ResponseEntity<Map<String, String[]>> result(HttpStatus status) {
            Map<String, String[]> ret = new HashMap<String, String[]>();
            for (Map.Entry<String, List<String>> v : errors.entrySet()) {
                ret.put(v.getKey(), v.getValue().toArray(new String[0]));
            }
            return new ResponseEntity<Map<String, String[]>>(ret, status);
        }
    }

}
