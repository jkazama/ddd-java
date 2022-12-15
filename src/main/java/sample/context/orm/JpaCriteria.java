package sample.context.orm;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import sample.context.DomainEntity;

/**
 * JPAのCriteriaBuilderラッパー。
 * <p>
 * Criteriaの簡易的な取り扱いを可能にします。
 * low: 必要最低限の処理を割り切りで実装
 * 
 * @author jkazama
 */
@Getter
public class JpaCriteria<T extends DomainEntity> {
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

    /** null一致条件を付与します。 */
    public JpaCriteria<T> isNull(String field) {
        predicates.add(cb.isNull(root.get(field)));
        return this;
    }

    /** 一致条件を付与します。 */
    public JpaCriteria<T> equal(String field, final Object value) {
        if (value != null) {
            predicates.add(cb.equal(root.get(field), value));
        }
        return this;
    }

    /** like条件を付与します。 */
    // low: 本番で利用する際はエスケープポリシーを明確にする必要あり。HibernateのMatchMode的なアプローチが安全
    public JpaCriteria<T> like(String field, final String value) {
        if (value != null) {
            predicates.add(cb.like(root.<String>get(field), value));
        }
        return this;
    }

    /** in条件を付与します。 */
    public JpaCriteria<T> in(String field, final Object[] values) {
        if (values != null && 0 < values.length) {
            predicates.add(root.get(field).in(values));
        }
        return this;
    }

    /** between条件を付与します。 */
    public JpaCriteria<T> between(String field, final Date from, final Date to) {
        if (from != null && to != null) {
            predicates.add(cb.between(root.<Date>get(field), from, to));
        }
        return this;
    }

    /** between条件を付与します。 */
    public JpaCriteria<T> between(String field, final String from, final String to) {
        if (from != null && to != null) {
            predicates.add(cb.between(root.<String>get(field), from, to));
        }
        return this;
    }

    /** 昇順条件を加えます。 */
    public JpaCriteria<T> sort(String field) {
        orders.add(cb.asc(root.get(field)));
        return this;
    }

    /** 降順条件を加えます。 */
    public JpaCriteria<T> sortDesc(String field) {
        orders.add(cb.desc(root.get(field)));
        return this;
    }

    /** 実行クエリを生成して返します。 */
    public CriteriaQuery<T> result() {
        CriteriaQuery<T> q = query.where(predicates.toArray(new Predicate[0]));
        return orders.isEmpty() ? q : q.orderBy(orders.toArray(new Order[0]));
    }
}
