package com.excelsior.xds.parser.editor.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.text.IDocument;

import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;

public final class EditorDocumentCache {
	private Map<ParsedModuleKey, IDocument> moduleKeyToDocument = new ConcurrentHashMap<ParsedModuleKey, IDocument>();
	
	private EditorDocumentCache() {
	}
	
	public void addDocument(ParsedModuleKey key, IDocument doc) {
		if (doc != null) {
			moduleKeyToDocument.put(key, doc);
		}
	}
	
	public IDocument getDocument(ParsedModuleKey key) {
		return moduleKeyToDocument.get(key);
	}
	
	public void removeDocument(ParsedModuleKey key) {
		moduleKeyToDocument.remove(key);
	}
	
	/**
	 * Method for debug purposes. Do not use!
	 */
	@Deprecated
	public Iterable<Map.Entry<ParsedModuleKey, IDocument>> iterable() {
		return moduleKeyToDocument.entrySet();
	}
	
	public void clear() {
		moduleKeyToDocument.clear();
	}
	
	public static EditorDocumentCache instance() {
		return EditorDocumentCacheHolder.INSTANCE;
	}
	
	private static final class EditorDocumentCacheHolder{
		static final EditorDocumentCache INSTANCE = new EditorDocumentCache();
	}
}
