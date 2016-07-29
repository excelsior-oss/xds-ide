package com.excelsior.xds.parser.modula.ast.modules;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstBody;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IFinallyBodySymbol;

/**
 * A finalization body of the module.
 * 
 * ModuleBody = InitializationBody [FinalizationBody] <br>
 * InitializationBody = "BEGIN" BlockBody <br>
 * FinalizationBody   = "FINALLY" BlockBody <br>
 */
public class AstFinallyBody extends AstBody<IFinallyBodySymbol> 
                            implements IAstNodeWithIdentifier
{
    public AstFinallyBody(ModulaCompositeType<AstFinallyBody> elementType) {
        super(null, elementType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.FINALLY_KEYWORD, PstNode.class);
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
