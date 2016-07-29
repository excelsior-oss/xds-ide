package com.excelsior.xds.ui.images;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

import com.excelsior.xds.ui.XdsPlugin;

public abstract class ImageUtils {
    public static final String BTN_TERMINATE = "btnTerminate.gif"; //$NON-NLS-1$
    public static final String BTN_TERMINATE_DISABLED = "btnTerminateDis.gif"; //$NON-NLS-1$
    public static final String BTN_FILTER = "btnFilter.gif"; //$NON-NLS-1$
    public static final String ERROR_16x16 = "error.gif"; //$NON-NLS-1$
    public static final String EMPTY_16x16 = "empty_16x16.gif"; //$NON-NLS-1$
    public static final String IN_COMP_SET_OVERLAY_IMAGE_NAME = "compilation_set_overlay.gif"; //$NON-NLS-1$
    public static final String LAUNCHER_TAB_ARGUMENTS = "launcherTabArguments.gif"; //$NON-NLS-1$
    public static final String LAUNCHER_TAB_SETTINGS = "launcherTabSettings.gif"; //$NON-NLS-1$
    public static final String XDS_PROJECT_FILE_IMAGE_NAME = "xds_project_file.gif"; //$NON-NLS-1$
    public static final String DEFINITION_MODULE_IMAGE_NAME = "m2_def_file.gif"; //$NON-NLS-1$
    public static final String IMPLEMENTATION_MODULE_IMAGE_NAME = "m2_mod_file.gif"; //$NON-NLS-1$
    public static final String SYMBOL_FILE_IMAGE_NAME = "m2_sym_file.gif"; //$NON-NLS-1$
    public static final String PACKAGE_FRAGMENT_IMAGE_NAME = "m2_folder.gif"; //$NON-NLS-1$
    public static final String EXTERNAL_DEPENDENCIES_FOLDER_IMAGE_NAME = "external_dependencies_folder.gif"; //$NON-NLS-1$
    public static final String SDK_LIBRARY_FOLDER_IMAGE_NAME = "external_dependencies_folder.gif"; //$NON-NLS-1$
    public static final String TEMPLATE_IMAGE_NAME = "template.gif"; //$NON-NLS-1$
    public static final String TOOLS = "tools.gif"; //$NON-NLS-1$
    public static final String XDS_GREEN_IMAGE       = "xds.gif"; //$NON-NLS-1$
    public static final String XDS_GREEN_IMAGE_16X16 = "xds16x16.gif"; //$NON-NLS-1$
    public static final String XDS_GREEN_IMAGE_74X66 = "xds74x66.gif"; //$NON-NLS-1$
    public static final String BTN_REFRESH           = "refresh.gif"; //$NON-NLS-1$
    public static final String BTN_REFRESH_DISABLED  = "refreshDis.gif"; //$NON-NLS-1$
    public static final String SYNC_WITH_EDITOR = "synced.gif"; //$NON-NLS-1$
    public static final String SORT_ALPHA       = "sort_alpha.gif"; //$NON-NLS-1$
    public static final String SEARCH_NEXT      = "search_next.gif"; //$NON-NLS-1$
    public static final String SEARCH_PREV      = "search_prev.gif"; //$NON-NLS-1$
    public static final String COLLAPSE_ALL = "collapseall.gif"; //$NON-NLS-1$
    public static final String EXPAND_ALL   = "expandall.gif"; //$NON-NLS-1$
    public static final String GROUP_BY_FILE    = "groupByFile.gif"; //$NON-NLS-1$ 
    public static final String GROUP_BY_PROJECT = "groupByProject.gif"; //$NON-NLS-1$ 
    public static final String FILTERS_ICON   = "filter_ps.gif"; //$NON-NLS-1$
    public static final String M2_PRJ_FOLDER_TRANSP   = "m2_project_folder_transp.gif"; //$NON-NLS-1$
    public static final String SCROLL_LOCK            = "scroll_lock.gif"; //$NON-NLS-1$
    public static final String SCROLL_LOCK_DIS        = "scroll_lock_dis.gif"; //$NON-NLS-1$
    public static final String CORRECTION_ADD         = "correction_add.gif"; //$NON-NLS-1$
    public static final String CORRECTION_RENAME      = "correction_rename.gif"; //$NON-NLS-1$
    public static final String CORRECTION_TURN_OFF    = "correction_turnoff.gif"; //$NON-NLS-1$

    
    // Object icons (used in outline view):
    public static final String OBJ_IMPORT_LIST_16x16    = "obj16/import_list.png";     //$NON-NLS-1$
    public static final String OBJ_IMPORT_16x16         = "obj16/import.png";     //$NON-NLS-1$
    
    public static final String OBJ_DEF_MODULE_16x16     = "obj16/def_module.png"; //$NON-NLS-1$
    public static final String OBJ_PROGRAM_MODULE_16x16 = "obj16/program_module.png"; //$NON-NLS-1$
    public static final String OBJ_LOCAL_MODULE_16x16   = "obj16/program_module.png";   //$NON-NLS-1$
    
