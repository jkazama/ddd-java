package sample.context;

import java.io.Serializable;
import java.util.List;

/**
 * 特定のドメインオブジェクトに依存しない汎用的なRepositoryです。
 * タイプセーフでないRepositoryとして利用することができます。
 * 
 * @author jkazama
 */
public interface Repository {

    /**
     * @return ドメイン層においてインフラ層コンポーネントへのアクセスを提供するヘルパーユーティリティを返します。
     */
    DomainHelper dh();

    /**
     * プライマリキーに一致する{@link DomainEntity}を返します。
     * 
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id    プライマリキー
     * @return プライマリキーに一致した{@link DomainEntity}。一致しない時はnull。
     */
    <T extends DomainEntity> T get(final Class<T> clazz, final Serializable id);

    /**
     * プライマリキーに一致する{@link DomainEntity}を返します。
     * 
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id    プライマリキー
     * @return プライマリキーに一致した{@link DomainEntity}。一致しない時は例外。
     */
    <T extends DomainEntity> T load(final Class<T> clazz, final Serializable id);

    /**
     * プライマリキーに一致する{@link DomainEntity}が存在するか返します。
     * 
     * @param <T>   確認型
     * @param clazz 対象クラス
     * @param id    プライマリキー
     * @return 存在する時はtrue
     */
    <T extends DomainEntity> boolean exists(final Class<T> clazz, final Serializable id);

    /**
     * 管理する{@link DomainEntity}を1件返します。
     * 
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @return {@link DomainEntity}
     */
    <T extends DomainEntity> T getOne(final Class<T> clazz);

    /**
     * 管理する{@link DomainEntity}を全件返します。
     * 条件検索などは#templateを利用して実行するようにしてください。
     * 
     * @param <T>   戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @return {@link DomainEntity}一覧
     */
    <T extends DomainEntity> List<T> findAll(final Class<T> clazz);

    /**
     * {@link DomainEntity}を新規追加します。
     * 
     * @param <T>    戻り値の型
     * @param entity 追加対象{@link DomainEntity}
     * @return 追加した{@link DomainEntity}のプライマリキー
     */
    <T extends DomainEntity> T save(final T entity);

    /**
     * {@link DomainEntity}を新規追加または更新します。
     * <p>
     * 既に同一のプライマリキーが存在するときは更新。
     * 存在しない時は新規追加となります。
     * 
     * @param entity 追加対象{@link DomainEntity}
     */
    <T extends DomainEntity> T saveOrUpdate(final T entity);

    /**
     * {@link DomainEntity}を更新します。
     * 
     * @param entity 更新対象{@link DomainEntity}
     */
    <T extends DomainEntity> T update(final T entity);

    /**
     * {@link DomainEntity}を削除します。
     * 
     * @param entity 削除対象{@link DomainEntity}
     */
    <T extends DomainEntity> T delete(final T entity);

}
