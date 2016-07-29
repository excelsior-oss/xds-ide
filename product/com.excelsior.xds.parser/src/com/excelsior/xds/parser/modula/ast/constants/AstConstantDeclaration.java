package com.excelsior.xds.parser.modula.ast.constants;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstDecoratedIdentifier;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.IConstantSymbol;

public class AstConstantDeclaration extends    AstSymbolDef<IConstantSymbol> 
                                    implements IAstNodeWithIdentifier 
{
    public AstConstantDeclaration(ModulaCompositeType<AstConstantDeclaration> elementType) {
        super(null, elementType);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getIdentifier() {
        AstDecoratedIdentifier identifierAst = findFirstChild( ModulaElementTypes.DECORATED_IDENTIFIER
                                                             , ModulaElementTypes.DECORATED_IDENTIFIER.getNodeClass()
                                                             );
        PstNode identifier = null;
        if (identifierAst != null) {
            identifier = identifierAst.getIdentifier();
        }
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
    	visitor.visit(this);
    }
    
}
