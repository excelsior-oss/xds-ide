package com.excelsior.xds.parser.modula.ast.types;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.excelsior.xds.parser.modula.ast.ModulaAstNode;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;

public class AstRecordFieldBlockList extends ModulaAstNode 
{
    public AstRecordFieldBlockList(ModulaCompositeType<AstRecordFieldBlockList> elementType) {
        super(null, elementType);
    }
    
    
    public List<AstRecordFieldBlock> getAstRecordFieldBlock() {
        return findChildren(newRecordFieldBlockHasSet(), AstRecordFieldBlock.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstRecordFieldBlock());
        }
    }

    
    private static Set<ModulaCompositeType<? extends AstRecordFieldBlock>> newRecordFieldBlockHasSet() 
    {
        Set<ModulaCompositeType<? extends AstRecordFieldBlock>> set = 
                new HashSet<ModulaCompositeType<? extends AstRecordFieldBlock>>();
        set.add(ModulaElementTypes.RECORD_SIMPLE_FIELD_BLOCK);
        set.add(ModulaElementTypes.RECORD_VARIANT_FIELD_BLOCK);
        return set;
    }
    
}
