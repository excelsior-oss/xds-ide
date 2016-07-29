package com.excelsior.xds.parser.modula.ast;

import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclaration;
import com.excelsior.xds.parser.modula.ast.constants.AstConstantDeclarationBlock;
import com.excelsior.xds.parser.modula.ast.expressions.AstCaseLabel;
import com.excelsior.xds.parser.modula.ast.expressions.AstCaseVariantSelector;
import com.excelsior.xds.parser.modula.ast.expressions.AstConstantExpression;
import com.excelsior.xds.parser.modula.ast.expressions.AstExpression;
import com.excelsior.xds.parser.modula.ast.imports.AstImportAliasDeclaration;
import com.excelsior.xds.parser.modula.ast.imports.AstImportFragmentList;
import com.excelsior.xds.parser.modula.ast.imports.AstImports;
import com.excelsior.xds.parser.modula.ast.imports.AstModuleAlias;
import com.excelsior.xds.parser.modula.ast.imports.AstSimpleImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstSimpleImportStatement;
import com.excelsior.xds.parser.modula.ast.imports.AstUnqualifiedImportFragment;
import com.excelsior.xds.parser.modula.ast.imports.AstUnqualifiedImportStatement;
import com.excelsior.xds.parser.modula.ast.modules.AstDefinitionModule;
import com.excelsior.xds.parser.modula.ast.modules.AstFinallyBody;
import com.excelsior.xds.parser.modula.ast.modules.AstLocalModule;
import com.excelsior.xds.parser.modula.ast.modules.AstModuleBody;
import com.excelsior.xds.parser.modula.ast.modules.AstProgramModule;
import com.excelsior.xds.parser.modula.ast.modules.AstQualifiedExportStatement;
import com.excelsior.xds.parser.modula.ast.modules.AstUnqualifiedExportStatement;
import com.excelsior.xds.parser.modula.ast.pragmas.AstInactiveCode;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragma;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaConditionalStatement;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaElseStatement;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaElsifStatement;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaEndStatement;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaIfStatement;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaInlineEquation;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaInlineOption;
import com.excelsior.xds.parser.modula.ast.pragmas.AstPragmaInlineSettings;
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
import com.excelsior.xds.parser.modula.ast.procedures.AstResultType;
import com.excelsior.xds.parser.modula.ast.statements.AstAsmStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstCaseElsePart;
import com.excelsior.xds.parser.modula.ast.statements.AstCaseLabelList;
import com.excelsior.xds.parser.modula.ast.statements.AstCaseStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstCaseVariant;
import com.excelsior.xds.parser.modula.ast.statements.AstCaseVariantList;
import com.excelsior.xds.parser.modula.ast.statements.AstForStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstIfStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstLoopStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstModulaWithStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstRepeatStatement;
import com.excelsior.xds.parser.modula.ast.statements.AstStatementList;
import com.excelsior.xds.parser.modula.ast.statements.AstWhileStatement;
import com.excelsior.xds.parser.modula.ast.types.AstArrayType;
import com.excelsior.xds.parser.modula.ast.types.AstEnumElement;
import com.excelsior.xds.parser.modula.ast.types.AstEnumerationType;
import com.excelsior.xds.parser.modula.ast.types.AstFormalParameterType;
import com.excelsior.xds.parser.modula.ast.types.AstFormalType;
import com.excelsior.xds.parser.modula.ast.types.AstFotmalParameterTypeList;
import com.excelsior.xds.parser.modula.ast.types.AstIndexType;
import com.excelsior.xds.parser.modula.ast.types.AstOpenArrayType;
import com.excelsior.xds.parser.modula.ast.types.AstPointerType;
import com.excelsior.xds.parser.modula.ast.types.AstProcedureType;
import com.excelsior.xds.parser.modula.ast.types.AstProcedureTypeOberon;
import com.excelsior.xds.parser.modula.ast.types.AstRangeType;
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

public interface ModulaElementTypes 
{
    ModulaCompositeType<AstDefinitionModule> DEFINITION_MODULE = new ModulaCompositeType<AstDefinitionModule>("DEFINITION_MODULE", AstDefinitionModule.class);          //$NON-NLS-1$ 
    ModulaCompositeType<AstProgramModule>    PROGRAM_MODULE = new ModulaCompositeType<AstProgramModule>("PROGRAM_MODULE", AstProgramModule.class);          //$NON-NLS-1$
    ModulaCompositeType<AstLocalModule>      LOCAL_MODULE = new ModulaCompositeType<AstLocalModule>("LOCAL_MODULE", AstLocalModule.class);          //$NON-NLS-1$

