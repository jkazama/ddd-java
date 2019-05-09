package sample.context;

import org.springframework.stereotype.Component;

import sample.context.actor.*;
import sample.context.uid.IdGenerator;

/**
 * The access to the domain infrastructure layer component which is necessary in handling it.
 */
@Component
public class DomainHelper {

    private final ActorSession actorSession;
    private final Timestamper time;
    private final IdGenerator uid;

    public DomainHelper(
            ActorSession actorSession,
            Timestamper time,
            IdGenerator uid) {
        this.actorSession = actorSession;
        this.time = time;
        this.uid = uid;
    }

    /** Return a login user. */
    public Actor actor() {
        return actorSession.actor();
    }

    /** Return the user session of the thread local scope. */
    public ActorSession actorSession() {
        return actorSession;
    }

    public Timestamper time() {
        return time;
    }

    public IdGenerator uid() {
        return uid;
    }

}
