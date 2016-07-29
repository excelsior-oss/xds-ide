package com.excelsior.xds.parser.modula.ast.types;

import java.util.List;

import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstRecordFieldList extends ModulaAstNode
{
    public AstRecordFieldList(ModulaCompositeType<AstRecordFieldList> elementType) {
        super(null, elementType);
    }

    public List<AstRecordField> getAstRecordFields() {
        return findChildren(ModulaElementTypes.RECORD_FIELD);
    }
    
    public AstTypeElement getAstTypeElement() {
        AstTypeElement typeElementAst = null;
        PstNode node = getParent();
        if (node instanceof AstRecordSimpleFieldBlock) {
            typeElementAst = ((AstRecordSimpleFieldBlock)node).getAstTypeElement();
        }
        return typeElementAst;
    }
            
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstRecordFields());
        }
    }

}
