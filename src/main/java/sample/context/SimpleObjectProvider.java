package sample.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

import lombok.RequiredArgsConstructor;

/**
 * 保有するオブジェクトを単純に返す ObjectProvider。
 * 
 * @author jkazama
 */
@RequiredArgsConstructor(staticName = "of")
public class SimpleObjectProvider<T> implements ObjectProvider<T> {
    private final T target;

    /** {@inheritDoc} */
    @Override
    public T getObject() throws BeansException {
        return target;
    }

    /** {@inheritDoc} */
    @Override
    public T getObject(Object... args) throws BeansException {
        return target;
    }

    /** {@inheritDoc} */
    @Override
    public T getIfAvailable() throws BeansException {
        return target;
    }

    /** {@inheritDoc} */
    @Override
    public T getIfUnique() throws BeansException {
        return target;
    }

}
