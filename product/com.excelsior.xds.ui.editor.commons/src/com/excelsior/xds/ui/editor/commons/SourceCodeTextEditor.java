package com.excelsior.xds.ui.editor.commons;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineNumberChangeRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import com.excelsior.xds.core.ide.symbol.ParseTaskFactory;
import com.excelsior.xds.core.ide.symbol.SymbolModelManager;
import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.marker.XdsMarkerConstants;
import com.excelsior.xds.core.preferences.PreferenceKeys;
import com.excelsior.xds.core.utils.time.ModificationStamp;
import com.excelsior.xds.parser.commons.symbol.ParsedModuleKey;
import com.excelsior.xds.parser.editor.model.EditorDocumentCache;
import com.excelsior.xds.ui.editor.commons.annotations.IAnnotationPaintHandler;
import com.excelsior.xds.ui.editor.commons.internal.modula.ruler.LineNumberColumnPainterRegistry;
import com.excelsior.xds.ui.editor.commons.ruler.IRulerPainter;
import com.excelsior.xds.ui.editor.commons.text.IndentGuidesModel;
import com.excelsior.xds.ui.editor.commons.text.IndentGuidesPainter;
import com.excelsior.xds.ui.editor.commons.text.PairedBracketsMatcher;


/**
 * Source code specific text editor.
 *
 * @author lion
 */
public abstract class SourceCodeTextEditor extends TextEditor 
{
	protected AbstractSourceViewerConfiguration configuration;
	private final IDocumentListener documentListener = new DocumentListener();
	
	private final List<IAnnotationPaintHandler> annotationPaintHandlers = new ArrayList<IAnnotationPaintHandler>();
	private boolean isDisposed;
	
	/**
	 * Modification stamp of the document
	 */
	private ModificationStamp modificationStamp = ModificationStamp.OLDEST;
	private final List<IRulerPainter> rulerPainters;
	private IPreferenceChangeListener corePluginPreferenceListener;
	
