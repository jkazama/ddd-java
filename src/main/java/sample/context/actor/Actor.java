package sample.context.actor;

import java.util.Locale;

import lombok.Builder;
import sample.context.actor.Actor.ActorRoleType;

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

    /**
     * 利用者の役割を表現します。
     * 
     * @author jkazama
     */
    public static enum ActorRoleType {
        /** 匿名利用者(ID等の特定情報を持たない利用者) */
        ANONYMOUS,
        /** 利用者(主にBtoCの顧客, BtoB提供先社員) */
        USER,
        /** 内部利用者(主にBtoCの社員, BtoB提供元社員) */
        INTERNAL,
        /** システム管理者(ITシステム担当社員またはシステム管理会社の社員) */
        ADMINISTRATOR,
        /** システム(システム上の自動処理) */
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
