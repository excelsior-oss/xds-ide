package com.excelsior.xds.ui.editor.internal.preferences.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.ui.IMemento;

import com.excelsior.xds.parser.commons.ast.IElementType;
import com.excelsior.xds.parser.commons.pst.PstCompositeNode;
import com.excelsior.xds.parser.commons.pst.PstLeafNode;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.ast.ModulaElementTypes;
import com.excelsior.xds.parser.modula.ast.tokens.ModulaTokenTypes;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.ProfileManager;
import com.excelsior.xds.ui.editor.internal.preferences.ProfileManager.IProfile;

public class FormatterProfile implements IProfile {
    

    /////////// Indents settings -------------------------------------------
    /////////// Indents settings -------------------------------------------
    /////////// Indents settings -------------------------------------------
    
    private static final int RANGE_CHECKBOX_UNCHECKED_BIT = 0x10000000;
    
    public static String TAB_SIZE_PROPERTY_NAME = "com.excelsior.xds.ui.editor.internal.preferences.formatter.activeTabSize"; //$NON-NLS-1$ 

    public enum IndentSetting {
        UseSpacesToIndentWrappedLines("useSpacesToIndentWrappedLines"), //$NON-NLS-1$
        IndentDeclInModule("indentDeclInModule"), //$NON-NLS-1$
        IndentDeclInProc("indentDeclInProc"), //$NON-NLS-1$
        IndentDeclInVCT("indentDeclInVCT"), //$NON-NLS-1$
        IndentDeclOfLocalProc("indentDeclOfLocalProc", 0, 32, true), //$NON-NLS-1$
        IndentDeclOfLocalMods("indentDeclOfLocalMods", 0, 32, true), //$NON-NLS-1$
        IndentDeclOfRecFields("indentDeclOfRecFields"), //$NON-NLS-1$
        IndentStatements("indentStatements"), //$NON-NLS-1$
        IndentInCaseBody("indentStatementsInCaseBody"), //$NON-NLS-1$
        IndentInCaseAlternative("indentCaseAlternative", 0, 32, true), //$NON-NLS-1$
        IndentSize("indentSize", 0, 32, false), //$NON-NLS-1$
        TabSize("tabSize", 1, 32, false), //$NON-NLS-1$
        TabMode("tabMode", TABMODE_SPACES, TABMODE_MIXED, false); //$NON-NLS-1$
        
        public boolean isRange() {
            return isRange;
        }

        public int getMinVal() {
            return minVal;
        }

        public int getMaxVal() {
            return maxVal;
        }

        public boolean isRangeWithCheckbox() {
            return isRangeWithCheckbox;
        }


        private IndentSetting(String name) {
            this.isRange = false;
            this.name = name;
            this.minVal = 0;
            this.maxVal = 1;
        }
        
        private IndentSetting(String name, int minVal, int maxVal, boolean isRangeWithCheckbox) {
            this.isRange = true;
            this.name = name;
            this.minVal = minVal;
            this.maxVal = maxVal;
            this.isRangeWithCheckbox = isRangeWithCheckbox;
        }
        
        private String name;
        private boolean isRange; // false => boolean
        private int minVal;
        private int maxVal;
        private boolean isRangeWithCheckbox; // setting value is 0 or negative => checked off
    }
    
    // This order:
    public static final int TABMODE_SPACES = 0;
    public static final int TABMODE_TABS   = 1;
    public static final int TABMODE_MIXED  = 2;

    public static final String ID_FORMAT_PREFS = "com.excelsior.xds.ui.editor.internal.preferences.FormatterPreference.Prefs"; //$NON-NLS-1$
    
    private static final String NAME_UNICAL_KEY = "formatterProfileName_34781689923"; // magic name to never accept import from incorrect xml files //$NON-NLS-1$
    private static final String DEF_PROFILE_NAME = Messages.FormatterProfile_XdsBuiltIn;
    
    private String name;
    private boolean isDefProfile;
    private static volatile FormatterProfile cachedActiveProfile;
    private static volatile String cachedActiveProfileName;
    private HashMap<String, Integer> hmIndentSettings;
    
    // Create default profile
    public FormatterProfile() {
        name = DEF_PROFILE_NAME;
        isDefProfile = true;
        // Def settings:
        hmIndentSettings = new HashMap<String, Integer>();

        hmIndentSettings.put(IndentSetting.IndentSize.name, 2);
        hmIndentSettings.put(IndentSetting.TabSize.name, 2);
        hmIndentSettings.put(IndentSetting.TabMode.name, TABMODE_SPACES);
        // Booleans:
        hmIndentSettings.put(IndentSetting.UseSpacesToIndentWrappedLines.name, 1);
        hmIndentSettings.put(IndentSetting.IndentDeclInModule.name, 0);
        hmIndentSettings.put(IndentSetting.IndentDeclInProc.name, 0);
        hmIndentSettings.put(IndentSetting.IndentDeclInVCT.name, 1);
        hmIndentSettings.put(IndentSetting.IndentDeclOfLocalProc.name, 2);
        hmIndentSettings.put(IndentSetting.IndentDeclOfLocalMods.name, 2);
        hmIndentSettings.put(IndentSetting.IndentDeclOfRecFields.name, 1);
        hmIndentSettings.put(IndentSetting.IndentStatements.name, 1);
        hmIndentSettings.put(IndentSetting.IndentInCaseBody.name, 0);
        hmIndentSettings.put(IndentSetting.IndentInCaseAlternative.name, 2);

        // White spaces:
        hmWhiteSettings = new HashMap<WhiteSpaceSetting, Integer>();
        for (WhiteSpaceSetting wss : WhiteSpaceSetting.values()) {
            int tags = (wss.isDefInsSpaceBefore() ? W_TAG_INS_L_SPACE : 0) |
                       (wss.isDefInsSpaceAfter()  ? W_TAG_INS_R_SPACE : 0);
            hmWhiteSettings.put(wss, tags); 
        }
        
        // Statement settings:
        hmStmtSettingsBefore = new HashMap<NewlineSetting, Integer>();
        hmStmtSettingsAfter  = new HashMap<NewlineSetting, Integer>();
        for (NewlineSetting ss : NewlineSetting.values()) {
            hmStmtSettingsBefore.put(ss, ss.insNewLineBeforeDef);
            hmStmtSettingsAfter.put(ss, ss.insNewLineAfterDef);
        }
        
        // Line wrapping settings:
        wrappingWidth = 160;

    }
    
    public FormatterProfile(String name) {
        this();
        this.name = name;
        this.isDefProfile = false;
    }
    
    public FormatterProfile(String name, FormatterProfile copyFrom) {
        copyFrom(copyFrom);
        this.name = name;
    }

    private FormatterProfile(IMemento memento) {
        this(""); //$NON-NLS-1$
        name = memento.getString(NAME_UNICAL_KEY);
        isDefProfile = memento.getBoolean("isDefProfile"); //$NON-NLS-1$
        for (IndentSetting bs : IndentSetting.values()) {
            try {
                int val = memento.getInteger(bs.name);
                boolean unchecked = false;
                if (bs.isRangeWithCheckbox) {
                    unchecked = ((val & RANGE_CHECKBOX_UNCHECKED_BIT) != 0);
                    val &= ~RANGE_CHECKBOX_UNCHECKED_BIT;
                }
                setValue(bs, val, unchecked);
            } catch (Exception e) {} // NPE in old profiles for unknown keys
        }
        for (WhiteSpaceSetting wss : WhiteSpaceSetting.values()) {
            try {
                int val = memento.getInteger(wss.name());
                hmWhiteSettings.put(wss, val);
            } catch (Exception e) {} // NPE in old profiles for unknown keys
        }
        for (NewlineSetting ss : NewlineSetting.values()) {
            try {
                int val = ss.insNewLineBeforeDef;
                if (val >= 0) {
                    val = memento.getInteger(ss.name() + "_Before"); //$NON-NLS-1$
                }
                hmStmtSettingsBefore.put(ss, val);
                
                val = ss.insNewLineAfterDef;
                if (val >= 0) {
                    val = memento.getInteger(ss.name() + "_After");  //$NON-NLS-1$
                }
                hmStmtSettingsAfter.put(ss, val);
            } catch (Exception e) {} // NPE in old profiles for unknown keys
        }
        try {
            wrappingWidth = memento.getInteger(WRAPPING_WIDTH_MEMENTO_KEY); 
        } catch (Exception e) {} // NPE in old profiles for unknown keys
    }

