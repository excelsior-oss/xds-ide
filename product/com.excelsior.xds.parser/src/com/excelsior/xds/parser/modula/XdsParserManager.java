package com.excelsior.xds.parser.modula;

import java.util.Objects;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.collections.Pair;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.NullParseEventReporter;
import com.excelsior.xds.parser.commons.TodoTaskParser.TaskEntry;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.internal.modula.ModulaAstCache;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.parser.modula.symbol.binding.DefaultImportResolver;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.excelsior.xds.parser.modula.utils.ModulaFileUtils;

/**
 * This class manages invocation of Modula-2 parser.  
 */
public final class XdsParserManager
{
	private XdsParserManager() {
	}
	
    /**
     * Invokes 'to-do' tasks parser on the given Modula-2 source file.
     * @throws CoreException 
     */
    public static TaskEntry[] parseTodoTaks( IFileStore sourceFile, CharSequence sourceText
                                           , BuildSettings buildSettings 
                                           , IParserEventListener reporter ) 
    {
        XdsSourceType sourceType = ModulaFileUtils.getSourceType(sourceFile.getName());
        XdsCommentParser commentParser = new XdsCommentParser(
            sourceFile, sourceText, new XdsSettings(buildSettings, sourceType), reporter  
        );
        return commentParser.parseTodoTaks();
    }
    
    /**
     * Returns module name without full parsing of the module.
     */
    public static String getModuleName( XdsSourceType sourceType
                                      , CharSequence sourceText
                                      , BuildSettings buildSettings) 
    {
        IParserEventListener reporter = NullParseEventReporter.getInstance();
        IImportResolver importResolver = new DefaultImportResolver(buildSettings, reporter, null);
        XdsParser parser = new XdsParser( null, sourceText
                                        , new XdsSettings(buildSettings, sourceType), importResolver
                                        , reporter );
        return parser.getModuleName();
    }
    
    /**
     * Invokes Modula-2 parser on the given source file and updates XDS model.
     * @throws CoreException 
     */
    public static ModulaAst parseModule( IFile sourceIFile, IImportResolver importResolver, BuildSettings buildSettings, IParserEventListener reporter ) throws CoreException
    {
    	IFileStore fileStore = ResourceUtils.toFileStore(sourceIFile);
		return parseModule(fileStore, ResourceUtils.toString(fileStore), importResolver, buildSettings, reporter);
    }
    
    /**
     * Invokes Modula-2 parser on the given source file and updates XDS model.
     * @throws CoreException 
     */
    public static ModulaAst parseModule( IFileStore sourceFile, CharSequence sourceText, IImportResolver importResolver
                                       , BuildSettings buildSettings 
                                       , IParserEventListener reporter ) throws CoreException
    {
        return parseModule(sourceFile, sourceText, importResolver, buildSettings, reporter, false);
    }

    public static ModulaAst parseModule( IFileStore sourceFile, CharSequence sourceText, IImportResolver importResolver
                                       , BuildSettings buildSettings 
                                       , IParserEventListener reporter
                                       , boolean isCacheModulaAst ) throws CoreException 
    {
        XdsSourceType sourceType = ModulaFileUtils.getSourceType(sourceFile.getName());
        ParserMonitor parserMonitor = null;
        if (isCacheModulaAst) {
            parserMonitor = new ParserMonitor(sourceFile);
        }
        
        XdsParser parser = new XdsParser( sourceFile, sourceText
                                        , new XdsSettings(buildSettings, sourceType), importResolver
                                        , reporter, parserMonitor );
        return parser.parseModule();
    }
    
    /**
     * A monitor of completion of a Modula-2 source files parsing. 
     */
    private static class ParserMonitor implements IXdsParserMonitor 
    {
        private final IFileStore sourceFile;

        public ParserMonitor(IFileStore sourceFile) {
            this.sourceFile = sourceFile;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void endModuleParsing( IFileStore sourceFile
                                    , ModulaAst modulaAst )
        {
            boolean isCacheModulaAst = (this.sourceFile != null) 
                                     && Objects.equals(this.sourceFile, sourceFile);
            if (isCacheModulaAst) {
                putModulaAst(modulaAst);
            }
        }
    }
    
    public static IModuleSymbol getModulaSymbol(ParsedModuleKey moduleKey) {
        return ModulaSymbolCache.instance().getModuleSymbol(moduleKey);
    }
    
    public static ModulaAst getModulaAst(ParsedModuleKey moduleKey) {
    	return ModulaAstCache.getModulaAst(moduleKey);
    }

    public static void putModulaAst(ModulaAst modulaAst) {
        ModulaAstCache.putModulaAst(modulaAst);
    }
    
    public static void discardModulaAst(ModulaAst modulaAst) {
    	if (modulaAst != null) {
    		ModulaAstCache.discardModulaAst(modulaAst.getParsedModuleKey());
    	}
    }
    
    public static void discardModulaAst(ParsedModuleKey key) {
    	if (key != null) {
    		ModulaAstCache.discardModulaAst(key);
    	}
    }
    
    public static Pair<IModuleSymbol, ModulaAst> getParsedModule(ParsedModuleKey key, boolean isNeedAst){
    	ModulaAst modulaAst = null;
    	if (isNeedAst) {
    		modulaAst = ModulaAstCache.getModulaAst(key);
    	}
    	return Pair.create(ModulaSymbolCache.instance().getModuleSymbol(key), modulaAst); 
    }

    public static void clearModulaAstCache() {
    	ModulaAstCache.clear();
    }
    
    /**
     * Method for test purposes only, do not use
     */
    @Deprecated
    public static Iterable<ModulaAst> astIterator() {
    	return ModulaAstCache.astIterator();
    }
    
    public static void turnOnDebugPrint(boolean isOn) {
    	XdsParser.IS_DEBUG_PRINT = isOn;
    }
}
