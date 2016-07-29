package com.excelsior.xds.parser.modula.ast.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.IAstSymbolScope;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.imports.AstImports;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbolScope;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public abstract class AstModule extends    AstSymbolDef<IModuleSymbol>
                                implements IAstSymbolScope, IAstNodeWithIdentifier, IAstFrameNode
{
    private List<PstNode> frame = new ArrayList<PstNode>(4);

    protected AstModule(ModulaCompositeType<? extends AstModule> elementType) {
        super(null, elementType);
    }

    public AstImports getAstImports() {
        return findFirstChild( ModulaElementTypes.IMPORTS
                             , ModulaElementTypes.IMPORTS.getNodeClass() );
    }
    
    @Override
    public PstNode getIdentifier() {
        return findFirstChild(ModulaElementTypes.MODULE_IDENTIFIER, PstNode.class);
    }
    
    protected void acceptChildren(ModulaAstVisitor visitor) {
        acceptChild(visitor, getAstImports());
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
        return "MODULE";
    }
    

}
