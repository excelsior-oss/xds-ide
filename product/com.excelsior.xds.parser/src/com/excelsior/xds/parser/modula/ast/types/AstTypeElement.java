package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class AstTypeElement extends ModulaAstNode 
{
    public AstTypeElement(ModulaCompositeType<AstTypeElement> elementType) {
        super(null, elementType);
    }

    @SuppressWarnings("unchecked")
    public AstTypeDef<? extends ITypeSymbol> getTypeDefinition() {
        return findAstFirtsChild(this, AstTypeDef.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChild(visitor, getTypeDefinition());
        }
    }
    
}
