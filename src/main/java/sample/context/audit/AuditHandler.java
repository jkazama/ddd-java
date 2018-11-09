package sample.context.audit;

import java.util.concurrent.Callable;

import org.slf4j.*;
import org.springframework.stereotype.Component;

import sample.*;
import sample.context.actor.*;

/**
 * 利用者監査やシステム監査(定時バッチや日次バッチ等)などを取り扱います。
 * <p>暗黙的な適用を望む場合は、AOPとの連携も検討してください。
 * low: 実際はLoggerだけでなく、システムスキーマの監査テーブルへ書きだされます。(開始時と完了時で別TXにする事で応答無し状態を検知可能)
 * low: Loggerを利用する時はlogger.xmlを利用してファイル等に吐き出す
 * 
 * @author jkazama
 */
@Component
public class AuditHandler {
    public static final Logger loggerActor = LoggerFactory.getLogger("Audit.Actor");
    public static final Logger loggerEvent = LoggerFactory.getLogger("Audit.Event");

    private final ActorSession session;

    public AuditHandler(ActorSession session) {
        this.session = session;
    }

    /** 与えた処理に対し、監査ログを記録します。 */
    public <T> T audit(String message, final Callable<T> callable) {
        logger().trace(message(message, "[開始]", null));
        long start = System.currentTimeMillis();
        try {
            T v = callable.call();
            logger().info(message(message, "[完了]", start));
            return v;
        } catch (ValidationException e) {
            logger().warn(message(message, "[審例]", start));
            throw e;
        } catch (RuntimeException e) {
            logger().error(message(message, "[例外]", start));
            throw (RuntimeException) e;
        } catch (Exception e) {
            logger().error(message(message, "[例外]", start));
            throw new InvocationException("error.Exception", e);
        }
    }

    /** 与えた処理に対し、監査ログを記録します。 */
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
