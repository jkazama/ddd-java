package sample.context.audit;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import sample.InvocationException;
import sample.ValidationException;
import sample.context.actor.Actor;
import sample.context.actor.ActorSession;

/**
 * 利用者監査やシステム監査(定時バッチや日次バッチ等)などを取り扱います。
 * <p>
 * 暗黙的な適用を望む場合は、AOPとの連携も検討してください。
 * low: 実際はLoggerだけでなく、システムスキーマの監査テーブルへ書きだされます。(開始時と完了時で別TXにする事で応答無し状態を検知可能)
 * low: Loggerを利用する時はlogger.xmlを利用してファイル等に吐き出す
 * 
 * @author jkazama
 */
@Component
public class AuditHandler {
    public static final Logger loggerActor = LoggerFactory.getLogger("Audit.Actor");
    public static final Logger loggerEvent = LoggerFactory.getLogger("Audit.Event");

    /** 与えた処理に対し、監査ログを記録します。 */
    public <T> T audit(String message, final Callable<T> callable) {
        this.logger().trace(message(message, "[開始]", null));
        long start = System.currentTimeMillis();
        try {
            T v = callable.call();
            this.logger().info(message(message, "[完了]", start));
            return v;
        } catch (ValidationException e) {
            this.logger().warn(message(message, "[審例]", start));
            throw e;
        } catch (RuntimeException e) {
            this.logger().error(message(message, "[例外]", start));
            throw (RuntimeException) e;
        } catch (Exception e) {
            this.logger().error(message(message, "[例外]", start));
            throw new InvocationException("error.Exception", e);
        }
    }

    /** 与えた処理に対し、監査ログを記録します。 */
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
