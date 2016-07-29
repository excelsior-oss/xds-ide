package com.excelsior.xds.core.model.internal.nls;

import java.text.MessageFormat;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS 
{
    private static final String BUNDLE_NAME = "com.excelsior.xds.core.model.internal.nls.messages"; //$NON-NLS-1$

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object });
    }

    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }

    public static String ConsoleType_XDS_Tool;
    public static String Activator_LinkingExtFilesToResources;
    public static String ExternalResourceManager_LinkingSdkFilesToResources;
    public static String NewProjectCreator_BuildingProject;
    public static String NewProjectCreator_CantCreateDir;
    public static String NewProjectCreator_CantNoSdk;
    public static String NewProjectCreator_CreatePrjFromScratch;
    public static String NewProjectCreator_DoYouWantToContinue;
    public static String NewProjectCreator_ErrReadingTpl;
    public static String NewProjectCreator_ErrWritingFile;
    public static String NewProjectCreator_ProjDirEmpty;
    public static String NewProjectCreator_ProjectWord;
    public static String NewProjectCreator_RedirectionWord;
    public static String NewProjectCreator_TheDirIsFile;
    public static String NewProjectCreator_TheFollowingProblems;
    public static String SdkIniFileWriter_ComentDefTmdPath;
    public static String SdkIniFileWriter_ComentDirsList0;
    public static String SdkIniFileWriter_ComentDirsList1;
    public static String SdkIniFileWriter_ComentPrimExts0;
    public static String SdkIniFileWriter_ComentPrimExts1;
    public static String SdkIniFileWriter_ComentExeExt;
    public static String SdkIniFileWriter_ComentLibPath0;
    public static String SdkIniFileWriter_ComentLibPath1;
    public static String SdkIniFileWriter_ComentMainComponents;
    public static String SdkIniFileWriter_ComentMainTmdPath;
    public static String SdkIniFileWriter_ComentManifestPath;
    public static String SdkIniFileWriter_ComentModulesTmdPath;
    public static String SdkIniFileWriter_ComentSdkName;
    public static String SdkIniFileWriter_ComentSimulatorPath;
    public static String SdkIniFileWriter_ComentTemptates;
    public static String SdkIniFileWriter_ComentToolArgs;
    public static String SdkIniFileWriter_ComentToolEnvNars0;
    public static String SdkIniFileWriter_ComentToolEnvVars1;
    public static String SdkIniFileWriter_ComentToolEnvVars2;
    public static String SdkIniFileWriter_ComentToolFileExtList0;
    public static String SdkIniFileWriter_ComentToolFileExtList1;
    public static String SdkIniFileWriter_ComentToolFor;
    public static String SdkIniFileWriter_ComentToolLocation;
    public static String SdkIniFileWriter_ComentToolMenuOff0;
    public static String SdkIniFileWriter_ComentToolMenuOff1;
    public static String SdkIniFileWriter_ComentToolMenuOn0;
    public static String SdkIniFileWriter_ComentToolMenuOn1;
    public static String SdkIniFileWriter_ComentToolName;
    public static String SdkIniFileWriter_ComentToolSourceRoot1;
    public static String SdkIniFileWriter_ComentToolSourceRoot2;
    public static String SdkIniFileWriter_ComentTools;
    public static String SdkIniFileWriter_ComentToolWorkDir;
    public static String SdkIniFileWriter_ComentTprPath;
    public static String SdkIniFileWriter_ComentTrdPath;
    public static String SdkIniFileWriter_ComentXcPath;
    public static String SdkIniFileWriter_ComentXdPath;
    public static String SdkIniFileWriter_CommentToolGroup0;
    public static String SdkIniFileWriter_CommentToolGroup1;
    public static String SdkIniFileWriter_SeparatorLine;
    public static String Tool_InvalidToolLocation;
    public static String Tool_InvalidToolName;
    public static String Tool_InvalidToolWorkDir;
    public static String Tool_InvalidToolWorkDir_BadVars;
    public static String XdsProject_ExternalDependencies;
    public static String XdsProject_SdkLibrary;
    public static String XdsProjectSettings_CantDetermineDefaultDir;
    public static String XdsProjectSettings_CantDetermineWorkDir;
    public static String XdsProjectSettings_InvalidWorkDir;
    public static String XdsProjectSettings_InvalidWorkDir2;
    public static String XdsProjectSettings_WrongWorkDir;
    public static String XTool_BadWorkDir;
    public static String XTool_ToolFileNotFound;
    public static String XTool_ToolTerminated;
    
    public static String XdsImportSection_Name;
    public static String XdsRecordVariantSelector_Name;
    public static String XdsRecordVariant_Name;

    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Not for instantiation
    }

}