    ModulaCompositeType<AstDeclarations> DECLARATIONS = new ModulaCompositeType<AstDeclarations>("DECLARATIONS", AstDeclarations.class);          //$NON-NLS-1$
    ModulaCompositeType<AstDefinitions>  DEFINITIONS  = new ModulaCompositeType<AstDefinitions>("DEFINITIONS", AstDefinitions.class);          //$NON-NLS-1$
    
    ModulaCompositeType<AstModuleBody>    MODULE_BODY    = new ModulaCompositeType<AstModuleBody>("MODULE_BODY", AstModuleBody.class);            //$NON-NLS-1$
    ModulaCompositeType<AstProcedureBody> PROCEDURE_BODY = new ModulaCompositeType<AstProcedureBody>("PROCEDURE_BODY", AstProcedureBody.class);   //$NON-NLS-1$
    ModulaCompositeType<AstFinallyBody>   FINALLY_BODY   = new ModulaCompositeType<AstFinallyBody>("FINALLY_BODY", AstFinallyBody.class);         //$NON-NLS-1$
    ModulaCompositeType<AstExceptBlock>   EXCEPT_BLOCK   = new ModulaCompositeType<AstExceptBlock>("EXCEPT_BLOCK", AstExceptBlock.class);         //$NON-NLS-1$

    ModulaCompositeType<AstImports>             IMPORTS = new ModulaCompositeType<AstImports>("IMPORTS", AstImports.class);     //$NON-NLS-1$
    ModulaCompositeType<AstImportFragmentList>  IMPORT_FRAGMENT_LIST = new ModulaCompositeType<AstImportFragmentList>("IMPORT_FRAGMENT_LIST", AstImportFragmentList.class);     //$NON-NLS-1$

    ModulaCompositeType<AstSimpleImportStatement> SIMPLE_IMPORT          = new ModulaCompositeType<AstSimpleImportStatement>("SIMPLE_IMPORT", AstSimpleImportStatement.class);     //$NON-NLS-1$
    ModulaCompositeType<AstSimpleImportFragment>  SIMPLE_IMPORT_FRAGMENT = new ModulaCompositeType<AstSimpleImportFragment>("SIMPLE_IMPORT_FRAGMENT", AstSimpleImportFragment.class);     //$NON-NLS-1$

    ModulaCompositeType<AstUnqualifiedImportStatement> UNQUALIFIED_IMPORT          = new ModulaCompositeType<AstUnqualifiedImportStatement>("UNQUALIFIED_IMPORT", AstUnqualifiedImportStatement.class);   //$NON-NLS-1$
    ModulaCompositeType<AstUnqualifiedImportFragment>  UNQUALIFIED_IMPORT_FRAGMENT = new ModulaCompositeType<AstUnqualifiedImportFragment>("UNQUALIFIED_IMPORT_FRAGMENT", AstUnqualifiedImportFragment.class);     //$NON-NLS-1$
    
    ModulaCompositeType<AstImportAliasDeclaration> ALIAS_DECLARATION  = new ModulaCompositeType<AstImportAliasDeclaration>("ALIAS_DECLARATION", AstImportAliasDeclaration.class);    //$NON-NLS-1$
    ModulaCompositeType<AstModuleAlias> MODULE_ALIAS_NAME  = new ModulaCompositeType<AstModuleAlias>("MODULE_ALIAS_NAME", AstModuleAlias.class);    //$NON-NLS-1$
    
