package com.excelsior.xds.core.sdk;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.internal.nls.Messages;
import com.excelsior.xds.core.project.XdsProjectType;
import com.excelsior.xds.core.resource.EncodingUtils;
import com.excelsior.xds.core.variables.VariableUtils;

/**
 * SDK Tool settings.
 *  
 * @author lsa80, lion, fsa
 */
public class SdkTool {

    public static enum Property {
        // property        | ini-file key              | xml-file tag         
        NAME               ("tool.name",                "Name",                 null), //$NON-NLS-1$  //$NON-NLS-2$
        LOCATION           ("tool.location",            "Location",             null), //$NON-NLS-1$  //$NON-NLS-2$ 
        SOURCE_ROOT        ("tool.source.root",         "SourceRoot",           null), //$NON-NLS-1$  //$NON-NLS-2$ 
        FILE_EXTENSIONS    ("tool.file.extensions",     "FileExtensions",       null), //$NON-NLS-1$  //$NON-NLS-2$ 
        MENU_ITEM          ("tool.menu.item",           "MenuItemBoth",         null), //$NON-NLS-1$  //$NON-NLS-2$
        INACTIVE_MENU_ITEM ("tool.menu.item.inactive",  "InactiveMenuItemBoth", null), //$NON-NLS-1$  //$NON-NLS-2$
        MENU_GROUP         ("tool.menu.group",          "MenuGroup",            null), //$NON-NLS-1$  //$NON-NLS-2$
        CONSOLE_CODEPAGE   ("tool.console.codepage",    "ConsoleCodepage",      null), //$NON-NLS-1$  //$NON-NLS-2$

        // Properties for all mode containers:
        
        ARGUMENTS_PRJ_FILE    ("tool.arguments.prj_file",     "PrjFileArguments",            SourceRootSettingsType.PRJ_FILE), //$NON-NLS-1$  //$NON-NLS-2$ 
        WORKDIR_PRJ_FILE      ("tool.workdir.prj_file",       "PrjFileWorkingDirectory",     SourceRootSettingsType.PRJ_FILE), //$NON-NLS-1$  //$NON-NLS-2$  
        
        ARGUMENTS_MAIN_MODULE ("tool.arguments.main_module",  "MainModuleArguments",         SourceRootSettingsType.MAIN_MODULE), //$NON-NLS-1$  //$NON-NLS-2$ 
        WORKDIR_MAIN_MODULE   ("tool.workdir.main_module",    "MainModuleWorkingDirectory",  SourceRootSettingsType.MAIN_MODULE), //$NON-NLS-1$  //$NON-NLS-2$  
        
        ARGUMENTS_ANY_TYPE    ("tool.arguments",              "Arguments",                   SourceRootSettingsType.ANY_TYPE), //$NON-NLS-1$  //$NON-NLS-2$ 
        WORKDIR_ANY_TYPE      ("tool.workdir",                "WorkingDirectory",            SourceRootSettingsType.ANY_TYPE); //$NON-NLS-1$  //$NON-NLS-2$  
        
        /** Tool property key for 'sdk.ini' file */
        public final String key;  
        
        /** SDK property key for internal 'sdk.xml' file */
        public final String tag;
        
        /** source root type or null */
        public final SourceRootSettingsType srcRoot;
        
        /** 
         * @param key ini-file key associated with the property
         * @param tag xml-tag associated with property   
         */
        Property (String key, String tag, SourceRootSettingsType srcRoot) {
            this.key     = key; 
            this.tag     = tag; 
            this.srcRoot = srcRoot;
        }
    };
    
    private static enum Error {
        NONE                     (""),                //$NON-NLS-1$
        INVALID_TOOL_NAME        (Messages.Tool_InvalidToolName),
        INVALID_TOOL_LOCATION    (Messages.Tool_InvalidToolLocation),
        INVALID_WORKDIR_VARS     (Messages.Tool_InvalidToolWorkDir_BadVars);
        
