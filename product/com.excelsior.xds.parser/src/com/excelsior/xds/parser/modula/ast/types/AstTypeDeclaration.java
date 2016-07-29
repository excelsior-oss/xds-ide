package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstDecoratedIdentifier;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.type.ITypeSymbol;

public class AstTypeDeclaration extends AstSymbolDef<ITypeSymbol> 
                                implements IAstNodeWithIdentifier
{
    public AstTypeDeclaration(ModulaCompositeType<AstTypeDeclaration> elementType) {
        super(null, elementType);
    }

    public PstNode getIdentifier() {
        AstDecoratedIdentifier identifierAst = findFirstChild( ModulaElementTypes.DECORATED_IDENTIFIER
                                                             , ModulaElementTypes.DECORATED_IDENTIFIER.getNodeClass() );
        PstNode identifier = null;
        if (identifierAst != null) {
            identifier = identifierAst.getIdentifier();
        }
        return identifier;
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
        if (visitor.visit(this)) {
            acceptChild(visitor, getAstTypeElement());
        }
    }
    
}
