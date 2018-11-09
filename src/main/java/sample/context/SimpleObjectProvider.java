package sample.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 保有するオブジェクトを単純に返す ObjectProvider。
 * 
 * @author jkazama
 */
public class SimpleObjectProvider<T> implements ObjectProvider<T> {

    private final T target;
    
    public SimpleObjectProvider(T target) {
        this.target = target;
    }
    
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
    
    public static <T> SimpleObjectProvider<T> of(T target) {
        return new SimpleObjectProvider<T>(target);
    }
    
}
