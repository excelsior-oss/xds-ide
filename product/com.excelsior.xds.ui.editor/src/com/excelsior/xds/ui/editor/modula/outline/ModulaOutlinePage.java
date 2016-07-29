package com.excelsior.xds.ui.editor.modula.outline;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.excelsior.xds.core.model.IElementChangedListener;
import com.excelsior.xds.core.model.ISourceBound;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsModule;
import com.excelsior.xds.core.model.IXdsSyntheticElement;
import com.excelsior.xds.core.model.XdsModelManager;
import com.excelsior.xds.core.model.utils.XdsElementUtils;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.core.utils.IClosure;
import com.excelsior.xds.parser.commons.symbol.ITextBinding;
import com.excelsior.xds.ui.actions.ToolbarActionButton;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.utils.SingleUiUpdateRunnable;
import com.excelsior.xds.ui.viewers.DecoratedXdsElementLabelProvider.DelegatingDecoratedXdsElementLabelProvider;
import com.excelsior.xds.ui.viewers.XdsElementLabelProvider;
import com.excelsior.xds.ui.viewers.XdsElementPatternFilter;
import com.excelsior.xds.ui.viewers.XdsElementViewerComparator;

/**
 * The content outline page of the Modula-2 editor.<br>
 * <br><br>
 * It implements IElementChangedListener only for one scenario : <br>
 * <br>
 * 1) Editor is opened <br>
 * 2) IDE is restarted <br>
 * 3) Because core plugin is restarting - compilation set is re-populated.  <br>
 * 4) When it is populated - IElementChangedListener will fire and this we should refresh <br>
 * <br><br>
 *  Otherwise, because initially compilation set is empty - view will forever stay in the "Loading..." state. <br>
 *   <br>
 *  Refresh is done via {@link SingleUiUpdateRunnable}, so UI overhead should not be that big. <br><br>
 * <br><br>
 */
public class ModulaOutlinePage extends ContentOutlinePage implements IElementChangedListener
{
    private static final String DLG_ID = "com.excelsior.xds.ui.editor.modula.ModulaOutlinePage"; //$NON-NLS-1$
	
	private ITextEditor editor;
	private StyledText styledText;
	private IEditorInput input;
	private ModulaOutlinePageContentProvider contentProvider;
	private TreeViewer treeViewer;
	private Composite mainComposite;
	private FilteredTree filteredTree;
	private boolean isViewVisible;
	private boolean isNotPropagateTreeSelectionChangeToEditor = false;
	private boolean isIgnoreSetSelectionInTree = false;
    private boolean sortAlph;
    private boolean isLinkWithEditor = true;
    private ModulaOutlineFilter filter;
    
    private final AtomicBoolean isUpdateRequestPending = new AtomicBoolean(false);
    
	public ModulaOutlinePage(ITextEditor editor, StyledText styledText)
	{
		super();
		this.editor = editor;
		this.styledText = styledText;
		filter = new ModulaOutlineFilter();
		filter.readFilters(XdsEditorsPlugin.getDefault().getPreferenceStore(), DLG_ID);
	}
	

