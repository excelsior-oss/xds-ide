package com.excelsior.xds.ui.editor.modula.compare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IResourceProvider;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.IMergeViewerTestAdapter;
import org.eclipse.compare.internal.MergeViewerContentProvider;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.exceptions.ExceptionHelper;
import com.excelsior.xds.core.ide.symbol.ParseTask;
import com.excelsior.xds.core.ide.symbol.SymbolModelListenerAdapter;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.editor.modula.IModulaPartitions;
import com.excelsior.xds.ui.editor.modula.ModulaDocumentSetupParticipant;
import com.excelsior.xds.ui.editor.modula.ModulaEditor;
import com.excelsior.xds.ui.editor.modula.ModulaSourceViewerConfiguration;
import com.excelsior.xds.ui.editor.modula.commons.InactiveCodeRefresher;
import com.excelsior.xds.ui.editor.modula.commons.InactiveCodeRefresher.IInactiveCodeRefresherListener;
import com.excelsior.xds.ui.editor.modula.commons.InactiveCodeRefresher.ITextPresentation;
import com.excelsior.xds.ui.editor.modula.commons.ModulaEditorCommons;
import com.google.common.io.ByteStreams;

@SuppressWarnings("restriction")
public class ModulaMergeViewer extends TextMergeViewer {
	private final TokenManager tokenManager = new TokenManager();
	private IPreferenceChangeListener corePluginPreferenceListener;

	public ModulaMergeViewer(Composite parent,
			CompareConfiguration configuration) {
		super(parent, configuration);
	}
	
	@Override
	protected void createControls(Composite composite) {
		super.createControls(composite);
		getControl().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				tokenManager.dispose();
			}
		});
		
		corePluginPreferenceListener = new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent e) {
				if (PreferenceKeys.PKEY_HIGHLIGHT_INACTIVE_CODE.isChanged(e)) {
					refreshViewers(getInput());
				}
			}
		};
		PreferenceKeys.addChangeListener(corePluginPreferenceListener);
	}
	
	@Override
	public void setInput(Object input) {
		super.setInput(input);
		refreshViewers(input);
	}

	private void refreshViewers(Object input) {
		if (input instanceof ICompareInput) {
			ICompareInput compareInput = (ICompareInput) input;
			BuildSettings buildSettings = getBuildSettings( compareInput.getLeft());
			if (buildSettings == null){
				buildSettings = getBuildSettings( compareInput.getRight());
			}
			
			if (buildSettings != null){
				try {
					File leftTempSourceFile = saveAsTempFile(compareInput.getLeft());
					File rightTempSourceFile = saveAsTempFile(compareInput.getRight());
					
					scheduleParse(leftTempSourceFile, buildSettings, MergeViewerContentProvider.LEFT_CONTRIBUTOR);
					scheduleParse(rightTempSourceFile, buildSettings, MergeViewerContentProvider.RIGHT_CONTRIBUTOR);
				} catch (CoreException e) {
					LogHelper.logError(e);
				}
			}
		}
	}
	
	private void scheduleParse(File file, BuildSettings buildSettings,
			char contributorType) {
		ParseTask task = new ParseTask(file);
		task.setNeedModulaAst(true);
		task.setBuildSettings(buildSettings);
		SymbolModelManager.instance().scheduleParse(
				task,
				new SymbolModelListenerAdapter(new ParsedModuleKey(
						buildSettings, ResourceUtils.toFileStore(file))) {
					@Override
					public void parsed(ParsedModuleKey key,
							IModuleSymbol moduleSymbol, ModulaAst ast) {
						IMergeViewerTestAdapter adapter = (IMergeViewerTestAdapter) getAdapter(IMergeViewerTestAdapter.class);
						IDocument doc = adapter.getDocument(contributorType);
						ModulaEditorCommons.configureModulaFastPartitioner(doc);
						
						InactiveCodeRefresher inactiveCodeRefresher = new InactiveCodeRefresher(
								ast, new ITextPresentation() {
									@Override
									public boolean isDisposed() {
										return getControl().isDisposed();
									}

									@Override
									public void invalidateTextPresentation() {
										ModulaMergeViewer.this
												.invalidateTextPresentation();
									}
								}, () -> doc,
								new IInactiveCodeRefresherListener() {
									@Override
									public void afterTextPresentationUpdated() {
										XdsParserManager.discardModulaAst(ast);
									}
								});
						inactiveCodeRefresher.refresh();
					}
				});
	}
	
	private File saveAsTempFile(ITypedElement el) throws CoreException {
		if ( el instanceof IEncodedStreamContentAccessor) {
			IEncodedStreamContentAccessor acc = (IEncodedStreamContentAccessor)  el;
			File temp;
			try {
				temp = File.createTempFile("mod", ".mod");
				try(FileOutputStream fos = new FileOutputStream(temp)){
					ByteStreams.copy(acc.getContents(), fos);
				}
				return temp;
			} catch (IOException e) {
				ExceptionHelper.rethrowAsCoreException(e);
			} 
		}
		return null;
	}

	private BuildSettings getBuildSettings(ITypedElement left) {
		if (left instanceof IResourceProvider) {
			IResourceProvider resourceProvider = (IResourceProvider) left;
			IResource resource = resourceProvider.getResource();
			if (resource instanceof IFile) {
				return BuildSettingsCache.createBuildSettings((IFile) resource);
			}
		}
		return null;
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			((SourceViewer)textViewer).configure(getSourceViewerConfiguration());
		}
	}
	
	@Override
	protected String getDocumentPartitioning() {
		return IModulaPartitions.M2_PARTITIONING;
	}
	private SourceViewerConfiguration getSourceViewerConfiguration() {
		return new ModulaSourceViewerConfiguration(tokenManager, null, EditorsPlugin.getDefault().getPreferenceStore(), ModulaEditor.eolCommentPrefix);
	}
	
	@Override
	protected IDocumentPartitioner getDocumentPartitioner() {
		return ModulaDocumentSetupParticipant.createDocumentPartitioner();
	}
}
