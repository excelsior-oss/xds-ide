package com.excelsior.xds.ui.editor.modula;

import java.util.Objects;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.texteditor.LineNumberColumn;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.rulers.IColumnSupport;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.eclipse.ui.texteditor.rulers.RulerColumnRegistry;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.builder.buildsettings.IBuildSettingsCacheListener;
import com.excelsior.xds.core.compiler.compset.CompilationSetManager;
import com.excelsior.xds.core.ide.symbol.ParseTask;
import com.excelsior.xds.core.ide.symbol.ParseTaskFactory;
import com.excelsior.xds.core.ide.symbol.SymbolModelListenerAdapter;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.XdsMarkerConstants;
import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.core.utils.launch.LaunchConfigurationUtils;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.parser.modula.symbol.IModuleSymbol;
import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.commons.utils.UiUtils;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;
import com.excelsior.xds.ui.editor.commons.annotations.MarkerAnnotationPaintHandler;
import com.excelsior.xds.ui.editor.commons.debug.DebugCommons;
import com.excelsior.xds.ui.editor.commons.text.PairedBracketsMatcher;
import com.excelsior.xds.ui.editor.commons.text.PairedBracketsPainter;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.editor.internal.preferences.EditorPreferencePage;
import com.excelsior.xds.ui.editor.internal.preferences.IXdsEditorsPreferenceIds;
import com.excelsior.xds.ui.editor.internal.preferences.formatter.FormatterProfile;
import com.excelsior.xds.ui.editor.modula.actions.DeclarationsSearchGroup;
import com.excelsior.xds.ui.editor.modula.actions.OpenDeclarationsAction;
import com.excelsior.xds.ui.editor.modula.actions.OpenViewActionGroup;
import com.excelsior.xds.ui.editor.modula.actions.ReferencesSearchGroup;
import com.excelsior.xds.ui.editor.modula.commons.InactiveCodeRefresher;
import com.excelsior.xds.ui.editor.modula.commons.InactiveCodeRefresher.ITextPresentation;
import com.excelsior.xds.ui.editor.modula.commons.ModulaEditorCommons;
import com.excelsior.xds.ui.editor.modula.outline.ModulaOutlinePage;
import com.excelsior.xds.ui.editor.modula.text.ModulaPairedBracketsMatcher;
import com.excelsior.xds.ui.editor.modula.utils.ModulaAstUtils;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;

/**
 * Modula-2 source code editor.
 */
@SuppressWarnings("restriction")
public class ModulaEditor extends  SourceCodeTextEditor
{
	public static final String CONTEXT_ID = "com.excelsior.xds.ui.modulaEditorScope";   //$NON-NLS-1$
    
    /** The end of line comment prefix. */
    public static final String eolCommentPrefix = "--"; //$NON-NLS-1$
    
    private ModulaOutlinePage outlinePage;
	private ActionGroup openInViewActionGroup;
	private ActionGroup declarationsSearchGroup;
	private ActionGroup referencesSearchGroup;
	private ModulaOccurrencesMarker occurrencesMarker;
	private IDocumentListener documentListener;
	
	private final TokenManager tokenManager = new TokenManager(); 
	
	private SymbolModelListener symbolModelListener;
	private IBuildSettingsCacheListener buildSettingsCacheListener = new BuildSettingsCacheListener();

	private IPropertyChangeListener editorPluginPreferenceListener;
	private IPreferenceChangeListener corePluginPreferenceListener;

	private IDebugEventSetListener debugEventSetListener;

    public ModulaEditor() {
        super();
        documentListener = new ModulaDocumentListener();
        setDocumentProvider(createDocumentProvider());
        configuration = new ModulaSourceViewerConfiguration(tokenManager, this 
                                                           , getPreferenceStore()
                                                           , eolCommentPrefix );
        setSourceViewerConfiguration(configuration);
        
        final IPreferenceStore store = XdsEditorsPlugin.getDefault().getPreferenceStore();
        editorPluginPreferenceListener = new IPropertyChangeListener() {
            // used when settings are imported via "org.eclipse.ui.preferenceTransfer" extension point
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                handleXdsPreferenceStoreChanged(store, event);
            }
        };
		store.addPropertyChangeListener(editorPluginPreferenceListener);
        
