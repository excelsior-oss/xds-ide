package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.type.IPointerTypeSymbol;

public class AstPointerType extends AstTypeDef<IPointerTypeSymbol> 
{
    public AstPointerType(ModulaCompositeType<AstPointerType> elementType) {
        super(null, elementType);
    }

    public AstTypeElement getAstTypeElement() {
        return findFirstChild( ModulaElementTypes.TYPE_ELEMENT
                             , ModulaElementTypes.TYPE_ELEMENT.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChild(visitor, getAstTypeElement());
        }
    }
    
}
