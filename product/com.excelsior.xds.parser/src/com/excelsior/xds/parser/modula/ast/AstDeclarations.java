package com.excelsior.xds.parser.modula.ast;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.modules.AstLocalModule;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureExternalSpecification;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.variables.AstVariableDeclarationBlock;

public class AstDeclarations extends AstBlock
{
    public AstDeclarations(ModulaCompositeType<AstDeclarations> elementType) {
        super(null, elementType);
    }

    public List<AstConstantDeclarationBlock> getAstConstantDeclarationBlocks() {
        return findChildren(ModulaElementTypes.CONSTANT_DECLARATION_BLOCK, AstConstantDeclarationBlock.class);
    }
    
    public List<AstVariableDeclarationBlock> getAstVariableDeclarationBlocks() {
        return findChildren(ModulaElementTypes.VARIABLE_DECLARATION_BLOCK);
    }
    
    public List<AstTypeDeclarationBlock> getAstTypeDeclarationBlocks() {
        return findChildren(ModulaElementTypes.TYPE_DECLARATION_BLOCK);
    }

    public List<AstLocalModule> getLocalModules() {
        return findChildren(ModulaElementTypes.LOCAL_MODULE);
    }
    
    public List<AstProcedureDeclaration> getProcedureDeclarations() {
        return findChildren(ModulaElementTypes.PROCEDURE_DECLARATION);
    }
    
    public List<AstProcedureForwardDeclaration> getProcedureForwardDeclarations() {
        return findChildren(ModulaElementTypes.PROCEDURE_FORWARD_DECLARATION);
    }

    public List<AstProcedureExternalSpecification> getProcedureExternalSpecifications() {
        return findChildren(ModulaElementTypes.PROCEDURE_EXTERNAL_SPECIFICATION);
    }
    
    public List<AstOberonMethodForwardDeclaration> getOberonMethodForwardDeclarations() {
        return findChildren(ModulaElementTypes.OBERON_METHOD_FORWARD_DECLARATION);
    }

    public List<AstOberonMethodDeclaration> getOberonMethodDeclarations() {
        return findChildren(ModulaElementTypes.OBERON_METHOD_DECLARATION);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doAccept(ModulaAstVisitor visitor) {
        boolean visitChildren = visitor.visit(this);
        if (visitChildren) {
            acceptChildren(visitor, getAstConstantDeclarationBlocks());
            acceptChildren(visitor, getAstTypeDeclarationBlocks());
            acceptChildren(visitor, getAstVariableDeclarationBlocks());
            acceptChildren(visitor, getLocalModules());
            acceptChildren(visitor, getProcedureExternalSpecifications());
            acceptChildren(visitor, getProcedureDeclarations());
            acceptChildren(visitor, getProcedureForwardDeclarations());
            acceptChildren(visitor, getOberonMethodDeclarations());
            acceptChildren(visitor, getOberonMethodForwardDeclarations());
        }
    }
    
}
