package com.excelsior.xds.parser.modula.symbol.binding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.editor.model.EditorDocumentCache;
import com.excelsior.xds.parser.modula.IXdsParserMonitor;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.XdsParser;
import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.parser.modula.XdsStandardNames;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.utils.ModulaFileUtils;

public abstract class BaseImportResolver implements IImportResolver {
	protected abstract IFileStore lookupModule(String moduleName);
	protected abstract XdsParser createXdsParser(IFileStore moduleFile, String fileContents, XdsSourceType sourceType);
	
	protected final IParserEventListener reporter;
	protected final IXdsParserMonitor monitor;
	
	private final Set<IFileStore> resolvingModules = new HashSet<IFileStore>();
	
	protected BaseImportResolver(IParserEventListener reporter,
			IXdsParserMonitor monitor) {
		this.reporter = reporter;
		this.monitor = monitor;
	}
	
	@Override
	public IModuleSymbol resolveModuleSymbol( XdsLanguage language, String moduleName, IFileStore hostFile) 
    {
        IModuleSymbol moduleSymbol = null;
        if (XdsStandardNames.SYSTEM.equals(moduleName)) {         
            moduleSymbol = ModulaSymbolCache.getSystemModule(language);
        }
        else if (XdsStandardNames.COMPILER.equals(moduleName)) {  
            moduleSymbol = ModulaSymbolCache.getCompilerModule();
        }
        else {
            IFileStore moduleFile = lookupModule(moduleName);
            if (moduleFile == null && hostFile != null) {
                String fileExtension = (XdsLanguage.Oberon2 == language)
                                     ? XdsFileUtils.OBERON_MODULE_FILE_EXTENSION
                                     : XdsFileUtils.MODULA_DEFINITION_MODULE_FILE_EXTENSION;
                String moduleFileName = moduleName + "." + fileExtension;
                moduleFile = ResourceUtils.getSibling(hostFile, moduleFileName);
            }
            
            if (moduleFile == null) {
                return null;
            }
            
            moduleSymbol = ModulaSymbolCache.instance().getModuleSymbol(createModuleKey(moduleFile));
          
            if (moduleSymbol == null) {
            	if (resolvingModules.contains(moduleFile)){
            		String cyclyMessage = String.format("Module %s participate in import cycle : %s", moduleFile.getName(), moduleNames());
                	reporter.error(hostFile, null, new TextPosition(0, 0, 0), 1, cyclyMessage);
                	return null;
            	}
                else {
                	resolvingModules.add(moduleFile);
                }
            	
                String fileContents = null;
                XdsSourceType sourceType;
                
                try {
                	if (XdsFileUtils.isSymbolFile(moduleFile.getName())) {
                        sourceType = XdsSourceType.OdfFile;
                        fileContents = CompileDriver.decodeSymFile(ResourceUtils.getAbsolutePath(moduleFile));
                    } 
                    else if (XdsFileUtils.isDefinitionModuleOrOberonModuleFile(moduleFile.getName()))
                    {
                        sourceType = ModulaFileUtils.getSourceType(moduleFile.getName());
                        if (moduleFile.fetchInfo().exists()) {
    						IDocument doc = EditorDocumentCache.instance().getDocument(createModuleKey(moduleFile));
    					    fileContents = doc != null? doc.get() : ResourceUtils.toString(moduleFile);
    					}
                    }
                    else {
                        return null;
                    }
                    
                    if (fileContents != null) {
                        XdsParser parser = createXdsParser( moduleFile, fileContents, sourceType);
                        moduleSymbol = parser.parseModule().getModuleSymbol();
                    }
				} catch (CoreException e) {
					LogHelper.logError(e);
				}
            }
        }
       
        return moduleSymbol;
    }

	private List<String> moduleNames() {
		return resolvingModules.stream().map(m -> m.getName()).collect(Collectors.toList());
	}
}
