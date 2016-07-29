package com.excelsior.xds.parser.modula.ast.types;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstRecordVariantList extends ModulaAstNode
{
    public AstRecordVariantList(ModulaCompositeType<AstRecordVariantList> elementType) {
        super(null, elementType);
    }

    public List<AstRecordVariant> getAstRecordVariants() {
        return findChildren(ModulaElementTypes.RECORD_VARIANT);
    }
    
    public AstRecordVariantElsePart getAstRecordVariantElsePart() {
        return findFirstChild( ModulaElementTypes.RECORD_VARIANT_ELSE_PART
                             , ModulaElementTypes.RECORD_VARIANT_ELSE_PART.getNodeClass() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstRecordVariants());
            acceptChild(visitor, getAstRecordVariantElsePart());
        }
    }

}
