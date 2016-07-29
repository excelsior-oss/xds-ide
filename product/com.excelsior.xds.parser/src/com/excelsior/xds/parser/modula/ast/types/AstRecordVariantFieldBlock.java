package com.excelsior.xds.parser.modula.ast.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstRecordVariantFieldBlock extends AstRecordFieldBlock implements IAstFrameNode
{
    private List<PstNode> frame = new ArrayList<PstNode>(4);

    public AstRecordVariantFieldBlock(ModulaCompositeType<AstRecordVariantFieldBlock> elementType) {
        super(null, elementType);
    }

    public AstRecordVariantSelector getAstRecordVariantSelector() {
        return findFirstChild( ModulaElementTypes.RECORD_VARIANT_SELECTOR
                             , ModulaElementTypes.RECORD_VARIANT_SELECTOR.getNodeClass() );
    }
    
    public AstRecordVariantList getAstRecordVariantList() {
        return findFirstChild( ModulaElementTypes.RECORD_VARIANT_LIST
                             , ModulaElementTypes.RECORD_VARIANT_LIST.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getAstRecordVariantSelector());
            acceptChild(visitor, getAstRecordVariantList());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PstNode> getFrameNodes() {
        return frame;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addFrameNode(PstNode node) {
        frame.add(node);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public String getFrameName() {
        return "CASE";
    }
}
