package sample.context.audit;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import sample.context.InvocationException;
import sample.context.ValidationException;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;

/**
 * Handles user auditing and EDP audit (scheduled batch processing or
 * daily batch processing).
 * <p>
 * When you expect implicit application behavior, please consider
 * integration with AOP.
 * <p>
 * The target log is written to both Logger and the audit table of
 * the system schema.
 * (You can detect unresponsive states by using separate transactions
 * for start and completion.)
 */
@Component
public class AuditHandler {
    public static final Logger loggerActor = LoggerFactory.getLogger("Audit.Actor");
    public static final Logger loggerEvent = LoggerFactory.getLogger("Audit.Event");

    public <T> T audit(String message, final Callable<T> callable) {
        this.logger().trace(message(message, "[Start]", null));
        long start = System.currentTimeMillis();
        try {
            T v = callable.call();
            this.logger().info(message(message, "[ End ]", start));
            return v;
        } catch (ValidationException e) {
            this.logger().warn(message(message, "[Warning]", start));
            throw e;
        } catch (RuntimeException e) {
            this.logger().error(message(message, "[Exception]", start));
            throw (RuntimeException) e;
        } catch (Exception e) {
            this.logger().error(message(message, "[Exception]", start));
            throw new InvocationException("error.Exception", e);
        }
    }

    public void audit(String message, final Runnable runnable) {
        this.audit(message, () -> {
            runnable.run();
            return null;
        });
    }

    private Logger logger() {
        return ActorSession.actor().roleType().isSystem() ? loggerEvent : loggerActor;
    }

    private String message(String message, String prefix, Long startMillis) {
        Actor actor = ActorSession.actor();
        var sb = new StringBuilder(prefix + " ");
        if (actor.roleType().isAnonymous()) {
            sb.append("[" + actor.source() + "] ");
        } else if (actor.roleType().notSystem()) {
            sb.append("[" + actor.id() + "] ");
        }
        sb.append(message);
        if (startMillis != null) {
            sb.append(" [" + (System.currentTimeMillis() - startMillis) + "ms]");
        }
        return sb.toString();
    }

}