    public static final String OBJ_CONSTANT_16x16       = "obj16/constant.png"; //$NON-NLS-1$
    public static final String OBJ_CONSTANT_PUB_16x16   = "obj16/constant_pub.png"; //$NON-NLS-1$
    
    public static final String OBJ_ENUM_16x16           = "obj16/enum.png"; //$NON-NLS-1$
    public static final String OBJ_ENUM_PUB_16x16       = "obj16/enum_pub.png"; //$NON-NLS-1$
    public static final String OBJ_ENUM_ITEM_16x16      = "obj16/enum_item.png"; //$NON-NLS-1$
    public static final String OBJ_ENUM_ITEM_PUB_16x16  = "obj16/enum_item_pub.png"; //$NON-NLS-1$
    
    public static final String OBJ_GLOB_VAR_16x16       = "obj16/glob_var.png"; //$NON-NLS-1$
    public static final String OBJ_GLOB_VAR_PUB_16x16   = "obj16/glob_var_pub.png"; //$NON-NLS-1$
    public static final String OBJ_GLOB_VAR_EXT_16x16   = "obj16/glob_var_ext.png"; //$NON-NLS-1$
    public static final String OBJ_GLOB_VAR_RO_16x16    = "obj16/glob_var_ro.png"; //$NON-NLS-1$
    public static final String OBJ_LOC_VAR_16x16        = "obj16/glob_var.png"; //$NON-NLS-1$
    
    public static final String OBJ_OBERON_PROCEDURE_16x16     = "obj16/ob2_procedure.png"; //$NON-NLS-1$
    public static final String OBJ_OBERON_PROCEDURE_PUB_16x16 = "obj16/ob2_procedure_pub.png"; //$NON-NLS-1$
    
    public static final String OBJ_OBERON_RECORD_16x16     = "obj16/ob2_record.png"; //$NON-NLS-1$
    public static final String OBJ_OBERON_RECORD_PUB_16x16 = "obj16/ob2_record_pub.png"; //$NON-NLS-1$
    public static final String OBJ_OBERON_RECORD_RO_16x16  = "obj16/ob2_record_ro.png"; //$NON-NLS-1$
    
    public static final String OBJ_PROCEDURE_16x16      = "obj16/procedure.png"; //$NON-NLS-1$
    public static final String OBJ_PROCEDURE_PUB_16x16  = "obj16/procedure_pub.png"; //$NON-NLS-1$
    public static final String OBJ_PROCEDURE_EXT_16x16  = "obj16/procedure_ext.png"; //$NON-NLS-1$
    
    public static final String OBJ_RECORD_16x16           = "obj16/record.png"; //$NON-NLS-1$
    public static final String OBJ_RECORD_PUB_16x16       = "obj16/record_pub.png"; //$NON-NLS-1$
    public static final String OBJ_RECORD_RO_16x16        = "obj16/record_ro.png"; //$NON-NLS-1$
    
    public static final String OBJ_RECORD_FIELD_16x16     = "obj16/record_field.png"; //$NON-NLS-1$
    public static final String OBJ_RECORD_FIELD_PUB_16x16 = "obj16/record_field_pub.png"; //$NON-NLS-1$
    public static final String OBJ_RECORD_FIELD_RO_16x16  = "obj16/record_field_ro.png"; //$NON-NLS-1$

    public static final String OBJ_VARIANT_FIELDS_16x16 = "obj16/variant_fields.png";     //$NON-NLS-1$
    public static final String OBJ_VARIANT_RECORD_16x16 = "obj16/variant_record.png";     //$NON-NLS-1$

    public static final String OBJ_TYPE_16x16           = "obj16/type.png"; //$NON-NLS-1$
    public static final String OBJ_TYPE_PUB_16x16       = "obj16/type_pub.png"; //$NON-NLS-1$
    public static final String OBJ_TYPE_RO_16x16        = "obj16/type_ro.png"; //$NON-NLS-1$

    public static final String OBJ_FORAML_PARAMETER_16x16     = "obj16/proc_param.png"; //$NON-NLS-1$
    public static final String OBJ_FORAML_PARAMETER_PUB_16x16 = "obj16/proc_param_pub.png"; //$NON-NLS-1$
    public static final String OBJ_FORAML_PARAMETER_RO_16x16  = "obj16/proc_param_ro.png"; //$NON-NLS-1$
    public static final String OBJ_OBERON_METHOD_RECEIVER_16x16 = "obj16/ob2_this_param.png"; //$NON-NLS-1$

    public static final String OBJ_LOADING_16x16        = "obj16/loading.gif";     //$NON-NLS-1$
    
    private static String ICONS_PATH = "$nl$/icons/"; //$NON-NLS-1$
    
    private static ImageRegistry imageRegistry;
    private static Object monitor = new Object();
    
