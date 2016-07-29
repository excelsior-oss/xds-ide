package com.excelsior.xds.parser.modula.ast.variables;

import com.excelsior.xds.parser.commons.ast.IAstNodeWithIdentifier;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.AstSymbolDef;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.ast.ModulaAstVisitor;
import com.excelsior.xds.parser.modula.ast.ModulaCompositeType;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedure;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.parser.modula.ast.types.AstTypeElement;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;

public class AstVariable extends AstSymbolDef<IVariableSymbol> implements IAstNodeWithIdentifier{

    public AstVariable(ModulaCompositeType<AstVariable> elementType) {
        super(null, elementType);
    }
    
    @Override
    public PstNode getIdentifier() {
        return findFirstChild(ModulaTokenTypes.IDENTIFIER, PstNode.class);
    }
    
    public AstVariableList getVariableList() {
        return ModulaAst.findParent(this, ModulaElementTypes.VARIABLE_LIST);
    }

    public boolean isLocal() {
        PstNode node = getParent();
        if (node instanceof AstVariableList) {
            node = node.getParent(); 
            if (node instanceof AstVariableDeclaration) {
                node = node.getParent(); 
                if (node instanceof AstVariableDeclarationBlock) {
                    node = node.getParent(); 
                }
            }
        }
        return (node instanceof AstProcedure);
    }

    public AstTypeElement getAstTypeElement() {
        AstTypeElement typeElementAst = null;
        PstNode node = getParent();
        if (node instanceof AstVariableList) {
            typeElementAst = ((AstVariableList)node).getAstTypeElement();
        }
        return typeElementAst;
    }
    
    public void visitTypeElement(ModulaAstVisitor visitor) {
        AstTypeElement typeElementAst = getAstTypeElement();
        if (typeElementAst != null) {
            typeElementAst.accept(visitor);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        visitor.visit(this);
    }
    
}
