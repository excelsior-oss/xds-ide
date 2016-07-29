package com.excelsior.xds.parser.modula.ast;

import java.util.List;

import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDefinition;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureExternalSpecification;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.variables.AstVariableDeclarationBlock;

public class AstDefinitions extends AstBlock
{
    public AstDefinitions(ModulaCompositeType<AstDefinitions> elementType) {
        super(null, elementType);
    }

    public List<AstConstantDeclarationBlock> getAstConstantDeclarationBlocks() {
        return findChildren(ModulaElementTypes.CONSTANT_DECLARATION_BLOCK);
    }
    
    public List<AstVariableDeclarationBlock> getAstVariableDeclarationBlocks() {
        return findChildren(ModulaElementTypes.VARIABLE_DECLARATION_BLOCK);
    }
    
    public List<AstTypeDeclarationBlock> getAstTypeDeclarationBlocks() {
        return findChildren(ModulaElementTypes.TYPE_DECLARATION_BLOCK);
    }

    public List<AstProcedureDefinition> getProcedureDefinitions() {
        return findChildren(ModulaElementTypes.PROCEDURE_DEFINITION);
    }
    
    public List<AstProcedureExternalSpecification> getProcedureExternalSpecifications() {
        return findChildren(ModulaElementTypes.PROCEDURE_EXTERNAL_SPECIFICATION);
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
            acceptChildren(visitor, getProcedureExternalSpecifications());
            acceptChildren(visitor, getProcedureDefinitions());
        }
    }
    
}
