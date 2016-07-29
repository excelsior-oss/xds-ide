package com.excelsior.xds.parser.modula.ast.types;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;

/**
 * RecordVariantElsePart = "ELSE" FieldListSequence <br>
 */
public class AstRecordVariantElsePart extends    ModulaAstNode
                                      implements IAstNodeWithIdentifier  
{
    public AstRecordVariantElsePart(ModulaCompositeType<AstRecordVariantElsePart> elementType) {
        super(null, elementType);
    }

    public AstRecordFieldBlockList getAstRecordFieldBlockList() {
        return findFirstChild( ModulaElementTypes.RECORD_FIELD_BLOCK_LIST
                             , ModulaElementTypes.RECORD_FIELD_BLOCK_LIST.getNodeClass() );
    }

    public PstLeafNode getElseKeyword() {
        return findFirstChild(ModulaTokenTypes.ELSE_KEYWORD, PstLeafNode.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getIdentifier() {
        return getElseKeyword();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, getAstRecordFieldBlockList());
        }
    }
    
}
