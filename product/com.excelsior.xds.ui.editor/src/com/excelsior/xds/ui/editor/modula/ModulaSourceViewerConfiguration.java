package com.excelsior.xds.ui.editor.modula;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.commons.AbstractSourceViewerConfiguration;
import com.excelsior.xds.ui.editor.commons.scanner.rules.CommentScanner;
import com.excelsior.xds.ui.editor.commons.scanner.rules.SingleTokenScanner;
import com.excelsior.xds.ui.editor.modula.contentassist2.ModulaAssistProcessor2;
import com.excelsior.xds.ui.editor.modula.format.ModulaTextFormatter;
import com.excelsior.xds.ui.editor.modula.reconciler.ModulaCompositeReconcilingStrategy;
import com.excelsior.xds.ui.editor.modula.reconciler.ModulaReconciler;
import com.excelsior.xds.ui.editor.modula.scanner.jflex.ModulaFlexBasedScanner;
import com.excelsior.xds.ui.editor.modula.scanner.jflex.ModulaPragmaFlexBasedScanner;

/**
 * Configuration for a source viewer which shows a Modula-2 source file.
 */
public class ModulaSourceViewerConfiguration extends AbstractSourceViewerConfiguration 
{
    /** The text editor to operate on. */
    private final ITextEditor textEditor;
    
    /** The Modula-2 code scanner. */
    private ITokenScanner modulaCodeScaner;

    /** The Modula-2 block comment scanner. */
    private ITokenScanner blockCommentScaner;
    
    /** The Modula-2 single-line comment scanner. */
    private ITokenScanner endOfLineCommentScaner;

    /** The Modula-2 string scanner. */
    private ITokenScanner stringScaner;

    /** The Modula-2 compiler pragma scanner.  */
    private ITokenScanner pragmaScaner;
    
    /** The prefixes to be used by the the prefix text operation by default. */
    private final String[] defaultPrefixes;

	private ModulaContentAssistant contentAssistant;

	private SingleTokenScanner inactiveCodeScaner;

	private TokenManager tokenManager;
	
    public ModulaSourceViewerConfiguration( TokenManager tokenManager, ITextEditor editor 
                                          , IPreferenceStore editorPreferenceStore
                                          , String eolCommentPrefix ) 
    {
        super(editorPreferenceStore);
        textEditor = editor;
        IPreferenceStore store = XdsEditorsPlugin.getDefault().getPreferenceStore(); 
        ModulaTokens.initStylesInStore(store);
        ModulaTokens.updateTokensFromStore(store, false);
        ModulaTokens.addStoreListener();

        defaultPrefixes = new String[] {eolCommentPrefix, ""};   //$NON-NLS-1$

        createScanners(tokenManager);
        this.tokenManager = tokenManager;
    }

	private void createScanners(TokenManager tokenManager) {
		modulaCodeScaner       = new ModulaFlexBasedScanner(tokenManager);
        
        blockCommentScaner     = new CommentScanner( tokenManager.createFrom(ModulaTokens.BlockComment.getToken())
                                                   , tokenManager.createFrom(ModulaTokens.TodoTask.getToken()) );
        endOfLineCommentScaner = new CommentScanner( tokenManager.createFrom(ModulaTokens.EndOfLineComment.getToken())
                                                   , tokenManager.createFrom(ModulaTokens.TodoTask.getToken()) );
        stringScaner           = new SingleTokenScanner(tokenManager.createFrom(ModulaTokens.String.getToken()));
        inactiveCodeScaner	   = new SingleTokenScanner(tokenManager.createFrom(ModulaTokens.InactiveCode.getToken()));
        pragmaScaner           = new ModulaPragmaFlexBasedScanner(tokenManager);
	}
    
    @Override
	public void refresh() {
    	/***
    	 * re-create scanners because simple scanners like {@link SingleTokenScanner} cache reference to the concrete color.
    	 */
    	createScanners(tokenManager);
	}

