package sample.context.actor;

import java.util.Locale;

import lombok.Builder;

/**
 * User in the use case.
 */
@Builder
public record Actor(
        String id,
        String name,
        ActorRoleType roleType,
        Locale locale,
        /** Connection channel name of the actor */
        String channel,
        /** Outside information to identify a actor. (including the IP) */
        String source) {

    /** Anonymous user */
    public static Actor Anonymous = Actor.builder()
            .id("unknown")
            .name("unknown")
            .roleType(ActorRoleType.ANONYMOUS)
            .locale(Locale.getDefault())
            .build();
    /** System user */
    public static Actor System = Actor.builder()
            .id("system")
            .name("system")
            .roleType(ActorRoleType.SYSTEM)
            .locale(Locale.getDefault())
            .build();

}
