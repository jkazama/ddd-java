package sample.context.rest;

import java.util.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.*;

import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;

import sample.ValidationException.Warn;
import sample.ValidationException.Warns;
import sample.ValidationException;

/**
 * REST用の例外Map変換サポート。
 * <p>AOPアドバイスで全てのRestControllerに対して例外処理を当て込みます。
 * 
 * @author jkazama
 */
@ControllerAdvice(annotations = RestController.class)
public class RestErrorAdvice {

    protected Log log = LogFactory.getLog(getClass());

    @Autowired
    private MessageSource msg;

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
    public ResponseEntity<Map<String, String[]>> handleBind(BindException e) {
        log.warn(e.getMessage());
        Warns warns = Warns.init();
        for (ObjectError oe : e.getAllErrors()) {
            String field = "";
            if (1 == oe.getCodes().length) {
                field = bindField(oe.getCodes()[0]);
            } else if (1 < oe.getCodes().length) {
                // low: プリフィックスは冗長なので外してます
                field = bindField(oe.getCodes()[1]);
            }
            List<String> args = new ArrayList<String>();
            for (Object arg : oe.getArguments()) {
                if (arg instanceof MessageSourceResolvable) {
                    continue;
                }
                args.add(arg.toString());
            }
            String message = oe.getDefaultMessage();
            if (0 <= oe.getCodes()[0].indexOf("typeMismatch")) {
                message = oe.getCodes()[2];
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
                    errorGlobal(warn.getMessage());
                } else {
                    error(warn.getField(), warn.getMessage());
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