        public final String message; 
        
        Error (String message) {
            this.message = message;
        }
    };
    
    /**
     * Possible values of the <code>Property.SOURCE_ROOT</code> in 'sdk.ini' file.
     */
    public static enum SourceRoot {
        PRJ_FILE              ("prj_file"),    //$NON-NLS-1$
        MAIN_MODULE           ("main_module"), //$NON-NLS-1$
        ANY_TYPE              ("any_type"),    //$NON-NLS-1$ 
        ANY_TYPE_OWN_SETTINGS ("any_type_with_own_settings"); //$NON-NLS-1$
        
        public final String keyValue; 
        
        SourceRoot (String keyValue) {
            this.keyValue = keyValue;
        }
        
        public static SourceRoot getInstance(String s) {
            for (SourceRoot srcRoot : values()) {
                if (srcRoot.keyValue.equals(s)) {
                    return srcRoot;
                }
            }
            return null;
        }
    };
    
    /**
     * Types of source root settings. Four source roots use tree type of settings.
     * Source root           | Settings type
     * ----------------------|---------------
     * PRJ_FILE              | PRJ_FILE
     * MAIN_MODULE           | MAIN_MODULE
     * ANY_TYPE              | ANY_TYPE
     * ANY_TYPE_OWN_SETTINGS | PRJ_FILE & MAIN_MODULE 
     */
    public static enum SourceRootSettingsType {
        PRJ_FILE   (0),
        MAIN_MODULE(1),
        ANY_TYPE   (2);

        public final int idx;    // Index in sourceRootSettings[]
        
        /** 
         * @param idx source root index in modeData[]     
         */
        SourceRootSettingsType (int idx) {
            this.idx = idx;
        }
    }

    private Sdk sdk;
	private Error error = Error.NONE;
	private boolean isSeparator = false;   // It is not tool, it is separator in tool menu
	
	private String toolName  = ""; //$NON-NLS-1$
	private String location  = ""; //$NON-NLS-1$
    private String menuItem  = ""; //$NON-NLS-1$
    private String inactiveMenuItem = ""; //$NON-NLS-1$
    private String menuGroup = ""; //$NON-NLS-1$
    private String fileExtensions   = ""; //$NON-NLS-1$
    private List<String> fileExtensionsList; // extensions list (in lower case)
    private SourceRoot sourceRoot; 
    private String consoleCodepage = EncodingUtils.DOS_ENCODING; // For unknown codepage names system-default CP will be used //$NON-NLS-1$ 

	// This settings depends on type of source root for which the tool is available:
    private class SourceRootSettings {
        String arguments        = ""; //$NON-NLS-1$
        String workingDirectory = ""; //$NON-NLS-1$
        
        public void copyFrom(SourceRootSettings from) {
            arguments        = from.arguments;
            workingDirectory = from.workingDirectory;
        }
    }
    
    private SourceRootSettings sourceRootSettings[] = { new SourceRootSettings()
                                                      , new SourceRootSettings()
                                                      , new SourceRootSettings() 
                                                      };
    
    private SourceRootSettings getSourceRootSettings(SourceRootSettingsType m) {
        return sourceRootSettings[m.idx];
    }
    
    private SourceRootSettings getSourceRootSettings(XdsProjectType prjtype) {
        switch (sourceRoot) {
        case PRJ_FILE:
            return (prjtype == XdsProjectType.PROJECT_FILE) ? sourceRootSettings[SourceRootSettingsType.PRJ_FILE.idx] : null; 
        case MAIN_MODULE:
            return prjtype == XdsProjectType.MAIN_MODULE ? sourceRootSettings[SourceRootSettingsType.MAIN_MODULE.idx] : null; 
        case ANY_TYPE_OWN_SETTINGS:
            return (prjtype == XdsProjectType.PROJECT_FILE) ? 
                    sourceRootSettings[SourceRootSettingsType.PRJ_FILE.idx] :
                    sourceRootSettings[SourceRootSettingsType.MAIN_MODULE.idx];
        default: // ALL_PROJECTS
            return sourceRootSettings[SourceRootSettingsType.ANY_TYPE.idx];
        }
    }
    