    public void toMemento(IMemento memento) {
        cachedActiveProfile = null;     // smth changed. drop cache
        cachedActiveProfileName = null; //

        memento.putString(NAME_UNICAL_KEY, name);
        memento.putBoolean("isDefProfile", isDefProfile); //$NON-NLS-1$
        
        for (IndentSetting bs : IndentSetting.values()) {
            memento.putInteger(bs.name, hmIndentSettings.get(bs.name));
        }
        for (WhiteSpaceSetting wss : WhiteSpaceSetting.values()) {
            memento.putInteger(wss.name(), hmWhiteSettings.get(wss));
        }
        for (NewlineSetting ss : NewlineSetting.values()) {
            memento.putInteger(ss.name() + "_Before", hmStmtSettingsBefore.get(ss)); //$NON-NLS-1$
            memento.putInteger(ss.name() + "_After",  hmStmtSettingsAfter.get(ss)); //$NON-NLS-1$
        }
        memento.putInteger(WRAPPING_WIDTH_MEMENTO_KEY, wrappingWidth); 
    }

    /**
     * There are 2 ways to get active profile: initialize whole ProfileManager with all profiles OR
     * use this method.
     * @return
     */
    public static FormatterProfile getActiveProfile() {
        String actName = ProfileManager.readActiveProfileNameFromStore(XdsEditorsPlugin.getDefault().getPreferenceStore(), ID_FORMAT_PREFS);
        if (cachedActiveProfile != null && cachedActiveProfileName != null && cachedActiveProfileName.equals(actName)) {
            return cachedActiveProfile;
        }
        
        FormatterProfile fp = new FormatterProfile(); // def. profile
        if (actName != null && !actName.isEmpty() && !actName.equals(DEF_PROFILE_NAME)) { 
            IProfile ip = ProfileManager.readProfileFromStore(actName, XdsEditorsPlugin.getDefault().getPreferenceStore(), ID_FORMAT_PREFS, fp);
            if (ip != null && actName.equals(ip.getName())) {
                fp = (FormatterProfile)ip;
            }
        }

        cachedActiveProfileName = actName;
        cachedActiveProfile     = fp;
        return fp;
    }
        
    //// Getters/setters:
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isDefaultProfile() {
        return isDefProfile;
    }
    
    @Override
    public void reActivate() {
        // Editors listens for this value and redraws tabs:
        XdsEditorsPlugin.getDefault().getPreferenceStore().setValue(TAB_SIZE_PROPERTY_NAME, hmIndentSettings.get(IndentSetting.TabSize.name));
    }


//    @Override
//    public IProfile createFromXml(String xml, String subRoot) {
////        FormatterProfile fp = new FormatterProfile(xml, subRoot, false);
////        return fp.name.isEmpty() ? null : fp;
//        return null;
//    }

    @Override
    public IProfile createFromMemento(IMemento memento) {
        FormatterProfile fp = new FormatterProfile(memento);
        return fp.name.isEmpty() ? null : fp; 
    }

    @Override 
    public IProfile createFromProfile(IProfile from, String newName) {
        FormatterProfile fp = new FormatterProfile(newName);
        fp.copyFrom((FormatterProfile)from);
        fp.setName(newName);
        return fp;
    }


    @Override
    /**
     * Copies all except 'isDefault', it will be 'false'
     * 'ip' - FormattedProfile
     */
        public void copyFrom(IProfile ip) {
        FormatterProfile fp = (FormatterProfile)ip; 

        isDefProfile = false;
        name         = fp.name;
        
        hmIndentSettings     = new HashMap<String, Integer>(fp.hmIndentSettings);
        hmWhiteSettings      = new HashMap<FormatterProfile.WhiteSpaceSetting, Integer>(fp.hmWhiteSettings);
        hmStmtSettingsBefore = new HashMap<FormatterProfile.NewlineSetting, Integer>(fp.hmStmtSettingsBefore);
        hmStmtSettingsAfter  = new HashMap<FormatterProfile.NewlineSetting, Integer>(fp.hmStmtSettingsAfter);
        
        wrappingWidth = fp.wrappingWidth;
    }
    
    
    //------------------------------ Getters/setters for indent settings: -------------------------------------------

    public boolean getAsBoolean(IndentSetting bs) {
        int v = hmIndentSettings.get(bs.name);
        return v != 0;
    }

    public int getValue(IndentSetting bs) {
        int val = hmIndentSettings.get(bs.name);
        if (bs.isRangeWithCheckbox && ((val & RANGE_CHECKBOX_UNCHECKED_BIT) != 0)) {
            return 0;
        }
        return val;
    }

    public int getValueForDialog(IndentSetting bs) {
        int val = hmIndentSettings.get(bs.name);
        if (bs.isRangeWithCheckbox) {
            val &= ~RANGE_CHECKBOX_UNCHECKED_BIT;
        }
        return val;
    }


    public boolean getRangeCheckboxUncheckedState(IndentSetting bs) {
        if (bs.isRangeWithCheckbox) {
            int val = hmIndentSettings.get(bs.name);
            return (val & RANGE_CHECKBOX_UNCHECKED_BIT) != 0;
        } 
        return false;
    }

    public void setBoolean(IndentSetting bs, boolean val) {
        hmIndentSettings.put(bs.name, val ? 1 : 0);
    }

    public void setValue(IndentSetting bs, int val) {
        setValue(bs, val, false);
        
    }

    public void setValue(IndentSetting bs, int val, boolean isRangeCheckboxUnchecked) {
        val = Math.max(val,  bs.getMinVal());
        val = Math.min(val,  bs.getMaxVal());
        if (bs.isRangeWithCheckbox && isRangeCheckboxUnchecked) {
            val |= RANGE_CHECKBOX_UNCHECKED_BIT;
        }
        hmIndentSettings.put(bs.name, val);
    }

    
    /////////// White space settings -------------------------------------------
    /////////// White space settings -------------------------------------------
    /////////// White space settings -------------------------------------------

    private HashMap<WhiteSpaceSetting, Integer> hmWhiteSettings;

    // default settings:
    private static final int W_TAG_INS_L_SPACE = 0x00000001;
    private static final int W_TAG_INS_R_SPACE = 0x00000002;

    public enum WhiteSpaceCategory {

        DeclImport(Messages.FormatterProfile_Import, "", null),  //$NON-NLS-1$
        
            DeclVariables(Messages.FormatterProfile_Variables, "", null),  //$NON-NLS-1$
                DeclTypesRange(Messages.FormatterProfile_RangeType, "", null), //$NON-NLS-1$
                DeclTypesEnum(Messages.FormatterProfile_EnumType, "", null),  //$NON-NLS-1$
                DeclTypesRecord(Messages.FormatterProfile_RecordType, "", null), //$NON-NLS-1$
            DeclTypes(Messages.FormatterProfile_Types, "", new WhiteSpaceCategory[]{DeclTypesRange, DeclTypesEnum, DeclTypesRecord}),  //$NON-NLS-1$
            DeclConstants(Messages.FormatterProfile_Constants, "", null),  //$NON-NLS-1$
            DeclProcedures(Messages.FormatterProfile_Procedures, "", null), //$NON-NLS-1$
        Declarations(Messages.FormatterProfile_DeclAndDefs, "", new WhiteSpaceCategory[]{DeclVariables, DeclTypes, DeclConstants, DeclProcedures}),  //$NON-NLS-1$

            StmtCase(Messages.FormatterProfile_CaseStmt, "", null),  //$NON-NLS-1$
        Statements(Messages.FormatterProfile_CtrlStmt, "", new WhiteSpaceCategory[]{StmtCase}), //$NON-NLS-1$

            BinaryOperations(Messages.FormatterProfile_BinaryOps, Messages.FormatterProfile_SetsForAllBinaryOps, null),
            UnaryOperations(Messages.FormatterProfile_UnaryOps, Messages.FormatterProfile_SetsForAllUnaryOps, null),
            ParenthesizedExpressions(Messages.FormatterProfile_ParenthExpr, "", null), //$NON-NLS-1$
            IndexExpressions(Messages.FormatterProfile_IndexExpr, "", null),  //$NON-NLS-1$
            ProcedureInvocations(Messages.FormatterProfile_ProcInvoc, "", null), //$NON-NLS-1$
        Expressions(Messages.FormatterProfile_Expressions, "", new WhiteSpaceCategory[]{BinaryOperations, UnaryOperations, ParenthesizedExpressions, IndexExpressions, ProcedureInvocations}),  //$NON-NLS-1$
        
        RootCategory("", "", new WhiteSpaceCategory[]{ DeclImport     //$NON-NLS-1$ //$NON-NLS-2$
                                                     , Declarations
                                                     , Statements
                                                     , Expressions} ); 
        
        private WhiteSpaceCategory(String name, String settingsString, WhiteSpaceCategory children[]) {
            this.name = name;
            this.settingsString = settingsString;
            this.children = children;
        }
        
