package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IEnumElementSymbol;

public class AstEnumElement extends AstSymbolDef<IEnumElementSymbol> implements IAstNodeWithIdentifier {

    public AstEnumElement(ModulaCompositeType<AstEnumElement> elementType) {
        super(null, elementType);
    }

    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.IDENTIFIER, PstNode.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        visitor.visit(this);
    }

}
