package com.excelsior.xds.parser.modula.ast.types;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstRecordVariantLabelList extends ModulaAstNode
{
    public AstRecordVariantLabelList(ModulaCompositeType<AstRecordVariantLabelList> elementType) {
        super(null, elementType);
    }

    public List<AstRecordVariantLabel> getAstRecordVariantLabes() {
        return findChildren( ModulaElementTypes.RECORD_VARIANT_LABEL
                           , ModulaElementTypes.RECORD_VARIANT_LABEL.getNodeClass() );
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstRecordVariantLabes());
        }
    }
    
}
