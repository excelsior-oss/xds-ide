package com.excelsior.xds.parser.modula.symbol.binding;

import java.io.File;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.builders.BuildSettingsKey;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.IXdsParserMonitor;
import com.excelsior.xds.parser.modula.XdsParser;
import com.excelsior.xds.parser.modula.XdsSettings;
import com.excelsior.xds.parser.modula.XdsSourceType;

public final class DefaultImportResolver extends BaseImportResolver
{
	private final BuildSettings buildSettings;
	
    public DefaultImportResolver(BuildSettings buildSettings, IParserEventListener reporter,
			IXdsParserMonitor monitor) {
    	super(reporter, monitor);
		this.buildSettings = buildSettings;
	}

	@Override
	protected IFileStore lookupModule(String moduleName) {
	   File moduleFile = null;
       if (buildSettings != null) {
           moduleFile = buildSettings.lookup(moduleName);
       }
       return ResourceUtils.toFileStore(moduleFile);
	}

	@Override
	public ParsedModuleKey createModuleKey(IFileStore moduleFile) {
		return new ParsedModuleKey(buildSettings, moduleFile);
	}

	@Override
	protected XdsParser createXdsParser(IFileStore moduleFile, String fileContents,
			XdsSourceType sourceType) {
		XdsParser parser = new XdsParser( moduleFile, fileContents
                , new XdsSettings(buildSettings, sourceType), this, reporter, monitor );
		return parser;
	}
}
