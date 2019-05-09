package sample.context.actor;

import java.util.Locale;

import lombok.*;
import sample.context.Dto;

/**
 * User in the use case.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actor implements Dto {

    private static final long serialVersionUID = 1L;

    /** Anonymous user */
    public static Actor Anonymous = new Actor("unknown", ActorRoleType.ANONYMOUS);
    /** System user */
    public static Actor System = new Actor("system", ActorRoleType.SYSTEM);

    private String id;
    private String name;
    private ActorRoleType roleType;
    private Locale locale;
    /** Connection channel name of the actor */
    private String channel;
    /** Outside information to identify a actor. (including the IP) */
    private String source;

    public Actor(String id, ActorRoleType roleType) {
        this(id, id, roleType, Locale.getDefault(), null, null);
    }

    /**
     * Role of the actor.
     */
    public static enum ActorRoleType {
        /** Anonymous user. (the actor who does not have specific information such as the ID) */
        ANONYMOUS,
        /** User (mainly customer of BtoC, staff of BtoB) */
        USER,
        /** Internal user (mainly staff of BtoC, staff manager of BtoB) */
        INTERNAL,
        /** System administrator (an IT system charge staff or staff of the system management company) */
        ADMINISTRATOR,
        /** System (automatic processing on the system) */
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
}
