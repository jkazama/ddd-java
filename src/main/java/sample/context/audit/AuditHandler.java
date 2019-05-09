package sample.context.audit;

import java.util.concurrent.Callable;

import org.slf4j.*;
import org.springframework.stereotype.Component;

import sample.*;
import sample.context.actor.*;

/**
 * It deals with user inspection or EDP audit (an appointed hour batch or kind of day batch).
 * <p>When you expect an implicit application, please examine the cooperation with AOP. 
 * <p>The target log is begun to write as well as Logger to the inspection table of the system schema.
 * (You can detect a replyless state by making the other transaction at a start and completion.)
 */
@Component
public class AuditHandler {
    public static final Logger loggerActor = LoggerFactory.getLogger("Audit.Actor");
    public static final Logger loggerEvent = LoggerFactory.getLogger("Audit.Event");

    private final ActorSession session;

    public AuditHandler(ActorSession session) {
        this.session = session;
    }

    public <T> T audit(String message, final Callable<T> callable) {
        logger().trace(message(message, "[Start]", null));
        long start = System.currentTimeMillis();
        try {
            T v = callable.call();
            logger().info(message(message, "[ End ]", start));
            return v;
        } catch (ValidationException e) {
            logger().warn(message(message, "[Warning]", start));
            throw e;
        } catch (RuntimeException e) {
            logger().error(message(message, "[Exception]", start));
            throw (RuntimeException) e;
        } catch (Exception e) {
            logger().error(message(message, "[Exception]", start));
            throw new InvocationException("error.Exception", e);
        }
    }

    public void audit(String message, final Runnable runnable) {
        audit(message, () -> {
            runnable.run();
            return null;
        });
    }

    private Logger logger() {
        return session.actor().getRoleType().isSystem() ? loggerEvent : loggerActor;
    }

    private String message(String message, String prefix, Long startMillis) {
        Actor actor = session.actor();
        StringBuilder sb = new StringBuilder(prefix + " ");
        if (actor.getRoleType().isAnonymous()) {
            sb.append("[" + actor.getSource() + "] ");
        } else if (actor.getRoleType().notSystem()) {
            sb.append("[" + actor.getId() + "] ");
        }
        sb.append(message);
        if (startMillis != null) {
            sb.append(" [" + (System.currentTimeMillis() - startMillis) + "ms]");
        }
        return sb.toString();
    }

}
