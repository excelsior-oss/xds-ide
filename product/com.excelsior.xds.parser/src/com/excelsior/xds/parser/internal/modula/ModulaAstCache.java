package com.excelsior.xds.parser.internal.modula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.ast.ModulaAst;

public final class ModulaAstCache 
{
	private final ReadWriteLock instanceLock = new ReentrantReadWriteLock();
    private final Map<ParsedModuleKey, ModulaAst> storage = new HashMap<ParsedModuleKey, ModulaAst>();
    
    public static ModulaAst getModulaAst(ParsedModuleKey key) {
        return getInstance().doGetModulaAst(key);
    }

    public static void putModulaAst(ModulaAst modulaAst) {
        getInstance().doPutModulaAst(modulaAst);
    }
    
    public static void clear() {
    	getInstance().doClear();
    }
    
    /**
     * Method for test purposes only, do not use
     */
    @Deprecated
    public static Iterable<ModulaAst> astIterator() {
    	return getInstance().createAstIterator();
    }
    
    private Iterable<ModulaAst> createAstIterator() {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
    		return new Iterable<ModulaAst>() {
    			@Override
    			public Iterator<ModulaAst> iterator() {
    				return new ArrayList<ModulaAst>(storage.values()).iterator();
    			}
    		};
    	}
    	finally{
    		readLock.unlock();
    	}
    }

    public static void discardModulaAst(ModulaAst modulaAst) {
    	getInstance().doRemoveModulaAst(modulaAst.getParsedModuleKey());
    }
    
    public static void discardModulaAst(ParsedModuleKey key) {
    	getInstance().doRemoveModulaAst(key);
    }

    private ModulaAst doGetModulaAst(ParsedModuleKey key) {
    	Lock readLock = instanceLock.readLock();
    	try{
    		readLock.lock();
    		return storage.get(key);
    	}
    	finally{
    		readLock.unlock();
    	}
    }
    
    private void doPutModulaAst( ModulaAst modulaAst) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		ParsedModuleKey parsedModuleKey = modulaAst.getParsedModuleKey();
    		storage.put(parsedModuleKey, modulaAst);
    	}
    	finally{
    		writeLock.unlock();
    	}
    }
    
    private void doRemoveModulaAst(ParsedModuleKey key) {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		storage.remove(key);
    	}
    	finally{
    		writeLock.unlock();
    	}
    }
    
    private void doClear() {
    	Lock writeLock = instanceLock.writeLock();
    	try{
    		writeLock.lock();
    		storage.clear();
    	}
    	finally{
    		writeLock.unlock();
    	}
    }
    private static class ModulaAstCacheHolder{
        static ModulaAstCache INSTANCE = new ModulaAstCache();
    }
    
    public static ModulaAstCache getInstance(){
        return ModulaAstCacheHolder.INSTANCE;
    }
}
