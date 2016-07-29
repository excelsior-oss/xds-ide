package com.excelsior.xds.core.ide.symbol;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager.INotifier;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.MarkerManager;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.commons.IParserEventListener;
import com.excelsior.xds.parser.commons.MarkerCollectorParserEventListener;
import com.excelsior.xds.parser.commons.ParserEventListenerAdapter;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.editor.model.EditorDocumentCache;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.symbol.binding.IImportResolver;
import com.excelsior.xds.parser.modula.symbol.binding.ModulaSymbolCache;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

class ParseRequest extends AbstractRequest {
	private IParserEventListener reporter;
	private final IParseTask parseTask;
	
	public ParseRequest(IParseTask parseTask, INotifier notifier) {
		super(notifier);
		this.parseTask = parseTask;
	}
	
	public IParseTask getParseTask() {
		return parseTask;
	}
	
	@Override
	public Iterable<ModificationStatus> apply() {
		if (parseTask.project() != null && !parseTask.project().isOpen()) {
			return Arrays.asList();
		}
		return Iterables.transform(parseTask.files(), new Function<IFileStore, ModificationStatus>() {
			@Override
			public ModificationStatus apply(IFileStore sourceFile) {
				initializeParserEventListener(parseTask);
				
//				if (SymbolModelManager.IS_DEBUG_MODEL_MODIFICATIONS) {
//					System.out.println("dequed " + sourceFile + " " + super.toString() + " " + parseTask + " ");
//				}
				BuildSettings buildSettings = parseTask.buildSettings();
				if (buildSettings == null) {
					return createErrorStatus(null);
				}
				final ParsedModuleKey key = new ParsedModuleKey(parseTask.buildSettings(), sourceFile);
				if (!sourceFile.fetchInfo().exists()) {
					return createErrorStatus(key);
				}
				
				ModificationStatus modificationStatus = new ModificationStatus(ModificationType.PARSED, parseTask.project(), key);
				try {
					if (!parseTask.isForce()) {
						boolean isParsed = ModulaSymbolCache.instance().getModuleSymbol(key) != null;
						if (isParsed && parseTask.isNeedModulaAst()) {
							isParsed = XdsParserManager.getModulaAst(key) != null;
						}
						if (isParsed) {
							return modificationStatus;
						}
					}
					
					IDocument doc = EditorDocumentCache.instance().getDocument(key);
					
					String sourceText = doc != null? doc.get() : readSourceFileText(sourceFile); 
					IImportResolver importResolver = new IdeImportResolver(parseTask.buildSettings(), reporter, null);
					XdsParserManager.parseModule(sourceFile, sourceText, importResolver, buildSettings, reporter, parseTask.isNeedModulaAst());
					return modificationStatus;
				} catch (IOException | CoreException e) {
					LogHelper.logError(e);
					return createErrorStatus(key);
				}
			}
		});
	}
	
	protected String readSourceFileText(IFileStore sourceFile)
			throws IOException, CoreException {
		String sourceText = null;
		if (!XdsFileUtils.isSymbolFile(sourceFile.fetchInfo().getName())) {
			sourceText = ResourceUtils.toString(sourceFile);
		}
		else {
			// TODO : for now, sym file can only have file form.
			String absolutePath = ResourceUtils.getAbsolutePath(sourceFile);
			if (absolutePath != null) {
				sourceText = CompileDriver.decodeSymFile(absolutePath);
			}
		}
		
		return sourceText;
	}

	protected ModificationStatus createErrorStatus(ParsedModuleKey key) {
		return new ModificationStatus(ModificationType.ERROR, parseTask.project(), key);
	}
	
	@Override
	public void completed() {
		if (reporter instanceof MarkerCollectorParserEventListener) {
			MarkerCollectorParserEventListener markerCollector = (MarkerCollectorParserEventListener)reporter;
			MarkerManager.commitParserMarkers(markerCollector.getFileToMarkerInfo(), new NullProgressMonitor());
		}
	}
	
	protected void notifyParsed(IFileStore file) {
		ParsedModuleKey key = new ParsedModuleKey(parseTask.buildSettings(), file);
		notifier.notifyParsed(key, true);
	}
	
	private void initializeParserEventListener(IParseTask parseTask) {
		if (reporter == null) {
			if (parseTask.isReportParseErrors() && parseTask.project() != null) {
				reporter = new MarkerCollectorListener(parseTask.project());
			}
			else{
				reporter = new ParserListener();
			}
		}
	}

	private class MarkerCollectorListener extends MarkerCollectorParserEventListener{
		public MarkerCollectorListener(IProject iproject) {
			super(iproject);
		}

		@Override
		public void endFileParsing(IFileStore file) {
			super.endFileParsing(file);
			notifyParsed(file);
		}
	}
	
	private class ParserListener extends ParserEventListenerAdapter {
		public ParserListener() {
		}
		
		@Override
		public void endFileParsing(IFileStore file) {
			notifyParsed(file);
		}
	}
}
