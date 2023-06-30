package sample.context;

import java.io.Serializable;
import java.util.List;

/**
 * It is general-purpose Repository which does not depend on the specific domain
 * object.
 * <p>
 * You can use it as Repository where a type is not safe.
 */
public interface Repository {

    /**
     * Return a helper utility to provide the access to an infrastructure layer
     * component in the domain layer.
     */
    DomainHelper dh();

    <T extends DomainEntity> T get(final Class<T> clazz, final Serializable id);

    <T extends DomainEntity> T load(final Class<T> clazz, final Serializable id);

    <T extends DomainEntity> boolean exists(final Class<T> clazz, final Serializable id);

    <T extends DomainEntity> T getOne(final Class<T> clazz);

    <T extends DomainEntity> List<T> findAll(final Class<T> clazz);

    <T extends DomainEntity> T save(final T entity);

    <T extends DomainEntity> T saveOrUpdate(final T entity);

    <T extends DomainEntity> T update(final T entity);

    <T extends DomainEntity> T delete(final T entity);

}