	/**
     * {@inheritDoc}
     */
    @Override
     public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return IModulaPartitions.M2_PARTITIONING;
	}
    
    /**
     * {@inheritDoc}
     */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return IModulaPartitions.ALL_CONFIGURED_CONTENT_TYPES;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
        return defaultPrefixes;
    }
		
    /**
     * {@inheritDoc}
     */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) 
	{
		PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		DefaultDamagerRepairer dr;
		
        dr = new DefaultDamagerRepairer(getModulaCodeScanner());
		reconciler.setDamager(dr,  IModulaPartitions.M2_CONTENT_TYPE_DEFAULT);
		reconciler.setRepairer(dr, IModulaPartitions.M2_CONTENT_TYPE_DEFAULT);
		
        dr = new DefaultDamagerRepairer(getEndOfLineCommentScanner());
        reconciler.setDamager(dr,  IModulaPartitions.M2_CONTENT_TYPE_END_OF_LINE_COMMENT);
        reconciler.setRepairer(dr, IModulaPartitions.M2_CONTENT_TYPE_END_OF_LINE_COMMENT);

        dr = new DefaultDamagerRepairer(getBlockCommentScanner());
		reconciler.setDamager(dr,  IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT);
		reconciler.setRepairer(dr, IModulaPartitions.M2_CONTENT_TYPE_BLOCK_COMMENT);

        dr = new DefaultDamagerRepairer(getStringScanner());
        reconciler.setDamager(dr,  IModulaPartitions.M2_CONTENT_TYPE_SINGLE_QUOTE_STRING);
        reconciler.setRepairer(dr, IModulaPartitions.M2_CONTENT_TYPE_SINGLE_QUOTE_STRING);

        dr = new DefaultDamagerRepairer(getStringScanner());
        reconciler.setDamager(dr,  IModulaPartitions.M2_CONTENT_TYPE_DOUBLE_QUOTE_STRING);
        reconciler.setRepairer(dr, IModulaPartitions.M2_CONTENT_TYPE_DOUBLE_QUOTE_STRING);

        dr = new DefaultDamagerRepairer(getPragmaScanner());
        reconciler.setDamager(dr,  IModulaPartitions.M2_CONTENT_TYPE_PRAGMA);
        reconciler.setRepairer(dr, IModulaPartitions.M2_CONTENT_TYPE_PRAGMA);
        
        dr = new DefaultDamagerRepairer(getInactiveCodeScaner());
        reconciler.setDamager(dr,  IModulaPartitions.M2_CONTENT_TYPE_DISABLED_CODE);
        reconciler.setRepairer(dr, IModulaPartitions.M2_CONTENT_TYPE_DISABLED_CODE);

        return reconciler;
	}

	
    /**
     * Returns the editor in which the configured viewer(s) will reside.
     *
     * @return the enclosing editor
     */
    protected ITextEditor getEditor() {
        return textEditor;
    }
	
	
    /**
     * Returns the Modula-2 source code scanner for this configuration.
     *
     * @return the Modula-2 source code scanner
     */
    protected ITokenScanner getModulaCodeScanner() {
        return modulaCodeScaner;
    }

    /**
     * Returns the  Modula-2 block comment scanner for this configuration.
     *
     * @return the Modula-2 block comment scanner
     */
    protected ITokenScanner getBlockCommentScanner() {
        return blockCommentScaner;
    }
	
    /**
     * Returns the Modula-2 single-line comment scanner for this configuration.
     *
     * @return the Modula-2 single-line comment scanner
     */
    protected ITokenScanner getEndOfLineCommentScanner() {
        return endOfLineCommentScaner;
    }

    /**
     * Returns the Modula-2 string scanner for this configuration.
     *
     * @return the Modula-2 string scanner
     */
    protected ITokenScanner getStringScanner() {
        return stringScaner;
    }

    /**
     * Returns the Modula-2 compiler pragma scanner for this configuration.
     *
     * @return the Modula-2 compiler pragma scanner
     */
    protected ITokenScanner getPragmaScanner() {
        return pragmaScaner;
    }
    
    protected SingleTokenScanner getInactiveCodeScaner() {
		return inactiveCodeScaner;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
    	if (contentAssistant == null) {
    		contentAssistant = new ModulaContentAssistant(sourceViewer);
    		contentAssistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
    		
//    	configureContentAssistanceProcessorsOld(contentAssistant);
    		configureContentAssistanceProcessors(contentAssistant);
    		contentAssistant.setRepeatedInvocationMode(true);
    		contentAssistant.setAutoActivationDelay(100);
    		contentAssistant.enableAutoActivation(true);
    		contentAssistant.enablePrefixCompletion(false);
    		contentAssistant.enableColoredLabels(true);
    		contentAssistant.enableAutoInsert(true);
    		
    		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
    	}
        
        return contentAssistant;
    }
    
    private static void configureContentAssistanceProcessors(ModulaContentAssistant contentAssistant) {
    	contentAssistant.setContentAssistProcessor(new ModulaAssistProcessor2(contentAssistant), IModulaPartitions.M2_CONTENT_TYPE_DEFAULT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        ITextHover defaultHover = super.getTextHover(sourceViewer, contentType); 
        return new ModulaEditorTextHover(getEditor(), defaultHover, tokenManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, ITextEditor> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
        @SuppressWarnings("unchecked")
        Map<String, ITextEditor> targets= super.getHyperlinkDetectorTargets(sourceViewer);
        targets.put(XdsEditorsPlugin.HYPERLINK_TARGET_MODULA_CODE, textEditor); //$NON-NLS-1$
        return targets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
        final ITextEditor editor = getEditor();
        if (editor != null) {
            ModulaCompositeReconcilingStrategy strategy = new ModulaCompositeReconcilingStrategy(sourceViewer, textEditor);
            ModulaReconciler reconciler = new ModulaReconciler(textEditor, strategy);
            reconciler.setIsAllowedToModifyDocument(false);
            reconciler.setDelay(500);
//            return super.getReconciler(sourceViewer);
            return reconciler;
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getTabWidth(ISourceViewer sourceViewer) {
        return (new ModulaTextFormatter()).getTabSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAutoEditStrategy[] getAutoEditStrategies( ISourceViewer sourceViewer
                                                    , String contentType ) 
    {
        IAutoEditStrategy strategy =  IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)
                                   ?  new ModulaAutoEditStrategy()
                                   :  new DefaultIndentLineAutoEditStrategy();
        return new IAutoEditStrategy[] { strategy };
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
        IInformationControlCreator icc = new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, true);
            }
        }; 
        InformationPresenter ip = new InformationPresenter(icc);
        ModulaInformationProvider mip = new ModulaInformationProvider(this);
        ip.setInformationProvider(mip, IModulaPartitions.M2_CONTENT_TYPE_DEFAULT);
        return ip;
        
    }
    
    @Override 
    protected boolean isShownInText(Annotation annotation) {
        if (ModulaOccurrencesMarker.OCCURENCE_ANNOTATION_ID.equals(annotation.getType()) ||
            ModulaOccurrencesMarker.WRITE_ANNOTATION_ID.equals(annotation.getType()))
        {
            return false;
        }
        return super.isShownInText(annotation);
    }
    
    public ITextEditor getTextEditor() {
        return textEditor;
    }

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		return new ModulaDoubleClickStrategy();
	}
}