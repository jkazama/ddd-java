package sample.context.actor;

/**
 * Role of the actor.
 */
public enum ActorRoleType {
    /**
     * Anonymous user.
     * (the actor who does not have specific information such as the ID)
     */
    ANONYMOUS,
    /**
     * User.
     * (mainly customer of BtoC, staff of BtoB)
     */
    USER,
    /**
     * Internal User.
     * (mainly staff of BtoC, staff manager of BtoB)
     */
    INTERNAL,
    /**
     * System Administrator.
     * (an IT system charge staff or staff of the system management company)
     */
    ADMINISTRATOR,
    /**
     * System.
     * (automatic processing on the system)
     */
    SYSTEM;

    public boolean isAnonymous() {
        return this == ANONYMOUS;
    }

    public boolean isSystem() {
        return this == SYSTEM;
    }

    public boolean notSystem() {
        return !isSystem();
    }

}
