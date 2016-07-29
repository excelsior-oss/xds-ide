package com.excelsior.xds.core.sdk;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.log.LogHelper;

public class SdkIniFileWriter {

    public static void exportSdk(Sdk sdk, String iniFileName) {
        SdkIniFileWriter sw = new SdkIniFileWriter(sdk);
        sw.exportSdk(iniFileName);
    }

    
    // Private:
    private static final String ENVVAR_XDSDIR = "XDSDIR";  //$NON-NLS-1$
    
    Sdk sdk;
    private SdkIniFileWriter(Sdk sdk) {
        this.sdk = sdk;
    }
    
    
    private void exportSdk(String iniFileName) {
        StringBuilder sb = new StringBuilder();
        String home = FilenameUtils.normalize(sdk.getSdkHomePath() + File.separator);
        
        
        putLine(sb, Messages.SdkIniFileWriter_ComentSdkName);
        putSdkProp(sb, Sdk.Property.XDS_NAME, sdk.getName()); //$NON-NLS-1$
        putNewLine(sb);
        
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putLine(sb, Messages.SdkIniFileWriter_ComentMainComponents);
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentXcPath);
        putSdkProp(sb, Sdk.Property.XDS_COMPILER,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_COMPILER))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentXdPath);
        putSdkProp(sb, Sdk.Property.XDS_DEBUGGER,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_DEBUGGER))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentProfilerPath);
        putSdkProp(sb, Sdk.Property.XDS_PROFILER,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_PROFILER))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentSimulatorPath);
        putSdkProp(sb, Sdk.Property.XDS_SIMULATOR,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_SIMULATOR))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentLibPath0);
        putLine(sb, Messages.SdkIniFileWriter_ComentLibPath1);
        putSdkProp(sb, Sdk.Property.XDS_LIB_DEFS_PATH,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_LIB_DEFS_PATH))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentExeExt);
        putSdkProp(sb, Sdk.Property.XDS_EXE_EXTENSION,  sdk.getPropertyValue(Sdk.Property.XDS_EXE_EXTENSION)); //$NON-NLS-1$
        putNewLine(sb);

        putLine(sb, Messages.SdkIniFileWriter_ComentPrimExts0);
        putLine(sb, Messages.SdkIniFileWriter_ComentPrimExts1);
        putSdkProp(sb, Sdk.Property.XDS_PRIM_EXTENSIONS,  sdk.getPropertyValue(Sdk.Property.XDS_PRIM_EXTENSIONS)); //$NON-NLS-1$
        putNewLine(sb);
        putNewLine(sb);
        
        
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putLine(sb, Messages.SdkIniFileWriter_ComentTemptates);
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentDirsList0);
        putLine(sb, Messages.SdkIniFileWriter_ComentDirsList1);
        putSdkProp(sb, Sdk.Property.XDS_DIRS_TO_CREATE,  sdk.getPropertyValue(Sdk.Property.XDS_DIRS_TO_CREATE)); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentPrjFolder); 
        putSdkProp(sb, Sdk.Property.XDS_FOLDER_PRJ_FILE,  sdk.getPropertyValue(Sdk.Property.XDS_FOLDER_PRJ_FILE)); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentMainFolder);
        putSdkProp(sb, Sdk.Property.XDS_FOLDER_MAIN_MODULE,  sdk.getPropertyValue(Sdk.Property.XDS_FOLDER_MAIN_MODULE)); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentTprPath); 
        putSdkProp(sb, Sdk.Property.XDS_TPR_FILE,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_TPR_FILE))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentTrdPath);
        putSdkProp(sb, Sdk.Property.XDS_TRD_FILE,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_TRD_FILE))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentMainTmdPath);
        putSdkProp(sb, Sdk.Property.XDS_MAIN_MOD_FILE,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_MAIN_MOD_FILE))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentModulesTmdPath);
        putSdkProp(sb, Sdk.Property.XDS_TMOD_FILE,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_TMOD_FILE))); //$NON-NLS-1$
        putNewLine(sb);
    
        putLine(sb, Messages.SdkIniFileWriter_ComentDefTmdPath);
        putSdkProp(sb, Sdk.Property.XDS_TDEF_FILE,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_TDEF_FILE))); //$NON-NLS-1$
        putNewLine(sb);

        putLine(sb, Messages.SdkIniFileWriter_ComentManifestPath);
        putSdkProp(sb, Sdk.Property.XDS_UPDATE_MANIFEST,  mkRelPath(home, sdk.getPropertyValue(Sdk.Property.XDS_UPDATE_MANIFEST))); //$NON-NLS-1$
        putNewLine(sb);
        putNewLine(sb);
    
    
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putLine(sb, Messages.SdkIniFileWriter_ComentTools);
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putNewLine(sb);
    
        for (SdkTool tool : sdk.getTools()) {
            putLine(sb, "[Tool]"); //$NON-NLS-1$
            if (tool.isSeparator()) {
                putLine(sb, "isSeparator = 1"); //$NON-NLS-1$
                
                putToolProp( sb, tool, SdkTool.Property.MENU_GROUP
                        , Messages.SdkIniFileWriter_CommentToolGroup0 
                        , Messages.SdkIniFileWriter_CommentToolGroup1 );

            } else {
                putToolProp( sb, tool, SdkTool.Property.NAME
                           , Messages.SdkIniFileWriter_ComentToolName );
        
                putLine(sb, Messages.SdkIniFileWriter_ComentToolLocation);
                putOpt(sb, SdkTool.Property.LOCATION.key, mkRelPath(home, tool.getPropertyValue(SdkTool.Property.LOCATION)));
                putNewLine(sb);
    
                putToolProp( sb, tool, SdkTool.Property.MENU_ITEM
                           , Messages.SdkIniFileWriter_ComentToolMenuOn0 
                           , Messages.SdkIniFileWriter_ComentToolMenuOn1 );
    
                putToolProp( sb, tool, SdkTool.Property.INACTIVE_MENU_ITEM
                           , Messages.SdkIniFileWriter_ComentToolMenuOff0 
                           , Messages.SdkIniFileWriter_ComentToolMenuOff1 );
                
                putToolProp( sb, tool, SdkTool.Property.MENU_GROUP
                           , Messages.SdkIniFileWriter_CommentToolGroup0 
                           , Messages.SdkIniFileWriter_CommentToolGroup1 );
                
                putToolProp( sb, tool, SdkTool.Property.CONSOLE_CODEPAGE
                        , Messages.SdkIniFileWriter_CommentToolCodepage0 
                        , Messages.SdkIniFileWriter_CommentToolCodepage1 );
             
                putToolProp( sb, tool, SdkTool.Property.FILE_EXTENSIONS 
                           , Messages.SdkIniFileWriter_ComentToolFileExtList0 
                           , Messages.SdkIniFileWriter_ComentToolFileExtList1 );
    
                {
                    String sourceRoots = "# "; //$NON-NLS-1$
                    for (SdkTool.SourceRoot srcRoot : SdkTool.SourceRoot.values()) {
                        sourceRoots += srcRoot.keyValue + "  "; //$NON-NLS-1$
                    }
                    putToolProp( sb, tool, SdkTool.Property.SOURCE_ROOT
                               , Messages.SdkIniFileWriter_ComentToolSourceRoot1 
                               , Messages.SdkIniFileWriter_ComentToolSourceRoot2
                               , sourceRoots );
                }
                
                putToolProp( sb, tool, SdkTool.Property.ARGUMENTS_ANY_TYPE 
                           , Messages.SdkIniFileWriter_ComentToolArgs
                           , Messages.SdkIniFileWriter_ComentToolFor + SdkTool.Property.SOURCE_ROOT.key + " = " + SdkTool.SourceRoot.ANY_TYPE.keyValue );  //$NON-NLS-1$
    
                putToolProp( sb, tool, SdkTool.Property.WORKDIR_ANY_TYPE
                           , Messages.SdkIniFileWriter_ComentToolWorkDir
                           , Messages.SdkIniFileWriter_ComentToolFor + SdkTool.Property.SOURCE_ROOT.key + " = " + SdkTool.SourceRoot.ANY_TYPE.keyValue );  //$NON-NLS-1$
    
    
                putToolProp( sb, tool, SdkTool.Property.ARGUMENTS_PRJ_FILE
                           , Messages.SdkIniFileWriter_ComentToolArgs 
                           , Messages.SdkIniFileWriter_ComentToolFor + SdkTool.Property.SOURCE_ROOT.key + " = " + SdkTool.SourceRoot.PRJ_FILE.keyValue + " | " + SdkTool.SourceRoot.ANY_TYPE_OWN_SETTINGS.keyValue );  //$NON-NLS-1$//$NON-NLS-2$
        
                putToolProp( sb, tool, SdkTool.Property.WORKDIR_PRJ_FILE
                           , Messages.SdkIniFileWriter_ComentToolWorkDir 
                           , Messages.SdkIniFileWriter_ComentToolFor + SdkTool.Property.SOURCE_ROOT.key + " = " + SdkTool.SourceRoot.PRJ_FILE.keyValue + " | " + SdkTool.SourceRoot.ANY_TYPE_OWN_SETTINGS.keyValue );  //$NON-NLS-1$//$NON-NLS-2$
        
                putToolProp( sb, tool, SdkTool.Property.ARGUMENTS_MAIN_MODULE
                           , Messages.SdkIniFileWriter_ComentToolArgs
                           , Messages.SdkIniFileWriter_ComentToolFor + SdkTool.Property.SOURCE_ROOT.key + " = " + SdkTool.SourceRoot.MAIN_MODULE.keyValue + " | " + SdkTool.SourceRoot.ANY_TYPE_OWN_SETTINGS.keyValue );  //$NON-NLS-1$//$NON-NLS-2$
    
                putToolProp( sb, tool, SdkTool.Property.WORKDIR_MAIN_MODULE
                           , Messages.SdkIniFileWriter_ComentToolWorkDir 
                           , Messages.SdkIniFileWriter_ComentToolFor + SdkTool.Property.SOURCE_ROOT.key + " = " + SdkTool.SourceRoot.MAIN_MODULE.keyValue + " | " + SdkTool.SourceRoot.ANY_TYPE_OWN_SETTINGS.keyValue );  //$NON-NLS-1$//$NON-NLS-2$
            }
            putNewLine(sb);
        }
    
        
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putLine(sb, Messages.SdkIniFileWriter_ComentToolEnvNars0);
        putLine(sb, Messages.SdkIniFileWriter_ComentToolEnvVars1); 
        putLine(sb, Messages.SdkIniFileWriter_ComentToolEnvVars2);
        putLine(sb, Messages.SdkIniFileWriter_SeparatorLine);
        putNewLine(sb);
        putLine(sb, "[Environment]"); //$NON-NLS-1$
        putNewLine(sb);
    
        Map<String, String> envs = sdk.getEnvironmentVariablesRaw();
        for (String var : envs.keySet()) {
            String val = envs.get(var);
            if (val == null) val = ""; //$NON-NLS-1$
            if (var.equalsIgnoreCase(ENVVAR_XDSDIR)) {
                String s = FilenameUtils.normalize(val + File.separator);
                boolean b;
                if (File.separatorChar == '\\') {
                    b = s.equalsIgnoreCase(home);
                } else {
                    b = s.equals(home);
                }
                if (b) {
                    continue; // skip XDSDIR var if it == sdk home (it will be created automatically)
                }
            }
            putOpt(sb, var, val); 
            putNewLine(sb);
        }
    
        putNewLine(sb);
        
        // Write ini file:
        try {
            FileWriter fw = new FileWriter(new File(iniFileName));
            fw.write(sb.toString());
            fw.close();
        } catch (Exception e) {
            LogHelper.logError(e);
        }
    }

    private void putLine(StringBuilder sb, String line) {
        if (! StringUtils.isBlank(line)) {
            sb.append(line).append("\n"); //$NON-NLS-1$
        }
    }
    
    private void putNewLine(StringBuilder sb) {
            sb.append("\n"); //$NON-NLS-1$
    }
    
    
    private void putSdkProp(StringBuilder sb, Sdk.Property prop, String value) {
        putOpt(sb, prop.key, value);
        for (Sdk.Tag tag : prop.possibleTags) {
            String val = sdk.getTag(prop, tag);
            if (val != null) {
                String tagName = prop.key + "." + tag.tagName; //$NON-NLS-1$
                sb.append(tagName).append(" = ").append(val).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    
    private void putOpt(StringBuilder sb, String name, String value) {
        if (StringUtils.isBlank(value)) { 
            sb.append("#").append(name).append(" =").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            sb.append(name).append(" = ").append(value).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    private void putToolProp( StringBuilder sb, SdkTool tool
                                   , SdkTool.Property prop
                                   , String... comments ) 
    {
        for (String comment :  comments) {
                putLine(sb, comment);
        }
        putOpt(sb, prop.key, tool.getPropertyValue(prop)); 
        putNewLine(sb);
    }

    
    private String mkRelPath(String home, String path) {
        if (path == null || Sdk.NOT_SUPPORTED.equals(path)) {
            return path;
        }
        String file = FilenameUtils.normalize(path);
        boolean isParent;
        if (File.separatorChar == '\\') {
            isParent = file.toLowerCase().startsWith(home.toLowerCase()); // Win
        } else {
            isParent = file.startsWith(home); // Linux
        }
        if (isParent) {
            file = file.substring(home.length());
            if (file.startsWith(File.separator)) {
                file = file.substring(1);
            }
        }
        return file;
    }
}
