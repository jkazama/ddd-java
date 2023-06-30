package sample.context.orm;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Setter;
import sample.context.DomainEntity;
import sample.context.DomainHelper;
import sample.context.Repository;

/**
 * Repository base implementation of JPA.
 * <p>
 * This component provides simple JPA implementation in form not to use a base
 * of Spring Data
 * to realize 1-n relations of Repository and Entity.
 * <p>
 * Repository made in succession to JpaRepository becomes the data source unit.
 */
@Setter
public abstract class OrmRepository implements Repository {

    @Autowired
    private DomainHelper dh;

    /**
     * Return EntityManager to manage.
     * <p>
     * Return EntityManager of the data source which you want to manage in
     * succession.
     */
    public abstract EntityManager em();

    /** {@inheritDoc} */
    @Override
    public DomainHelper dh() {
        return dh;
    }

    /**
     * Return the simple accessor of the JPA operation.
     * <p>
     * JpaTemplate is created each call.
     */
    public OrmTemplate tmpl() {
        return OrmTemplate.of(em());
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T get(Class<T> clazz, Serializable id) {
        return em().find(clazz, id);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T load(Class<T> clazz, Serializable id) {
        return em().getReference(clazz, id);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> boolean exists(Class<T> clazz, Serializable id) {
        return get(clazz, id) != null;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainEntity> T getOne(Class<T> clazz) {
        return (T) em().createQuery("FROM " + clazz.getSimpleName()).getSingleResult();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainEntity> List<T> findAll(Class<T> clazz) {
        return em().createQuery("FROM " + clazz.getSimpleName()).getResultList();
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T save(T entity) {
        em().persist(entity);
        return entity;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T saveOrUpdate(T entity) {
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T update(T entity) {
        return em().merge(entity);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends DomainEntity> T delete(T entity) {
        em().remove(entity);
        return entity;
    }

    /**
     * Perform DB and synchronization of all the entities
     * which are not perpetuated in a session cache (SQL execution).
     * <p>
     * Please call it at the point that wants to make an SQL execution timing clear.
     * You call #flushAndClear with the case that session cash is tight by batch
     * processing in memory regularly,
     * and please prevent enlargement of the session cash.
     */
    public OrmRepository flush() {
        em().flush();
        return this;
    }

    /**
     * Initialize session cash after having synchronized the entities
     * which is not perpetuated in a session cache with DB.
     * <p>
     * Session cash maintained implicitly is tight by the batch processing that mass
     * update produces
     * in memory and often causes a big problem and is seen.
     * You call this processing regularly, and please maintain size of the session
     * cash in fixed-quantity.
     */
    public OrmRepository flushAndClear() {
        em().flush();
        em().clear();
        return this;
    }

    /** Repository of the standard schema. */
    @org.springframework.stereotype.Repository
    public static class DefaultRepository extends OrmRepository {
        @PersistenceContext
        @Setter
        private EntityManager em;

        @Override
        public EntityManager em() {
            return em;
        }
    }

}
