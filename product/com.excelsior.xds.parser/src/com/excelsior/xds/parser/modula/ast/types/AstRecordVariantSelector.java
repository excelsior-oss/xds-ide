package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstQualifiedName;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IRecordVariantSelectorSymbol;

public class AstRecordVariantSelector extends    AstSymbolDef<IRecordVariantSelectorSymbol> 
                                      implements IAstNodeWithIdentifier  
{
    public AstRecordVariantSelector(ModulaCompositeType<AstRecordVariantSelector> elementType) {
        super(null, elementType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.IDENTIFIER, PstNode.class);
    }
    
    public AstQualifiedName getAstQualident() {
        return findFirstChild( ModulaElementTypes.QUALIFIED_NAME
                             , ModulaElementTypes.QUALIFIED_NAME.getNodeClass() );
    }
    
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        visitor.visit(this);
    }

}