    ModulaCompositeType<AstUnqualifiedExportStatement> UNQUALIFIED_EXPORT = new ModulaCompositeType<AstUnqualifiedExportStatement>("UNQUALIFIED_EXPORT", AstUnqualifiedExportStatement.class);    //$NON-NLS-1$
    ModulaCompositeType<AstQualifiedExportStatement>   QUALIFIED_EXPORT   = new ModulaCompositeType<AstQualifiedExportStatement>("QUALIFIED_EXPORT", AstQualifiedExportStatement.class);    //$NON-NLS-1$

    
    ModulaCompositeType<AstProcedureDeclaration>           PROCEDURE_DECLARATION  = new ModulaCompositeType<AstProcedureDeclaration>("PROCEDURE_DECLARATION", AstProcedureDeclaration.class);       //$NON-NLS-1$
    ModulaCompositeType<AstProcedureForwardDeclaration>    PROCEDURE_FORWARD_DECLARATION = new ModulaCompositeType<AstProcedureForwardDeclaration>("PROCEDURE_FORWARD_DECLARATION", AstProcedureForwardDeclaration.class);       //$NON-NLS-1$
    ModulaCompositeType<AstProcedureDefinition>            PROCEDURE_DEFINITION = new ModulaCompositeType<AstProcedureDefinition>("PROCEDURE_DEFINITION", AstProcedureDefinition.class);       //$NON-NLS-1$
    ModulaCompositeType<AstProcedureExternalSpecification> PROCEDURE_EXTERNAL_SPECIFICATION = new ModulaCompositeType<AstProcedureExternalSpecification>("PROCEDURE_EXTERNAL_SPECIFICATION", AstProcedureExternalSpecification.class);       //$NON-NLS-1$
    
    ModulaCompositeType<AstOberonMethodDeclaration>        OBERON_METHOD_DECLARATION  = new ModulaCompositeType<AstOberonMethodDeclaration>("OBERON_METHOD_DECLARATION", AstOberonMethodDeclaration.class);       //$NON-NLS-1$
    ModulaCompositeType<AstOberonMethodForwardDeclaration> OBERON_METHOD_FORWARD_DECLARATION = new ModulaCompositeType<AstOberonMethodForwardDeclaration>("OBERON_METHOD_FORWARD_DECLARATION", AstOberonMethodForwardDeclaration.class);       //$NON-NLS-1$
    ModulaCompositeType<AstOberonMethodReceiver>           OBERON_METHOD_RECEIVER = new ModulaCompositeType<AstOberonMethodReceiver>("OBERON_METHOD_RECEIVER", AstOberonMethodReceiver.class);       //$NON-NLS-1$
    
    ModulaCompositeType<AstFormalParameterBlock>       FORMAL_PARAMETER_BLOCK = new ModulaCompositeType<AstFormalParameterBlock>("FORMAL_PARAMETER_BLOCK", AstFormalParameterBlock.class);    //$NON-NLS-1$
    ModulaCompositeType<AstFormalParameterDeclaration> FORMAL_PARAMETER_DECLARATION = new ModulaCompositeType<AstFormalParameterDeclaration>("FORMAL_PARAMETER_DECLARATION", AstFormalParameterDeclaration.class);    //$NON-NLS-1$
    ModulaCompositeType<AstFormalParameterList>        FORMAL_PARAMETER_LIST = new ModulaCompositeType<AstFormalParameterList>("FORMAL_PARAMETER_LIST", AstFormalParameterList.class);    //$NON-NLS-1$
    ModulaCompositeType<AstFormalParameter>            FORMAL_PARAMETER = new ModulaCompositeType<AstFormalParameter>("FORMAL_PARAMETER", AstFormalParameter.class);    //$NON-NLS-1$
    ModulaCompositeType<AstResultType>                 RESULT_TYPE = new ModulaCompositeType<AstResultType>("RESULT_TYPE", AstResultType.class);    //$NON-NLS-1$

    
    ModulaCompositeType<AstVariableDeclarationBlock> VARIABLE_DECLARATION_BLOCK = new ModulaCompositeType<AstVariableDeclarationBlock>("VARIABLE_DECLARATION_BLOCK", AstVariableDeclarationBlock.class);    //$NON-NLS-1$
    ModulaCompositeType<AstVariableDeclaration> VARIABLE_DECLARATION = new ModulaCompositeType<AstVariableDeclaration>("VARIABLE_DECLARATION", AstVariableDeclaration.class);         //$NON-NLS-1$
    ModulaCompositeType<AstVariableList> VARIABLE_LIST = new ModulaCompositeType<AstVariableList>("VARIABLE_LIST", AstVariableList.class);         //$NON-NLS-1$
    ModulaCompositeType<AstVariable> VARIABLE = new ModulaCompositeType<AstVariable>("VARIABLE", AstVariable.class);         //$NON-NLS-1$
    
