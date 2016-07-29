package com.excelsior.xds.ui.internal.nls;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.ui.internal.nls.messages"; //$NON-NLS-1$

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object});
    }

    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }

    public static String Common_Browse;
    public static String Common_Add;
    public static String Common_Edit;
    public static String Common_DirectorySelection;
    public static String Common_Remove;
    public static String Common_Export;
    public static String Common_Warning;
    public static String Common_Configure;
    public static String Common_Variables;
    public static String Common_Error;
    
    public static String AbstractBuildFileCommandHandler_Building;
    public static String AbstractBuildProjectCommandHandler_BuildingProject;

    public static String CompileFileCommandHandler_Compiling;
    public static String CompilerWorkingDirectoryBlock_Default;
    public static String CompilerWorkingDirectoryBlock_FileSystem;
    public static String CompilerWorkingDirectoryBlock_Other;
    public static String CompilerWorkingDirectoryBlock_SelectWorkspaceRelWorkDir;
    public static String CompilerWorkingDirectoryBlock_SelectXcWorkDir;
    public static String CompilerWorkingDirectoryBlock_Variables;
    public static String CompilerWorkingDirectoryBlock_Workspace;

    public static String ConsoleType_XDS_Tool;

    public static String DynamicToolsMenu_NoActiveToolsAvailable;
        
    public static String ProjectSdkBlock_DefaultSDK;
    public static String ProjectSdkBlock_InvalidSDK;
    public static String ProjectSdkBlock_Error_NoSelectedSDK;
    public static String ProjectSdkBlock_Error_InvalidSDK;
    public static String ProjectSdkBlock_Error_NoInstalledSDK;

    public static String ProjectSdkPanel_InvalidSdk;
        
    public static String EditSdkDialog_Title;
    public static String EditSdkDialog_Header;
    public static String EditSdkDialog_Description;

    public static String EditSdkDialog_NotSupportedText;

    public static String EditSdkDialog_XdsHome;
    public static String EditSdkDialog_XdsHomeBrowseText;
    public static String EditSdkDialog_EnterXdsHomeDir;
    public static String EditSdkDialog_InvalidXdsHome;

    public static String EditSdkDialog_XdsName;
    public static String EditSdkDialog_EnterNameForXds;
    public static String EditSdkDialog_NameIsUsed;

    public static String EditSdkDialog_Components;
    public static String EditSdkDialog_ExtraTools;
    
    public static String EditSdkDialog_Compiler;
    public static String EditSdkDialog_CompilerBrowseText;
    public static String EditSdkDialog_EnterCompilerPath;
    public static String EditSdkDialog_InvalidCompilerPath;

    public static String EditSdkDialog_Debugger;
    public static String EditSdkDialog_DebuggerBrowseText;

    public static String EditSdkDialog_DefTemplate;

    public static String EditSdkDialog_DefTemplateBrowseTxt;
    public static String EditSdkDialog_DirsListExpected;

    public static String EditSdkDialog_DirsToCreate;

    public static String EditSdkDialog_EnterDebuggerPath;

    public static String EditSdkDialog_EnterDefTemplateName;
    public static String EditSdkDialog_InvalidDebuggerPath;

    public static String EditSdkDialog_InvalidDefTemplate;
        
    public static String EditSdkDialog_Simulator;
    public static String EditSdkDialog_SimulatorBrowseText;
    public static String EditSdkDialog_EnterSimulatorPath;
    public static String EditSdkDialog_InvalidSimulatorPath;

    public static String EditSdkDialog_LibraryDefs;
    public static String EditSdkDialog_LibraryDefsBrowseText;
    public static String EditSdkDialog_EnterLibraryDefsPath;
    public static String EditSdkDialog_InvalidLibraryDefsPath;

    public static String EditSdkDialog_ExeFileExtension;
    public static String EditSdkDialog_EnterExeFileExtension;

    public static String EditSdkDialog_EnterMainTemplateName;

    public static String EditSdkDialog_EnterModuleTemplateName;

    public static String EditSdkDialog_EnterPrjTemplateName;

    public static String EditSdkDialog_EnterRedTemplateName;
    public static String EditSdkDialog_InvalidExeFileExtension;

    public static String EditSdkDialog_InvalidMainTemplate;

    public static String EditSdkDialog_InvalidmoduleTemplate;

    public static String EditSdkDialog_InvalidPrjTemplate;

    public static String EditSdkDialog_InvalidRedTemplate;

    public static String EditSdkDialog_UpdateManifest;
    public static String EditSdkDialog_UpdateManifestBrowseText;
    public static String EditSdkDialog_EnterUpdateManifestPath;
    public static String EditSdkDialog_InvalidUpdateManifestPath;

    
    public static String EditSdkDialog_PrjTemplate;

    public static String EditSdkDialog_PrjTemplateBrowseTxt;

    public static String EditSdkDialog_FilesTypes;
    public static String EditSdkDialog_PrimaryFilesExtensions;
    public static String EditSdkDialog_PrimExtListExpected;
    public static String EditSdkDialog_ProjectTemplates;
    public static String EditSdkDialog_EnvironmentVariables;

    public static String EditSdkDialog_MainTemplate;
    public static String EditSdkDialog_MainTemplateBrowseTxt;
    public static String EditSdkDialog_ModuleTemplate;
    public static String EditSdkDialog_ModuleTemplateBrowseTxt;
    public static String EditSdkDialog_RedTemplate;
    public static String EditSdkDialog_RedTemplateBrowseTxt;

    public static String EditSdkDialogTabAbstract_EnterLocationOfModue;
    public static String EditSdkDialogTabAbstract_EnterProfilerPath;
    public static String EditSdkDialogTabAbstract_FoldersLayout;
    public static String EditSdkDialogTabAbstract_InvalidLocation;
    public static String EditSdkDialogTabAbstract_InvalidPrjFileFolderName;
    public static String EditSdkDialogTabAbstract_InvalidProfilerPath;
    public static String EditSdkDialogTabAbstract_IvvalidMainModuleFolder;
    public static String EditSdkDialogTabAbstract_MainModuleFolder;
    public static String EditSdkDialogTabAbstract_Ob2ModuleTemplate;
    public static String EditSdkDialogTabAbstract_Profiler;
    public static String EditSdkDialogTabAbstract_ProfilerBrowseText;
    public static String EditSdkDialogTabAbstract_ProjectFileFolder;
    public static String EditSdkDialogTabAbstract_SelectOb2ModuleTemplate;
    public static String EditSdkDialogTabComponents_Components;
    public static String EditSdkDialogTabEnvironment_Environment;
    public static String EditSdkDialogTabTemplates_Templates;

    public static String EditSdkToolDialog_Title;
    public static String EditSdkToolDialog_Header;
    public static String EditSdkToolDialog_Description;

    public static String EditSdkToolDialog_Name;
    public static String EditSdkToolDialog_Location;
    public static String EditSdkToolDialog_LocationBrowseText;

    public static String EditSdkToolDialog_ToolMenuSettings;
    public static String EditSdkToolDialog_MenuItem;
    public static String EditSdkToolDialog_InactiveMenuItem;
    public static String EditSdkToolDialog_MenuGroup;

    public static String EditSdkToolDialog_ToolLaunchSettings;
    public static String EditSdkToolDialog_AvailableForFileExtensions;
    public static String EditSdkToolPage_EnterExtensionOrList;

    public static String EditSdkToolDialog_AvailableForProjectsBasedOn;
    public static String EditSdkToolDialog_AvailableForProjectsBasedOn_AnySourceRoot;
    public static String EditSdkToolDialog_AvailableForProjectsBasedOn_AnySourceRootIndividualSettings;
    public static String EditSdkToolDialog_AvailableForProjectsBasedOn_ProjectFile;
    public static String EditSdkToolDialog_AvailableForProjectsBasedOn_MainModule;

    public static String EditSdkToolDialog_ToolsSettings_AnySourceRoot;
    public static String EditSdkToolDialog_ToolsSettings_ProjectFile;
    public static String EditSdkToolDialog_ToolsSettings_MainModule;
    public static String EditSdkToolDialog_Arguments;
    public static String EditSdkToolDialog_WorkingDirectory;
    public static String EditSdkToolDialog_DefaultWorkingDirectory;
    
    
    public static String EditSdkToolPage_EnterToolLocation;
    public static String EditSdkToolPage_EnterToolName;
    public static String EditSdkToolPage_InvalidLocation;

    public static String EditSdkToolPage_InvalidOutputEncoding;
    public static String EditSdkToolPage_NoGroup;
    public static String EditSdkToolPage_OutputEncoding;

    public static String GotoCompilationUnitHandler_OpenModule;

    
    public static String XdsBasePreferencePage_Description;

    public static String XdsConsolePreferencePage_AlwaysClearBeforeBuilding;
    public static String XdsConsolePreferencePage_BackgroundColor;
    public static String XdsConsolePreferencePage_ConsoleTextColor;
    public static String XdsConsolePreferencePage_ErrorMessage;
    public static String XdsConsolePreferencePage_ErrorNotification;
    public static String XdsConsolePreferencePage_GeneralConsoleSettings;
    public static String XdsConsolePreferencePage_InfoMessage;
    public static String XdsConsolePreferencePage_InputText;
    public static String XdsConsolePreferencePage_OutputText;
    public static String XdsConsolePreferencePage_ShowConsoleWhenBuilding;
    public static String XdsConsolePreferencePage_WarningMessage;

    public static String XdsConsoleTerminateAction_Terminate;
        
    public static String SDKsPreferencePage_AskOverwriteQuestion;
    public static String SDKsPreferencePage_AskOverwriteTitle;
    public static String SDKsPreferencePage_Description;
    public static String SDKsPreferencePage_RegisteredSDKs;
    public static String SDKsPreferencePage_SdkName;
    public static String SDKsPreferencePage_SdkLocation;
    public static String SDKsPreferencePage_CantReadSdk_EditManualy;
    public static String SDKsPreferencePage_SelectXdsHome;
    public static String SDKsPreferencePage_SaveSdkAs;

    public static String SDKsPreferencePage_UpdatingSdkInfo;
        
    public static String M2SearchQuery_SearchProblems;

    public static String M2SearchResult_EN_or_RU;

    public static String M2SearchResult_match_sovpadenie;
    public static String M2SearchResult_matches_sovpadeniy;
    public static String M2SearchResult_matches_sovpadeniya;

    public static String MakeFileCommandHandler_Making;

    public static String ModulaProjectPreferencePage_AskToRebuildOrRefresh;
    public static String ModulaProjectPreferencePage_BuildingProject;

    public static String ModulaProjectPreferencePage_ProjectFile;
    public static String ModulaProjectPreferencePage_MainModule;
    public static String ModulaProjectPreferencePage_WorkingDirectory;
    public static String ModulaProjectPreferencePage_EnterApplExecutable;
    public static String ModulaProjectPreferencePage_Executable;
    public static String ModulaProjectPreferencePage_JustRefresh;
    public static String ModulaProjectPreferencePage_Rebuild;
    public static String ModulaProjectPreferencePage_RebuildRequired;
    public static String ModulaProjectPreferencePage_SelectMainModule;
    public static String ModulaProjectPreferencePage_SelectPrjFile;

    public static String ModulaSearchLabelProvider_cnt_matches;

    public static String ModulaSearchPage_AllOccurencies;
    public static String ModulaSearchPage_AllSources;
    public static String ModulaSearchPage_AnyElement;
    public static String ModulaSearchPage_CaseSensitive;
    public static String ModulaSearchPage_CompilationSet;
    public static String ModulaSearchPage_Constant;
    public static String ModulaSearchPage_Declarations;
    public static String ModulaSearchPage_Field;
    public static String ModulaSearchPage_LimitTo;
    public static String ModulaSearchPage_Module;
    public static String ModulaSearchPage_Procedure;
    public static String ModulaSearchPage_Usages;
    public static String ModulaSearchPage_SdkLibraries;
    public static String ModulaSearchPage_SearchFor;
    public static String ModulaSearchPage_SearchIn;
    public static String ModulaSearchPage_SearchString;
    public static String ModulaSearchPage_Type;
    public static String ModulaSearchPage_Variable;

    public static String ModulaSearchResult_MatchesFmt;

    public static String ModulaSearchResultPage_GroupByFile;
    public static String ModulaSearchResultPage_GroupByProject;
    public static String ModulaSearchResultPage_SortAlphabetically;
    
    public static String NewModuleWizard_CreateNewModuleError;
    public static String NewModuleWizard_Title;
    public static String NewOb2ModulePage_CantCreateNoXdsProject;
    public static String NewOb2ModulePage_CreateMainModule;
    public static String NewOb2ModulePage_CreateNewOb2Module;
    public static String NewOb2ModulePage_EnterModuleName;
    public static String NewOb2ModulePage_ModNameInvalid;
    public static String NewOb2ModulePage_ModuleExists;
    public static String NewOb2ModulePage_ModuleFolder;
    public static String NewOb2ModulePage_ModuleName;
    public static String NewOb2ModulePage_Ob2Module;
    public static String NewOb2ModulePage_SelectFolder;
    public static String NewOb2ModuleWizard_Title;
    public static String NewOb2ModuleWizard_CreateNewModuleError;

    public static String NewProjectFromSourcesPage_Title;
    public static String NewProjectFromSourcesPage_Description;

    public static String NewProjectFromSourcesPage_Extension;
    public static String NewProjectFromSourcesPage_InvalidFile;

    public static String NewProjectFromSourcesPage_IsNotSelected;
    public static String NewProjectFromSourcesPage_MainModule;
    public static String NewProjectFromSourcesPage_MainModuleLabel;
    public static String NewProjectFromSourcesPage_ModOrOb2;
    public static String NewProjectFromSourcesPage_ProjectFile;
    public static String NewProjectFromSourcesPage_ProjectFileLabel;
    public static String NewProjectFromSourcesPage_ProjectRoot_BrowseMessage;
    public static String NewProjectFromSourcesPage_SelectMainModule;
    public static String NewProjectFromSourcesPage_SelectProjectFile;
    public static String NewProjectFromSourcesPage_ShouldBeInsideProjectFilesLocation;
    public static String NewProjectFromSourcesPage_ShouldHave;


    public static String NewProjectWizard_Title;
    public static String NewProjectWizard_DoYouWantToSwitsh;
    public static String NewProjectWizard_PerspectiveSwitch;

    public static String NewProjectPage_ProjectName;
    public static String NewProjectPage_ProjectRoot;
    public static String NewProjectPage_EnterProjectName;
    public static String NewProjectPage_EnterProjectRootLocation;


    public static String NewProjectFromScratchPage_CreateDirectories;
    public static String NewProjectFromScratchPage_CreateMainModule;
    public static String NewProjectFromScratchPage_CreateRedFile;
    public static String NewProjectFromScratchPage_CreateXdsPrjFile;

    public static String NewProjectFromScratchPage_Title;
    public static String NewProjectFromScratchPage_Description;

    public static String NewProjectFromScratchPage_EnterMainModuleName;
    public static String NewProjectFromScratchPage_EnterXdsPrjFileName;
    public static String NewProjectFromScratchPage_InvalidMainModuleName;
    public static String NewProjectFromScratchPage_LocationShouldBeNotRoot;
    public static String NewProjectFromScratchPage_LocationShouldBeValidFullPath;
    public static String NewProjectFromScratchPage_MainModPathShouldBeRelative;
    public static String NewProjectFromScratchPage_OtherProjectUsesThisFilesLocation;
    public static String NewProjectFromScratchPage_PrjForMainModuleShouldBeSpecified;
    public static String NewProjectFromScratchPage_ProjectAlreadyExists;
    public static String NewProjectFromScratchPage_SelectDirForProject;
    public static String NewProjectFromScratchPage_UseSdkProjectTemplate;
        
    public static String NewModulePage_CantWhenNoXdsProject;

    public static String NewModulePage_Title;
    public static String NewModulePage_Description;
    public static String NewModulePage_ModuleName;

    public static String NewModulePage_ModuleNameInvalid;

    public static String NewModulePage_ModuleNameRequired;
    public static String NewModulePage_SelFolderForDefinition;

    public static String NewModulePage_SelFolderForImplementation;

    public static String NewModulePage_SelFolderForModule;

    public static String NewModulePage_SourceFolder;
    public static String NewModulePage_MainModule;
    public static String NewModulePage_DefAlreadyExists;

    public static String NewModulePage_Definition;

    public static String NewModulePage_DefOrImplShouldBeSpecified;
    public static String NewModulePage_ImplAlreadyExists;

    public static String NewModulePage_Implementation;
    public static String NewModulePage_NoDefName;
    public static String NewModulePage_NoImplName;

    public static String NoSdkDefinedResolutionGenerator_DefineSdk;

    public static String ProjectSDKPanel_Label;
    
    public static String LauncherTabArguments_Arguments;
    public static String LauncherTabArguments_ProgramArgs;

    public static String LauncherTabMain_EnterProjectName;
    public static String LauncherTabMain_IncorreatApplicationFile;
    public static String LauncherTabMain_IncorrectLdp;
    public static String LauncherTabMain_IncorrectPkt;
    public static String LauncherTabMain_IncorrectProjectName;
    public static String LauncherTabMain_Title;
    public static String LauncherTabMain_Project;

    public static String LauncherTabMain_ProjectSelection;
    public static String LauncherTabMain_ProgramToRun;
        
    public static String LauncherTabMain_RunLdp;
    public static String LauncherTabMain_RunPkt;
    public static String LauncherTabMain_SelectXdsProject;
	public static String LauncherTabMain_UseConsoleDebugger;

    public static String LauncherTabSettings_DebuggerArgs;
    public static String LauncherTabSettings_SimulatorArgs;
    public static String LauncherTabSettings_Title;

    public static String LaunchShortcut_CantCreateLaunchCfg;
    public static String LaunchShortcut_NoLaunchableContentInEditor;
    public static String LaunchShortcut_LaunchError;
    public static String LaunchShortcut_NoLaunchableInSelection;
    public static String LaunchShortcut_SelectXdsProject;
    public static String LaunchShortcut_SelectXdsProjectToLaunch;
    public static String LaunchShortcut_SelectLaunchCongiguration;
    public static String LaunchShortcut_SelectLaunchCongigurationToLaunch;

    public static String LaunchShortcutPkt_NoLaunchableInSelection;

    public static String OpenAction_0;
    public static String OpenAction_CantOpenFiles;
    public static String OpenAction_FileNotFound;
    public static String OpenAction_FilesNotFound;
    public static String OpenAction_Open;
    
    public static String ProjectExplorerViewMenu_ShowAllFiles;

    public static String RebuildFileCommandHandler_Rebuilding;

    public static String RefreshXdsDecoratorJob_RefreshXdsDecorators;

    public static String RenameCompilationUnitHandler_CannotChangeFilesOfDebuggedProject;
	public static String RenameCompilationUnitHandler_CannotPerformRefactoringWithCurrentSelection;
	public static String RenameCompilationUnitHandler_InvalidSelection;
	public static String RenameCompilationUnitHandler_ProjectDebugged;
	public static String RenameRefactoringPage_ErrorNameHasSeveralParts;
	public static String RenameRefactoringPage_ErrorNameIsNotCorrectModulaName;
	public static String RenameRefactoringPage_NewName;
	public static String RenameRefactoringPage_WarningNameIsAlreadyInScope;
	public static String RenameRefactoringPage_WarningRenamingDuplicatedSymbol;
    
    public static String SdkEnvironmentControl_Delete;
    public static String SdkEnvironmentControl_Edit;
    public static String SdkEnvironmentControl_EditEnvVar;
    public static String SdkEnvironmentControl_New;
    public static String SdkEnvironmentControl_NewEnvVar;
    public static String SdkEnvironmentControl_Value;
    public static String SdkEnvironmentControl_ValueLabel;
    public static String SdkEnvironmentControl_Variable;
    public static String SdkEnvironmentControl_VariableLabel;
    public static String SdkEnvironmentControl_Variables;
    public static String SdkEnvironmentControl_VarNameIsEmpty;

    public static String SdkToolsControl_AddSeparator;
    public static String SdkToolsControl_Down;
    public static String SdkToolsControl_EnterGroupName;
    public static String SdkToolsControl_Location;
    public static String SdkToolsControl_Name;
    public static String SdkToolsControl_NameIsEmpty;
    public static String SdkToolsControl_NameIsUsed;
    public static String SdkToolsControl_NewGroupName;
    public static String SdkToolsControl_SeparatorLine;
    public static String SdkToolsControl_ToolMenuSeparator;
    public static String SdkToolsControl_ToolName;
    public static String SdkToolsControl_Up;

    public static String SdkToolsControlAddDialog_Add;
    public static String SdkToolsControlAddDialog_Description;

    public static String SdkToolsControlAddDialog_AddGroup;
    public static String SdkToolsControlAddDialog_AddSeparator;
    public static String SdkToolsControlAddDialog_AddTool;
    public static String SdkToolsControlAddDialog_AddToToolMenu;
    public static String SdkToolsControlAddDialog_EnterGroupName;
    public static String SdkToolsControlAddDialog_GroupName;
    public static String SdkToolsControlAddDialog_NameIsEmpty;
    public static String SdkToolsControlAddDialog_NameIsUsed;
    public static String SdkToolsControlAddDialog_NewGroupName;

    public static String SelectModulaSourceFileDialog_CompilationSetOnly;
    public static String SelectModulaSourceFileDialog_WorkspaceMatches;
    public static String SelectModulaSourceFileDialog_SdkLibraryDecorator;
    public static String SelectModulaSourceFileDialog_ExternalFilesDecorator;

    public static String UpdateAvailableDialog_UpdatesPending;

    public static String UpdateManager_NoNewUpdatesFound;
    public static String UpdateManager_XdsUpdateManager;
	public static String NewProjectPage_ProjectExistsWithAnotherCase;
	 public static String TodoMarkerBuilder_JobName;

    static {
            // initialize resource bundle
            NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Not for instantiation
}

    
}
