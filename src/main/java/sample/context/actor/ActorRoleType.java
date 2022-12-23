package sample.context.actor;

/**
 * 利用者の役割を表現します。
 * 
 * @author jkazama
 */
public enum ActorRoleType {
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
