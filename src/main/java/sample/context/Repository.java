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
     * プライマリキーに一致する{@link Entity}を返します。
     * @param <T> 戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id プライマリキー
     * @return プライマリキーに一致した{@link Entity}。一致しない時はnull。
     */
    <T extends Entity> T get(final Class<T> clazz, final Serializable id);

    /**
     * プライマリキーに一致する{@link Entity}を返します。
     * @param <T> 戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @param id プライマリキー
     * @return プライマリキーに一致した{@link Entity}。一致しない時は例外。
     */
    <T extends Entity> T load(final Class<T> clazz, final Serializable id);

    /**
     * プライマリキーに一致する{@link Entity}が存在するか返します。
     * @param <T> 確認型
     * @param clazz 対象クラス
     * @param id プライマリキー
     * @return 存在する時はtrue
     */
    <T extends Entity> boolean exists(final Class<T> clazz, final Serializable id);

    /**
     * 管理する{@link Entity}を1件返します。
     * @param <T> 戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @return {@link Entity}
     */
    <T extends Entity> T getOne(final Class<T> clazz);

    /**
     * 管理する{@link Entity}を全件返します。
     * 条件検索などは#templateを利用して実行するようにしてください。
     * @param <T> 戻り値の型
     * @param clazz 取得するインスタンスのクラス
     * @return {@link Entity}一覧
     */
    <T extends Entity> List<T> findAll(final Class<T> clazz);

    /**
     * {@link Entity}を新規追加します。
     * @param <PK> 戻り値の型
     * @param entity 追加対象{@link Entity}
     * @return 追加した{@link Entity}のプライマリキー
     */
    <T extends Entity> T save(final T entity);

    /**
     * {@link Entity}を新規追加または更新します。
     * <p>既に同一のプライマリキーが存在するときは更新。
     * 存在しない時は新規追加となります。
     * @param entity 追加対象{@link Entity}
     */
    <T extends Entity> T saveOrUpdate(final T entity);

    /**
     * {@link Entity}を更新します。
     * @param entity 更新対象{@link Entity}
     */
    <T extends Entity> T update(final T entity);

    /**
     * {@link Entity}を削除します。
     * @param entity 削除対象{@link Entity}
     */
    <T extends Entity> T delete(final T entity);

}
