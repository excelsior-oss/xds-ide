package com.excelsior.xds.parser.modula.ast.modules;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstBody;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IModuleBodySymbol;

/**
 * A initialization body of a program and local module.
 * 
 * ModuleBody = InitializationBody [FinalizationBody] <br>
 * InitializationBody = "BEGIN" BlockBody <br>
 */
public class AstModuleBody extends    AstBody<IModuleBodySymbol> 
                           implements IAstNodeWithIdentifier
{
    
    public AstModuleBody(ModulaCompositeType<AstModuleBody> elementType) {
        super(null, elementType);
    }
    
    public PstNode getBeginKeyword() {
        return findFirstChild(ModulaTokenTypes.BEGIN_KEYWORD, PstNode.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getIdentifier() {
        return getBeginKeyword();
    }
    
    public AstModule getModuleAst() {
        AstModule astModule = null;
        PstNode parent = getParent();
        if (parent instanceof AstModule) {
            astModule = (AstModule) parent;
        }
        return astModule;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            visitStatementList(visitor);
        }
    }
}