    ModulaCompositeType<AstConstantDeclarationBlock> CONSTANT_DECLARATION_BLOCK = new ModulaCompositeType<AstConstantDeclarationBlock>("CONSTANT_DECLARATION_BLOCK", AstConstantDeclarationBlock.class);    //$NON-NLS-1$
    ModulaCompositeType<AstConstantDeclaration>      CONSTANT_DECLARATION       = new ModulaCompositeType<AstConstantDeclaration>("CONSTANT_DECLARATION", AstConstantDeclaration.class);                    //$NON-NLS-1$

    ModulaCompositeType<AstTypeDeclarationBlock> TYPE_DECLARATION_BLOCK = new ModulaCompositeType<AstTypeDeclarationBlock>("TYPE_DECLARATION_BLOCK", AstTypeDeclarationBlock.class);    //$NON-NLS-1$
    ModulaCompositeType<AstTypeDeclaration>      TYPE_DECLARATION = new ModulaCompositeType<AstTypeDeclaration>("TYPE_DECLARATION", AstTypeDeclaration.class);         //$NON-NLS-1$
    ModulaCompositeType<AstTypeElement>          TYPE_ELEMENT = new ModulaCompositeType<AstTypeElement>("TYPE_ELEMENT", AstTypeElement.class);             //$NON-NLS-1$

    ModulaCompositeType<AstIndexType> INDEX_TYPE = new ModulaCompositeType<AstIndexType>("INDEX_TYPE", AstIndexType.class);       //$NON-NLS-1$
    ModulaElementType BASE_TYPE = new ModulaElementType("BASE_TYPE");        //$NON-NLS-1$

    ModulaCompositeType<AstRecordType>  RECORD_TYPE_DEFINITION = new ModulaCompositeType<AstRecordType>("RECORD_TYPE_DEFINITION", AstRecordType.class);       //$NON-NLS-1$
    ModulaCompositeType<AstRecordFieldBlockList>    RECORD_FIELD_BLOCK_LIST = new ModulaCompositeType<AstRecordFieldBlockList>("RECORD_FIELD_BLOCK_LIST", AstRecordFieldBlockList.class);        //$NON-NLS-1$
    ModulaCompositeType<AstRecordSimpleFieldBlock>  RECORD_SIMPLE_FIELD_BLOCK = new ModulaCompositeType<AstRecordSimpleFieldBlock>("RECORD_SIMPLE_FIELD_BLOCK", AstRecordSimpleFieldBlock.class);      //$NON-NLS-1$
    ModulaCompositeType<AstRecordVariantFieldBlock> RECORD_VARIANT_FIELD_BLOCK = new ModulaCompositeType<AstRecordVariantFieldBlock>("RECORD_VARIANT_FIELD_BLOCK", AstRecordVariantFieldBlock.class);     //$NON-NLS-1$
    ModulaCompositeType<AstRecordVariantSelector>   RECORD_VARIANT_SELECTOR = new ModulaCompositeType<AstRecordVariantSelector>("RECORD_VARIANT_SELECTOR", AstRecordVariantSelector.class);    //$NON-NLS-1$

    ModulaCompositeType<AstRecordFieldList> RECORD_FIELD_LIST = new ModulaCompositeType<AstRecordFieldList>("RECORD_FIELD_LIST", AstRecordFieldList.class);     //$NON-NLS-1$
    ModulaCompositeType<AstRecordField>     RECORD_FIELD = new ModulaCompositeType<AstRecordField>("RECORD_FIELD", AstRecordField.class);      //$NON-NLS-1$
    
    ModulaCompositeType<AstRecordVariantList> RECORD_VARIANT_LIST = new ModulaCompositeType<AstRecordVariantList>("RECORD_VARIANT_LIST", AstRecordVariantList.class);        //$NON-NLS-1$
    ModulaCompositeType<AstRecordVariant>     RECORD_VARIANT = new ModulaCompositeType<AstRecordVariant>("RECORD_VARIANT", AstRecordVariant.class);        //$NON-NLS-1$
    ModulaCompositeType<AstRecordVariantLabelList> RECORD_VARIANT_LABEL_LIST = new ModulaCompositeType<AstRecordVariantLabelList>("RECORD_VARIANT_LABEL_LIST", AstRecordVariantLabelList.class);        //$NON-NLS-1$
    ModulaCompositeType<AstRecordVariantLabel>     RECORD_VARIANT_LABEL = new ModulaCompositeType<AstRecordVariantLabel>("RECORD_VARIANT_LABEL", AstRecordVariantLabel.class);        //$NON-NLS-1$
    ModulaCompositeType<AstRecordVariantElsePart>  RECORD_VARIANT_ELSE_PART = new ModulaCompositeType<AstRecordVariantElsePart>("RECORD_VARIANT_ELSE_PART", AstRecordVariantElsePart.class);        //$NON-NLS-1$
    
