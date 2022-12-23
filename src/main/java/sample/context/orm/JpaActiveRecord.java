package sample.context.orm;

import java.io.Serializable;

import sample.context.DomainEntity;

/**
 * JPAベースでActiveRecordの概念を提供するEntity基底クラス。
 * <p>
 * ここでは自インスタンスの状態に依存する簡易な振る舞いのみをサポートします。
 * 実際のActiveRecordモデルにはget/find等の概念も含まれますが、それらは 自己の状態を
 * 変える行為ではなく対象インスタンスを特定する行為(クラス概念)にあたるため、
 * クラスメソッドとして継承先で個別定義するようにしてください。
 * 
 * <pre>
 * public static Account findAll(final JpaRepository rep) {
 *     return rep.findAll(Account.class);
 * }
 * </pre>
 * 
 * @author jkazama
 */
public abstract class JpaActiveRecord<T extends DomainEntity> implements Serializable, DomainEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 与えられたレポジトリを経由して自身を新規追加します。
     * 
     * @param rep 永続化の際に利用する関連{@link JpaRepository}
     * @return 自身の情報
     */
    @SuppressWarnings("unchecked")
    public T save(final JpaRepository rep) {
        return (T) rep.save(this);
    }

    /**
     * 与えられたレポジトリを経由して自身を更新します。
     * 
     * @param rep 永続化の際に利用する関連{@link JpaRepository}
     */
    @SuppressWarnings("unchecked")
    public T update(final JpaRepository rep) {
        return (T) rep.update(this);
    }

    /**
     * 与えられたレポジトリを経由して自身を物理削除します。
     * 
     * @param rep 永続化の際に利用する関連{@link JpaRepository}
     */
    @SuppressWarnings("unchecked")
    public T delete(final JpaRepository rep) {
        return (T) rep.delete(this);
    }

    /**
     * 与えられたレポジトリを経由して自身を新規追加または更新します。
     * 
     * @param rep 永続化の際に利用する関連{@link JpaRepository}
     */
    @SuppressWarnings("unchecked")
    public T saveOrUpdate(final JpaRepository rep) {
        return (T) rep.saveOrUpdate(this);
    }

}