		corePluginPreferenceListener = new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent e) {
				if (PreferenceKeys.PKEY_HIGHLIGHT_INACTIVE_CODE.isChanged(e)) {
					ModulaEditorCommons.configureModulaFastPartitioner(getEditorDocument(ModulaEditor.this));
	    			refreshInactiveCodePartitions();
				}
				else if (PreferenceKeys.OVERRIDE_SHOW_LINE_NUMBER_COLUMN.isChanged(e)){
					refreshLineNumberColumn();
				}
			}
		};
		PreferenceKeys.addChangeListener(corePluginPreferenceListener);
		
        occurrencesMarker = new ModulaOccurrencesMarker();
        
        addAnnotationPaintHandler(new NotInCompilationSetMarkerHandler());
        addAnnotationPaintHandler(new GrayBuildMarkerHandler());
        
        setEditorContextMenuId("#ModulaEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#ModulaEditorRulerContext"); //$NON-NLS-1$
		
		/*
		 * refresh isEditable state on debug events (CREATE,TERMINATE) - i.e. when debugging started or terminated.
		 * */
		debugEventSetListener = new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(DebugEvent[] events) {
				for (DebugEvent e : events) {
					if (e.getKind() == DebugEvent.CREATE || e.getKind() == DebugEvent.TERMINATE) {
						if (isSourceCorrespondsToSameDebugSession(e)) {
							refreshLineNumberColumn();
							
							// update 'read only' state of the editor - will call isEditable()
							Display.getDefault().asyncExec(
									() -> validateState(getEditorInput()));
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(debugEventSetListener);
    }
    
    private boolean isSourceCorrespondsToSameDebugSession(DebugEvent e) {
    	if (e.getSource() instanceof IAdaptable) {
    		IAdaptable adaptable = (IAdaptable) e.getSource();
    		ILaunch launch = (ILaunch)adaptable.getAdapter(ILaunch.class);
    		if (launch != null){
    			try {
					IProject iProjectOfLaunch = LaunchConfigurationUtils.getProject(launch.getLaunchConfiguration());
					return Objects.equals(getProject(), iProjectOfLaunch);
				} catch (CoreException err) {
					LogHelper.logError(err);
				}
    		}
		}
    	return false;
    }
    
    private void refreshLineNumberColumn(){
    	Display.getDefault().asyncExec(() ->{
    		IVerticalRuler ruler= getVerticalRuler();
    		// The following sequence mimics what happens during the setInput method.
    		// For now, this is the only known way to get the LineNumberColumn to update its visible status.
    		
    		// called at the end of org.eclipse.ui.texteditor.AbstractTextEditor.createPartControl(Composite)
			if (ruler instanceof CompositeRuler) {
				updateContributedRulerColumns((CompositeRuler) ruler);
			}
			
			// called at the end of AbstractDecoratedTextEditor.doSetInput(IEditorInput) 
			RulerColumnDescriptor lineNumberColumnDescriptor= RulerColumnRegistry.getDefault().getColumnDescriptor(LineNumberColumn.ID);
			if (lineNumberColumnDescriptor != null) {
				IColumnSupport columnSupport= (IColumnSupport)getAdapter(IColumnSupport.class);
				columnSupport.setColumnVisible(lineNumberColumnDescriptor, isLineNumberRulerVisible() || isPrefQuickDiffAlwaysOn());
			}
			
			// force redraw of the ruler`s content.
			IVerticalRuler verticalRuler = getVerticalRuler();
			if (verticalRuler != null) {
				verticalRuler.update();
			}
    	});
    }
    
    @Override
    protected boolean isLineNumberRulerVisible() {
    	return isProjectInDebug() && PreferenceKeys.OVERRIDE_SHOW_LINE_NUMBER_COLUMN.getStoredBoolean()? true : super.isLineNumberRulerVisible();
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException 
    {
    	super.init(site, input);
    }
    
    @Override
    public void createPartControl(Composite parent) {
    	super.createPartControl(parent);
    }
    
    @Override
	public void refreshConfiguration() {
    	tokenManager.clear(); // forget all cached tokens - so resetting styles of the ModulaTokens can succeed.
		super.refreshConfiguration();
	}

	protected void installSymbolModelListener() {
		uninstallSymbolModelListener();
		IEditorInput editorInput = getEditorInput();
		ParsedModuleKey parsedModuleKey = CoreEditorUtils.editorInputToParsedModuleKey(editorInput);
		if (parsedModuleKey != null) {
			IProject project = CoreEditorUtils.getIProjectFrom(editorInput);
			symbolModelListener = new SymbolModelListener(project, parsedModuleKey, true, true);
			SymbolModelManager.instance().addListener(symbolModelListener, true, true);
		}
	}
	
	private void installBuildSettingsCacheListener(){
		BuildSettingsCache.addListener(buildSettingsCacheListener);
	}
	
	private void uninstallBuildSettingsCacheListener(){
		BuildSettingsCache.removeListener(buildSettingsCacheListener);
	}
	

	protected void uninstallSymbolModelListener() {
		if (symbolModelListener != null) {
			SymbolModelManager.instance().removeListener(symbolModelListener);
			symbolModelListener = null;
		}
	}
    
    @Override
	protected void createActions() {
		super.createActions();
		
		openInViewActionGroup = createOpenViewActionGroup();
		declarationsSearchGroup = createDeclarationsSearchGroup();
		referencesSearchGroup = createReferencesSearchGroup();
	}
    
    
	@Override
	public boolean isEditable() {
		return isProjectInDebug()? false : super.isEditable();
	}
	
	/**
	 * @return whether project related to {@link IResource} of the {@link IEditorInput} (if there is such) is being debugged.
	 */
	private boolean isProjectInDebug() {
		return DebugCommons.isProjectInDebug(getProject());
	}

	/**
	 * @return project related to {@link IResource} of the {@link IEditorInput} (if there is such)
	 */
	private IProject getProject() {
		return CoreEditorUtils.getIProjectFrom(getEditorInput());
	}

	@Override
	public void dispose() {
		super.dispose();
		
		uninstallBuildSettingsCacheListener();
		uninstallSymbolModelListener();
		
		if (openInViewActionGroup != null) {
			openInViewActionGroup.dispose();
			openInViewActionGroup = null;
		}
		
		if (declarationsSearchGroup != null) {
			declarationsSearchGroup.dispose();
			declarationsSearchGroup = null;
		}
		
		if (referencesSearchGroup != null) {
			referencesSearchGroup.dispose();
			referencesSearchGroup = null;
		}
		
		UiUtils.dispose(tokenManager);
		
		DebugPlugin.getDefault().removeDebugEventListener(debugEventSetListener);
		PreferenceKeys.removeChangeListener(corePluginPreferenceListener);
		XdsEditorsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(editorPluginPreferenceListener);
	}
	
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		
        addAction(menu, ICommonMenuConstants.GROUP_OPEN, OpenDeclarationsAction.ID); //$NON-NLS-1$
		
		if (openInViewActionGroup != null) {
			openInViewActionGroup.fillContextMenu(menu);
		}
		
		if (declarationsSearchGroup != null) {
			declarationsSearchGroup.fillContextMenu(menu);
		}
		
		if (referencesSearchGroup != null) {
			referencesSearchGroup.fillContextMenu(menu);
		}
	}
	
	

	/**
     * Creates document provider for this editor.
     * 
     * @return the document provider
     */
    protected IDocumentProvider createDocumentProvider() {
        return XdsEditorsPlugin.getDefault().getModulaDocumentProvider();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] {CONTEXT_ID});
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEOLCommentPrefix() {
        return eolCommentPrefix;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected PairedBracketsMatcher getPairedBraceMatcher() {
        return new ModulaPairedBracketsMatcher();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class required)
	{
		if (IContentOutlinePage.class.equals(required))
		{
			if (outlinePage == null)
			{
				outlinePage = createOutlinePage();
				if (getEditorInput() != null)
					outlinePage.setInput(getEditorInput());
			}
			return outlinePage;
		}
		else if (ISourceViewer.class.equals(required)) {
			return getSourceViewer();
		}

		return super.getAdapter(required);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetInput(IEditorInput newInput) throws CoreException
	{
    	IEditorInput oldInput = getEditorInput();
    	if (oldInput != null) { // discard AST for the old input - this occurs when refactoring renames resource
    		ModulaAst modulaAst = ModulaEditorSymbolUtils.getModulaAst(oldInput);
    		XdsParserManager.discardModulaAst(modulaAst);
    	}
    	
		super.doSetInput(newInput);
        setOutlinePageInput(outlinePage, newInput);
        installSymbolModelListener();
        installBuildSettingsCacheListener();
	}
    
    @Override 
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) 
    {
        ISourceViewer viewer = super.createSourceViewer(parent, ruler, styles);
        
        // TODO: BEGIN Replace with AbstractPreferenceInitializer  
        IPreferenceStore xdsStore = XdsEditorsPlugin.getDefault().getPreferenceStore();
        EditorPreferencePage.initEditorPrefsInStoreIfNeed(xdsStore);
        // TODO: END Replace with AbstractPreferenceInitializer  
        
        PairedBracketsMatcher braceMatcher = getPairedBraceMatcher();
        if (braceMatcher != null) {
            PairedBracketsPainter.installToEditor( viewer
                                                 , braceMatcher
                                                 , xdsStore
                                                 , IXdsEditorsPreferenceIds.PREF_HIGHLIGHT_MATCHING_BRACKETS 
                                                 , IXdsEditorsPreferenceIds.PREF_MATCHED_BRACKETS_COLOR
                                                 , IXdsEditorsPreferenceIds.PREF_UNMATCHED_BRACKETS_COLOR );
        }

        occurrencesMarker.setViewer(viewer, this);
        
        viewer.addTextInputListener(new ITextInputListener() {
            @Override
            public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
            }

            @Override
            public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
                if (oldInput != null) {
                    oldInput.removeDocumentListener(documentListener);
                }
                if (newInput != null) {
                    newInput.addDocumentListener(documentListener);
                }
            }
        });
        
        return viewer;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
        String property= event.getProperty();
        if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
            // Ignore common editor tabs size, use our tabs from formatter settings
            return;
        }
        super.handlePreferenceStoreChanged(event); 
    }
    
    protected void handleXdsPreferenceStoreChanged(IPreferenceStore store, PropertyChangeEvent event) {
        String property= event.getProperty();
        if (FormatterProfile.TAB_SIZE_PROPERTY_NAME.equals(property)) {
            int tabSize = store.getInt(FormatterProfile.TAB_SIZE_PROPERTY_NAME);
            StyledText stext = getSourceViewer().getTextWidget();
            if (tabSize != stext.getTabs()) {
                stext.setTabs(tabSize);
                if (isTabsToSpacesConversionEnabled()) {
                    uninstallTabsToSpacesConverter();
                    installTabsToSpacesConverter();
                }
            }
        }
    }

    protected ActionGroup createOpenViewActionGroup() {
		return new OpenViewActionGroup(this);
	}
    
    protected ActionGroup createDeclarationsSearchGroup() {
		return new DeclarationsSearchGroup(this);
	}
    
    protected ActionGroup createReferencesSearchGroup() {
		return new ReferencesSearchGroup(this);
	}

    /**
     * Creates the outline page used with this editor.
     *
     * @return the created Modula-2 outline page
     */
    protected ModulaOutlinePage createOutlinePage() {
        ModulaOutlinePage page = new ModulaOutlinePage(this, getSourceViewer().getTextWidget());
        setOutlinePageInput(page, getEditorInput());
        return page;
    }
    
    /**
     * Sets the input of the editor's outline page.
     *
     * @param page the Modula-2 outline page
     * @param input the editor input
     */
    protected void setOutlinePageInput(ModulaOutlinePage page, IEditorInput input) {
        if (page != null) {
            page.setInput(input);
        }
    }

    protected void reconciled(ParsedModuleKey key, ModulaAst ast) {
        if (outlinePage != null) {
            outlinePage.doRefresh();
        }
        occurrencesMarker.reconciled();
        refreshContentProposals();
        repartitionAndRefresh(ast);
    }

    private void refreshContentProposals() {
    	final IContentAssistant contentAssistant = configuration.getContentAssistant(getSourceViewer());
    	if (contentAssistant instanceof ModulaContentAssistant) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (isDisposed()) {
						return;
					}
					ModulaContentAssistant modulaContentAssistant = (ModulaContentAssistant) contentAssistant;
					if (modulaContentAssistant.isProposalPopupActive()) {
						modulaContentAssistant.showPossibleCompletions(true);
					}
				}
			});
		}
    }
    
    private void refreshInactiveCodePartitions() {
    	ModulaAst ast = ModulaAstUtils.getAstIfUpToDate(this);
    	repartitionAndRefresh(ast);
    }
    
    /**
     * Recomputes partitioning and refreshes editor, in case there are disabled code regions.
     * 
     * @param ast
     */
    private void repartitionAndRefresh(final ModulaAst ast) 
    {
    	InactiveCodeRefresher inactiveCodeRefresher = new InactiveCodeRefresher(ast, new ITextPresentation() {
			@Override
			public boolean isDisposed() {
				return ModulaEditor.this.isDisposed();
			}
			
			@Override
			public void invalidateTextPresentation() {
				ISourceViewer sourceViewer = ModulaEditor.this.getSourceViewer();
				if (sourceViewer != null) {
					sourceViewer.invalidateTextPresentation();
				}
			}
		}, () -> getEditorDocument(this));
    	inactiveCodeRefresher.refresh();
	}
    
    /**
     * Schedules re-parse of the module whose source is being edited
     */
    private void scheduleReparse() {
		ParseTask task = ParseTaskFactory.create(getEditorInput());
		task.setNeedModulaAst(true);
		task.setParseImportSection(false);
		task.setForce(false);
		SymbolModelManager.instance().scheduleParse(task, null);
	}

	private static IDocument getEditorDocument(ITextEditor editor) {
    	IDocument document = null;
    	IDocumentProvider documentProvider = editor.getDocumentProvider();
    	if (documentProvider != null) {
    		document = documentProvider.getDocument(editor.getEditorInput());
    	}
    	return document;
    }
    
    private final class SymbolModelListener extends SymbolModelListenerAdapter {
		public SymbolModelListener(IProject project, ParsedModuleKey targetModuleKey, boolean isReportOnExistingModule, boolean isNeedModulaAst) {
			super(targetModuleKey);
		}
		
		@Override
		public void parsed(ParsedModuleKey key, IModuleSymbol moduleSymbol, ModulaAst ast) {
			reconciled(key, ast);
		}
		
		/* (non-Javadoc)
		 * 
		 * For now, only possible way for the module to be removed from the model 
		 * while the editor is opened is when SDK was changed - so schedule reparse
		 * 
		 * @see com.excelsior.xds.builder.symbol.SymbolModelListenerAdapter#removed(com.excelsior.xds.parser.commons.symbol.ParsedModuleKey)
		 */
		@Override
		public void removed(ParsedModuleKey key) {
			scheduleReparse();
		}
	}
    
    private final class BuildSettingsCacheListener implements IBuildSettingsCacheListener{
		@Override
		public void buildSettingsReload(IProject p) {
			IEditorInput editorInput = getEditorInput();
			if (ObjectUtils.equals(p, CoreEditorUtils.getIProjectFrom(editorInput))) {
				ParseTask parseTask = ParseTaskFactory.create(editorInput);
				parseTask.setNeedModulaAst(true);
				SymbolModelManager.instance().scheduleParse(parseTask, null);
			}
		}
    }

	private final class NotInCompilationSetMarkerHandler extends MarkerAnnotationPaintHandler {
    	@Override
    	public boolean paint(SourceCodeTextEditor editor, MarkerAnnotation annotation,
    			GC gc, Canvas canvas, Rectangle bounds) {
    		IMarker marker = annotation.getMarker();
			try {
				if (marker.exists() && XdsMarkerConstants.PARSER_PROBLEM.equals(marker.getType())) {
					boolean isInCompilationSet = CompilationSetManager.getInstance().isInCompilationSet((IFile)marker.getResource());
					if (!isInCompilationSet) {
						int actualSeverity = marker.getAttribute(XdsMarkerConstants.PARSER_PROBLEM_SEVERITY_ATTRIBUTE, -1);
						if (actualSeverity > -1) {
							Image image = null;
							switch (actualSeverity) {
							case IMarker.SEVERITY_ERROR:
								image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK); 
								break;
							case IMarker.SEVERITY_WARNING:
								image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK); 
								break;
							default:
								break;
							}

							if (image != null) {
								ImageUtilities.drawImage(image, gc, canvas, bounds, SWT.CENTER, SWT.TOP);
								return true;
							}

							return false;
						}
					}
				}
			} catch (CoreException e) {
				LogHelper.logError(e);
			}
    		return false;
    	}

		@Override
		public void dispose() {
		}
    }

    private final class GrayBuildMarkerHandler extends MarkerAnnotationPaintHandler {
        private Image grayErrorImage;
        private Image grayWarningImage;
        
        @Override
        public boolean paint(SourceCodeTextEditor editor, MarkerAnnotation annotation,
                             GC gc, Canvas canvas, Rectangle bounds) 
        {
            IMarker marker = annotation.getMarker();
            try {
                if (grayErrorImage == null) {
                    Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
                    grayErrorImage= new Image(Display.getCurrent(), image, SWT.IMAGE_GRAY);
                    image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK); 
                    grayWarningImage= new Image(Display.getCurrent(), image, SWT.IMAGE_GRAY);
                }

                
                if (marker.exists() && XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE.equals(marker.getType())) {
                    if (marker.getAttribute(XdsMarkerConstants.MARKER_GRAY_STATE, false))
                    {
                        Image image = null;
                        int severity = marker.getAttribute(IMarker.SEVERITY, -1);
                        switch (severity) {
                        case IMarker.SEVERITY_ERROR:
                            image = grayErrorImage; 
                            break;
                        case IMarker.SEVERITY_WARNING:
                            image = grayWarningImage; 
                            break;
                        default:
                            break;
                        }

                        if (image != null) {
                        	ImageUtilities.drawImage(image, gc, canvas, bounds, SWT.CENTER, SWT.TOP);
                            return true;
                        }
                    }
                }
            } catch (CoreException e) {
                LogHelper.logError(e);
            }
            return false;
        }

		@Override
		public void dispose() {
			UiUtils.dispose(grayErrorImage);
			grayErrorImage = null; // to prevent ImageUtils from attempts to draw it
			UiUtils.dispose(grayWarningImage);
			grayWarningImage = null; // to prevent ImageUtils from attempts to draw it
		}
    }
    
    /**
     * Listens to text changes and makes gray BUILD_PROBLEM_MARKER_TYPE markers
     * on the changed lines
     *   
     * @author fsa
     */
    private final class ModulaDocumentListener implements IDocumentListener {
        @Override
        public void documentAboutToBeChanged(DocumentEvent event) {
            grayChangedLineMarkers(event);
        }

        @Override
        public void documentChanged(DocumentEvent event) {
            
        }
        
    }
    
    private void grayChangedLineMarkers(DocumentEvent event) {
        try {
        	final IMarker[][] builderMarkers = new IMarker[1][];
        	IEditorInput input = getEditorInput();
            IFile file = null;
            if (input instanceof IFileEditorInput) {
                file = ((IFileEditorInput)input).getFile();
                builderMarkers[0] = file.findMarkers(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE, false, IResource.DEPTH_INFINITE);
            }
            else {
            	return;
            }
            
            IDocument doc = event.getDocument();
            // offs/end := affected area length
            int offs = event.getOffset();
            int end = offs + event.getLength();
            
            int begLine = doc.getLineOfOffset(offs);
            int endLine = doc.getLineOfOffset(end);
            IRegion endReg = doc.getLineInformation(endLine);
            
            // offs/end := affected area includes whole 1st/last lines
            final int offsParam = doc.getLineOffset(begLine);
            final int endParam = endReg.getOffset() + endReg.getLength();
            
            if (file != null) {
            	IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
            	ISchedulingRule markerRule = ruleFactory.markerRule(file);
            	ResourceUtils.scheduleWorkspaceRunnable(new IWorkspaceRunnable() {
					@Override
					public void run(IProgressMonitor monitor) throws CoreException {
						monitor.beginTask(Messages.ModulaEditor_GrayingMarkers, builderMarkers[0].length);
						for (IMarker im : builderMarkers[0]) {
		            		int moffs = im.getAttribute(IMarker.CHAR_START, -1);
		            		if (offsParam <= moffs && moffs < endParam) {
		            			im.setAttribute(XdsMarkerConstants.MARKER_GRAY_STATE, true);
		            		}
		            		monitor.worked(1);
		            	}
						monitor.done();
					}
				}, markerRule, Messages.ModulaEditor_GrayingMarkers, false);
            }
        } catch (BadLocationException e) {
            LogHelper.logError(e);
        }
        catch (CoreException e) {
            LogHelper.logError(e);
        }
    }

}
