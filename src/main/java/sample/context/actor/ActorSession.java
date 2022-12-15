package sample.context.actor;

/**
 * スレッドローカルスコープの利用者セッション。
 * low: 今回スコープ外ですが、認証ロジックに#bind/#unbindを組み込んで運用します。
 * 
 * @author jkazama
 */
public class ActorSession {
    private static final ThreadLocal<Actor> actorLocal = new ThreadLocal<>();

    /** 利用者セッションへ利用者を紐付けます。 */
    public static void bind(final Actor actor) {
        actorLocal.set(actor);
    }

    /** 利用者セッションを破棄します。 */
    public static void unbind() {
        actorLocal.remove();
    }

    /**
     * @return 有効な利用者を返します。紐付けされていない時は匿名者が返されます。
     */
    public static Actor actor() {
        Actor actor = actorLocal.get();
        return actor != null ? actor : Actor.Anonymous;
    }

}
