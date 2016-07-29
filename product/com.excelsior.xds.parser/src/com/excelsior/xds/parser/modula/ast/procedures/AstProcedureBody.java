package com.excelsior.xds.parser.modula.ast.procedures;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstBody;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.symbol.IProcedureBodySymbol;

/**
 * "BEGIN"-block of a procedure declaration.
 */
public class AstProcedureBody extends    AstBody<IProcedureBodySymbol> 
                              implements IAstNodeWithIdentifier
{

    public AstProcedureBody(ModulaCompositeType<AstProcedureBody> elementType) {
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