        @Override
        public String toString() {
            return name;
        }

        public String toSettingsString() {
            return settingsString.isEmpty() ? name : settingsString;
        }

        
        public WhiteSpaceCategory getParent() {
            for (WhiteSpaceCategory wsc : WhiteSpaceCategory.values()) {
                if (wsc.children != null) {
                    for (WhiteSpaceCategory ccc : wsc.children) {
                        if (this.equals(ccc)) {
                            return wsc;
                        }
                    }
                }
            }
            return null;
        }

        public ArrayList<Object> getChildren() {
            ArrayList<Object> al = new ArrayList<Object>();
            if (children != null) {
                for (WhiteSpaceCategory wsc : children) {
                    al.add(wsc);
                }
            }
            for (WhiteSpaceSetting wss : WhiteSpaceSetting.values()) {
                if (this.equals(wss.getCategory())) {
                    al.add(wss);
                }
            }
            return al;
        }
        
        private WhiteSpaceCategory children[];
        private String name;
        private String settingsString;
    }

    
    
    public enum WhiteSpaceSetting {
        // !!!
        // NOTE: the same ModulaTokenTypes settings are searched in the this definition order, first matched one will be found!
        // !!!
        
        // Declarations / Variables:
        WSpaceCommaInVars      (ModulaTokenTypes.COMMA, ModulaElementTypes.VARIABLE_LIST,        WhiteSpaceCategory.DeclVariables, 
                "-+", Messages.FormatterProfile_CommaInMd, Messages.FormatterProfile_CommaInMd2), //$NON-NLS-1$
        WSpaceColonInDeclVars  (ModulaTokenTypes.COLON, ModulaElementTypes.VARIABLE_DECLARATION, WhiteSpaceCategory.DeclVariables, 
                "-+", Messages.FormatterProfile_Colon, Messages.FormatterProfile_ColonInVar), //$NON-NLS-1$
        
        // Declarations / Constants:
        WSpaceEquInDeclConsts  (ModulaTokenTypes.EQU, ModulaElementTypes.CONSTANT_DECLARATION,   WhiteSpaceCategory.DeclConstants, 
                "++", Messages.FormatterProfile_Equ, Messages.FormatterProfile_EquInConst), //$NON-NLS-1$
        
        // Declarations / Types:
        WSpaceEquInDeclTypes    (ModulaTokenTypes.EQU,         ModulaElementTypes.TYPE_DECLARATION,            WhiteSpaceCategory.DeclTypes, 
                "++", Messages.FormatterProfile_EquInMd, Messages.FormatterProfile_EquInMd2), //$NON-NLS-1$
        WSpaceLBracketInRange   (ModulaTokenTypes.LBRACKET,    ModulaElementTypes.RANGE_TYPE_DEFINITION,       WhiteSpaceCategory.DeclTypesRange, 
                "+-", Messages.FormatterProfile_LBracket, Messages.FormatterProfile_LBracketInRange), //$NON-NLS-1$
        WSpaceRBracketInRange   (ModulaTokenTypes.RBRACKET,    ModulaElementTypes.RANGE_TYPE_DEFINITION,       WhiteSpaceCategory.DeclTypesRange, 
                "-+", Messages.FormatterProfile_RBracket, Messages.FormatterProfile_RBracketInRange), //$NON-NLS-1$
        WSpaceRange             (ModulaTokenTypes.RANGE,       ModulaElementTypes.RANGE_TYPE_DEFINITION,       WhiteSpaceCategory.DeclTypesRange, 
                "--", Messages.FormatterProfile_Range, Messages.FormatterProfile_Range2), //$NON-NLS-1$
        WSpaceCommaInEnum       (ModulaTokenTypes.COMMA,       ModulaElementTypes.ENUMERATION_TYPE_DEFINITION, WhiteSpaceCategory.DeclTypesEnum, 
                "++", Messages.FormatterProfile_Comma, Messages.FormatterProfile_CommaInEnum), //$NON-NLS-1$
        WSpaceLParenthInEnum    (ModulaTokenTypes.LPARENTH,    ModulaElementTypes.ENUMERATION_TYPE_DEFINITION, WhiteSpaceCategory.DeclTypesEnum, 
                "+-", Messages.FormatterProfile_LParenth, Messages.FormatterProfile_LParenthInEnum), //$NON-NLS-1$
        WSpaceRParenthInEnum    (ModulaTokenTypes.RPARENTH,    ModulaElementTypes.ENUMERATION_TYPE_DEFINITION, WhiteSpaceCategory.DeclTypesEnum, 
                "-+", Messages.FormatterProfile_RParenth, Messages.FormatterProfile_RParenthInEnum), //$NON-NLS-1$
        WSpaceColonInRecDef     (ModulaTokenTypes.COLON,       ModulaElementTypes.RECORD_TYPE_DEFINITION,      WhiteSpaceCategory.DeclTypesRecord, 
                "-+", Messages.FormatterProfile_Colon, Messages.FormatterProfile_ColonInTypeDef), //$NON-NLS-1$
        WSpaceSepInRecDef       (ModulaTokenTypes.SEP,         ModulaElementTypes.RECORD_TYPE_DEFINITION,      WhiteSpaceCategory.DeclTypesRecord, 
                "++", Messages.FormatterProfile_Separator, Messages.FormatterProfile_SeparatorInRecordTypeDef),  //$NON-NLS-1$
      
        // Declarations / Procedures:
        WSpaceLParenthInProc    (ModulaTokenTypes.LPARENTH,    ModulaElementTypes.FORMAL_PARAMETER_BLOCK,       WhiteSpaceCategory.DeclProcedures, 
                "--", Messages.FormatterProfile_LParenthInProcDecl, Messages.FormatterProfile_LParenthInProcDecl2), //$NON-NLS-1$
        WSpaceRParenthInProc    (ModulaTokenTypes.RPARENTH,    ModulaElementTypes.FORMAL_PARAMETER_BLOCK,       WhiteSpaceCategory.DeclProcedures, 
                "--", Messages.FormatterProfile_RParenthInProcDecl, Messages.FormatterProfile_RParenthInProcDecl2), //$NON-NLS-1$
        WSpaceCommaInFormParams (ModulaTokenTypes.COMMA,       ModulaElementTypes.FORMAL_PARAMETER_LIST,        WhiteSpaceCategory.DeclProcedures,
                "-+", Messages.FormatterProfile_CommaInFormParams, Messages.FormatterProfile_CommaInFormParams2), //$NON-NLS-1$
        WSpaceSemicolonInFormParams(ModulaTokenTypes.SEMICOLON,ModulaElementTypes.FORMAL_PARAMETER_DECLARATION, WhiteSpaceCategory.DeclProcedures, 
                "-+", Messages.FormatterProfile_SemicolonFormParams, Messages.FormatterProfile_SemicolonFormParams2), //$NON-NLS-1$
        WSpaceColonInParams     (ModulaTokenTypes.COLON,       ModulaElementTypes.FORMAL_PARAMETER_DECLARATION, WhiteSpaceCategory.DeclProcedures,
                "-+", Messages.FormatterProfile_ColonFormParams, Messages.FormatterProfile_ColonFormParams2), //$NON-NLS-1$
        WSpaceColonInRetType    (ModulaTokenTypes.COLON,       ModulaElementTypes.RESULT_TYPE,                  WhiteSpaceCategory.DeclProcedures,
                "-+", Messages.FormatterProfile_ColonInRetType, Messages.FormatterProfile_ColonInRetType2), //$NON-NLS-1$

        // Declarations / Import modules:
        WSpaceCommaInImport     (ModulaTokenTypes.COMMA,       ModulaElementTypes.IMPORT_FRAGMENT_LIST, WhiteSpaceCategory.DeclImport, 
                "-+", Messages.FormatterProfile_CommaInImportLists, Messages.FormatterProfile_CommaInImportLists2, 1), //$NON-NLS-1$
        WSpaceBecomesInImport   (ModulaTokenTypes.BECOMES,     ModulaElementTypes.ALIAS_DECLARATION,    WhiteSpaceCategory.DeclImport, 
                "++", Messages.FormatterProfile_AssignInModuleAlias, Messages.FormatterProfile_AssignInModuleAlias2), //$NON-NLS-1$

        // Control statements / Case statement:
        WSpaceColon             (ModulaTokenTypes.COLON,       null,                            WhiteSpaceCategory.StmtCase,
                "-+", Messages.FormatterProfile_ColonInCase, Messages.FormatterProfile_ColonInCase2), // def after WSpaceColonInParams, WSpaceColonInRetType, WSpaceColonInRecDef //$NON-NLS-1$
        WSpaceSep               (ModulaTokenTypes.SEP,         null,                            WhiteSpaceCategory.StmtCase, 
                "++", Messages.FormatterProfile_SeparatorInCase, Messages.FormatterProfile_SeparatorInCase2),  //$NON-NLS-1$
        