    ModulaCompositeType<AstSetType>         SET_TYPE_DEFINITION         = new ModulaCompositeType<AstSetType>("SET_TYPE_DEFINITION", AstSetType.class);       //$NON-NLS-1$
    ModulaCompositeType<AstRangeType>       RANGE_TYPE_DEFINITION       = new ModulaCompositeType<AstRangeType>("RANGE_TYPE_DEFINITION", AstRangeType.class);        //$NON-NLS-1$
    ModulaCompositeType<AstArrayType>       ARRAY_TYPE_DEFINITION       = new ModulaCompositeType<AstArrayType>("ARRAY_TYPE_DEFINITION", AstArrayType.class);       //$NON-NLS-1$
    ModulaCompositeType<AstPointerType>     POINTER_TYPE_DEFINITION     = new ModulaCompositeType<AstPointerType>("POINTER_TYPE_DEFINITION", AstPointerType.class);       //$NON-NLS-1$
    ModulaCompositeType<AstOpenArrayType>   OPEN_ARRAY_TYPE_DEFINITION  = new ModulaCompositeType<AstOpenArrayType>("OPEN_ARRAY_TYPE_DEFINITION", AstOpenArrayType.class);       //$NON-NLS-1$

    ModulaCompositeType<AstProcedureTypeOberon> PROCEDURE_TYPE_OBERON_DEFINITION = new ModulaCompositeType<AstProcedureTypeOberon>("PROCEDURE_TYPE_OBERON_DEFINITION", AstProcedureTypeOberon.class);       //$NON-NLS-1$

    ModulaCompositeType<AstProcedureType> PROCEDURE_TYPE_DEFINITION   = new ModulaCompositeType<AstProcedureType>("PROCEDURE_TYPE_DEFINITION", AstProcedureType.class);       //$NON-NLS-1$
    ModulaCompositeType<AstFotmalParameterTypeList> FORMAL_PARAMETER_TYPE_LIST = new ModulaCompositeType<AstFotmalParameterTypeList>("FORMAL_PARAMETER_TYPE_LIST", AstFotmalParameterTypeList.class);        //$NON-NLS-1$
    ModulaCompositeType<AstFormalParameterType>     FORMAL_PARAMETER_TYPE      = new ModulaCompositeType<AstFormalParameterType>("FORMAL_PARAMETER_TYPE", AstFormalParameterType.class);        //$NON-NLS-1$
    ModulaCompositeType<AstFormalType>              FORMAL_TYPE                = new ModulaCompositeType<AstFormalType>("FORMAL_TYPE", AstFormalType.class);             //$NON-NLS-1$
    
    ModulaCompositeType<AstEnumerationType> ENUMERATION_TYPE_DEFINITION = new ModulaCompositeType<AstEnumerationType>("ENUMERATION_TYPE_DEFINITION", AstEnumerationType.class);  //$NON-NLS-1$
    ModulaCompositeType<AstEnumElement>     ENUM_ELEMENT = new ModulaCompositeType<AstEnumElement>("ENUM_ELEMENT", AstEnumElement.class);    //$NON-NLS-1$

    ModulaCompositeType<AstStatementList>   STATEMENT_LIST = new ModulaCompositeType<AstStatementList>("STATEMENT_LIST", AstStatementList.class);         //$NON-NLS-1$
    
    ModulaCompositeType<AstCaseStatement>       CASE_STATEMENT = new ModulaCompositeType<AstCaseStatement>("CASE_STATEMENT", AstCaseStatement.class);         //$NON-NLS-1$
    ModulaCompositeType<AstCaseVariantSelector> CASE_VARIANT_SELECTOR = new ModulaCompositeType<AstCaseVariantSelector>("CASE_VARIANT_SELECTOR", AstCaseVariantSelector.class);         //$NON-NLS-1$
    ModulaCompositeType<AstCaseVariantList>     CASE_VARIANT_LIST = new ModulaCompositeType<AstCaseVariantList>("CASE_VARIANT_LIST", AstCaseVariantList.class);         //$NON-NLS-1$
    ModulaCompositeType<AstCaseVariant>         CASE_VARIANT = new ModulaCompositeType<AstCaseVariant>("CASE_VARIANT", AstCaseVariant.class);         //$NON-NLS-1$
    ModulaCompositeType<AstCaseLabelList>       CASE_LABEL_LIST = new ModulaCompositeType<AstCaseLabelList>("CASE_LABEL_LIST", AstCaseLabelList.class);         //$NON-NLS-1$
    ModulaCompositeType<AstCaseLabel>           CASE_LABEL = new ModulaCompositeType<AstCaseLabel>("CASE_LABEL", AstCaseLabel.class);         //$NON-NLS-1$
    ModulaCompositeType<AstCaseElsePart>        CASE_ELSE_PART = new ModulaCompositeType<AstCaseElsePart>("CASE_ELSE_PART", AstCaseElsePart.class);         //$NON-NLS-1$
    
