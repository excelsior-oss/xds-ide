package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.commons.ast.Ast;
import com.excelsior.xds.parser.commons.ast.AstVisitor;
import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclaration;
import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.imports.AstImportAliasDeclaration;
import com.excelsior.xds.parser.modula.ast.imports.AstImportFragmentList;
import com.excelsior.xds.parser.modula.ast.imports.AstImports;
import com.excelsior.xds.parser.modula.ast.imports.AstModuleAlias;
import com.excelsior.xds.parser.modula.ast.imports.AstSimpleImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstSimpleImportStatement;
import com.excelsior.xds.parser.modula.ast.imports.AstUnqualifiedImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstUnqualifiedImportStatement;
import com.excelsior.xds.parser.modula.ast.modules.AstFinallyBody;
import com.excelsior.xds.parser.modula.ast.modules.AstLocalModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModuleBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameter;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameterBlock;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameterDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstFormalParameterList;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstOberonMethodReceiver;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureBody;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDeclaration;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureDefinition;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureExternalSpecification;
import com.excelsior.xds.parser.modula.ast.procedures.AstProcedureForwardDeclaration;
import com.excelsior.xds.parser.modula.ast.types.AstEnumElement;
import com.excelsior.xds.parser.modula.ast.types.AstEnumerationType;
import com.excelsior.xds.parser.modula.ast.types.AstPointerType;
import com.excelsior.xds.parser.modula.ast.types.AstRecordField;
import com.excelsior.xds.parser.modula.ast.types.AstRecordFieldBlockList;
import com.excelsior.xds.parser.modula.ast.types.AstRecordFieldList;
import com.excelsior.xds.parser.modula.ast.types.AstRecordSimpleFieldBlock;
import com.excelsior.xds.parser.modula.ast.types.AstRecordType;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariant;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantElsePart;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantFieldBlock;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantLabel;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantLabelList;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantList;
import com.excelsior.xds.parser.modula.ast.types.AstRecordVariantSelector;
import com.excelsior.xds.parser.modula.ast.types.AstSetType;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDeclaration;
import com.excelsior.xds.parser.modula.ast.types.AstTypeDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.types.AstTypeElement;
import com.excelsior.xds.parser.modula.ast.variables.AstVariable;
import com.excelsior.xds.parser.modula.ast.variables.AstVariableDeclaration;
import com.excelsior.xds.parser.modula.ast.variables.AstVariableDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.variables.AstVariableList;


public abstract class ModulaAstVisitor extends AstVisitor {
    
    public boolean visit(AstModule astNode) {
        return true;
    }

    public boolean visit(AstImports astNode) {
        return true;
    }

    public boolean visit(AstDeclarations astNode) {
        return true;
    }

    public boolean visit(AstDefinitions astNode) {
        return true;
    }


    public boolean visit(AstProcedureDefinition astNode) {
        return true;
    }

    public boolean visit(AstProcedureDeclaration astNode) {
        return true;
    }
    

    public boolean visit(AstFormalParameterBlock astNode) {
        return true;
    }

    public boolean visit(AstFormalParameterDeclaration astNode) {
        return true;
    }

    public boolean visit(AstFormalParameterList astNode) {
        return true;
    }

    public boolean visit(AstFormalParameter astNode) {
        return true;
    }


    public boolean visit(AstModuleBody astNode) {
        return true;
    }

    public boolean visit(Ast node) {
        return true;
    }

	public boolean visit(AstConstantDeclaration astNode) {
		return true;
	}

	public boolean visit(AstConstantDeclarationBlock astNode) {
		return true;
	}

	public boolean visit(AstOberonMethodDeclaration astNode) {
		return true;
	}

    public boolean visit(AstOberonMethodReceiver astNode) {
        return true;
    }

	public boolean visit(AstProcedureExternalSpecification astNode) {
		return true;
	}

	public boolean visit(AstProcedureForwardDeclaration astNode) {
		return true;
	}

    public boolean visit(AstRecordType astNode) {
        return true;
    }

    public boolean visit(AstTypeDeclaration astNode) {
        return true;
    }

    public boolean visit(AstPointerType astNode) {
        return true;
    }
    
    public boolean visit(AstTypeDeclarationBlock astNode) {
         return true;
    }

	public boolean visit(AstSimpleImportStatement astNode) {
		return true;
	}

	public boolean visit(AstSimpleImportFragment astNode) {
		return true;
	}

	public boolean visit(AstModuleName astNode) {
		return true;
	}

	public boolean visit(AstImportAliasDeclaration astNode) {
		return true;
	}

	public boolean visit(AstImportFragmentList astNode) {
		return true;
	}

	public boolean visit(AstModuleAlias astNode) {
		return true;
	}

    public boolean visit(AstVariableDeclarationBlock astNode) {
        return true;
    }

    public boolean visit(AstVariableDeclaration astNode) {
        return true;
    }

    public boolean visit(AstVariableList astNode) {
        return true;
    }

    public boolean visit(AstOberonMethodForwardDeclaration astNode) {
        return true;
    }

    public boolean visit(AstUnqualifiedImportStatement astNode) {
        return true;
    }

    public boolean visit(AstUnqualifiedImportFragment astNode) {
        return true;
    }

    public boolean visit(AstTypeElement astNode) {
        return true;
    }

    public boolean visit(AstEnumerationType astNode) {
        return true;
    }

    public boolean visit(AstEnumElement astNode) {
        return true;
    }
    
    public boolean visit(AstSetType astNode) {
		return true;
	}

    public boolean visit(AstLocalModule astNode) {
        return true;
    }
    
    
    public boolean visit(AstRecordSimpleFieldBlock astNode) {
        return true;
    }

    public boolean visit(AstRecordFieldBlockList astNode) {
        return true;
    }

    public boolean visit(AstRecordFieldList node) {
        return true;
    }

    
    public boolean visit(AstRecordVariantFieldBlock astNode) {
        return true;
    }

    public boolean visit(AstRecordVariantList astNode) {
        return true;
    }

    public boolean visit(AstRecordVariant astNode) {
        return true;
    }

    public boolean visit(AstRecordVariantElsePart astNode) {
        return true;
    }

    public boolean visit(AstRecordVariantLabelList astNode) {
        return true;
    }

    public boolean visit(AstRecordVariantLabel astNode) {
        return true;
    }
    
    
    public boolean visit(AstVariable astNode) {
        return true;
    }
    
    public boolean visit(AstRecordField astNode) {
        return true;
    }

    public boolean visit(AstRecordVariantSelector astNode) {
        return true;
    }


    public boolean visit(AstProcedureBody astNode) {
        return true;
    }
    
    public boolean visit(AstFinallyBody astNode) {
        return true;
    }

    
    public void postVisit(AstUnqualifiedImportStatement astNode) {
    }

}
