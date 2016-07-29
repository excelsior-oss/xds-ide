package com.excelsior.xds.parser.modula.symbol.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.excelsior.xds.core.utils.time.ModificationStamp;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.internal.modula.symbol.StandardModuleSymbol;
import com.excelsior.xds.parser.modula.XdsLanguage;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;

public class ModulaSymbolCache{
	
	private final boolean ID_DEBUG_PRINT_CACHE_MODIFICATIONS = false;
	
	private final ReadWriteLock instanceLock = new ReentrantReadWriteLock();
    
    private final StandardModuleSymbol modulaSuperModule;
    private final StandardModuleSymbol oberonSuperModule;

    private final StandardModuleSymbol modulaSystemModule;
    private final StandardModuleSymbol oberonSystemModule;

    private final StandardModuleSymbol compilerModule;
    
    private final Map<ParsedModuleKey, IModuleSymbol> modulePath2ModuleSymbol = new HashMap<ParsedModuleKey, IModuleSymbol>(); 
    
    private final List<IModulaSymbolCacheListener> listeners = new CopyOnWriteArrayList<>();
    
    public ModulaSymbolCache() {
    	super();
        modulaSuperModule  = StandardModuleBuilder.buildStandardModuleSymbol(false);
        oberonSuperModule  = StandardModuleBuilder.buildStandardModuleSymbol(true);

        modulaSystemModule = StandardModuleBuilder.buildSystemModuleSymbol(false);
        oberonSystemModule = StandardModuleBuilder.buildSystemModuleSymbol(true); 

        compilerModule = StandardModuleBuilder.buildCompilerModuleSymbol();
    }
    
    public void addListener(IModulaSymbolCacheListener l) {
    	listeners.add(l);
    }
    
    public void removeListener(IModulaSymbolCacheListener l) {
    	listeners.remove(l);
    }
    
    public static StandardModuleSymbol getCompilerModule() {
        return SymbolTableHolder.INSTANCE.compilerModule;
    }

    public static StandardModuleSymbol getModulaSuperModule() {
        return SymbolTableHolder.INSTANCE.modulaSuperModule;
    }

    public static StandardModuleSymbol getModulaSystemModule() {
        return SymbolTableHolder.INSTANCE.modulaSystemModule;
    }

    public static StandardModuleSymbol getOberonSuperModule() {
        return SymbolTableHolder.INSTANCE.oberonSuperModule;
    }

    public static StandardModuleSymbol getOberonSystemModule() {
        return SymbolTableHolder.INSTANCE.oberonSystemModule;
    }

    public static StandardModuleSymbol getSuperModule(XdsLanguage language) {
        if (language == XdsLanguage.Oberon2) {
            return getOberonSuperModule();
        }
        else {
            return getModulaSuperModule();
        }
    }
    
    public static StandardModuleSymbol getSystemModule(XdsLanguage language) {
        if (language == XdsLanguage.Oberon2) {
            return getOberonSystemModule();
        }
        else {
            return getModulaSystemModule();
        }
    }
    
    public static ModulaSymbolCache instance() {
        return SymbolTableHolder.INSTANCE;
    }
    
    public void addModule(IModuleSymbol moduleSymbol) {
    	Lock writeLock = instanceLock.writeLock();
    	IModuleSymbol oldSymbol = null;
    	try{
    		writeLock.lock();
    		oldSymbol = modulePath2ModuleSymbol.put(moduleSymbol.getKey(), moduleSymbol);
    		if (ID_DEBUG_PRINT_CACHE_MODIFICATIONS) System.out.println(String.format("Added %s %s", moduleSymbol.getKey().moduleFile, new ModificationStamp()));
    	}
    	finally{
    		writeLock.unlock();
    	}
    	notifyListenersSymbolAdded(oldSymbol, moduleSymbol);
    }
    
    public void addModule(ModulaAst ast) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		
    		if ((ast != null) && (ast.getSourceFile() != null) && ast.getModuleSymbol() != null) {
                addModule(ast.getModuleSymbol());
            }
    	}
    	finally{
    		writeLock.unlock();
    	}
    }
    
    public boolean removeModule(ParsedModuleKey moduleKey) {
    	Lock writeLock = instanceLock.writeLock();
    	IModuleSymbol result = null;
    	try{
    		writeLock.lock();
    		result = modulePath2ModuleSymbol.remove(moduleKey);
    		if (ID_DEBUG_PRINT_CACHE_MODIFICATIONS)  {
    			if (result != null) {
    				System.out.println(String.format("Removed %s %s", result.getSourceFile(), new ModificationStamp()));
    			}
    			else{
    				System.out.println(String.format("Nothing to remove for the %s %s", moduleKey.moduleFile, new ModificationStamp())); 
    			}
    		}
    	}
    	finally {
    		writeLock.unlock();
    	}
    	notifyListenersSymbolRemoved(result);
    	return result != null;
    }
    
    public boolean removeModule(IModuleSymbol modulaSymbol) {
    	return removeModule(modulaSymbol.getKey());
    }

    public IModuleSymbol getModuleSymbol(ParsedModuleKey moduleKey) {
        if (moduleKey == null) {
            return null;
        }
        else {
        	Lock readLock = instanceLock.readLock();
        	try{
        		readLock.lock();
        		return modulePath2ModuleSymbol.get(moduleKey);
        	}
        	finally{
        		readLock.unlock();
        	}
        }
    }
    
    public Iterable<IModuleSymbol> moduleIterator() {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
    		return new Iterable<IModuleSymbol>() {
    			@Override
    			public Iterator<IModuleSymbol> iterator() {
    				return new ArrayList<IModuleSymbol>(modulePath2ModuleSymbol.values()).iterator();
    			}
    		};
    	}
    	finally{
    		readLock.unlock();
    	}
    }

    /**
     * Removes all of the symbols from this symbol table.
     * The symbol table will be empty after this call returns.
     */
    public void clear() {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		modulePath2ModuleSymbol.clear();
    	}
    	finally{
    		writeLock.unlock();
    	}
    }
    
    public void debugDump() {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
			Iterable<IModuleSymbol> moduleIterator = ModulaSymbolCache.instance().moduleIterator();
			System.out.println("-=-=-=-=-=- BEGIN ModulaSymbolCache -=-=-=-=-=-");
			for (IModuleSymbol moduleSymbol : moduleIterator) {
				System.out.println(moduleSymbol.getQualifiedName());
			}
			System.out.println("-=-=-=-=-=- END ModulaSymbolCache -=-=-=-=-=-");
		}
		finally{
			readLock.unlock();
		}
    }
    
    private void notifyListenersSymbolAdded(IModuleSymbol oldSymbol, IModuleSymbol newSymbol) {
    	for (IModulaSymbolCacheListener l : listeners) {
			l.moduleSymbolAdded(oldSymbol, newSymbol);
		}
    }
    
    private void notifyListenersSymbolRemoved(IModuleSymbol symbol) {
    	for (IModulaSymbolCacheListener l : listeners) {
			l.moduleSymbolRemoved(symbol);
		}
    }
    
    private static class SymbolTableHolder{
        static ModulaSymbolCache INSTANCE = new ModulaSymbolCache();
    }
}
