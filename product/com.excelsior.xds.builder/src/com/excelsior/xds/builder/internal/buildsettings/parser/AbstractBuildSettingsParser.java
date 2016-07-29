package com.excelsior.xds.builder.internal.buildsettings.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.compiler.driver.CompileDriver.CompilationMode;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;
import com.google.common.collect.ArrayListMultimap;

/**
 * Calls settings parser (setting is either option or equation), may call xn.exe to resolve conflicts related to option truncation.
 * 
 * @param <T> - type of the option value
 */
public abstract class AbstractBuildSettingsParser<T> {
	private final Sdk sdk;
	private final File prjFile;
	private final File workDir;
	private final int truncationNameLength;
	private final ISettingsParser<T> settingsParser;
	private final CompilationMode compilationMode;
	
	private final String[] longXcOptionNames = new String[]{"CODENAMEPREFIXED", "PRESERVENAMECASE","SUPPRESS_UNDEFINED_FLOAT"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private Set<String> truncatedLongXcOptionNames; // used as 'last resort', when recovery using xn fails.
	
	protected AbstractBuildSettingsParser(Sdk sdk, File prjFile, File workDir, int truncationNameLength,
			ISettingsParser<T> settingsParser, CompilationMode compilationMode) {
		this.sdk = sdk;
		this.prjFile = prjFile;
		this.workDir = workDir;
		this.truncationNameLength = truncationNameLength;
		this.settingsParser = settingsParser;
		this.compilationMode = compilationMode;
		this.truncatedLongXcOptionNames = new HashSet<String>();
		for(String optionName : longXcOptionNames) {
			truncatedLongXcOptionNames.add(truncateName(optionName));
		}
	}

	public void parse(String settings) throws IOException {
		class XcSettings implements ISettingsParserListener<T>{
    		Map<String, List<T>> truncatedSettingName2Values = new HashMap<String, List<T>>(); // truncatedOptionName -> optionValues mapping
    		ArrayListMultimap<String, String> truncatedSettingName2Names = ArrayListMultimap.create(); // commonSettingPrefix -> settingNames mapping
    		
			@Override
			public void settingParsed(String settingName, T value) {
				String truncatedSettingName = truncateName(settingName);
				if (truncatedSettingName.equals(settingName)) {
					AbstractBuildSettingsParser.this.settingParsed(settingName, value);
				}
				else{
					List<T> values = truncatedSettingName2Values.get(truncatedSettingName);
					if (values == null) {
						values = new ArrayList<T>();
						truncatedSettingName2Values.put(truncatedSettingName, values);
					}
					values.add(value);
					truncatedSettingName2Names.put(truncatedSettingName, settingName);
				}
			}
    	}
    	
    	XcSettings xcSettings = new XcSettings(); // settings parsed from xc.exe
    	settingsParser.addListener(xcSettings);
    	settingsParser.parse(settings);
    	settingsParser.removeListener(xcSettings);
    	
    	if (xcSettings.truncatedSettingName2Values.size() > 0) {
    		class XnSettings implements ISettingsParserListener<T>{
        		Map<String, T> settingName2Value = new HashMap<String, T>(); // settingName -> settingValue mapping
        		ArrayListMultimap<String, String> truncatedSettingName2Names = ArrayListMultimap.create(); // commonSettingPrefix -> settingNames mapping
        		
    			@Override
    			public void settingParsed(String settingName, T value) {
    				String truncatedSettingName = truncateName(settingName);
    				if (!truncatedSettingName.equals(settingName)) {
    					settingName2Value.put(settingName, value);
    					truncatedSettingName2Names.put(truncatedSettingName, settingName);
    				}
    			}
        	}
    		
            List<String> args = Collections.emptyList();
			CompileDriver compileDriver = new CompileDriver(sdk);
            String xnOutput = compileDriver.getCompilerSimulatorOutput(
            		compilationMode, prjFile, args, workDir
            );
            if (xnOutput != null) {
                XnSettings xnSettings = new XnSettings(); // settings parsed from xn.exe
                settingsParser.addListener(xnSettings);
                settingsParser.parse(xnOutput);
                settingsParser.removeListener(xnSettings);
                
                for ( Map.Entry<String, List<T>> keyValue : xcSettings.truncatedSettingName2Values.entrySet()) {
                    String truncateSettingName = keyValue.getKey();
                    List<T> settingValues = keyValue.getValue();
                    List<String> conflictingSettingNames = xnSettings.truncatedSettingName2Names.get(truncateSettingName);
                    if (conflictingSettingNames != null) {
                    	if (settingValues.size() > 1) {
                    		for (String conflictingSettingName : conflictingSettingNames) {
                    			AbstractBuildSettingsParser.this.settingParsed(conflictingSettingName, xnSettings.settingName2Value.get(conflictingSettingName));
                    		}
                    	}
                    	else { // keyValue.getValue().size() == 1
                    		if (conflictingSettingNames.size() == 1) {
                    			String fullSettingName = conflictingSettingNames.get(0);
                    			AbstractBuildSettingsParser.this.settingParsed(fullSettingName, settingValues.get(0));
                    		}
                    	}
                    }
                    else{
                    	boolean isResolved = false;
                    	if (truncatedLongXcOptionNames.contains(truncateSettingName)) {
                    		List<String> conflictingSettingNamesOfXc = xcSettings.truncatedSettingName2Names.get(truncateSettingName);
                    		List<T> conflictingSettingValuesOfXc = xcSettings.truncatedSettingName2Values.get(truncateSettingName);
                    		if (CollectionsUtils.size(conflictingSettingNamesOfXc) == 1 && CollectionsUtils.size(conflictingSettingValuesOfXc) == 1) {
                    			isResolved = true;
                    			AbstractBuildSettingsParser.this.settingParsed(conflictingSettingNamesOfXc.get(0), conflictingSettingValuesOfXc.get(0));
                    		}
                    	}
                    	
                    	
                    	if (!isResolved){
                    		// TODO : localize
                    		LogHelper.logError(String.format("Failed to resolve conflicts for the truncated option '%s'. xn doesnot know about this option.", truncateSettingName));
                    	}
                    }
                }
            }
    	}
	}
	
	private String truncateName(String name) {
		if (name.length() > truncationNameLength) {
			name = name.substring(0, truncationNameLength);
		}
		return name;
	}
	
	/**
	 * Setting was parsed, conflicts are resolved.
	 */
	protected abstract void settingParsed(String name, T value);
}