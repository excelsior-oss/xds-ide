package com.excelsior.xds.core.utils.collections;

import java.util.ArrayList;

/**
 * The <code>Stack</code> class represents a last-in-first-out
 * (LIFO) stack of objects. It extends class <tt>ArrayList</tt>.
 * 
 * When a stack is first created, it contains no items.
 */
public class ArrayListStack<T> extends ArrayList<T> {
    
    /**
     * Constructs an empty stack with an initial capacity of ten.
     */
    public ArrayListStack() {
        this(10);
    }

    /**
     * Constructs an empty stack with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list
     */
    public ArrayListStack (int initialCapacity) {
        super(initialCapacity);
    }

    private static final long serialVersionUID = 236346862596086763L;

    public void push(T o) {
        add(o);
    }

    public T pop() {
        return remove(size() - 1);
    }

    public boolean empty() {
        return size() == 0;
    }

    public T peek() {
        return get(size() - 1);
    }
    
}