    ////////////////
    
	public SdkTool(Sdk sdk) {
		this.sdk = sdk;
		this.sourceRoot = SourceRoot.ANY_TYPE;
	}

	/**
	 * Makes manu separator, not a tool
	 */
    public SdkTool() {
        isSeparator = true;
    }
	public Sdk getSdk() {
		return sdk;
	}
	
	public boolean isSeparator() {
	    return isSeparator;
	}

	public String getToolName() {
		return toolName;
	}
	
	public String getLocation() {
		return location;
	}

    public String getArguments(XdsProjectType prjtype) {
        SourceRootSettings md = getSourceRootSettings(prjtype); 
        return md == null ? "" : md.arguments; //$NON-NLS-1$
    }

    public String getArguments(SourceRootSettingsType mode) {
        return getSourceRootSettings(mode).arguments;
    }

	public String getWorkingDirectory(XdsProjectType prjtype) {
        SourceRootSettings md = getSourceRootSettings(prjtype); 
        return md == null ? "" : md.workingDirectory; //$NON-NLS-1$
	}

    public String getWorkingDirectory(SourceRootSettingsType mode) {
        return getSourceRootSettings(mode).workingDirectory;
    }

    public String getMenuItem() {
        return menuItem;
    }

	public String getInactiveMenuItem() {
        return inactiveMenuItem;
	}
	
    public String getMenuGroup() {
        return menuGroup;
    }

    public SourceRoot getSourceRoot() {
        return sourceRoot;
    }
    
    public String getFileExtensions() {
        return fileExtensions;
    }
    
	public List<String> getFileExtensionsList() {
		return fileExtensionsList;
	}

