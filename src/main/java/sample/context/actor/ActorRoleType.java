package sample.context.actor;

/**
 * Role of the actor.
 */
public enum ActorRoleType {
    /**
     * Anonymous user.
     * (the actor who does not have specific information such as an ID)
     */
    ANONYMOUS,
    /**
     * User.
     * (mainly customers in BtoC, staff in BtoB)
     */
    USER,
    /**
     * Internal User.
     * (mainly staff in BtoC, staff managers in BtoB)
     */
    INTERNAL,
    /**
     * System Administrator.
     * (IT system staff or staff of the system management company)
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