	@Override
	public void createControl(final Composite parent) {
	    IDialogSettings settings = getDialogSettings();
	    ToolbarActionButton tba = new ToolbarActionButton(Messages.XdsOutlinePage_ExpandAll, DLG_ID + "DIALOG_EXPAND_ALL", false, ImageUtils.EXPAND_ALL, //$NON-NLS-1$ 
	            false, settings, new IClosure<Boolean>() {  
        			@Override
        			public void execute(Boolean param) {
        				if (input != null) {
                            treeViewer.expandAll();
                        }
        			}
        	    });
	    this.getSite().getActionBars().getToolBarManager().add(tba);  

        tba = new ToolbarActionButton(Messages.XdsOutlinePage_CollapseAll, DLG_ID + "DIALOG_COLLAPSE_ALL", false, ImageUtils.COLLAPSE_ALL, //$NON-NLS-1$
                false, settings, new IClosure<Boolean>() {
        			@Override
        			public void execute(Boolean param) {
        				if (input != null) {
                            treeViewer.collapseAll();
                        }
        			}
                });
        this.getSite().getActionBars().getToolBarManager().add(tba);  

        tba = new ToolbarActionButton(Messages.XdsOutlinePage_LinkWithEditor, DLG_ID + "DIALOG_LINK_WITH_EDITOR", true, ImageUtils.SYNC_WITH_EDITOR, //$NON-NLS-1$
                true, settings, new IClosure<Boolean>() {
        			@Override
        			public void execute(Boolean isChecked) {
        				isLinkWithEditor = isChecked;
                        if (isLinkWithEditor) {
                        	ISelection selection = editor.getSelectionProvider().getSelection();
                        	synchronizeOutlineWithEditor(selection);
                        }
        			}
                });
        this.getSite().getActionBars().getToolBarManager().add(tba);  
        isLinkWithEditor = tba.isChecked();

        tba = new ToolbarActionButton(Messages.XdsOutlinePage_Sort, DLG_ID + "DIALOG_SORT_ALPHABETICALLY", true, ImageUtils.SORT_ALPHA, //$NON-NLS-1$
                false, settings, new IClosure<Boolean>() {
        			@Override
        			public void execute(Boolean isChecked) {
        				reSort(isChecked);
        			}
                });
        this.getSite().getActionBars().getToolBarManager().add(tba);  
        sortAlph = tba.isChecked();
        
        tba = new ToolbarActionButton(Messages.XdsOutlinePage_Filters, DLG_ID + "DIALOG_FILTERS_DIALOG", false, ImageUtils.FILTERS_ICON, //$NON-NLS-1$
                false, settings, new IClosure<Boolean>() {
                    @Override
                    public void execute(Boolean param) {
                        if (ModulaOutlineFiltersDialog.playDialog(getSite().getShell(), filter.getElementFilters())) { 
                            update();
                            filter.saveFilters(XdsEditorsPlugin.getDefault().getPreferenceStore(), DLG_ID);
                        }
                    }
                });
        this.getSite().getActionBars().getToolBarManager().add(tba);

	    isViewVisible = true;
	    installElementChangedListener();
	    
	    XdsElementPatternFilter patternFilter = new XdsElementPatternFilter();
        filteredTree = new FilteredTree(parent, getTreeStyle() | SWT.SINGLE, patternFilter, true);
		treeViewer = filteredTree.getViewer();
		treeViewer.setUseHashlookup(true);
		treeViewer.getTree().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                isIgnoreSetSelectionInTree = true;
                try{
                    selectElementInEditor(treeViewer.getSelection(), true);
                }
                finally{
                    isIgnoreSetSelectionInTree = false;
                }
            }
        });
		treeViewer.addPostSelectionChangedListener(this);
        treeViewer.getTree().addListener (SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type == SWT.KeyDown && event.character == SWT.CR) {
                    selectElementInEditor(treeViewer.getSelection(), true);
                }
            }
        });

		contentProvider = new ModulaOutlinePageContentProvider(editor, filter);
		treeViewer.setContentProvider(contentProvider);
		
		XdsElementLabelProvider labelProvider = new XdsElementLabelProvider();
		IBaseLabelProvider decoratedLabelProvider = new DelegatingDecoratedXdsElementLabelProvider(labelProvider);
		treeViewer.setLabelProvider(decoratedLabelProvider);
        treeViewer.setComparator(new OutlineSorter(labelProvider));

		//control is created after input is set
        update();
		
		styledText.addCaretListener(new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				if (contentProvider != null && isLinkWithEditor) {
					selectInTheOutlineTree(event.caretOffset);
				}
			}
		});
		
		mainComposite = filteredTree;
	}
	
	private void installElementChangedListener() {
		XdsModelManager.getModel().addElementChangedListener(this);
	}
	
	private void unInstallElementChangedListener() {
		XdsModelManager.getModel().removeElementChangedListener(this);
	}
	
	@Override
	public void elementChanged() {
		doRefresh();
	}

	@Override
    public void dispose() {
        super.dispose();
        
        unInstallElementChangedListener();
    }


	@Override
    public void init(IPageSite pageSite) {
        super.init(pageSite);
        pageSite.getPage().addPartListener(new ViewPartListener());
    }
    
    public Control getControl() {
        if (mainComposite == null) {
			return null;
		}
        return mainComposite;
    }
	
	public ISelection getSelection() {
        if (treeViewer == null) {
			return StructuredSelection.EMPTY;
		}
        return treeViewer.getSelection();
    }
	
	protected TreeViewer getTreeViewer() {
        return treeViewer;
    }
	
	public void setFocus() {
        treeViewer.getControl().setFocus();
    }
	
	 public void setSelection(ISelection selection) {
	     if (isIgnoreSetSelectionInTree) return;
	     
	     isNotPropagateTreeSelectionChangeToEditor = true;
         try
         {
             if (treeViewer != null) {
                 treeViewer.setSelection(selection);
             }
         }
         finally{
             isNotPropagateTreeSelectionChangeToEditor = false;
         }
	    }
	
	/**
	 * Sets the input of the outline page
	 */
	public void setInput(Object input)
	{
		if (this.input != input) { // update only if input changed
			this.input = (IEditorInput) input;
			update();
		}
	}

	/*
	 * Change in selection
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		if (isNotPropagateTreeSelectionChangeToEditor || 
		        getControl().isDisposed() ||
		        !getTreeViewer().getControl().isFocusControl()) 
		    return;
		
		super.selectionChanged(event);

		ISelection selection = event.getSelection();
		if (isLinkWithEditor) {
			try{
				isIgnoreSetSelectionInTree = true;
				selectElementInEditor(selection, false);
			}
			finally{
				isIgnoreSetSelectionInTree = false;
			}
		}
	}

    private void selectElementInEditor(ISelection selection, boolean isSelect) {
        if (editor == null) {
            return;
        }
        
        if (selection.isEmpty())
			editor.resetHighlightRange();
		else
		{
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof ISourceBound) {
			    ISourceBound sourceBound = (ISourceBound) firstElement;		
				try
				{
				    if (isSelect) {
	                    Control ctr = (Control) editor.getAdapter(Control.class);
	                    ctr.setFocus();
				    }
				    
				    ITextBinding sourceBinding = sourceBound.getSourceBinding();
				    
				    if (sourceBinding != null) {
				    	ITextRegion identifierRegion = sourceBinding.getNameTextRegion();
				    	if (identifierRegion != null) {
				    		editor.selectAndReveal(identifierRegion.getOffset(), identifierRegion.getLength());
				    	}
				    	ITextRegion elementRegion = sourceBinding.getDeclarationTextRegion();
				    	if (elementRegion != null) {
				    		editor.setHighlightRange(elementRegion.getOffset(), elementRegion.getLength(), false);
	                        if ((identifierRegion == null) && (firstElement instanceof IXdsSyntheticElement)) {
	                            // syntactic elements like "import list" have not name text region at all. 
	                            editor.selectAndReveal(elementRegion.getOffset(), elementRegion.getLength());
	                        }
				    	}
				    }
				}
				catch (IllegalArgumentException x)
				{
					editor.resetHighlightRange();
				}
			}
		}
    }

	/**
	 * The editor is saved, so we should refresh representation
	 * 
	 * @param tableNamePositions
	 */
	public void update()
	{
		//set the input so that the outlines parse can be called
		//update the tree viewer state
		TreeViewer viewer = getTreeViewer();

		if (viewer != null)
		{
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed())
			{
				try{
					control.setRedraw(false);
					viewer.setInput(input);
				}
				finally{
					control.setRedraw(true);
				}
			}
		}
	}
	
	private final class ViewPartListener implements IPartListener2 {
        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            IWorkbenchPart part= partRef.getPart(false);
            if (part instanceof ContentOutline) {
                isViewVisible= true;
            }
        }

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {
        }

        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {
        }

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {
            IWorkbenchPart part= partRef.getPart(false);
            if (part instanceof ContentOutline) {
                isViewVisible= false;
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {
        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part instanceof ContentOutline) {
            	isViewVisible = false;
            }
            if (part instanceof ITextEditor) {
                waitForControl();
            }
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {
        }

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {
            IWorkbenchPart part = partRef.getPart(false);
            if (part instanceof ITextEditor) {
                editor = (ITextEditor)part;
                waitForControl();
                setInput(editor.getEditorInput());
            }
        }
    }
	
	private void waitForControl() {
        while(treeViewer.isBusy()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }
	
	
    private IDialogSettings getDialogSettings() {
        String sectionName= DLG_ID;

        IDialogSettings settings = XdsEditorsPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null) {
            settings = XdsEditorsPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }
        return settings;
    }
    
    private void reSort(boolean sortAlph) {
        this.sortAlph = sortAlph;
        treeViewer.refresh();
    }


    private class OutlineSorter extends XdsElementViewerComparator {

        public OutlineSorter(ILabelProvider labelProvider) {
            super(labelProvider);
        }

        @Override
        public boolean isSortAlphabetically() {
            return sortAlph;
        }
    }

    public void doRefresh() {
    	if (isViewVisible && isUpdateRequestPending.compareAndSet(false, true)) {
        	Display.getDefault().asyncExec(new SingleUiUpdateRunnable(isUpdateRequestPending) {
				@Override
				protected void doRun() {
					if (!treeViewer.getControl().isDisposed()) {
                        treeViewer.refresh();
                        if (isLinkWithEditor) {
                        	synchronizeOutlineWithEditor();
                        }
                    }
				}
			});
        }
    }

	/**
	 * Selects corresponding element in the Outline tree.
	 * @param offset offset in the editor
	 */
	private void selectInTheOutlineTree(int offset) {
		IXdsModule root = contentProvider.getRoot();
		if (root != null) {
			IXdsElement child = XdsElementUtils.findBottomostChildCoveringPosition(root, offset);
		    if (child != null) {
		        setSelection(new StructuredSelection(child));
		    }
		}
	}
	
	/**
	 * Synchronizes outline view selection with editor selection
	 */
	private void synchronizeOutlineWithEditor() {
		ISelection selection = editor.getSelectionProvider().getSelection();
		synchronizeOutlineWithEditor(selection);
	}
	
	/**
	 * Synchronizes outline view selection with editor selection, using the given editor selection
	 */
	private void synchronizeOutlineWithEditor(ISelection selection) {
		if (selection.isEmpty()) {
			return;
		}
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			selectInTheOutlineTree(textSelection.getOffset());
			editor.setFocus();
		}
	}
}
