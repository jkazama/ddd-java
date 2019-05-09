package sample.context.orm;

import java.util.List;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;

import sample.context.Entity;

/**
 * Simple Accessor for EntityManager of JPA.
 * (you are formed every session, and please use it)
 * <p>If there is the handling of that You want to use by a method EntityManager,
 * add a wrap method as needed.
 */
public class JpaTemplate {

    private final EntityManager em;

    public JpaTemplate(final EntityManager em) {
        this.em = em;
    }

    //low: The paging system is omitted. Use Query#setFirstResult / Query#setMaxResults when implementing
    public <T extends Entity> List<T> find(CriteriaQuery<T> criteria) {
        return em.createQuery(criteria).getResultList();
    }

    //low: The arguments should be Map (named), but use variable arguments because they are samples.
    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> find(String qlName, Object... args) {
        return bind(em.createNamedQuery(qlName), args).getResultList();
    }

    protected Query bind(final Query query, Object... args) {
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return query;
    }

    public int execute(String qlName, Object... args) {
        return bind(em.createNamedQuery(qlName), args).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> findBySql(Class<T> returnClass, String sql, Object... args) {
        return bind(em.createNativeQuery(sql, returnClass), args).getResultList();
    }

    public int executeSql(String sql, Object... args) {
        return bind(em.createNativeQuery(sql), args).executeUpdate();
    }

}
