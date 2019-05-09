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
/**
 * Repository base implementation of JPA.
 * <p>This component provides simple JPA implementation in form not to use a base of Spring Data
 *  to realize 1-n relations of Repository and Entity.
 * <p>Repository made in succession to JpaRepository becomes the data source unit.
 */
@Setter
public abstract class JpaRepository implements Repository {

    @Autowired
    private ObjectProvider<DomainHelper> dh;

    /**
     * Return EntityManager to manage.
     * <p>Return EntityManager of the data source which you want to manage in succession.
     */
    public abstract EntityManager em();

    public <T extends Entity> JpaCriteria<T> criteria(Class<T> clazz) {
        return new JpaCriteria<T>(clazz, em().getCriteriaBuilder());
    }

    /** {@inheritDoc} */
    @Override
    public DomainHelper dh() {
        return dh.getObject();
    }

    /**
     * Return the simple accessor of the JPA operation.
     * <p>JpaTemplate is created each call.
     */
    public JpaTemplate tmpl() {
        return new JpaTemplate(em());
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T get(Class<T> clazz, Serializable id) {
        return em().find(clazz, id);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T load(Class<T> clazz, Serializable id) {
        return em().getReference(clazz, id);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> boolean exists(Class<T> clazz, Serializable id) {
        return get(clazz, id) != null;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> T getOne(Class<T> clazz) {
        return (T) em().createQuery("from " + clazz.getSimpleName()).getSingleResult();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> findAll(Class<T> clazz) {
        return em().createQuery("from " + clazz.getSimpleName()).getResultList();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T save(T entity) {
        em().persist(entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T saveOrUpdate(T entity) {
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T update(T entity) {
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Entity> T delete(T entity) {
        em().remove(entity);
        return entity;
    }

    /**
     * Perform DB and synchronization of all the entities
     *  which are not perpetuated in a session cache (SQL execution).
     * <p>Please call it at the point that wants to make an SQL execution timing clear.
     * You call #flushAndClear with the case that session cash is tight by batch processing in memory regularly,
     *  and please prevent enlargement of the session cash.
     */
    public JpaRepository flush() {
        em().flush();
        return this;
    }

    /**
     * Initialize session cash after having synchronized the entities 
     * which is not perpetuated in a session cache with DB.
     * <p>Session cash maintained implicitly is tight by the batch processing that mass update produces
     *  in memory and often causes a big problem and is seen.
     * You call this processing regularly, and please maintain size of the session cash in fixed-quantity.
     */
    public JpaRepository flushAndClear() {
        em().flush();
        em().clear();
        return this;
    }

    /** Repository of the standard schema. */
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