        // Control statements:
        WSpaceLParenthInStmt    (ModulaTokenTypes.LPARENTH,    null,   WhiteSpaceCategory.Statements,
                "+-", Messages.FormatterProfile_StmtOpenedParenthesis, Messages.FormatterProfile_StmtOpenedParenthesis2), //  def before WSpaceLParenthInExpr //$NON-NLS-1$
        WSpaceRParenthInStmt    (ModulaTokenTypes.RPARENTH,    null,   WhiteSpaceCategory.Statements,
                "-+", Messages.FormatterProfile_StmtClosedParenthesis, Messages.FormatterProfile_StmtClosedParenthesis2),  // def before WSpaceRParenthInExpr //$NON-NLS-1$
                
        // Expressions / Procedure invocations:
        WSpaceLParenthInProcCall(ModulaTokenTypes.LPARENTH,    ModulaElementTypes.DESIGNATOR,           WhiteSpaceCategory.ProcedureInvocations,
                "--", Messages.FormatterProfile_LParenthInProcCall, Messages.FormatterProfile_LParenthInProcCall2), //$NON-NLS-1$
        WSpaceRParenthInProcCall(ModulaTokenTypes.RPARENTH,    ModulaElementTypes.DESIGNATOR,           WhiteSpaceCategory.ProcedureInvocations, 
                "--", Messages.FormatterProfile_RParenthInProcCall, Messages.FormatterProfile_RParenthInProcCall2), //$NON-NLS-1$
        WSpaceCommaInCall       (ModulaTokenTypes.COMMA,       ModulaElementTypes.DESIGNATOR,           WhiteSpaceCategory.ProcedureInvocations,
                "-+", Messages.FormatterProfile_CommaInParList, Messages.FormatterProfile_CommaInParList2), //$NON-NLS-1$

