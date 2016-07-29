package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

/**
 * RecordVariant = RecordVariantLabelList ":" FieldListSequence <br>
 */
public class AstRecordVariant extends ModulaAstNode
{
    public AstRecordVariant(ModulaCompositeType<AstRecordVariant> elementType) {
        super(null, elementType);
    }

    public AstRecordVariantLabelList getAstRecordVariantLabelList() {
        return findFirstChild( ModulaElementTypes.RECORD_VARIANT_LABEL_LIST
                             , ModulaElementTypes.RECORD_VARIANT_LABEL_LIST.getNodeClass() );
    }
    
    public AstRecordFieldBlockList getAstRecordFieldBlockList() {
        return findFirstChild( ModulaElementTypes.RECORD_FIELD_BLOCK_LIST
                             , ModulaElementTypes.RECORD_FIELD_BLOCK_LIST.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getAstRecordVariantLabelList());
            acceptChild(visitor, getAstRecordFieldBlockList());
        }
    }

}
