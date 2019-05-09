package sample.context.orm;

import java.util.*;

import javax.persistence.criteria.*;

import lombok.Getter;
import sample.context.Entity;

/**
 * The CriteriaBuilder wrapper who handles a variable condition of JPA.
 * <p>Enable the simple handling of Criteria.
 * <p>Add the condition phrase to use in Criteria as needed.
 * low: implement the minimum required processing.
 */
@Getter
public class JpaCriteria<T extends Entity> {
    private final CriteriaBuilder cb;
    private final CriteriaQuery<T> query;
    private final Root<T> root;
    private final Set<Predicate> predicates = new LinkedHashSet<>();
    private final Set<Order> orders = new LinkedHashSet<>();

    public JpaCriteria(Class<T> clazz, CriteriaBuilder cb) {
        this.cb = cb;
        this.query = cb.createQuery(clazz);
        this.root = query.from(clazz);
    }

    public JpaCriteria<T> isNull(String field) {
        predicates.add(cb.isNull(root.get(field)));
        return this;
    }

    public JpaCriteria<T> equal(String field, final Object value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(field), value));
        }
        return this;
    }

    public JpaCriteria<T> like(String field, final String value) {
        if (value != null) {
            predicates.add(cb.like(root.<String> get(field), value));
        }
        return this;
    }

    public JpaCriteria<T> in(String field, final Object[] values) {
        if (values != null && 0 < values.length) {
            predicates.add(root.get(field).in(values));
        }
        return this;
    }

    public JpaCriteria<T> between(String field, final Date from, final Date to) {
        if (from != null && to != null) {
            predicates.add(cb.between(root.<Date> get(field), from, to));
        }
        return this;
    }

    public JpaCriteria<T> between(String field, final String from, final String to) {
        if (from != null && to != null) {
            predicates.add(cb.between(root.<String> get(field), from, to));
        }
        return this;
    }

    public JpaCriteria<T> sort(String field) {
        orders.add(cb.asc(root.get(field)));
        return this;
    }

    public JpaCriteria<T> sortDesc(String field) {
        orders.add(cb.desc(root.get(field)));
        return this;
    }

    /**
     * Return built CriteriaQuery..
     * <p>A complicated query and aggregate function are these methods,
     *  and please build returned query for the cause more.
     */
    public CriteriaQuery<T> result() {
        CriteriaQuery<T> q = query.where(predicates.toArray(new Predicate[0]));
        return orders.isEmpty() ? q : q.orderBy(orders.toArray(new Order[0]));
    }
}
