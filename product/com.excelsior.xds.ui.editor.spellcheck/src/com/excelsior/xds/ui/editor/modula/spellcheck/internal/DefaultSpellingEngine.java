package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.texteditor.spelling.ISpellingEngine;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingContext;

import com.excelsior.xds.core.XdsCorePlugin;

public class DefaultSpellingEngine implements ISpellingEngine{
	/** Text content type */
	private static final IContentType TEXT_CONTENT_TYPE= Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);
	
	private static final IContentType XDS_SOURCE_CONTENT_TYPE = Platform.getContentTypeManager().getContentType(XdsCorePlugin.CONTENT_TYPE_XDS_SOURCE);
	
	/** Available spelling engines by content type */
	private Map<IContentType, SpellingEngine> fEngines= new HashMap<IContentType, SpellingEngine>();
	
	public DefaultSpellingEngine() {
		if (TEXT_CONTENT_TYPE != null)
			fEngines.put(TEXT_CONTENT_TYPE, new TextSpellingEngine());
		
		if (XDS_SOURCE_CONTENT_TYPE != null) {
			fEngines.put(XDS_SOURCE_CONTENT_TYPE, new ModulaSpellingEngine());
		}
	}

	@Override
	public void check(IDocument document, IRegion[] regions,
			SpellingContext context, ISpellingProblemCollector collector,
			IProgressMonitor monitor) {
		ISpellingEngine engine = getEngine(context.getContentType());
		if (engine == null){
			engine = getEngine(TEXT_CONTENT_TYPE);
		}
		
		if (engine != null){
			engine.check(document, regions, context, collector, monitor);
		}
	}
	
	/**
	 * Returns a spelling engine for the given content type or
	 * <code>null</code> if none could be found.
	 *
	 * @param contentType the content type
	 * @return a spelling engine for the given content type or
	 *         <code>null</code> if none could be found
	 */
	private ISpellingEngine getEngine(IContentType contentType) {
		if (contentType == null)
			return null;

		if (fEngines.containsKey(contentType))
			return fEngines.get(contentType);

		return getEngine(contentType.getBaseType());
	}
}