    public SourceCodeTextEditor() {
		super();
		rulerPainters = LineNumberColumnPainterRegistry.get().contributions();
		
		corePluginPreferenceListener = new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				String key = event.getKey();
				if (PreferenceKeys.PKEY_EXECUTABLE_SOURCE_CODE_COLOR.getKey().equals(key)) {
					fLineNumberRulerColumn.redraw();
				}
			}
		};
		PreferenceKeys.addChangeListener(corePluginPreferenceListener);
	}
    
	public SourceViewerConfiguration getConfiguration() {
		return configuration;
	}
    
    @Override
	public void dispose() {
    	for (IAnnotationPaintHandler paintHandler : annotationPaintHandlers) {
    		paintHandler.dispose();
		}
		super.dispose();
		isDisposed = true;
		
		if (key != null) {
			EditorDocumentCache.instance().removeDocument(key);
			SymbolModelManager.instance().scheduleParse(ParseTaskFactory.create(getEditorInput()), null);
		}
		PreferenceKeys.removeChangeListener(corePluginPreferenceListener);
	}
    
	public boolean isDisposed() {
		return isDisposed;
	}

	protected void addAnnotationPaintHandler(IAnnotationPaintHandler handler) {
    	annotationPaintHandlers.add(handler);
    }
    
	/**
     * Mutex for the reconciler. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=63898
     * for a description of the problem.
     * <p>
     * XXX remove once the underlying problem (https://bugs.eclipse.org/bugs/show_bug.cgi?id=66176) is solved.
     * </p>
     */
    private final Object reconcilerLock = new Object();
	private DocumentEvent lastDocumentEvent;
	private ParsedModuleKey key;
    
    /**
     * Returns the mutex for the reconciler. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=63898
     * for a description of the problem.
     * <p>
     * XXX remove once the underlying problem (https://bugs.eclipse.org/bugs/show_bug.cgi?id=66176) is solved.
     * </p>
     * @return the lock reconciler may use to synchronize on
     */
    public Object getReconcilerLock() {
        return reconcilerLock;
    }
    
    /**
     * Reloads the text editor source view configuration to refresh syntax coloring.
     */
    public void refreshConfiguration() {
        ISourceViewer isv = getSourceViewer();
        if (isv instanceof SourceViewer) {
            SourceViewer sv = (SourceViewer)isv;
            sv.unconfigure();
            configuration.refresh();
            sv.configure(configuration);
        }
    }

    /**
     * Reloads the text editor source view configuration for all editors of given class.
     */
    public static void refreshEditorsConfiguration(final Class<? extends SourceCodeTextEditor> editorClass) 
    {
        Display.getDefault().asyncExec(() ->{
        	IEditorReference editors[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().getEditorReferences();
            for (IEditorReference editorRef : editors) {
                IEditorPart editorPart = editorRef.getEditor(false);
                if (editorPart != null && editorClass.isInstance(editorPart)) {
                    ((SourceCodeTextEditor)editorPart).refreshConfiguration();
                }
            }
        });
    }
    
    @Override
	protected IAnnotationAccess createAnnotationAccess() {
		return new InternalMarkerAnnotationAccess();
	}

	/** 
     * Returns the end of line comment prefix to be used by the line-prefix operation.
     * 
     * @return the end of line comment prefix or  <code>null</code> if the end 
     *         of line comment prefix operation should not be supported
     */
    public String getEOLCommentPrefix() {
        return null;
    }

    /** 
     * Returns the instance of <code>PairedBraceMatcher</code> specific for this editor or
     * <code>null</code> if the brace matching is not required. 
     * 
     * @return instance of <code>PairedBraceMatcher</code>  or 
     *         <code>null</code> if the brace matching is not required. 
     */
    protected PairedBracketsMatcher getPairedBraceMatcher() {
        return null;
    }
        
    /**
     * Returns whether the vertical indent guides is supported by this editor.
     * 
     * @return <code>true</code> if vertical indent guides is supported, <code>false</code> otherwise
     */
    protected boolean isIndenGuidesSupported() {
        return true;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        final ISourceViewer viewer = super.createSourceViewer(parent, ruler, styles);
        if (isIndenGuidesSupported()) {
            IndentGuidesPainter.installToViewer(viewer, new IndentGuidesModel(this));
        }
        

        viewer.addTextInputListener(new TextInputListener());
        
        return viewer;
    }
    
    @Override
    public void createPartControl(Composite parent) {
    	super.createPartControl(parent);
    	initializeRulerPainters(); // sourceViewer needs to be initialized.
    }

	private void initializeRulerPainters() {
		for (IRulerPainter p : rulerPainters) {
			p.setTextEditor(this);
		}
	}
    
    public boolean isSourceViewer(ITextViewer v) {
        return v != null && v.equals(getSourceViewer()); 
    }
    
    public ModificationStamp getLastDocumentChangeModificationStamp() {
        return modificationStamp;
    }
    
    public DocumentEvent getLastDocumentEvent() {
		return lastDocumentEvent;
	}

	/**
     * Listens for the SourceView`s document change. Initialize modificationStamp.
     *  
     * @author lsa80
     */
    private final class TextInputListener implements ITextInputListener {
		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			updateEditorDocumentCache(newInput);;
		}

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldInput,
				IDocument newInput) {
			if (oldInput != null) {
				oldInput.removeDocumentListener(documentListener);
			}
			if (newInput != null) {
				newInput.addDocumentListener(documentListener);
			}
			modificationStamp = ModificationStamp.OLDEST;
		}
	}
    
    private final class DocumentListener implements IDocumentListener {
		@Override
		public void documentChanged(DocumentEvent event) {
			modificationStamp = new ModificationStamp();
			lastDocumentEvent = event;
			
			updateEditorDocumentCache(event.getDocument());
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	}
    
    private void updateEditorDocumentCache(IDocument doc) {
		if (key != null) {
			EditorDocumentCache.instance().removeDocument(key);
		}
		key = CoreEditorUtils.editorInputToParsedModuleKey(getEditorInput());
		if (key != null) {
			EditorDocumentCache.instance().addDocument(key, doc);
		}
	}
    
    private class InternalMarkerAnnotationAccess extends DefaultMarkerAnnotationAccess {
		@Override
		public void paint(Annotation annotation, GC gc, Canvas canvas,
				Rectangle bounds) {
			boolean isPaintRequestHandled = false;
			for (IAnnotationPaintHandler annotationPaintHandler : annotationPaintHandlers) {
				if (annotationPaintHandler.paint(SourceCodeTextEditor.this, annotation, gc, canvas, bounds)) {
					isPaintRequestHandled = true;
					break;
				}
			}
			
			if (!isPaintRequestHandled) {
				super.paint(annotation, gc, canvas, bounds);
			}
		}
		
		@Override
		public int getLayer(Annotation annotation) {
	        if (annotation instanceof MarkerAnnotation) {
	            MarkerAnnotation markerAnnotation = (MarkerAnnotation)annotation;
	            IMarker im = markerAnnotation.getMarker();
	            try {
                    if (XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE.equals(im.getType()) &&
                        im.getAttribute(XdsMarkerConstants.MARKER_GRAY_STATE, false)) 
                    {
                        markerAnnotation.markDeleted(true);
                        return IAnnotationAccessExtension.DEFAULT_LAYER; // place gray markers under all other 
                    }
                } catch (Exception e) { // hz
                }
	        }
            return super.getLayer(annotation);
		}
    } // class InternalMarkerAnnotationAccess

	
	@Override
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		fLineNumberRulerColumn= new LineNumberChangeRulerColumn(getSharedColors()){
			@Override
			public void redraw() {
				for (IRulerPainter p : rulerPainters) {
					p.beforePaint();
				}
				super.redraw();
			}
			
			@Override
			protected void paintLine(int line, int y, int lineheight, GC gc,
					Display display) {
				boolean isPaintLine = true;
				Rectangle r = new Rectangle(0, y, getWidth(), lineheight);
				for (IRulerPainter p : rulerPainters) {
					isPaintLine = isPaintLine && !p.paintLine(line, r, gc, display);
				}
				if (isPaintLine){
					super.paintLine(line, y, lineheight, gc, display);
				}
			}
		};
		((IChangeRulerColumn) fLineNumberRulerColumn).setHover(createChangeHover());
		initializeLineNumberRulerColumn(fLineNumberRulerColumn);
		return fLineNumberRulerColumn;
	}
}
