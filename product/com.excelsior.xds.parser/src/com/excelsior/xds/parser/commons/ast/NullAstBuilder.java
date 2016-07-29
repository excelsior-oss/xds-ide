package com.excelsior.xds.parser.commons.ast;

import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstNode;


/**
 * The Abstract Syntax Tree (AST) builder which omits AST building.
 */
public class NullAstBuilder implements IAstBuilder
{
    /**
     * Thread-safe singleton support.
     */
    public static NullAstBuilder getInstance() {
        return NullAstBuilderHolder.INSTANCE;
    }

    
    private static class NullAstBuilderHolder {
        static NullAstBuilder INSTANCE = new NullAstBuilder();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode getAstRoot() {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode getCurrentProduction() {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getLastNode() {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCurrentProduction(ElementType elementType) {
        throw new UnsupportedOperationException("NullAstBuilder cannot check current production");  //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T beginProduction(T node) {
       return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode beginProduction(ElementType elementType) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T beginProduction(CompositeType<T> elementType) {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode endProduction(PstCompositeNode node) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode endProduction(IElementType elementType) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T endParentProduction(CompositeType<T> elementType) {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PstCompositeNode changeProduction(PstCompositeNode node) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PstCompositeNode> T changeProduction(CompositeType<T> elementType) {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void dropProduction(PstCompositeNode node) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dropProduction(IElementType elementType) {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addToken(TokenType token, int offset, int length) {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptLastToken() {
    }
    
}