	public void setToolName(String name) {
		this.toolName = name;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setArguments(String commandLineArguments, SourceRootSettingsType m) {
	    getSourceRootSettings(m).arguments = commandLineArguments;
	}

	public void setWorkingDirectory(String workingDirectory, SourceRootSettingsType m) {
        getSourceRootSettings(m).workingDirectory = workingDirectory;
	}

    public void setFileExtensions(String fileExtensions) {
        this.fileExtensions = fileExtensions;
        if (fileExtensions != null) {
            String[] extensions = StringUtils.split(fileExtensions.toLowerCase(), ';');
            fileExtensionsList = Arrays.asList(extensions);
        } else {
            fileExtensionsList = null;
        }
    }

    public void setMenuItem(String menuItemCaption) {
        menuItem = menuItemCaption;
	}

	public void setInactiveMenuItem(String menuItemCaption) {
	    inactiveMenuItem = menuItemCaption;
	}
	
    public void setMenuGroup(String grp) {
        menuGroup = grp;
    }

    public void setSourceRoot(SourceRoot srcRoot) {
        sourceRoot = srcRoot;
    }
    
    public void setPropertyValue (Property property, String val) {
        switch (property) {
        case NAME:               
            toolName = val;
            break;
        case LOCATION:           
            location = FilenameUtils.concat(sdk.getSdkHomePath(), val);
            break;
        case ARGUMENTS_PRJ_FILE:          
        case ARGUMENTS_MAIN_MODULE:          
        case ARGUMENTS_ANY_TYPE:   
            SourceRootSettings md = getSourceRootSettings(property.srcRoot); 
            md.arguments = val;
            break;
        case WORKDIR_PRJ_FILE:            
        case WORKDIR_MAIN_MODULE:            
        case WORKDIR_ANY_TYPE:            
            getSourceRootSettings(property.srcRoot).workingDirectory = val;
            break;
        case MENU_ITEM:          
            menuItem = val;
            break;
        case INACTIVE_MENU_ITEM: 
            inactiveMenuItem = val;
            break;
        case MENU_GROUP: 
            menuGroup = val;
            break;
        case SOURCE_ROOT:       
            SourceRoot sr = SourceRoot.getInstance(val);
            Assert.isTrue(sr != null, "Wrong availablilty value: " + val);  //$NON-NLS-1$
            sourceRoot = sr;
            break;
        case FILE_EXTENSIONS:    
            setFileExtensions(val);
            break;
        case CONSOLE_CODEPAGE:    
            consoleCodepage = val;
            break;
      default:
            Assert.isTrue(false, "Internal error: invalid SDK Tool property identifier");  //$NON-NLS-1$
        }        
    }

    public String getPropertyValue (Property property) {
        switch (property) {
        case NAME:               
            return toolName;
        case LOCATION:           
            return location;
        case ARGUMENTS_PRJ_FILE:          
        case ARGUMENTS_MAIN_MODULE:          
        case ARGUMENTS_ANY_TYPE:          
            return getSourceRootSettings(property.srcRoot).arguments;
        case WORKDIR_PRJ_FILE:            
        case WORKDIR_MAIN_MODULE:            
        case WORKDIR_ANY_TYPE:            
            return getSourceRootSettings(property.srcRoot).workingDirectory;
        case MENU_ITEM:          
            return menuItem;
        case INACTIVE_MENU_ITEM: 
            return inactiveMenuItem;
        case MENU_GROUP: 
            return menuGroup;
        case SOURCE_ROOT:       
            return sourceRoot.keyValue;
        case FILE_EXTENSIONS:    
            return fileExtensions;
        case CONSOLE_CODEPAGE:    
            return consoleCodepage;
        }
        Assert.isTrue(false, "Internal error: invalid SDK Tool property identifier");  //$NON-NLS-1$
        return null;
    }
    
    // Used in SdkIniFileReader
    public boolean isValid() {
        if (isSeparator()) {
            return true;
        }
        if (StringUtils.isBlank(toolName)) {
            error = Error.INVALID_TOOL_NAME;
            return false;
        }
        if (StringUtils.isBlank(location) || !(new File(location).isFile())) {
            error = Error.INVALID_TOOL_LOCATION;
            return false;
        }
        for (SourceRootSettings md : sourceRootSettings) {
            if (!StringUtils.isBlank(md.workingDirectory)) {
                try {
                	md.workingDirectory = VariableUtils.performStringSubstitution(this.getSdk(), md.workingDirectory);
                } catch (CoreException e) {
                    error = Error.INVALID_WORKDIR_VARS;
                    return false;
                }
            }
        }
        error = Error.NONE;
        return true;
    }

    public String getErrorMessage() {
        return error.message;
    }
    
    @Override
    public SdkTool clone() {
        SdkTool tool = new SdkTool(sdk);
        tool.copyFrom(this);
        return tool;
    }

    public void copyFrom(SdkTool from) {
	    sdk              = from.sdk;
        error            = from.error;
        toolName         = from.toolName;
        location         = from.location;
        menuItem         = from.menuItem;
        menuGroup     = from.menuGroup;
        inactiveMenuItem = from.inactiveMenuItem;
        fileExtensions   = from.fileExtensions;
        sourceRoot       = from.sourceRoot; 
        consoleCodepage  = from.consoleCodepage; 
        setFileExtensions(from.fileExtensions);
        for (int i=0; i<sourceRootSettings.length; ++i) {
            sourceRootSettings[i].copyFrom(from.sourceRootSettings[i]);
        }
	}
    
    @Override
    public String toString() {
        return (isSeparator ? "Separator" : ("Name='" + toolName + "'")) + "(grp='" + menuGroup + "')"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
	
//	@Override
//	public String toString() {
//		return "Tool [name=" + toolName + ", location=" + location //$NON-NLS-1$ //$NON-NLS-2$
//				+ ", fileExtensions=" + fileExtensions + "]"; //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
//	}
}