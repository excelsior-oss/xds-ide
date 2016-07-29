package com.excelsior.xds.parser.modula.ast.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.IAstSymbolScope;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.type.IRecordTypeSymbol;

public class AstRecordType extends    AstTypeDef<IRecordTypeSymbol>  
                           implements IAstSymbolScope,  IAstFrameNode
{
    private List<PstNode> frame = new ArrayList<PstNode>(4);

    public AstRecordType(ModulaCompositeType<AstRecordType> elementType) {
        super(null, elementType);
    }

    public AstRecordFieldBlockList getAstRecordFieldBlockList() {
        return findFirstChild( ModulaElementTypes.RECORD_FIELD_BLOCK_LIST
                             , ModulaElementTypes.RECORD_FIELD_BLOCK_LIST.getNodeClass() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IModulaSymbolScope getScope() {
        return getSymbol();
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
        return "RECORD";
    }
}
