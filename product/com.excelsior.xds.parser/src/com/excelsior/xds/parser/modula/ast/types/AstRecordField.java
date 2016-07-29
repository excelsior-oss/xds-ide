package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IRecordFieldSymbol;

public class AstRecordField extends AstSymbolDef<IRecordFieldSymbol> {

    public AstRecordField(ModulaCompositeType<AstRecordField> elementType) {
        super(null, elementType);
    }
    
    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.IDENTIFIER, PstNode.class);
    }

    public AstTypeElement getAstTypeElement() {
        AstTypeElement typeElementAst = null;
        PstNode node = getParent();
        if (node instanceof AstRecordFieldList) {
            typeElementAst = ((AstRecordFieldList)node).getAstTypeElement();
        }
        return typeElementAst;
    }
    
    public void visitTypeElement(ModulaAstVisitor visitor) {
        AstTypeElement typeElementAst = getAstTypeElement();
        if (typeElementAst != null) {
            typeElementAst.accept(visitor);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        visitor.visit(this);
    }

}