        // Expressions / Binary operations: (shrinked in settings dialog into one "Binary operations" item)
        WSpaceBinaryPlus        (ModulaTokenTypes.PLUS,        ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations,
                "++", "Plus '+'", "Plus '+'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryMinus       (ModulaTokenTypes.MINUS,       ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations,
                "++", "Minus '-'", "Minus '-'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryTimes       (ModulaTokenTypes.TIMES,       ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations, 
                "++", "Multiplication '*'", "Multiplication '*'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinarySlash       (ModulaTokenTypes.SLASH,       ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations, 
                "++", "Division '/'", "Division '/'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryAnd         (ModulaTokenTypes.AND,         ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations, 
                "++", "And '&'", "And '&'", 1), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryEqu         (ModulaTokenTypes.EQU,         ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations, 
                "++", "Equal '='", "Equal '='"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryNeq         (ModulaTokenTypes.NEQ,         ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations, 
                "++", "Not equal '#' or '<>'", "Not equal '#' or '<>'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryLss         (ModulaTokenTypes.LSS,         ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations,
                "++", "Lower '<'", "Lower '<'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryGtr         (ModulaTokenTypes.GTR,         ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations,
                "++", "Greater '>'", "Greater '>'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryLteq        (ModulaTokenTypes.LTEQ,        ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations, 
                "++", "Lower or Equal '<='", "Lower or equal '<='"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryGteq        (ModulaTokenTypes.GTEQ,        ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations, 
                "++", "Greater or Equal '>='", "Greater or equal '>='"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryLShift      (ModulaTokenTypes.LEFT_SHIFT,  ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations,
                "++", "Left shift '<<'", "Left shift '<<'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryRShift      (ModulaTokenTypes.RIGHT_SHIFT, ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations,
                "++", "Right shift '>>'", "Right shift '>>'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        WSpaceBinaryExponent    (ModulaTokenTypes.EXPONENT,    ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.BinaryOperations,
                "++", "Exponent '**'", "Exponent '**'"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        // Expressions / Unary operations: (shrinked in settings dialog into one "Unary operations" item)
        WSpaceUnaryNot          (ModulaTokenTypes.NOT,         ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.UnaryOperations,
                "+-", "Not operation '~'", "Not operation '~'", 1),  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //XXX hbz how to recognize unary +/- -- see tree for x := 2.3e-1 - - (- 1 +2 + -3);        
        //      UnaryPlus(ModulaTokenTypes.PLUS, WhiteSpaceCategory.RootCategory, 
        //              new IElementType[]{ModulaElementTypes.EXPRESSION, ModulaElementTypes.FACTOR}, "+-", "Unary plus '+'"),
        //      UnaryMinus(ModulaTokenTypes.MINUS, WhiteSpaceCategory.RootCategory, 
        //              new IElementType[]{ModulaElementTypes.EXPRESSION, ModulaElementTypes.FACTOR}, "+-", "Unary minus '-'"),

        // Expressions / Parenthesized expressions:
        WSpaceLParenthInExpr    (ModulaTokenTypes.LPARENTH,    ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.ParenthesizedExpressions,
                "--", Messages.FormatterProfile_LParenthInExpr, Messages.FormatterProfile_LParenthInExpr2), // def after WSpaceLParenthInProc* //$NON-NLS-1$
        WSpaceRParenthInExpr    (ModulaTokenTypes.RPARENTH,    ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.ParenthesizedExpressions,
                "--", Messages.FormatterProfile_RParenthInExpr, Messages.FormatterProfile_RParenthInExpr2), // def after WSpaceRParenthInProc* //$NON-NLS-1$

        // Expressions / Index expressions:
        WSpaceLBracket          (ModulaTokenTypes.LBRACKET,    ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.IndexExpressions, 
                "--", Messages.FormatterProfile_LBracketInExpr, Messages.FormatterProfile_LBracketInExpr2), //$NON-NLS-1$
        WSpaceRBracket          (ModulaTokenTypes.RBRACKET,    ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.IndexExpressions, 
                "--", Messages.FormatterProfile_RBracketInExpr, Messages.FormatterProfile_RBracketInExpr2), //$NON-NLS-1$
  
        // Expressions:
        WSpaceBinaryBecomes     (ModulaTokenTypes.BECOMES,     null,                            WhiteSpaceCategory.Expressions,
                "++", Messages.FormatterProfile_AssignInExpr, Messages.FormatterProfile_AssignInExpr2), //$NON-NLS-1$
        WSpaceRUnaryBar         (ModulaTokenTypes.BAR,         ModulaElementTypes.EXPRESSION,   WhiteSpaceCategory.Expressions,
                "-+", Messages.FormatterProfile_PtrDeref, Messages.FormatterProfile_PtrDeref2), //$NON-NLS-1$
        
        // Not categorized:
        WSpaceSemicolon         (ModulaTokenTypes.SEMICOLON,   null,                            WhiteSpaceCategory.RootCategory, 
                "-+", Messages.FormatterProfile_Semicolon, Messages.FormatterProfile_Semicolon2), // hidden in settings dialog //$NON-NLS-1$
        WSpaceDot               (ModulaTokenTypes.DOT,         null,                            WhiteSpaceCategory.RootCategory, 
                "--", Messages.FormatterProfile_Dot, Messages.FormatterProfile_Dot2); // hidden in settings dialog //$NON-NLS-1$
        // don't support it: WSpaceAlias(ModulaTokenTypes.ALIAS, null, WhiteSpaceCategory.Declarations, "++", "Alias '::='"),
        
        private IElementType getElementType() {
            return etype;
        }
        
        private boolean testElement(PstLeafNode pln) {
            if (etype.equals(pln.getElementType())) {
                if (len > 0 && len != pln.getLength()) {
                    return false;
                }
                if (this.equals(WSpaceLParenthInStmt)) {
                    PstNode stmt = isFirstChild(pln, ModulaElementTypes.FACTOR);
                    stmt = isFirstChild(stmt, ModulaElementTypes.TERM);
                    
                    if (isFirstChild(stmt, ModulaElementTypes.CASE_VARIANT_SELECTOR) != null) {
                        return true; // CASE (smth) OF
                    }
                    stmt = isFirstChild(stmt, ModulaElementTypes.EXPRESSION);
                    
                    if (stmt != null && stmt.getParent() != null) {
                       IElementType et = stmt.getParent().getElementType();
                       return et.equals( ModulaElementTypes.FOR_STATEMENT) ||
                               et.equals( ModulaElementTypes.IF_STATEMENT) ||
                               et.equals( ModulaElementTypes.WHILE_STATEMENT) ||
                               et.equals( ModulaElementTypes.REPEAT_STATEMENT) ||
                               et.equals( ModulaElementTypes.RETURN_STATEMENT);
                    } 
                    return false;
                    
                } else if (this.equals(WSpaceRParenthInStmt)) {
                    PstNode stmt = isLastChild(pln, ModulaElementTypes.FACTOR);
                    stmt = isLastChild(stmt, ModulaElementTypes.TERM);

                    if (isFirstChild(stmt, ModulaElementTypes.CASE_VARIANT_SELECTOR) != null) {
                        return true; // CASE (smth) OF
                    }
                    stmt = isLastChild(stmt, ModulaElementTypes.EXPRESSION);
                    
                    if (stmt != null && stmt.getParent() != null) {
                        IElementType et = stmt.getParent().getElementType();
                       return et.equals( ModulaElementTypes.FOR_STATEMENT) ||
                               et.equals( ModulaElementTypes.IF_STATEMENT) ||
                               et.equals( ModulaElementTypes.WHILE_STATEMENT) ||
                               et.equals( ModulaElementTypes.REPEAT_STATEMENT);
                    } 
                    return false;
                }
                
                if (hsParentType == null) {
                    return true;
                }
                for (PstNode n = pln.getParent(); n != null; n = n.getParent()) {
                    if (hsParentType.contains(n.getElementType())) {
                        return true;
                    }
                }
                
            }
            return false;
        }
        
        private PstNode isFirstChild(PstNode child, IElementType expectedParentType) {
            try {
                PstNode parent = child.getParent();
                if (parent != null &&
                    parent.getElementType().equals(expectedParentType) &&
                    ((PstCompositeNode)parent).getChildren().get(0) == child) 
                {
                    return parent;
                }
            } catch (Exception e) {}
            return null;
        }
        
        private PstNode isLastChild(PstNode child, IElementType expectedParentType) {
            try {
                PstNode parent = child.getParent();
                if (parent != null &&
                    parent.getElementType().equals(expectedParentType)) 
                {
                    List<PstNode> children = ((PstCompositeNode)parent).getChildren();
                    if (children.get(children.size()-1) == child) { 
                        return parent;
                    }
                }
            } catch (Exception e) {}
            return null;
        }
        
        /////////////
        
        private WhiteSpaceSetting (IElementType etype, WhiteSpaceCategory cat, IElementType parentTypes[], String defSettings, 
                                   String propText, String settingsText, int len) 
        {
            this.etype = etype;
            this.wcategory = cat;
            this.propText = propText;
            this.settingsText = settingsText;
            this.len = len;
            this.isDefInsSpaceBefore = defSettings.charAt(0)=='+';
            this.isDefInsSpaceAfter  = defSettings.charAt(1)=='+';
            if (parentTypes != null) {
                this.hsParentType = new HashSet<IElementType>(parentTypes.length);
                for (IElementType et : parentTypes) {
                    hsParentType.add(et);
                }
            }
        }
        
        private WhiteSpaceSetting (IElementType etype, IElementType parentType, WhiteSpaceCategory cat, String defSettings, 
                                   String propText, String genetiveText) 
        {
            this(etype, cat, parentType == null ? null : new IElementType[]{parentType}, defSettings, propText, genetiveText, 0);
        }
        
        private WhiteSpaceSetting (IElementType etype, IElementType parentType, WhiteSpaceCategory cat, String defSettings, 
                                   String propText, String genetiveText, int len) 
        {
            this(etype, cat, parentType == null ? null : new IElementType[]{parentType}, defSettings, propText, genetiveText, len);
        }

        @Override
        public String toString() {
            return propText;
        }
        
        public String toSettingsString() {
            return settingsText.isEmpty() ? toString() : settingsText;
        }
        
        public boolean isDefInsSpaceBefore() {
            return isDefInsSpaceBefore;
        }
        
        public boolean isDefInsSpaceAfter() {
            return isDefInsSpaceAfter;
        }
        
        public WhiteSpaceCategory getCategory() {
            return wcategory;
        }
        
        private IElementType etype;
        private HashSet<IElementType> hsParentType;
        private WhiteSpaceCategory wcategory;
        private String propText;
        private String settingsText;
        private int len;  // 0 or length of the token (to recognize "&" and "AND" and so on)

        private boolean isDefInsSpaceBefore;
        private boolean isDefInsSpaceAfter;

    } // WhiteSpaceSetting
    
    private static HashMap<IElementType, ArrayList<WhiteSpaceSetting>> hmToSearchWhites;
    static {
        hmToSearchWhites = new HashMap<IElementType, ArrayList<WhiteSpaceSetting>>();
        for (WhiteSpaceSetting wss : WhiteSpaceSetting.values()) {
            IElementType et = wss.getElementType();
            ArrayList<WhiteSpaceSetting> al = hmToSearchWhites.get(et);
            if (al != null) {
                al.add(wss);
            } else {
                al = new ArrayList<FormatterProfile.WhiteSpaceSetting>();
                al.add(wss);
                hmToSearchWhites.put(et,  al);
            }
        }
    }
    
    public WhiteSpaceSetting searchWhiteSpaceSetting(PstLeafNode pln) {
        ArrayList<WhiteSpaceSetting> al = hmToSearchWhites.get(pln.getElementType());
        if (al != null) {
            for (WhiteSpaceSetting wss : al) {
                if (wss.testElement(pln)) {
                    return wss;
                }
            }
        }
        return null;
    }
    
    public boolean isInsSpaceBefore(WhiteSpaceSetting wss) {
        return (hmWhiteSettings.get(wss) & W_TAG_INS_L_SPACE) != 0;
    }
    
    public boolean isInsSpaceAfter(WhiteSpaceSetting wss) {
        return (hmWhiteSettings.get(wss) & W_TAG_INS_R_SPACE) != 0;
    }
    
    public void setInsSpaceBefore(WhiteSpaceSetting wss, boolean ins) {
        int bits = hmWhiteSettings.get(wss);
        if (ins) {
            bits |= W_TAG_INS_L_SPACE;
        } else {
            bits &= ~W_TAG_INS_L_SPACE;
        }
        hmWhiteSettings.put(wss, bits);
    }
    
    public void setInsSpaceAfter(WhiteSpaceSetting wss, boolean ins) {
        int bits = hmWhiteSettings.get(wss);
        if (ins) {
            bits |= W_TAG_INS_R_SPACE;
        } else {
            bits &= ~W_TAG_INS_R_SPACE;
        }
        hmWhiteSettings.put(wss, bits);
    }
    
    
    /////////// Control statements settings -------------------------------------------
    /////////// Control statements settings -------------------------------------------
    /////////// Control statements settings -------------------------------------------

    private HashMap<NewlineSetting, Integer> hmStmtSettingsBefore;
    private HashMap<NewlineSetting, Integer> hmStmtSettingsAfter;

    private enum SpecialCheckId {
        None,
        CheckParent,
        InGlobalScope,
        InLocalScope,
        RecordVariant, // parent == RECORD_VARIANT_LIST or RECORD_VARIANT
        ModuleEnd,     // parent == null or LOCAL_MODULE
    }
    

//    public enum NewlineSettingCategory {
//        NlscRoot,
//        NlscDeclarations,
//        NlscDeclTypes,
//        NlscDeclProcs,
//        NlscDeclMods,
//        NlscStatements,
//        NlscStmtIf,
//        NlscStmtCase,
//        NlscStmtOther
//    }

    
    public enum NewlineSettingCategory {
        NlscDeclMods(Messages.FormatterProfile_Modules, "", null), //$NON-NLS-1$
        NlscDeclTypes(Messages.FormatterProfile_Types, "", null), //$NON-NLS-1$
        NlscDeclProcs(Messages.FormatterProfile_Procedures, "", null), //$NON-NLS-1$
        NlscDeclarations(Messages.FormatterProfile_Declarations, "", new NewlineSettingCategory[]{NlscDeclMods, NlscDeclTypes, NlscDeclProcs}), //$NON-NLS-1$

        NlscStmtIf(Messages.FormatterProfile_IfStatements, "", null), //$NON-NLS-1$
        NlscStmtCase(Messages.FormatterProfile_CaseStatements, "", null), //$NON-NLS-1$
        NlscStmtOther(Messages.FormatterProfile_OtherStatements, "", null), //$NON-NLS-1$
        NlscStatements(Messages.FormatterProfile_ControlStatements, "", new NewlineSettingCategory[]{NlscStmtIf, NlscStmtCase, NlscStmtOther}), //$NON-NLS-1$
        
        NlscRoot("", "", new NewlineSettingCategory[]{NlscDeclarations, NlscStatements}); //$NON-NLS-1$ //$NON-NLS-2$
        
        private NewlineSettingCategory(String name, String settingsString, NewlineSettingCategory children[]) {
            this.name = name;
            this.settingsString = settingsString;
            this.children = children;
        }
        
        @Override
        public String toString() {
            return name;
        }

        public String toSettingsString() {
            return settingsString.isEmpty() ? name : settingsString;
        }

        
        public NewlineSettingCategory getParent() {
            for (NewlineSettingCategory nsc : NewlineSettingCategory.values()) {
                if (nsc.children != null) {
                    for (NewlineSettingCategory ccc : nsc.children) {
                        if (this.equals(ccc)) {
                            return nsc;
                        }
                    }
                }
            }
            return null;
        }

        public ArrayList<Object> getChildren() {
            ArrayList<Object> al = new ArrayList<Object>();
            if (children != null) {
                for (NewlineSettingCategory nsc : children) {
                    al.add(nsc);
                }
            }
            for (NewlineSetting nss : NewlineSetting.values()) {
                if (this.equals(nss.getCategory())) {
                    al.add(nss);
                }
            }
            return al;
        }
        
        private NewlineSettingCategory children[];
        private String name;
        private String settingsString;
    }

    
    public enum NewlineSetting{
        // "01" - x     - do nothing and don't show control (-2)
        //        ' '   - do nothing (-1)
        //        0     - force to remove line
        //        1,2.. - insert this number of newlines
        // Formatter knows all but user interface uses now {x, ' ', 1} only
        
        // Declarations
        //   Declarations/Types
        NlsTypRecord (ModulaTokenTypes.RECORD_KEYWORD,     ModulaElementTypes.RECORD_TYPE_DEFINITION, NewlineSettingCategory.NlscDeclTypes, 
                "x1", Messages.FormatterProfile_Record), //$NON-NLS-1$ 
                
        NlsTypEnd    (ModulaTokenTypes.END_KEYWORD,        ModulaElementTypes.RECORD_TYPE_DEFINITION, NewlineSettingCategory.NlscDeclTypes, 
                "1x", Messages.FormatterProfile_EndInRec), //$NON-NLS-1$ 

        NlsTypCase   (ModulaTokenTypes.CASE_KEYWORD,       ModulaElementTypes.RECORD_VARIANT_FIELD_BLOCK, NewlineSettingCategory.NlscDeclTypes, 
                "1x", Messages.FormatterProfile_CaseInVarRec), //$NON-NLS-1$
                
        NlsTypOf     (ModulaTokenTypes.OF_KEYWORD,         ModulaElementTypes.RECORD_VARIANT_FIELD_BLOCK, NewlineSettingCategory.NlscDeclTypes, 
                "x1", Messages.FormatterProfile_OfInVarRec), //$NON-NLS-1$
                
        NlsTypSep    (ModulaTokenTypes.SEP,                SpecialCheckId.RecordVariant, NewlineSettingCategory.NlscDeclTypes, 
                "1 ", Messages.FormatterProfile_SepInVarRec), //$NON-NLS-1$
                
        NlsTypElse   (ModulaTokenTypes.ELSE_KEYWORD,       ModulaElementTypes.RECORD_VARIANT_ELSE_PART, NewlineSettingCategory.NlscDeclTypes, 
                "1 ", Messages.FormatterProfile_ElseInVarRec), //$NON-NLS-1$

        NlsTypEndCase(ModulaTokenTypes.END_KEYWORD,       ModulaElementTypes.RECORD_VARIANT_FIELD_BLOCK, NewlineSettingCategory.NlscDeclTypes, 
                "1x", Messages.FormatterProfile_EndInVarRec), //$NON-NLS-1$

        //   Declarations/Procedures
        NlsProc      (ModulaTokenTypes.PROCEDURE_KEYWORD,  SpecialCheckId.None, NewlineSettingCategory.NlscDeclProcs, 
                "1x", Messages.FormatterProfile_Procedure), //$NON-NLS-1$
                
        NlsProcBegin (ModulaTokenTypes.BEGIN_KEYWORD,      ModulaElementTypes.PROCEDURE_BODY, NewlineSettingCategory.NlscDeclProcs, 
                "11", Messages.FormatterProfile_BeginInProc, Messages.FormatterProfile_BeginInProc2), //$NON-NLS-1$
                
        NlsProcEnd   (ModulaTokenTypes.END_KEYWORD,        ModulaElementTypes.PROCEDURE_DECLARATION, NewlineSettingCategory.NlscDeclProcs, 
                "1x", Messages.FormatterProfile_EndInProc, Messages.FormatterProfile_EndInProc2), //$NON-NLS-1$

        //   Declarations/Modules
        NlsModImport (ModulaTokenTypes.IMPORT_KEYWORD,     ModulaElementTypes.SIMPLE_IMPORT,      NewlineSettingCategory.NlscDeclMods, 
                "1x", Messages.FormatterProfile_ImportSimple), //$NON-NLS-1$
                
        NlsModFrom   (ModulaTokenTypes.FROM_KEYWORD,       ModulaElementTypes.UNQUALIFIED_IMPORT, NewlineSettingCategory.NlscDeclMods, 
                "1x", Messages.FormatterProfile_From), //$NON-NLS-1$
                
        NlsModExport (ModulaTokenTypes.EXPORT_KEYWORD,     SpecialCheckId.None, NewlineSettingCategory.NlscDeclMods, 
                "1x", Messages.FormatterProfile_Export), // //$NON-NLS-1$
                
        NlsModBegin  (ModulaTokenTypes.BEGIN_KEYWORD,      ModulaElementTypes.MODULE_BODY, NewlineSettingCategory.NlscDeclMods, 
                "11", Messages.FormatterProfile_BeginInMod), //$NON-NLS-1$
                
        NlsModExcept (ModulaTokenTypes.EXCEPT_KEYWORD,     SpecialCheckId.None, NewlineSettingCategory.NlscDeclMods, 
                "11", Messages.FormatterProfile_Except), // //$NON-NLS-1$
                
        NlsModFinal  (ModulaTokenTypes.FINALLY_KEYWORD,    SpecialCheckId.None, NewlineSettingCategory.NlscDeclMods, 
                "11", Messages.FormatterProfile_Finally), // //$NON-NLS-1$
                
        NlsModEnd    (ModulaTokenTypes.END_KEYWORD,        SpecialCheckId.ModuleEnd, NewlineSettingCategory.NlscDeclMods, 
                "1x", Messages.FormatterProfile_EndInMod, Messages.FormatterProfile_EndInMod2), //$NON-NLS-1$
        
        // Declarations
                
        NlsConstGlob (ModulaTokenTypes.CONST_KEYWORD,      SpecialCheckId.InGlobalScope, NewlineSettingCategory.NlscDeclarations,
                "11", Messages.FormatterProfile_ConstGlob), //$NON-NLS-1$

        NlsConstLoc  (ModulaTokenTypes.CONST_KEYWORD,      SpecialCheckId.InLocalScope,  NewlineSettingCategory.NlscDeclarations,
                "11", Messages.FormatterProfile_ConstLoc), //$NON-NLS-1$
                
        NlsTypeGlob  (ModulaTokenTypes.TYPE_KEYWORD,       SpecialCheckId.InGlobalScope, NewlineSettingCategory.NlscDeclarations,
                "11", Messages.FormatterProfile_TypeGlob), //$NON-NLS-1$

        NlsTypeLoc   (ModulaTokenTypes.TYPE_KEYWORD,       SpecialCheckId.InLocalScope,  NewlineSettingCategory.NlscDeclarations,
                "11", Messages.FormatterProfile_TypeLoc), //$NON-NLS-1$
                
        NlsVarGlobal (ModulaTokenTypes.VAR_KEYWORD,        SpecialCheckId.InGlobalScope, NewlineSettingCategory.NlscDeclarations,
                "11", Messages.FormatterProfile_VarGlob), //$NON-NLS-1$
                
        NlsVarLocal  (ModulaTokenTypes.VAR_KEYWORD,        SpecialCheckId.InLocalScope,  NewlineSettingCategory.NlscDeclarations,
                "11", Messages.FormatterProfile_VarLoc), //$NON-NLS-1$
                              
        
        // Control statements
        //   Control statements/If statements
        NlsIf        (ModulaTokenTypes.IF_KEYWORD,         SpecialCheckId.None, NewlineSettingCategory.NlscStmtIf,
                "1x", Messages.FormatterProfile_If), //$NON-NLS-1$
                
        NlsIfThen    (ModulaTokenTypes.THEN_KEYWORD,       SpecialCheckId.None, NewlineSettingCategory.NlscStmtIf,
                " 1", Messages.FormatterProfile_Then), //$NON-NLS-1$
                
        NlsIfElsif   (ModulaTokenTypes.ELSIF_KEYWORD,      SpecialCheckId.None, NewlineSettingCategory.NlscStmtIf,
                "1x", Messages.FormatterProfile_Elsif), //$NON-NLS-1$

        NlsElse      (ModulaTokenTypes.ELSE_KEYWORD,       ModulaElementTypes.IF_STATEMENT, NewlineSettingCategory.NlscStmtIf,
                "11", Messages.FormatterProfile_ElseInIf), //$NON-NLS-1$
        
        //   Control statements/Case statements
        NlsCase      (ModulaTokenTypes.CASE_KEYWORD,       ModulaElementTypes.CASE_STATEMENT, NewlineSettingCategory.NlscStmtCase,
                "1x", Messages.FormatterProfile_Case), //$NON-NLS-1$
                
        NlsCaseOf    (ModulaTokenTypes.OF_KEYWORD,         ModulaElementTypes.CASE_STATEMENT, NewlineSettingCategory.NlscStmtCase,
                "x1", Messages.FormatterProfile_OfInCase), //$NON-NLS-1$
                
        NlsCaseSep   (ModulaTokenTypes.SEP,                ModulaElementTypes.CASE_VARIANT_LIST, NewlineSettingCategory.NlscStmtCase,
                "1 ", Messages.FormatterProfile_SepInCase), //$NON-NLS-1$
                
        NlsElseInCase(ModulaTokenTypes.ELSE_KEYWORD,       ModulaElementTypes.CASE_ELSE_PART, NewlineSettingCategory.NlscStmtCase,
                "1 ", Messages.FormatterProfile_ElseInCase), //$NON-NLS-1$

        //   Control statements/Other statements
        NlsFor       (ModulaTokenTypes.FOR_KEYWORD,        SpecialCheckId.None, NewlineSettingCategory.NlscStmtOther,
                "1x", Messages.FormatterProfile_For), //$NON-NLS-1$
                              
        NlsLoop      (ModulaTokenTypes.LOOP_KEYWORD,       SpecialCheckId.None, NewlineSettingCategory.NlscStmtOther,
                "1 ", Messages.FormatterProfile_Loop), //$NON-NLS-1$
                              
        NlsRepeat    (ModulaTokenTypes.REPEAT_KEYWORD,     SpecialCheckId.None, NewlineSettingCategory.NlscStmtOther,
                "1 ", Messages.FormatterProfile_Repeat), //$NON-NLS-1$
                
        NlsUntil     (ModulaTokenTypes.UNTIL_KEYWORD,      SpecialCheckId.None, NewlineSettingCategory.NlscStmtOther, 
                "1x", Messages.FormatterProfile_Until), //$NON-NLS-1$
                
        NlsWhile     (ModulaTokenTypes.WHILE_KEYWORD,      SpecialCheckId.None, NewlineSettingCategory.NlscStmtOther,
                "1x", Messages.FormatterProfile_While), //$NON-NLS-1$
                
        NlsWith      (ModulaTokenTypes.WITH_KEYWORD,       SpecialCheckId.None, NewlineSettingCategory.NlscStmtOther,
                "1x", Messages.FormatterProfile_With), //$NON-NLS-1$
        
        NlsDo        (ModulaTokenTypes.DO_KEYWORD,         SpecialCheckId.None, NewlineSettingCategory.NlscStmtOther,
                " 1", Messages.FormatterProfile_Do),  //$NON-NLS-1$

        // Control statements
        NlsEnd       (ModulaTokenTypes.END_KEYWORD,        SpecialCheckId.None, NewlineSettingCategory.NlscStatements,
                "1x", Messages.FormatterProfile_End); //$NON-NLS-1$

        
        
//        NlsTypeLoc   (ModulaTokenTypes.TYPE_KEYWORD,       SpecialCheckId.InLocalScope,  "11", "TYPE keyword in local scope"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsTypeGlob  (ModulaTokenTypes.TYPE_KEYWORD,       SpecialCheckId.InGlobalScope, "11", "TYPE keyword in global scope"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsConstLoc  (ModulaTokenTypes.CONST_KEYWORD,      SpecialCheckId.InLocalScope,  "10", "CONST keyword in local scope"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsConstGlob (ModulaTokenTypes.CONST_KEYWORD,      SpecialCheckId.InGlobalScope, "11", "CONST keyword in global scope"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsVarLocal  (ModulaTokenTypes.VAR_KEYWORD,        SpecialCheckId.InLocalScope,  "10", "VAR keyword in local scope"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsVarGlobal (ModulaTokenTypes.VAR_KEYWORD,        SpecialCheckId.InGlobalScope, "11", "VAR keyword in global scope"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsSemicolon (ModulaTokenTypes.SEMICOLON,          SpecialCheckId.None, " 1", "Semicolon ';'"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsArray     (ModulaTokenTypes.ARRAY_KEYWORD,      SpecialCheckId.None, "00", "ARRAY keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsOfInArr   (ModulaTokenTypes.OF_KEYWORD,         ModulaElementTypes.ARRAY_TYPE_DEFINITION, "00", "OF keyword in ARRAY type definition"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsOfInCase  (ModulaTokenTypes.OF_KEYWORD,         ModulaElementTypes.CASE_STATEMENT,        "00", "OF keyword in CASE statement"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsPackedset (ModulaTokenTypes.PACKEDSET_KEYWORD,  SpecialCheckId.None, "00", "PACKEDSET keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsPointer   (ModulaTokenTypes.POINTER_KEYWORD,    SpecialCheckId.None, "00", "POINTER keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsToInPtr   (ModulaTokenTypes.TO_KEYWORD,         ModulaElementTypes.POINTER_TYPE_DEFINITION, "00", "TO keyword in POINTER type definition"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsToInFor   (ModulaTokenTypes.TO_KEYWORD,         ModulaElementTypes.FOR_STATEMENT, "00", "TO keyword in FOR statement"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsDoInWhile (ModulaTokenTypes.DO_KEYWORD,         ModulaElementTypes.WHILE_STATEMENT, "01", "DO keyword in WHILE statement"),  //$NON-NLS-1$ //$NON-NLS-2$
//        NlsDoInWith  (ModulaTokenTypes.DO_KEYWORD,         ModulaElementTypes.MODULA_WITH_STATEMENT, "01", "DO keyword in WITH statement"),  //$NON-NLS-1$ //$NON-NLS-2$
//        NlsProcedure (ModulaTokenTypes.PROCEDURE_KEYWORD,  SpecialCheckId.None, "00", "PROCEDURE keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsRecord    (ModulaTokenTypes.RECORD_KEYWORD,     SpecialCheckId.None, "00", "RECORD keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsIF        (ModulaTokenTypes.IF_KEYWORD,         SpecialCheckId.None, "10", "IF keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsThen      (ModulaTokenTypes.THEN_KEYWORD,       SpecialCheckId.None, "01", "THEN keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsElse      (ModulaTokenTypes.ELSE_KEYWORD,       SpecialCheckId.None, "11", "ELSE keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsElsif     (ModulaTokenTypes.ELSIF_KEYWORD,      SpecialCheckId.None, "10", "ELSIF keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsBegin     (ModulaTokenTypes.BEGIN_KEYWORD,      SpecialCheckId.None, "11", "BEGIN keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsEnd       (ModulaTokenTypes.END_KEYWORD,        SpecialCheckId.None, "1 ", "END keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsFor       (ModulaTokenTypes.FOR_KEYWORD,        SpecialCheckId.None, "10", "FOR keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsBy        (ModulaTokenTypes.BY_KEYWORD,         SpecialCheckId.None, "00", "BY keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsCase      (ModulaTokenTypes.CASE_KEYWORD,       SpecialCheckId.None, "10", "CASE keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsExcept    (ModulaTokenTypes.EXCEPT_KEYWORD,     SpecialCheckId.None, "1 ", "EXCEPT keyword"), // //$NON-NLS-1$ //$NON-NLS-2$
//        NlsExit      (ModulaTokenTypes.EXIT_KEYWORD,       SpecialCheckId.None, "1 ", "EXIT keyword"),  //$NON-NLS-1$ //$NON-NLS-2$
//        NlsExport    (ModulaTokenTypes.EXPORT_KEYWORD,     SpecialCheckId.None, "10", "EXPORT keyword"), // //$NON-NLS-1$ //$NON-NLS-2$
//        NlsFinally   (ModulaTokenTypes.FINALLY_KEYWORD,    SpecialCheckId.None, "1 ", "FINALLY keyword"), // //$NON-NLS-1$ //$NON-NLS-2$
//        NlsForward   (ModulaTokenTypes.FORWARD_KEYWORD,    SpecialCheckId.None, "10", "FORWARD keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsFrom      (ModulaTokenTypes.FROM_KEYWORD,       SpecialCheckId.None, "10", "FROM keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsImportQual(ModulaTokenTypes.IMPORT_KEYWORD,     ModulaElementTypes.SIMPLE_IMPORT,      "10", "IMPORT keyword in qualified import"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsImportUnq (ModulaTokenTypes.IMPORT_KEYWORD,     ModulaElementTypes.UNQUALIFIED_IMPORT, "10", "IMPORT keyword in unqualified import"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsIn        (ModulaTokenTypes.IN_KEYWORD,         SpecialCheckId.None, "00", "IN keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsIs        (ModulaTokenTypes.IS_KEYWORD,         SpecialCheckId.None, "00", "IS keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsLoop      (ModulaTokenTypes.LOOP_KEYWORD,       SpecialCheckId.None, "11", "LOOP keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsRepeat    (ModulaTokenTypes.REPEAT_KEYWORD,     SpecialCheckId.None, "11", "REPEAT keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsRetry     (ModulaTokenTypes.RETRY_KEYWORD,      SpecialCheckId.None, "1 ", "RETRY keyword"), // //$NON-NLS-1$ //$NON-NLS-2$
//        NlsReturn    (ModulaTokenTypes.RETURN_KEYWORD,     SpecialCheckId.None, "1 ", "RETURN keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsSeq       (ModulaTokenTypes.SEQ_KEYWORD,        SpecialCheckId.None, "00", "SEQ keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsSet       (ModulaTokenTypes.SET_KEYWORD,        SpecialCheckId.None, " 0", "SET keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsUntil     (ModulaTokenTypes.UNTIL_KEYWORD,      SpecialCheckId.None, "10", "UNTIL keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsWhile     (ModulaTokenTypes.WHILE_KEYWORD,      SpecialCheckId.None, "10", "WHILE keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsWith      (ModulaTokenTypes.WITH_KEYWORD,       SpecialCheckId.None, "10", "WITH keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsLabel     (ModulaTokenTypes.LABEL_KEYWORD,      SpecialCheckId.None, "10", "LABEL keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsGoto      (ModulaTokenTypes.GOTO_KEYWORD,       SpecialCheckId.None, "10", "GOTO keyword"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsSep       (ModulaTokenTypes.SEP,                SpecialCheckId.None, "10", "Separator '|' (in CASE statements)"), //$NON-NLS-1$ //$NON-NLS-2$
//        NlsComma     (ModulaTokenTypes.COMMA,              SpecialCheckId.None, "00", "Comma ','"), // diff //$NON-NLS-1$ //$NON-NLS-2$
//        NlsColon     (ModulaTokenTypes.COLON,              SpecialCheckId.None, "00", "Colon ':'"); // diff //$NON-NLS-1$ //$NON-NLS-2$

                    
                    
        private NewlineSetting(IElementType etype, SpecialCheckId specialCheckId, NewlineSettingCategory category, 
                String defInss, String stmtName, String settingsText) {
            this.etype = etype;
            this.specialCheckId = specialCheckId;
            this.category = category;
            this.stmtName = stmtName;
            this.settingsText = settingsText;
            
            insNewLineBeforeDef = char2val(defInss.charAt(0));
            insNewLineAfterDef = char2val(defInss.charAt(1));
        }
        
        private int char2val(char ch) {
            if (ch >= '0' && ch <= '9') {
                return ch - '0';
            } else if (ch == ' ') {
                return -1;
            }
            return -2; // 'x' 
        }
        
        private NewlineSetting(IElementType etype, SpecialCheckId specialCheckId, NewlineSettingCategory category, String defInss, String stmtName) {
            this(etype, specialCheckId, category, defInss, stmtName, stmtName);
        }

        private NewlineSetting(IElementType etype, IElementType etParent, NewlineSettingCategory category, String defInss, 
                String stmtName, String settingsText) {
            this(etype, SpecialCheckId.CheckParent, category, defInss, stmtName, settingsText);
            this.etParent = etParent;
        }

        private NewlineSetting(IElementType etype, IElementType etParent, NewlineSettingCategory category, String defInss, String stmtName) {
            this(etype, SpecialCheckId.CheckParent, category, defInss, stmtName, stmtName);
            this.etParent = etParent;
        }

        
        public int insNewLineBeforeDef() {
            return insNewLineBeforeDef;
        }
        
        public int insNewLineAfterDef() {
            return insNewLineAfterDef;
        }
        
        private boolean testElement(PstLeafNode pln) {
            if (!etype.equals(pln.getElementType())) {
                return false;
            }
            PstNode parent = pln.getParent();
            IElementType parentType = parent == null ? null : parent.getElementType(); 
            
            if ((this == NlsVarGlobal || this == NlsVarLocal) &&
                ModulaElementTypes.FORMAL_PARAMETER_DECLARATION.equals(parentType)) 
            {
                return false; // exclude "VAR" keyword in parameters
            }

            switch(specialCheckId) {
            case None:
                return true;
            case CheckParent:
                return this.etParent.equals(parentType);
            case RecordVariant:
                return ModulaElementTypes.RECORD_VARIANT_LIST.equals(parentType) || 
                       ModulaElementTypes.RECORD_VARIANT.equals(parentType);
            case ModuleEnd: 
                return ModulaElementTypes.PROGRAM_MODULE.equals(parentType) || 
                       ModulaElementTypes.LOCAL_MODULE.equals(parentType);
            case InGlobalScope:
            case InLocalScope:
                boolean procFound = false;
                for (PstNode n = parent; n != null; n = n.getParent()) {
                    if (n.getElementType().equals(ModulaElementTypes.PROCEDURE_DECLARATION)) {
                        procFound = true;
                        break;
                    }
                }
                return (specialCheckId == SpecialCheckId.InLocalScope) == procFound;
            }
            return false;
        }
        
        @Override
        public String toString() {
            return stmtName;
        }
        
        public String toSettingsString() {
            return settingsText;
        }
        
        public NewlineSettingCategory getCategory() {
            return category;
        }

        private IElementType etype;
        private SpecialCheckId specialCheckId;
        private NewlineSettingCategory category;
        private String stmtName;
        private String settingsText;
        private int insNewLineBeforeDef; // 0 - don't insert, 
        private int insNewLineAfterDef;  // 1,2.. lines to insert, -1 - hz, -2 - hz + don't show control
        private IElementType etParent;   // for SpecialCheckId.CheckParent 
    }
    
    private static HashMap<IElementType, ArrayList<NewlineSetting>> hmToSearchStmt;
    static {
        hmToSearchStmt = new HashMap<IElementType, ArrayList<NewlineSetting>>();
        for (NewlineSetting ss : NewlineSetting.values()) {
            IElementType et = ss.etype;
            ArrayList<NewlineSetting> al = hmToSearchStmt.get(et);
            if (al != null) {
                al.add(ss);
            } else {
                al = new ArrayList<NewlineSetting>();
                al.add(ss);
                hmToSearchStmt.put(et,  al);
            }
        }
    }
    
    public NewlineSetting searchStmtSetting(PstLeafNode pln) {
        ArrayList<NewlineSetting> al = hmToSearchStmt.get(pln.getElementType());
        if (al != null) {
            for (NewlineSetting ss : al) {
                if (ss.testElement(pln)) {
                    return ss;
                }
            }
        }
        return null;
    }
    
    
    /**
     * @return 0..9 - prefer to have this number of newlines
     *     or -1    - no matter
     */
    public int getInsNewLinesBefore(NewlineSetting ss) {
        return (hmStmtSettingsBefore.get(ss));
    }
    public int getInsNewLinesAfter(NewlineSetting ss) {
        return (hmStmtSettingsAfter.get(ss));
    }
    

    /**
     * @param val 0..9 - prefer to have this number of newlines
     *        or -1    - no matter
     */
    public void setInsNewLineBefore(NewlineSetting ss, int val) {
        hmStmtSettingsBefore.put(ss, val);
    }
    public void setInsNewLineAfter(NewlineSetting ss, int val) {
        hmStmtSettingsAfter.put(ss, val);
    }
    
    /////////// Line wrapping settings -------------------------------------------
    /////////// Line wrapping settings -------------------------------------------
    /////////// Line wrapping settings -------------------------------------------
    
    private static final String WRAPPING_WIDTH_MEMENTO_KEY = "LineWrappingWidth"; //$NON-NLS-1$

    private int wrappingWidth;
    
    public int getWrappingWidth() {
        return wrappingWidth;
    }

    public void setWrappingWidth(int w) {
        wrappingWidth = w;
    }
}
