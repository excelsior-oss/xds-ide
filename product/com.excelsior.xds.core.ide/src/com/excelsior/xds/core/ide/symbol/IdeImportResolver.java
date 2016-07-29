package com.excelsior.xds.core.ide.symbol;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.IXdsParserMonitor;
import com.excelsior.xds.parser.modula.XdsParser;
import com.excelsior.xds.parser.modula.XdsSettings;
import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.parser.modula.symbol.binding.BaseImportResolver;

public class IdeImportResolver extends BaseImportResolver {
	private final BuildSettings buildSettings;

	public IdeImportResolver(BuildSettings buildSettings, IParserEventListener reporter, IXdsParserMonitor monitor) {
		super(reporter, monitor);
		this.buildSettings = buildSettings;
	}

	@Override
	protected IFileStore lookupModule(String moduleName) {
		File moduleFile = null;
		if (buildSettings != null) {
			moduleFile = buildSettings.lookup(moduleName);
		}
		if (moduleFile == null || XdsFileUtils.isSymbolFile(moduleFile.getName())) {
			// TODO : Release activities : BEGIN turn-off lookup in the SDK definitions   
			File tempModuleFile = lookupModuleInSdkDefinitions(buildSettings.getSdk(), moduleName);
			if (tempModuleFile != null) {
				moduleFile = tempModuleFile;
			}
			// TODO : Release activities : END turn-off lookup in the SDK definitions
		}
		return ResourceUtils.toFileStore(moduleFile);
	}

	@Override
	public ParsedModuleKey createModuleKey(IFileStore sourceFile) {
		return new ParsedModuleKey(buildSettings, sourceFile);
	}

	@Override
	protected XdsParser createXdsParser(IFileStore moduleFile, String fileContents,
			XdsSourceType sourceType) {
		XdsParser parser = new XdsParser( moduleFile, fileContents
                , new XdsSettings(buildSettings, sourceType), this, reporter, monitor );
		return parser;
	}

	private File lookupModuleInSdkDefinitions(Sdk sdk, String moduleName) {
		if (sdk == null || sdk.getLibraryDefinitionsPath() == null || !new File(sdk.getLibraryDefinitionsPath()).exists()) {
			return null;
		}
		
		File moduleFile = null;
		Iterator<File> dirIterator = ResourceUtils.listDirectories(new File(sdk.getLibraryDefinitionsPath())).iterator();
outer:
		while(dirIterator.hasNext()){
			File dir = dirIterator.next();
			String[] extensions = new String[] {XdsFileUtils.MODULA_DEFINITION_MODULE_FILE_EXTENSION, XdsFileUtils.OBERON_DEFINITION_MODULE_FILE_EXTENSION};
			for (String ext : extensions) {
				File tempModuleFile = new File(FilenameUtils.concat(dir.getAbsolutePath(), moduleName + "." + ext)); //$NON-NLS-1$
				if (tempModuleFile.exists()) {
					moduleFile = tempModuleFile;
					break outer;
				}
			}
		}
		return moduleFile;
	}
}
