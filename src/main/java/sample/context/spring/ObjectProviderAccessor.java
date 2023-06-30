package sample.context.spring;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * ObjectProvider Fast Access Utility.
 */
@Component
public class ObjectProviderAccessor {

    private final ConcurrentMap<Class<?>, Object> cache = new ConcurrentHashMap<Class<?>, Object>();

    @SuppressWarnings("unchecked")
    public <T> T bean(ObjectProvider<T> target, Class<T> clazz) {
        cache.computeIfAbsent(clazz, k -> target.getObject());
        return (T) cache.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> beanOpt(ObjectProvider<T> target, Class<T> clazz) {
        cache.computeIfAbsent(clazz, k -> target.getIfAvailable());
        return Optional.ofNullable(cache.get(clazz)).map(v -> (T) v);
    }

}
