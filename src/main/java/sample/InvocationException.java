package sample;

/**
 * Application's runtime exception.
 * <p>use it for the purpose of wrapping the system exception that cannot restore.
 */
public class InvocationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationException(String message) {
        super(message);
    }

    public InvocationException(Throwable cause) {
        super(cause);
    }

}