    ModulaCompositeType<AstModulaWithStatement> MODULA_WITH_STATEMENT = new ModulaCompositeType<AstModulaWithStatement>("MODULA_WITH_STATEMENT", AstModulaWithStatement.class);       //$NON-NLS-1$
    ModulaElementType OBERON_WITH_STATEMENT = new ModulaElementType("OBERON_WITH_STATEMENT");       //$NON-NLS-1$

    ModulaElementType ASSIGMENT_STATEMENT = new ModulaElementType("ASSIGMENT_STATEMENT");    //$NON-NLS-1$
    ModulaElementType RETURN_STATEMENT    = new ModulaElementType("RETURN_STATEMENT");       //$NON-NLS-1$
    ModulaElementType EXIT_STATEMENT      = new ModulaElementType("EXIT_STATEMENT");         //$NON-NLS-1$
    ModulaElementType GOTO_STATEMENT      = new ModulaElementType("GOTO_STATEMENT");         //$NON-NLS-1$
    ModulaElementType RETRY_STATEMENT     = new ModulaElementType("RETRY_STATEMENT");        //$NON-NLS-1$

    ModulaCompositeType<AstForStatement>    FOR_STATEMENT    = new ModulaCompositeType<AstForStatement>("FOR_STATEMENT", AstForStatement.class);          //$NON-NLS-1$
    ModulaCompositeType<AstIfStatement>     IF_STATEMENT     = new ModulaCompositeType<AstIfStatement>("IF_STATEMENT", AstIfStatement.class);             //$NON-NLS-1$
    ModulaCompositeType<AstLoopStatement>   LOOP_STATEMENT   = new ModulaCompositeType<AstLoopStatement>("LOOP_STATEMENT", AstLoopStatement.class);       //$NON-NLS-1$
    ModulaCompositeType<AstRepeatStatement> REPEAT_STATEMENT = new ModulaCompositeType<AstRepeatStatement>("REPEAT_STATEMENT", AstRepeatStatement.class); //$NON-NLS-1$
    ModulaCompositeType<AstWhileStatement>  WHILE_STATEMENT  = new ModulaCompositeType<AstWhileStatement>("WHILE_STATEMENT", AstWhileStatement.class);    //$NON-NLS-1$
    ModulaCompositeType<AstAsmStatement>    ASM_STATEMENT    = new ModulaCompositeType<AstAsmStatement>("ASM_STATEMENT", AstAsmStatement.class);          //$NON-NLS-1$
    
    ModulaCompositeType<AstExpression> EXPRESSION = new ModulaCompositeType<AstExpression>("EXPRESSION", AstExpression.class);       //$NON-NLS-1$
    ModulaCompositeType<AstConstantExpression> CONSTANT_EXPRESSION = new ModulaCompositeType<AstConstantExpression>("CONSTANT_EXPRESSION", AstConstantExpression.class);       //$NON-NLS-1$
    ModulaElementType TERM             = new ModulaElementType("TERM");       //$NON-NLS-1$
    ModulaElementType FACTOR           = new ModulaElementType("FACTOR");       //$NON-NLS-1$
    ModulaElementType CALL_EXPRESSION  = new ModulaElementType("CALL_EXPRESSION");       //$NON-NLS-1$
    
    ModulaElementType IDENTIFIER_LIST = new ModulaElementType("IDENTIFIER_LIST");     //$NON-NLS-1$
    
    ModulaCompositeType<AstDecoratedIdentifier> DECORATED_IDENTIFIER = new ModulaCompositeType<AstDecoratedIdentifier>("DECORATED_IDENTIFIER", AstDecoratedIdentifier.class);     //$NON-NLS-1$

