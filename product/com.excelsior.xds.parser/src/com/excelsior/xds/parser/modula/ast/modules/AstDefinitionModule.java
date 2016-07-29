package com.excelsior.xds.parser.modula.ast.modules;

import com.excelsior.xds.parser.modula.ast.AstDefinitions;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstDefinitionModule extends AstModule {

    public AstDefinitionModule(ModulaCompositeType<AstDefinitionModule> elementType) {
        super(elementType);
    }

    public AstDefinitions getAstDefinitions() {
        return findFirstChild( ModulaElementTypes.DEFINITIONS
                             , ModulaElementTypes.DEFINITIONS.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void acceptChildren(ModulaAstVisitor visitor) {
        super.acceptChildren(visitor);
        acceptChild(visitor, getAstDefinitions());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor);
        }
    }
    
}
