package sample.context.orm;

import java.io.Serializable;

import sample.context.Entity;
import sample.util.Validator;

/**
 * The Entity base class which provides a concept of ActiveRecord on the basis of JPA.
 * <p>Support only simple behavior depending on a state of the own instance here.
 * The concepts such as get/find are included in real ActiveRecord model,
 *  but they are not acts to change a state of the self.
 * Please define them as a class method individually in succession
 *  to deal with an act to identify target instance.
 * <pre>
 * public static Account get(final JpaRepository rep, String id) {
 *     return rep.get(Account.class, id);
 * }
 * 
 * public static Account findAll(final JpaRepository rep) {
 *     return rep.findAll(Account.class);
 * }
 * </pre>
 */
public abstract class JpaActiveRecord<T extends Entity> implements Serializable, Entity {

    private static final long serialVersionUID = 1L;

    protected Validator validator() {
        return new Validator();
    }

    @SuppressWarnings("unchecked")
    public T save(final JpaRepository rep) {
        return (T) rep.save(this);
    }

    @SuppressWarnings("unchecked")
    public T update(final JpaRepository rep) {
        return (T) rep.update(this);
    }

    @SuppressWarnings("unchecked")
    public T delete(final JpaRepository rep) {
        return (T) rep.delete(this);
    }

    @SuppressWarnings("unchecked")
    public T saveOrUpdate(final JpaRepository rep) {
        return (T) rep.saveOrUpdate(this);
    }

}
