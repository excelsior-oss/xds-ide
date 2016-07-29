package com.excelsior.xds.core.utils.collections;
import java.util.Iterator;

public class BaseTypeIterator<T> implements Iterator<T>
{
    private final Iterator<? extends T> iterator;
    
    public BaseTypeIterator(Iterator<? extends T> iterator) {
        this.iterator = iterator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        return iterator.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        iterator.remove();
    }

}