    ModulaElementType MODULE_IDENTIFIER    = new ModulaElementType("MODULE_IDENTIFIER");        //$NON-NLS-1$
    ModulaElementType PROCEDURE_IDENTIFIER = new ModulaElementType("PROCEDURE_IDENTIFIER");     //$NON-NLS-1$
    
    ModulaCompositeType<AstModuleName>    MODULE_NAME    = new ModulaCompositeType<AstModuleName>("MODULE_NAME", AstModuleName.class);              //$NON-NLS-1$
    ModulaCompositeType<AstQualifiedName> QUALIFIED_NAME = new ModulaCompositeType<AstQualifiedName>("QUALIFIED_NAME", AstQualifiedName.class);     //$NON-NLS-1$
    ModulaCompositeType<AstSimpleName>    SIMPLE_NAME    = new ModulaCompositeType<AstSimpleName>("SIMPLE_NAME", AstSimpleName.class);        //$NON-NLS-1$
    ModulaCompositeType<AstDecoratedName> DECORATED_NAME = new ModulaCompositeType<AstDecoratedName>("DECORATED_NAME", AstDecoratedName.class);     //$NON-NLS-1$

    ModulaCompositeType<AstDesignator> DESIGNATOR = new ModulaCompositeType<AstDesignator>("DESIGNATOR", AstDesignator.class);              //$NON-NLS-1$
    
    ModulaElementType DIRECT_LANGUAGE_SPEC = new ModulaElementType("DIRECT_LANGUAGE_SPEC");     //$NON-NLS-1$
    ModulaElementType EXPORT_MARKER        = new ModulaElementType("EXPORT_MARKER");     //$NON-NLS-1$
    
    
    ModulaCompositeType<AstPragma> PRAGMA = new ModulaCompositeType<AstPragma>("PRAGMA", AstPragma.class);     //$NON-NLS-1$

    ModulaCompositeType<AstPragmaInlineSettings> PRAGMA_INLINE_SETTINGS = new ModulaCompositeType<AstPragmaInlineSettings>("PRAGMA_INLINE_SETTINGS", AstPragmaInlineSettings.class);     //$NON-NLS-1$
    ModulaCompositeType<AstPragmaInlineOption> PRAGMA_INLINE_OPTION = new ModulaCompositeType<AstPragmaInlineOption>("PRAGMA_INLINE_OPTION", AstPragmaInlineOption.class);     //$NON-NLS-1$
    ModulaCompositeType<AstPragmaInlineEquation> PRAGMA_INLINE_EQUATION = new ModulaCompositeType<AstPragmaInlineEquation>("PRAGMA_INLINE_EQUATION", AstPragmaInlineEquation.class);     //$NON-NLS-1$

    ModulaCompositeType<AstPragmaConditionalStatement> PRAGMA_CONDITIONAL_STATEMENT = new ModulaCompositeType<AstPragmaConditionalStatement>("PRAGMA_CONDITIONAL_STATEMENT", AstPragmaConditionalStatement.class);     //$NON-NLS-1$
    ModulaCompositeType<AstPragmaIfStatement>    PRAGMA_IF_STATEMENT = new ModulaCompositeType<AstPragmaIfStatement>("PRAGMA_IF_STATEMENT", AstPragmaIfStatement.class);     //$NON-NLS-1$
    ModulaCompositeType<AstPragmaElsifStatement> PRAGMA_ELSIF_STATEMENT = new ModulaCompositeType<AstPragmaElsifStatement>("PRAGMA_ELSIF_STATEMENT", AstPragmaElsifStatement.class);     //$NON-NLS-1$
    ModulaCompositeType<AstPragmaElseStatement>  PRAGMA_ELSE_STATEMENT = new ModulaCompositeType<AstPragmaElseStatement>("PRAGMA_ELSE_STATEMENT", AstPragmaElseStatement.class);     //$NON-NLS-1$
    ModulaCompositeType<AstPragmaEndStatement>   PRAGMA_END_STATEMENT = new ModulaCompositeType<AstPragmaEndStatement>("PRAGMA_END_STATEMENT", AstPragmaEndStatement.class);     //$NON-NLS-1$

    ModulaCompositeType<AstInactiveCode> INACTIVE_CODE = new ModulaCompositeType<AstInactiveCode>("INACTIVE_CODE", AstInactiveCode.class);     //$NON-NLS-1$
    
}
