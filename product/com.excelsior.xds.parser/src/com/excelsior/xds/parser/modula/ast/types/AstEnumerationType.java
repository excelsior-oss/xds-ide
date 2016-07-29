package com.excelsior.xds.parser.modula.ast.types;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.type.IEnumTypeSymbol;

public class AstEnumerationType extends AstTypeDef<IEnumTypeSymbol> 
{
    public AstEnumerationType(ModulaCompositeType<AstEnumerationType> elementType) {
        super(null, elementType);
    }

    public List<AstEnumElement> getAstEnumElements() {
        return findChildren(ModulaElementTypes.ENUM_ELEMENT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstEnumElements());
        }
    }
    
}
