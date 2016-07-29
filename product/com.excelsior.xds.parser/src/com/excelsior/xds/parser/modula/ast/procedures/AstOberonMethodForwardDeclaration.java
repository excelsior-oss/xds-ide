package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodDefinitionSymbol;

public class AstOberonMethodForwardDeclaration extends AstOberonMethod<IOberonMethodDefinitionSymbol> 
{
    public AstOberonMethodForwardDeclaration(ModulaCompositeType<AstOberonMethodForwardDeclaration> elementType) {
        super(null, elementType);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor);
        }
    }
    
}
