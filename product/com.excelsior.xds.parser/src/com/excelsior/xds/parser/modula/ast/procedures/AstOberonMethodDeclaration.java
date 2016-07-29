package com.excelsior.xds.parser.modula.ast.procedures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.parser.commons.ast.IAstFrameNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstDeclarations;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.symbol.IOberonMethodDeclarationSymbol;

public class AstOberonMethodDeclaration extends AstOberonMethod<IOberonMethodDeclarationSymbol> 
                                        implements IAstFrameNode
{
    private List<PstNode> frame = new ArrayList<PstNode>(4);

    public AstOberonMethodDeclaration(ModulaCompositeType<AstOberonMethodDeclaration> elementType) {
        super(null, elementType);
    }

    public AstDeclarations getAstDeclarations() {
        return findFirstChild( ModulaElementTypes.DECLARATIONS
                             , ModulaElementTypes.DECLARATIONS.getNodeClass() );
    }
    
    public AstProcedureBody getProcedureBody() {
        return findFirstChild( ModulaElementTypes.PROCEDURE_BODY
                             , ModulaElementTypes.PROCEDURE_BODY.getNodeClass() );
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
        return "PROCEDURE";
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void acceptChildren(ModulaAstVisitor visitor) {
        super.acceptChildren(visitor);
        acceptChild(visitor, getAstDeclarations());
        acceptChild(visitor, getProcedureBody());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void doAccept(ModulaAstVisitor visitor) {
    	if (visitor.visit(this)) {
    		acceptChildren(visitor);
    	}
    }
}
