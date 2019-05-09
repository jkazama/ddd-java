package sample.context.actor;

import org.springframework.stereotype.Component;

/**
 * The actor session of the thread local scope.
 */
@Component
public class ActorSession {

    private ThreadLocal<Actor> actorLocal = new ThreadLocal<>();

    /** Relate a actor with a actor session. */
    public ActorSession bind(final Actor actor) {
        actorLocal.set(actor);
        return this;
    }

    /** Unbind a actor session. */
    public ActorSession unbind() {
        actorLocal.remove();
        return this;
    }

    /** Return an effective actor. When You are not related, an anonymous is returned. */
    public Actor actor() {
        Actor actor = actorLocal.get();
        return actor != null ? actor : Actor.Anonymous;
    }

}