    public static Image getImage(String name) {
        return getImageRegistry().get(name);
    }
    
    private static void declareImages() {
             for (String s : new String[] {
            	BTN_TERMINATE,
            	BTN_TERMINATE_DISABLED,
                BTN_FILTER,
                ERROR_16x16,
                EMPTY_16x16,
                IMPLEMENTATION_MODULE_IMAGE_NAME,
                DEFINITION_MODULE_IMAGE_NAME,
                SYMBOL_FILE_IMAGE_NAME,
                XDS_PROJECT_FILE_IMAGE_NAME,
                PACKAGE_FRAGMENT_IMAGE_NAME,
                EXTERNAL_DEPENDENCIES_FOLDER_IMAGE_NAME,
                SDK_LIBRARY_FOLDER_IMAGE_NAME,
                LAUNCHER_TAB_ARGUMENTS,
                LAUNCHER_TAB_SETTINGS,
                IN_COMP_SET_OVERLAY_IMAGE_NAME,
                XDS_GREEN_IMAGE,
                XDS_GREEN_IMAGE_16X16,
                XDS_GREEN_IMAGE_74X66,
                TEMPLATE_IMAGE_NAME,
                TOOLS,
                BTN_REFRESH,
                BTN_REFRESH_DISABLED,
                SYNC_WITH_EDITOR,
                SORT_ALPHA,
                SEARCH_NEXT,
                SEARCH_PREV,
                COLLAPSE_ALL,
                EXPAND_ALL,
                GROUP_BY_FILE, 
                GROUP_BY_PROJECT, 
                FILTERS_ICON,
                M2_PRJ_FOLDER_TRANSP,
                SCROLL_LOCK,
                SCROLL_LOCK_DIS,
                CORRECTION_ADD,
                CORRECTION_RENAME,
                CORRECTION_TURN_OFF,

                // Object icons (used in outline view):
                OBJ_IMPORT_LIST_16x16,
                OBJ_VARIANT_FIELDS_16x16,
                OBJ_VARIANT_RECORD_16x16,
                OBJ_IMPORT_16x16,
                OBJ_DEF_MODULE_16x16,
                OBJ_PROGRAM_MODULE_16x16,
                OBJ_LOCAL_MODULE_16x16,
                OBJ_CONSTANT_16x16,
                OBJ_CONSTANT_PUB_16x16,
                OBJ_ENUM_16x16,
                OBJ_ENUM_PUB_16x16,
                OBJ_ENUM_ITEM_16x16,
                OBJ_ENUM_ITEM_PUB_16x16,
                OBJ_GLOB_VAR_16x16,
                OBJ_GLOB_VAR_PUB_16x16,
                OBJ_GLOB_VAR_EXT_16x16,
                OBJ_GLOB_VAR_RO_16x16,
                OBJ_LOC_VAR_16x16,
                OBJ_OBERON_PROCEDURE_16x16,
                OBJ_OBERON_PROCEDURE_PUB_16x16,
                OBJ_OBERON_RECORD_16x16,
                OBJ_OBERON_RECORD_PUB_16x16,
                OBJ_OBERON_RECORD_RO_16x16,
                OBJ_PROCEDURE_16x16,
                OBJ_PROCEDURE_PUB_16x16,
                OBJ_PROCEDURE_EXT_16x16,
                OBJ_RECORD_16x16,
                OBJ_RECORD_PUB_16x16,
                OBJ_RECORD_RO_16x16,
                OBJ_RECORD_FIELD_16x16,
                OBJ_RECORD_FIELD_PUB_16x16,
                OBJ_RECORD_FIELD_RO_16x16,
                OBJ_TYPE_16x16,
                OBJ_TYPE_PUB_16x16,
                OBJ_TYPE_RO_16x16,
                OBJ_FORAML_PARAMETER_16x16,
                OBJ_FORAML_PARAMETER_PUB_16x16,
                OBJ_FORAML_PARAMETER_RO_16x16,
                OBJ_OBERON_METHOD_RECEIVER_16x16,
                OBJ_LOADING_16x16,
        })
        {
            declareRegistryImage(s, ICONS_PATH + s);
        }
    }
    
    public static ImageRegistry getImageRegistry() {
    	synchronized (monitor) {
    		if (imageRegistry == null) {
                initializeImageRegistry();
            }
            return imageRegistry;
		}
    }
    
    public synchronized static ImageRegistry initializeImageRegistry() {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry(Display.getDefault());
            declareImages();
        }
        return imageRegistry;
    }
    
    private final static void declareRegistryImage(String key, String path) {
        ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
        Bundle bundle = Platform.getBundle(XdsPlugin.PLUGIN_ID);
        URL url = null;
        if (bundle != null){
            url = FileLocator.find(bundle, new Path(path), null);
            if(url != null) {
                desc = ImageDescriptor.createFromURL(url);
            }
        }
        imageRegistry.put(key, desc);
    }
}
