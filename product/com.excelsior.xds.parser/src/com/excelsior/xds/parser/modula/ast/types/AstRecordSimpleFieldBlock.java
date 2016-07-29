package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstRecordSimpleFieldBlock extends AstRecordFieldBlock 
{
    public AstRecordSimpleFieldBlock(ModulaCompositeType<AstRecordSimpleFieldBlock> elementType) {
        super(null, elementType);
    }

    public AstRecordFieldList getAstRecordFieldList() {
        return findFirstChild( ModulaElementTypes.RECORD_FIELD_LIST
                             , ModulaElementTypes.RECORD_FIELD_LIST.getNodeClass() );
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
            acceptChild(visitor, getAstRecordFieldList());
            acceptChild(visitor, getAstTypeElement());
        }
    }
    
}
