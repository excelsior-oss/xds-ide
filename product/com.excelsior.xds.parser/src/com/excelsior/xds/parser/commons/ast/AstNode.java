package com.excelsior.xds.parser.commons.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.utils.IClosure;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;

/**
 * A node in the AST tree. 
 */
public abstract class AstNode extends PstCompositeNode 
{
    protected AstNode(PstCompositeNode parent, ElementType elementType) {
        super(parent, elementType);
    }

    /**
     * Accepts the given visitor on a visit of the current node.
     *
     * @param visitor the visitor object
     * @exception IllegalArgumentException if the visitor is null
     */
    public final void accept(AstVisitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException();
        }
        // begin with the generic pre-visit
        if (visitor.preVisit(this)) {
            // dynamic dispatch to internal method for type-specific visit/endVisit
            doAccept(visitor);
        }
        // end with the generic post-visit
        visitor.postVisit(this);
    }
    
    protected abstract void doAccept(AstVisitor visitor);
    
    
    /**
     * Accepts the given visitor on a visit of the current node.
     * <p>
     * This method should be used by the concrete implementations of
     * <code>accept0</code> to traverse optional properties. Equivalent
     * to <code>child.accept(visitor)</code> if <code>child</code>
     * is not <code>null</code>.
     * </p>
     *
     * @param visitor the visitor object
     * @param child the child AST node to dispatch too, or <code>null</code>
     *    if none
     */
    protected final void acceptChild(AstVisitor visitor, AstNode child) {
        if (child != null) {
            child.accept(visitor);
        }
    }
    
    /**
     * Accepts the given visitor on a visit of the given live list of
     * child nodes.
     * <p>
     * This method must be used by the concrete implementations of
     * <code>accept</code> to traverse list-values properties; it
     * encapsulates the proper handling of on-the-fly changes to the list.
     * </p>
     *
     * @param visitor the visitor object
     * @param children the child AST node to dispatch too, or <code>null</code>
     *    if none
     */
    protected final void acceptChildren( AstVisitor visitor
                                       , List<? extends AstNode> children ) 
    {
        if (children != null) {
            Iterator<? extends AstNode> cursor = children.iterator();
            while (cursor.hasNext()) {
                AstNode child = (AstNode) cursor.next();
                child.accept(visitor);
            }
        }
    }
    
    protected static <T extends AstNode> List<T> findAstChildren( PstCompositeNode pstCompositeNode
                                                                , Class<T> filterClass ) 
    {
        final List<T> astNodes = new ArrayList<T>();
        
        class CollectAstNodes implements IClosure<T> {
            @Override
            public void execute(T node) {
                astNodes.add(node);
            }
            
        }
        forEachChild(pstCompositeNode, filterClass, new CollectAstNodes());
        
        return astNodes;
    }
    
    protected static <T extends AstNode> void visitAstChidren( PstCompositeNode pstCompositeNode
                                                             , Class<T> filterClass
                                                             , final AstVisitor visitor ) 
    {
    	forEachChild(pstCompositeNode, filterClass, new IClosure<T>() {
            @Override
            public void execute(T node) {
                node.accept(visitor);
            }
        });
    }

    /**
     * Bridge method to visit AstNode children of the PstNode using AstVisitor.
     * TODO : remove when (and if) no more dumb PstCompositeNode`s left, used only as container for some AstNodes.
     * @param pstCompositeNode
     * @param filterClass
     * @return
     */
	protected static <T extends AstNode> void forEachChild(
			PstCompositeNode pstCompositeNode, Class<T> filterClass,
			IClosure<T> operation)
    {
        if (pstCompositeNode != null) {
            List<PstNode> pstChildren = pstCompositeNode.getChildren();
            for (PstNode pstNode : pstChildren) {
                if (filterClass.isAssignableFrom(pstNode.getClass())) {
                    @SuppressWarnings("unchecked")
                    T astNode = (T)pstNode;
                    try {
                        operation.execute(astNode);
                    } catch (Exception e) {
                        //Should never get here
                        LogHelper.logError(e);
                    }
                }
            }
        }
    }

    
    @SuppressWarnings("unchecked")
    protected static <T extends AstNode> T findAstFirtsChild( PstCompositeNode pstCompositeNode
                                                            , Class<T> filterClass ) 
    {
        if (pstCompositeNode != null) {
            List<PstNode> pstChildren = pstCompositeNode.getChildren();
            for (PstNode pstNode : pstChildren) {
                if (filterClass.isAssignableFrom(pstNode.getClass())) {
                    return (T)pstNode;
                }
            }
        }
        return null;
    }
    
}
