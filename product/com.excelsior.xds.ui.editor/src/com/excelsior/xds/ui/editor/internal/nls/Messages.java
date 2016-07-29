package com.excelsior.xds.ui.editor.internal.nls;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.ui.editor.internal.nls.messages"; //$NON-NLS-1$

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object});
    }

    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }


    public static String CodeStylePreferencePage_ExpandTreeToEdit;

    public static String CreateProfileDialog_InitFromProfile;
    public static String CreateProfileDialog_NameExists;
    public static String CreateProfileDialog_NewProfile;
    public static String CreateProfileDialog_OpenEditDlg;
    public static String CreateProfileDialog_ProfName;
    public static String CreateProfileDialog_ProfNameEmpty;

    public static String MarkOccurrencesPreferencePage_LinkText;
    public static String MarkOccurrencesPreferencePage_MarkOccurrencesOfElement;

    public static String ModifyDialog_Export;
    public static String ModifyDialog_ProfileName;

    public static String ModulaContextualCompletionProcessor_CloseBlockComment;

    public static String ModulaContextualCompletionProcessor_ClosePragma;

    public static String ModulaContextualCompletionProcessor_ExternalDeps;

    public static String ModulaContextualCompletionProcessor_inProjectPath;

    public static String ModulaContextualCompletionProcessor_SdkLibrary;

    public static String ModulaOccurrencesMarker_StructureOf;

    public static String ModulaTokens_Text;
    public static String ModulaTokens_Brackets;
    public static String ModulaTokens_Strings;
    public static String ModulaTokens_Numbers;
    public static String ModulaTokens_SystemModuleKeywords;
    public static String ModulaTokens_Keywords;
    public static String ModulaTokens_BuiltinConstants;

    public static String ModulaTokens_Pragmas;
    public static String ModulaTokens_CompilerPragmas;
    public static String ModulaTokens_PragmaKeywords;
    public static String ModulaTokens_InactiveCode;
    
    public static String ModulaTokens_Comments;
    public static String ModulaTokens_BlockComments;
    public static String ModulaTokens_EndOfLineComments;
    public static String ModulaTokens_TodoTask;

    public static String DeclarationsSearchGroup_Declarations;
    
    public static String EditorPreferencePage_HlghlightOperatorsStructure;

    public static String EditorPreferencePage_LinkToAnnotationsPage;

    public static String EditorPreferencePage_LinkToColorSettingsPage;

    public static String FindDeclarationsAction_SearchDeclsOfSelInWorkspace;
    public static String FindDeclarationsAction_Workspace;
    public static String FindDeclarationsProjectAction_Project;

    public static String FindDeclarationsProjectAction_SearchDeclsOfSelInPrj;

    public static String FindReferencesAction_SearchRefsToSelInWorkspace;
    public static String FindReferencesAction_Workspace;

    public static String FindReferencesProjectAction_Project;
    public static String FindReferencesProjectAction_SearchRefsToSelInPrj;

    public static String FormatterModifyDialog_CantModifyDefProfile;
    public static String FormatterModifyDialog_Indentation;
    public static String FormatterModifyDialog_LineWrapping;
    public static String FormatterModifyDialog_NewLines;
    public static String FormatterModifyDialog_WhiteSpace;

    public static String FormatterPreferencePage_ActiveProfile;
    public static String FormatterPreferencePage_Edit;
    public static String FormatterPreferencePage_ExportAll;
    public static String FormatterPreferencePage_Import;
    public static String FormatterPreferencePage_New;
    public static String FormatterPreferencePage_Preview;
    public static String FormatterPreferencePage_Profile;
    public static String FormatterPreferencePage_Remove;

    public static String FormatterProfile_AssignInExpr;
    public static String FormatterProfile_AssignInExpr2;
    public static String FormatterProfile_AssignInModuleAlias;
    public static String FormatterProfile_AssignInModuleAlias2;
    
    public static String FormatterProfile_BeginInMod;
    public static String FormatterProfile_BeginInProc;
    public static String FormatterProfile_BeginInProc2;
    public static String FormatterProfile_BinaryOps;
    
    public static String FormatterProfile_CaseInVarRec;
    public static String FormatterProfile_CaseStatements;
    public static String FormatterProfile_Case;
    public static String FormatterProfile_CaseStmt;
    
    public static String FormatterProfile_Colon;
    public static String FormatterProfile_ColonFormParams;
    public static String FormatterProfile_ColonFormParams2;
    public static String FormatterProfile_ColonInCase;
    public static String FormatterProfile_ColonInCase2;
    public static String FormatterProfile_ColonInRetType;
    public static String FormatterProfile_ColonInRetType2;
    public static String FormatterProfile_ColonInTypeDef;
    public static String FormatterProfile_ColonInVar;

    public static String FormatterProfile_Comma;
    public static String FormatterProfile_CommaInEnum;
    public static String FormatterProfile_CommaInFormParams;
    public static String FormatterProfile_CommaInFormParams2;
    public static String FormatterProfile_CommaInImportLists;
    public static String FormatterProfile_CommaInImportLists2;
    public static String FormatterProfile_CommaInMd;
    public static String FormatterProfile_CommaInMd2;

    public static String FormatterProfile_CommaInParList;
    public static String FormatterProfile_CommaInParList2;

    public static String FormatterProfile_Constants;
    public static String FormatterProfile_ConstGlob;
    public static String FormatterProfile_ConstLoc;

    public static String FormatterProfile_ControlStatements;
    public static String FormatterProfile_CtrlStmt;

    public static String FormatterProfile_DeclAndDefs;
    public static String FormatterProfile_Declarations;

    public static String FormatterProfile_Do;
    public static String FormatterProfile_Dot;
    public static String FormatterProfile_Dot2;

    public static String FormatterProfile_ElseInCase;
    public static String FormatterProfile_ElseInIf;
    public static String FormatterProfile_ElseInVarRec;
    public static String FormatterProfile_Elsif;

    public static String FormatterProfile_End;
    public static String FormatterProfile_EndInMod;
    public static String FormatterProfile_EndInMod2;
    public static String FormatterProfile_EndInProc;
    public static String FormatterProfile_EndInProc2;
    public static String FormatterProfile_EndInRec;
    public static String FormatterProfile_EndInVarRec;

    public static String FormatterProfile_EnumType;

    public static String FormatterProfile_Equ;
    public static String FormatterProfile_EquInConst;
    public static String FormatterProfile_EquInMd;
    public static String FormatterProfile_EquInMd2;

    public static String FormatterProfile_Except;
    public static String FormatterProfile_Export;
    public static String FormatterProfile_Expressions;
    public static String FormatterProfile_Finally;
    public static String FormatterProfile_For;
    public static String FormatterProfile_From;
    public static String FormatterProfile_If;
    public static String FormatterProfile_IfStatements;

    public static String FormatterProfile_Import;
    public static String FormatterProfile_ImportSimple;

    public static String FormatterProfile_IndexExpr;

    public static String FormatterProfile_LBracket;
    public static String FormatterProfile_LBracketInExpr;
    public static String FormatterProfile_LBracketInExpr2;
    public static String FormatterProfile_LBracketInRange;

    public static String FormatterProfile_Loop;

    public static String FormatterProfile_LParenth;
    public static String FormatterProfile_LParenthInEnum;
    public static String FormatterProfile_LParenthInExpr;
    public static String FormatterProfile_LParenthInExpr2;
    public static String FormatterProfile_LParenthInProcCall;
    public static String FormatterProfile_LParenthInProcCall2;
    public static String FormatterProfile_LParenthInProcDecl;
    public static String FormatterProfile_LParenthInProcDecl2;

    public static String FormatterProfile_Modules;

    public static String FormatterProfile_OfInCase;
    public static String FormatterProfile_OfInVarRec;

    public static String FormatterProfile_OtherStatements;

    public static String FormatterProfile_ParenthExpr;

    public static String FormatterProfile_Procedure;
    public static String FormatterProfile_Procedures;
    public static String FormatterProfile_ProcInvoc;

    public static String FormatterProfile_PtrDeref;
    public static String FormatterProfile_PtrDeref2;

    public static String FormatterProfile_Range;
    public static String FormatterProfile_Range2;
    public static String FormatterProfile_RangeType;

    public static String FormatterProfile_RBracket;
    public static String FormatterProfile_RBracketInExpr;
    public static String FormatterProfile_RBracketInExpr2;
    public static String FormatterProfile_RBracketInRange;

    public static String FormatterProfile_Record;
    public static String FormatterProfile_RecordType;

    public static String FormatterProfile_Repeat;

    public static String FormatterProfile_RParenth;
    public static String FormatterProfile_RParenthInEnum;
    public static String FormatterProfile_RParenthInExpr;
    public static String FormatterProfile_RParenthInExpr2;
    public static String FormatterProfile_RParenthInProcCall;
    public static String FormatterProfile_RParenthInProcCall2;
    public static String FormatterProfile_RParenthInProcDecl;
    public static String FormatterProfile_RParenthInProcDecl2;

    public static String FormatterProfile_Semicolon;
    public static String FormatterProfile_Semicolon2;
    public static String FormatterProfile_SemicolonFormParams;
    public static String FormatterProfile_SemicolonFormParams2;

    public static String FormatterProfile_Separator;
    public static String FormatterProfile_SeparatorInCase;
    public static String FormatterProfile_SeparatorInCase2;
    public static String FormatterProfile_SeparatorInRecordTypeDef;

    public static String FormatterProfile_SepInCase;
    public static String FormatterProfile_SepInVarRec;

    public static String FormatterProfile_SetsForAllBinaryOps;
    public static String FormatterProfile_SetsForAllUnaryOps;

    public static String FormatterProfile_StmtClosedParenthesis;
    public static String FormatterProfile_StmtClosedParenthesis2;
    public static String FormatterProfile_StmtOpenedParenthesis;
    public static String FormatterProfile_StmtOpenedParenthesis2;

    public static String FormatterProfile_Then;

    public static String FormatterProfile_TypeGlob;
    public static String FormatterProfile_TypeLoc;
    public static String FormatterProfile_Types;

    public static String FormatterProfile_UnaryOps;

    public static String FormatterProfile_Until;

    public static String FormatterProfile_VarGlob;
    public static String FormatterProfile_Variables;
    public static String FormatterProfile_VarLoc;

    public static String FormatterProfile_While;

    public static String FormatterProfile_With;

    public static String FormatterProfile_XdsBuiltIn;

    public static String IndentationTabPage_DeclOfLocalMods;
    public static String IndentationTabPage_DeclOfLocalProcs;
    public static String IndentationTabPage_DeclOfRecFields;
    public static String IndentationTabPage_DeclWithinModule;
    public static String IndentationTabPage_DeclWithinProc;
    public static String IndentationTabPage_DeclWithinVCT;

    public static String IndentationTabPage_GeneralSettings;
    public static String IndentationTabPage_Indent;
    public static String IndentationTabPage_IndentSize;
    public static String IndentationTabPage_Mixed;
    public static String IndentationTabPage_Preview;

    public static String IndentationTabPage_SpacesOnly;
    public static String IndentationTabPage_SpacesToWrappedLines;

    public static String IndentationTabPage_Statements;
    public static String IndentationTabPage_StatementsInCase;
    public static String IndentationTabPage_StatementsInCaseAlt;

    public static String IndentationTabPage_TabPolicy;
    public static String IndentationTabPage_TabSize;
    public static String IndentationTabPage_UseTabs;

	public static String SpellingPreferenceBlock_Advanced;
    public static String SpellingPreferenceBlock_BadDictFile;

    public static String SpellingPreferenceBlock_Browse;
    public static String SpellingPreferenceBlock_Dictionaries;
    public static String SpellingPreferenceBlock_Dictionary;
    public static String SpellingPreferenceBlock_Encoding;
    public static String SpellingPreferenceBlock_IgnoreCapitalization;
    public static String SpellingPreferenceBlock_IgnoreM2Strings;
    public static String SpellingPreferenceBlock_IgnoreMixedCaseWords;
    public static String SpellingPreferenceBlock_IgnoreNonLettersBoundaries;
    public static String SpellingPreferenceBlock_IgnoreSingleLetters;
    public static String SpellingPreferenceBlock_IgnoreUpperCaseWords;
    public static String SpellingPreferenceBlock_IgnoreURLs;
    public static String SpellingPreferenceBlock_IgnoreWordsWithDigits;
    public static String SpellingPreferenceBlock_InvalidInt;
    public static String SpellingPreferenceBlock_MaxProblems;
    public static String SpellingPreferenceBlock_MaxProposals;
    public static String SpellingPreferenceBlock_none;
    public static String SpellingPreferenceBlock_NumberRequired;
    public static String SpellingPreferenceBlock_Options;
    public static String SpellingPreferenceBlock_PlatformDictionary;
    public static String SpellingPreferenceBlock_RWAccessRequired;
    public static String SpellingPreferenceBlock_SelectUserDictionary;
    public static String SpellingPreferenceBlock_UserDefDictionary;
    public static String SpellingPreferenceBlock_UserDictDesc;
    public static String SpellingPreferenceBlock_Variables;

    public static String XdsEditorPreferencePage_link;
    public static String XdsEditorPreferencePage_HighlightBrackets;
    public static String XdsEditorPreferencePage_MatchedBracketsColor;
    public static String XdsEditorPreferencePage_UnmatchedBracketsColor;
    public static String XdsEditorPreferencePage_HighlightInactiveCode;

    public static String XdsOutlineFilter_ConstantsName;
    public static String XdsOutlineFilter_ConstantsDesc;

    public static String XdsOutlineFilter_ImportName;
    public static String XdsOutlineFilter_ImportDesc;

    public static String XdsOutlineFilter_LocModulesName;
    public static String XdsOutlineFilter_LocModulesDesc;

    public static String XdsOutlineFilter_ProceduresName;
    public static String XdsOutlineFilter_ProceduresDesc;

    public static String XdsOutlineFilter_FormalParametersName;
    public static String XdsOutlineFilter_FormalParametersDesc;

    public static String XdsOutlineFilter_TypesName;
    public static String XdsOutlineFilter_TypesDesc;

    public static String XdsOutlineFilter_RecordFieldsName;
    public static String XdsOutlineFilter_RecordFieldsDesc;
    
    public static String XdsOutlineFilter_GlobalVariablesName;
    public static String XdsOutlineFilter_GlobalVariablesDesc;

    public static String XdsOutlineFilter_LocalVariablesName;
    public static String XdsOutlineFilter_LocalVariablesDesc;

    public static String XdsOutlineFiltersDialog_DeselectAll;
    public static String XdsOutlineFiltersDialog_FilterDescription;
    public static String XdsOutlineFiltersDialog_SelectAll;
    public static String XdsOutlineFiltersDialog_SelectToExclude;
    public static String XdsOutlineFiltersDialog_ViewFilters;

    public static String XdsOutlinePage_CollapseAll;
    public static String XdsOutlinePage_ExpandAll;
    public static String XdsOutlinePage_Loading;

    public static String XdsOutlinePage_Filters;
    public static String XdsOutlinePage_LinkWithEditor;
    public static String XdsOutlinePage_Sort;

    public static String XdsOutlinePageContentProvider_0;

    public static String XdsQuickOutlineDialog_Filters;

    public static String XdsQuickOutlineDialog_SortAlphabetically;

    public static String UpdateModel;

    public static String HyperlinkText_ModulaDeclaration;

    public static String OccurrencesMarker_MarkingOccurrences;
    public static String OccurrencesMarker_OccurrenceOf;
    public static String OccurrencesMarker_WriteOccurrencesMarker;

    public static String OpenDeclarations_description;
	public static String OpenDeclarations_label;
	public static String OpenDeclarations_tooltip;

	public static String ProfileManager_ExportProfile;
    public static String ProfileManager_ExportProfiles;
    public static String ProfileManager_ImportedProfiles;
    public static String ProfileManager_ImportProfiles;
    public static String ProfileManager_NoProfilesFound;
    public static String ProfileManager_ReplaceQuestion;

    public static String ReferencesSearchGroup_References;

    
    public static String TabLineWrapping_GeneralSettings;
    public static String TabLineWrapping_InvalidValue;
    public static String TabLineWrapping_MaxLineWidth;
    public static String TabLineWrapping_SetWidthToPreview;

    public static String TabPageWhiteSpace_InsertSpace;
    public static String TabPageWhiteSpace_InsSpaceAfter;
    public static String TabPageWhiteSpace_InsSpaceBefore;

    public static String TabPageNewLines_InsertNewLine;
    public static String TabPageNewLines_InsNewlineAfter;
    public static String TabPageNewLines_InsNewlineBefore;

    public static String SymFileEditor_CannotOpenFile;
    
    public static String ModulaEditor_GrayingMarkers;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

	public static String Spelling_add_askToConfigure_ignoreMessage;

	public static String Spelling_add_askToConfigure_question;

	public static String Spelling_add_askToConfigure_title;

	public static String Spelling_add_info;

	public static String Spelling_addWordProposal;

	public static String Spelling_case_label;

	public static String Spelling_correct_label;

	public static String Spelling_disable_info;

	public static String Spelling_disable_label;

	public static String Spelling_error_case_label;

	public static String Spelling_error_label;

	public static String Spelling_ignore_info;

	public static String Spelling_ignore_label;
	
	public static String SyntaxColoringPreferencePage_Modula2;

    private Messages() {
    }
}
