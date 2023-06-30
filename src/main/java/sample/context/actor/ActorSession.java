package sample.context.actor;

/**
 * The actor session of the thread local scope.
 */
public class ActorSession {
    private static final ThreadLocal<Actor> actorLocal = new ThreadLocal<>();

    /** Relate a actor with a actor session. */
    public static void bind(final Actor actor) {
        actorLocal.set(actor);
    }

    /** Unbind a actor session. */
    public static void unbind() {
        actorLocal.remove();
    }

    /**
     * Return an effective actor. When You are not related, an anonymous is
     * returned.
     */
    public static Actor actor() {
        Actor actor = actorLocal.get();
        return actor != null ? actor : Actor.Anonymous;
    }

}
