package com.excelsior.xds.core.templates;

import com.excelsior.xds.core.sdk.Sdk;

/**
 * Defines traits of the source template :
 * template source file name, extension, related SDK property
 */
public enum SourceFileTemplate {
	MAIN_MODULE("main.tmd", ".mod", Sdk.Property.XDS_MAIN_MOD_FILE),//$NON-NLS-1$ //$NON-NLS-2$
	DEFITION("new_def.tmd", ".def", Sdk.Property.XDS_TDEF_FILE),//$NON-NLS-1$ //$NON-NLS-2$
	IMPLEMENTATION("new_mod.tmd", ".mod", Sdk.Property.XDS_TMOD_FILE),//$NON-NLS-1$ //$NON-NLS-2$
    OBERON("new_ob2.tmd", ".ob2", Sdk.Property.XDS_TOB2_FILE);//$NON-NLS-1$ //$NON-NLS-2$
	
    public final String fileName;  
    public final String extension;
    public final Sdk.Property sdkProperty;
    
	private SourceFileTemplate(String fileName, String extension, Sdk.Property sdkProperty) {
		this.fileName = fileName;
		this.extension = extension;
		this.sdkProperty = sdkProperty;
	}
}
