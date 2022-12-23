package sample.context.actor;

import java.util.Locale;

import lombok.Builder;

/**
 * ユースケースにおける利用者を表現します。
 * 
 * @author jkazama
 */
@Builder
public record Actor(
        /** 利用者ID */
        String id,
        /** 利用者名称 */
        String name,
        /** 利用者が持つ{@link ActorRoleType} */
        ActorRoleType roleType,
        /** 利用者が使用する{@link Locale} */
        Locale locale,
        /** 利用者の接続チャネル名称 */
        String channel,
        /** 利用者を特定する外部情報。(IPなど) */
        String source) {

    /** 匿名利用者定数 */
    public static Actor Anonymous = Actor.builder()
            .id("unknown")
            .name("unknown")
            .roleType(ActorRoleType.ANONYMOUS)
            .locale(Locale.getDefault())
            .build();
    /** システム利用者定数 */
    public static Actor System = Actor.builder()
            .id("system")
            .name("system")
            .roleType(ActorRoleType.SYSTEM)
            .locale(Locale.getDefault())
            .build();

}
