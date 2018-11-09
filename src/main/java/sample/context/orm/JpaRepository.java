package sample.context.orm;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import lombok.Setter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import sample.context.*;
import sample.context.Entity;

/**
 * JPAのRepository基底実装。
 * <p>Springが提供するJpaRepositoryとは役割が異なる点に注意してください。
 * 本コンポーネントはRepositoryとEntityの1-n関係を実現するためにSpringDataの基盤を
 * 利用しない形で単純なJPA実装を提供します。
 * <p>JpaRepositoryを継承して作成されるRepositoryの粒度はデータソース単位となります。
 * 
 * @author jkazama
 */
@Setter
public abstract class JpaRepository implements Repository {

    @Autowired
    private ObjectProvider<DomainHelper> dh;

    /**
     * 管理するEntityManagerを返します。
     * <p>継承先で管理したいデータソースのEntityManagerを返してください。
     */
    public abstract EntityManager em();

    public <T extends Entity> JpaCriteria<T> criteria(Class<T> clazz) {
        return new JpaCriteria<T>(clazz, em().getCriteriaBuilder());
    }

    /* (non-Javadoc)
     * @see sample.context.Repository#dh()
     */
    @Override
    public DomainHelper dh() {
        return dh.getObject();
    }

    /**
     * JPA操作の簡易アクセサを生成します。
     * <p>JpaTemplateは呼出しの都度生成されます。
     */
    public JpaTemplate tmpl() {
        return new JpaTemplate(em());
    }

    @Override
    public <T extends Entity> T get(Class<T> clazz, Serializable id) {
        return em().find(clazz, id);
    }

    @Override
    public <T extends Entity> T load(Class<T> clazz, Serializable id) {
        return em().getReference(clazz, id);
    }

    @Override
    public <T extends Entity> boolean exists(Class<T> clazz, Serializable id) {
        return get(clazz, id) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T getOne(Class<T> clazz) {
        return (T) em().createQuery("from " + clazz.getSimpleName()).getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> findAll(Class<T> clazz) {
        return em().createQuery("from " + clazz.getSimpleName()).getResultList();
    }

    @Override
    public <T extends Entity> T save(T entity) {
        em().persist(entity);
        return entity;
    }

    @Override
    public <T extends Entity> T saveOrUpdate(T entity) {
        return em().merge(entity);
    }

    @Override
    public <T extends Entity> T update(T entity) {
        return em().merge(entity);
    }

    @Override
    public <T extends Entity> T delete(T entity) {
        em().remove(entity);
        return entity;
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティを全てDBと同期(SQL発行)します。
     * <p>SQL発行タイミングを明確にしたい箇所で呼び出すようにしてください。バッチ処理などでセッションキャッシュが
     * メモリを逼迫するケースでは#flushAndClearを定期的に呼び出してセッションキャッシュの肥大化を防ぐようにしてください。
     */
    public JpaRepository flush() {
        em().flush();
        return this;
    }

    /**
     * セッションキャッシュ中の永続化されていないエンティティをDBと同期化した上でセッションキャッシュを初期化します。
     * <p>大量の更新が発生するバッチ処理などでは暗黙的に保持されるセッションキャッシュがメモリを逼迫して
     * 大きな問題を引き起こすケースが多々見られます。定期的に本処理を呼び出してセッションキャッシュの
     * サイズを定量に維持するようにしてください。
     */
    public JpaRepository flushAndClear() {
        em().flush();
        em().clear();
        return this;
    }

    /** 標準スキーマのRepositoryを表現します。 */
    @org.springframework.stereotype.Repository
    @Setter
    public static class DefaultRepository extends JpaRepository {
        @PersistenceContext
        private EntityManager em;

        @Override
        public EntityManager em() {
            return em;
        }
    }

}
