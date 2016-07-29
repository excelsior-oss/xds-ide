package com.excelsior.xds.parser.commons.ast;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.utils.IPredicate;
import com.excelsior.xds.core.utils.JavaUtils;
import com.excelsior.xds.core.utils.collections.ArrayListStack;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;

/**
 * Default implementation of the Abstract Syntax Tree (AST) builder.
 */
public class AstBuilder implements IAstBuilder
{
    private final ArrayListStack<PstCompositeNode> stack = new ArrayListStack<PstCompositeNode>();
    private final IPredicate<PstNode> whiteSpacePredicate;
    private final IPredicate<PstNode> isTrailingWhiteSpaceAllowed;
    
    private PstCompositeNode production;
    private PstLeafNode      lastToken;

    /** Handler of parsing errors and warnings. */
    protected final IParserEventListener reporter;
    
    /** The source file to be parsed. */
    protected final IFileStore sourceFile;
    
    
    public AstBuilder(IFileStore sourceFile, IParserEventListener reporter) {
        this(sourceFile, reporter, WHITE_SPACE_PREDICATE, ALWAYS_TRUE_PREDICATE);
    }

    public AstBuilder( IFileStore sourceFile
                     , IParserEventListener reporter
                     , IPredicate<PstNode> whiteSpacePredicate
                     , IPredicate<PstNode> isTrailingWhiteSpaceAllowed ) 
    {
        this.sourceFile = sourceFile;
        this.reporter   = reporter;
        this.whiteSpacePredicate = whiteSpacePredicate;
        this.isTrailingWhiteSpaceAllowed = isTrailingWhiteSpaceAllowed;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        production = null;
        lastToken  = null;
        stack.clear();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode getAstRoot() {
        if (stack.isEmpty()) {
            return production; 
        }
        else {
            return stack.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode getCurrentProduction() {
        return production;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getLastNode() {
        if (lastToken != null) {
            return lastToken;
        }
        else if (production.getChildren().isEmpty()) {
            return production;
        }
        return production.getLastChild();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCurrentProduction(ElementType elementType) {
        return (production != null)
            && (production.getElementType() == elementType);    
    }
    
    
    /**
     * Setups the given node as the current production. The previous current 
     * production is pushed on the stack. The second argument is assigned as 
     * a parent of the given node. 
     * All subsequent nodes will be added as children of the given node.
     * 
     * @param node - the production to be setup as a current one 
     * @param parent - the node to be set as a parent of the given node 
     * 
     * @return AST node of the created production
     */
    protected <T extends PstCompositeNode> T beginProduction
        (T node, PstCompositeNode parent) 
    {
        if (production != null) {
            production.addChild(node);
            stack.push(production);
        }
        node.setParent(parent);
        production = node;
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T beginProduction(T node) {
        return beginProduction(node, production);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode beginProduction(ElementType elementType) {
        PstCompositeNode node = new PstCompositeNode(elementType);
        return beginProduction(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T beginProduction(CompositeType<T> elementType) {
        T node = elementType.createNode();
        beginProduction(node);
        return node;
    }
        

    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode endProduction(IElementType elementType) {
        if (elementType == null) {
            logIncorrectEndProductionError(production, elementType);
            return null;
        }
        else if ((production instanceof AstNode) && (production.getElementType() != elementType)) {
            logIncorrectEndProductionError(production, elementType);
            return null;
        }
        
        PstCompositeNode finalizedProduction = production;
        if (! stack.isEmpty()) {
            PstCompositeNode parent = stack.pop();
            
            List<PstNode> removedChildren = Collections.emptyList();
            if (!isTrailingWhiteSpaceAllowed.evaluate(production)) {
                removedChildren = production.removeLastChildren(whiteSpacePredicate);
            }
            if (production.getChildren().isEmpty()) {
                parent.removeLastChild();
            }
            parent.addChildren(removedChildren);
                
            production = parent;
        }
        return finalizedProduction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode endProduction(PstCompositeNode node) {
        return endProduction(node.getElementType());
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T endParentProduction(CompositeType<T> elementType) {
        if (stack.size() < 2) {
            logIncorrectEndParentProductionError(production, elementType);
            return null;
        }

        PstCompositeNode currentProduction = production;
        PstCompositeNode parentProduction = stack.peek();
        if (parentProduction.getElementType() != elementType) {
            logIncorrectEndProductionError(production, elementType);
            return null;
        }
        
        production = stack.pop();
        production.removeLastChild();
        parentProduction = endProduction(elementType);
        beginProduction(currentProduction);
        
        return JavaUtils.as(elementType.getNodeClass(), parentProduction);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode changeProduction(PstCompositeNode node) {
        for (PstNode child : production.getChildren()) {
            node.addChild(child);
        }

        if (!stack.isEmpty()) {
            PstCompositeNode parent = stack.peek();
            parent.removeLastChild();
            parent.addChild(node);
        }
                
        production = node;
        return node;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T changeProduction(CompositeType<T> elementType) {
        T node = elementType.createNode();
        changeProduction(node);
        return node;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dropProduction(IElementType elementType) {
        if (production.getElementType() != elementType) {
            logIncorrectDropProductionError(production, elementType);
//            throw new IllegalArgumentException(String.format(
//                "Unexpected element type %s: one should drop only the current production. Expected element type %s.",   //$NON-NLS-1$ 
//                elementType, production.getElementType())
//            );
        }

        PstCompositeNode parent = stack.pop();
        parent.removeLastChild();
        parent.addChildren(production.getChildren());
        
        production = parent;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dropProduction(PstCompositeNode node) {
        dropProduction(node.getElementType());
    }

        
    /**
     * {@inheritDoc}
     */
    @Override
    public void addToken(TokenType token, int offset, int length) {
        if (lastToken != null) {
            production.addChild(lastToken);
        }
        lastToken = new PstLeafNode(null, token, offset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptLastToken() {
        if (lastToken != null) {
            production.addChild(lastToken);
            lastToken = null;
        }
    }

    
    /**
     * Log the incorrect end of the parent production error with specified message.
     * 
     * @param elementType - the element type of the dropped production
     * @param injuredProduction, a production with incorrect termination.
     */
    private void logIncorrectEndParentProductionError( PstCompositeNode injuredProduction
                                                     , IElementType elementType ) 
    {
        logIncorrectProductionError(
            "Incorrect end of the parent of the AST production '%s' at %d by the '%s'.\nThere is no parent.",    //$NON-NLS-1$
            injuredProduction, elementType
        );
    }

    /**
     * Log the incorrect end production error with specified message.
     * 
     * @param elementType - the element type of the dropped production
     * @param injuredProduction, a parent production with incorrect termination.
     */
    private void logIncorrectEndProductionError( PstCompositeNode injuredProduction
                                               , IElementType elementType ) 
    {
        logIncorrectProductionError(
            "Incorrect end of the AST production %s at %d by the '%s'.",    //$NON-NLS-1$
            injuredProduction, elementType
        );
    }

    
    /**
     * Log the incorrect drop production error with specified message.
     * 
     * @param injuredProduction, a production with incorrect termination.
     * @param elementType - the element type of the dropped production
     */
    private void logIncorrectDropProductionError( PstCompositeNode injuredProduction
                                                , IElementType elementType ) 
    {
        logIncorrectProductionError(
            "Incorrect drop of the AST production '%s' at %d by the '%s'.",    //$NON-NLS-1$
            injuredProduction, elementType
        );
    }
    
    /**
     * Log the incorrect drop production error with specified message.
     * 
     * @param injuredProduction, a production with incorrect termination.
     * @param elementType - the element type of the dropped production
     * @param message, a human-readable message.
     */
    private void logIncorrectProductionError( String message
                                            , PstCompositeNode injuredProduction
                                            , IElementType elementType )
    {
        int offset = -1;
        String productionName = "";
        if (production != null) {
            offset = production.getOffset();
            productionName = production.toString();
        }
        String elementTypeName = "";
        if (elementType != null) {
            elementTypeName = elementType.toString(); 
        }
        message = String.format(message, productionName, offset, elementTypeName);
        reporter.logInternalError(sourceFile, message);
    }
    
    
    static protected IPredicate<PstNode> WHITE_SPACE_PREDICATE = new IPredicate<PstNode>() 
    {
        /** {@inheritDoc} */
        @Override
        public boolean evaluate(PstNode node) {
            return TokenTypes.WHITE_SPACE == node.getElementType();
        }
    };  
    
    static protected IPredicate<PstNode> ALWAYS_TRUE_PREDICATE = new IPredicate<PstNode>() 
    {
        /** {@inheritDoc} */
        @Override
        public boolean evaluate(PstNode node) {
            return true;
        }
    };
    
}
