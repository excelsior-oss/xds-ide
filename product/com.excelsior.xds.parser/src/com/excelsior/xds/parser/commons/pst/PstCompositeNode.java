package com.excelsior.xds.parser.commons.pst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.excelsior.xds.core.utils.IPredicate;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.excelsior.xds.core.utils.collections.ISearchDirector;
import com.excelsior.xds.parser.commons.ast.CompositeType;
import com.excelsior.xds.parser.commons.ast.IElementType;

/**
 * A composite node in the Program Structure Tree. 
 * This nodes match multiple-tokens fragments.
 */
public class PstCompositeNode extends PstNode 
{
    private final List<PstNode> children;
        
    public PstCompositeNode(PstCompositeNode parent, IElementType elementType) {
        super(parent, elementType);
        this.children = new ArrayList<PstNode>();
    }

    public PstCompositeNode(IElementType elementType) {
        this(null, elementType);
    }
    
    /**
     * Trims the capacity of child elements' list to be the current size.
     * An application can use this operation to minimize the storage 
     * of a <tt>PstCompositeNode</tt> instance.
     */
    public void trim() {
        ((ArrayList<PstNode>) children).trimToSize();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getOffset() {
        if (!children.isEmpty()) {
            return children.get(0).getOffset();
        }
        return UNKNOW_OFFSET;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength() {
        int length = 0;
        if (!children.isEmpty()) {
            PstNode firstChild = children.get(0);
            PstNode lastChild  = children.get(children.size()-1);
            length = lastChild.getOffset() - firstChild.getOffset() + lastChild.getLength();  
        }
        return length;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getPstNodeAt(final int offset) {
        PstNode node = findChildAtOffset(children, offset);
        if (node != null) {
            node = node.getPstNodeAt(offset); 
        }
        return node;
    }

	private static PstNode findChildAtOffset(List<PstNode> children, final int offset) {
		PstNode node = CollectionsUtils.binarySearch(children, new ISearchDirector<PstNode>() {
			@Override
			public int direct(PstNode key) {
				if (offset < key.getOffset()) {
	                return -1;
	            }
				else if (offset > key.getOffset() + key.getLength() - 1) {
					return 1;
				}
				else { // offset between [key.getOffset(),
					// key.getOffset() + key.getLength() - 1] -
					// i.e. inside region covered by key
					return 0;
				}
			}
		});
		return node;
	}
    
    
    /**
     * Appends the specified element to the end of the children list of this node.
     * 
     * @param child element to be appended to this list
     */
    public void addChild(PstNode child) {
        child.setParent(this);
        children.add(child);
    }
    
    /**
     * Appends all of the elements in the specified collection to the end of
     * the child list of this node, in the order that they are returned by the 
     * specified collection's iterator.
     *   
     * @param children collection containing elements to be added to the child list of this node.
     */
    public void addChildren(Collection<PstNode> children) {
        if (children != null) {
            for (PstNode child : children) {
                addChild(child);
            }
        }
    }
    
    
    /**
     * Inserts the specified element at the specified position in the child list
     * of this node. Shifts the element currently at that position (if any) and 
     * any subsequent elements to the right (adds one to their indices).
     * 
     * @param index index at which the specified element is to be inserted
     * @param child element to be inserted
     */
    public void insertChild(int index, PstNode child) {
        child.setParent(this);
        children.add(index, child);
    }
    
    /**
     * Inserts all of the elements in the specified collection into the child 
     * list of this node at the specified position.  Shifts the element currently 
     * at that position (if any) and any subsequent elements to the right 
     * (increases their indices).  The new elements will appear in this list in 
     * the order that they are returned by the specified collection's iterator.
     * 
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param children collection containing elements to be added to the child list of this node.
     */
    public void insertChildren(int index, Collection<PstNode> children) {
        if (children != null) {
            for (PstNode child : children) {
                insertChild(index, child);
            }
        }
    }

    
    public List<PstNode> getChildren() {
        return children;
    }
    
    public PstNode getLastChild() {
        PstNode lastChild = null;
        int size = children.size();
        if (size > 0) {
            lastChild = children.get(size - 1);
        }
        return lastChild;
    }
    
    /**
     * Removes the element at the specified position in the child list of this node.
     * Shifts any subsequent elements to the left (subtracts one from their indices). 
     * Returns the element that was removed from the list.
     * 
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     */
    public PstNode removeChild(int index) {
        return children.remove(index);
    }
    
    public PstNode removeLastChild() {
        if (children.isEmpty()) { 
            return null;
        }
        else {
            PstNode child = children.remove(children.size() - 1);
            return child;
        }
    }
    
    public List<PstNode> removeLastChildren(IPredicate<PstNode> predicate) {
    	List<PstNode> removedChildren = new ArrayList<PstNode>();
    	for (int i = children.size() - 1; i > -1; --i) {
    		PstNode child = children.get(i);
    		if (predicate.evaluate(child)) {
    			removedChildren.add(removeLastChild());
    		}
    		else {
    		    break;
    		}
		}
    	Collections.reverse(removedChildren);
    	return removedChildren;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(PstVisitor visitor) {
        if (visitor.visit(this)) {
            for (PstNode child : children) {
                child.accept(visitor);
            }
        }
        visitor.endVisit(this);
    }
    
    
    @SuppressWarnings("unchecked")
    public <T extends PstNode> T findFirstChild(IElementType elementType, Class<T> selector) {
        PstNode node = null;
        for (PstNode child : children) {
            if (child.getElementType().equals(elementType)) {
                node = child;
                break;
            }
        }
        return (T)node;
    }

    @SuppressWarnings("unchecked")
    public <T extends PstNode> T findLastChild(IElementType elementType, Class<T> selector) {
        PstNode node = null;
        for (PstNode child : children) {
            if (child.getElementType().equals(elementType)) {
                node = child;
            }
        }
        return (T)node;
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends PstNode> List<T> findChildren(IElementType elementType, Class<T> selector) {
        List<T> nodes = new ArrayList<T>();
        for (PstNode child : children) {
            if (child.getElementType().equals(elementType)) {
                nodes.add((T)child);
            }
        }
        return nodes;
    }
    
    /** This method is very alike to findChildren with single elementType parameter, but we duplicate code 
     * because there are very hot methods
     * @param elementTypes - element types to be matched
     * @param selector - lowest common denominator in the hierarchy of matched children
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T extends PstNode> List<T> findChildren(Set<? extends IElementType> elementTypes, Class<T> selector) {
        List<T> nodes = new ArrayList<T>();
        for (PstNode child : children) {
        	if (elementTypes.contains(child.getElementType())) {
        		nodes.add((T)child);
        	}
        }
        return nodes;
    }
    
    public <T extends PstCompositeNode> List<T> findChildren(CompositeType<T> elementType) {
        return findChildren(elementType, elementType.getNodeClass());
    }
    
}
