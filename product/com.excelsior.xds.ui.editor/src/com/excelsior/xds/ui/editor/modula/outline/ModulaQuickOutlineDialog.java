package com.excelsior.xds.ui.editor.modula.outline;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.model.ISourceBound;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsSyntheticElement;
import com.excelsior.xds.core.model.utils.XdsElementUtils;
import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.parser.commons.symbol.ITextBinding;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.commons.utils.SwtUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.XdsEditorsPlugin;
import com.excelsior.xds.ui.editor.internal.nls.Messages;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.viewers.DecoratedXdsElementLabelProvider.DelegatingDecoratedXdsElementLabelProvider;
import com.excelsior.xds.ui.viewers.XdsElementLabelProvider;
import com.excelsior.xds.ui.viewers.XdsElementPatternFilter;
import com.excelsior.xds.ui.viewers.XdsElementViewerComparator;

public class ModulaQuickOutlineDialog extends PopupDialog 
{
    private FilteredTree filteredTree;
    private TreeViewer treeViewer;
    private ITextEditor editor;
    private MenuManager menuManager = null;
    private ToolBar toolBar;
    private ModulaOutlinePageContentProvider contentProvider;
    private boolean sortAlph = false;
    private final ModulaOutlineFilter filter;
    
    private static final String DLG_ID = "com.excelsior.xds.ui.editor.modula.ModulaQuickOutlineDialog"; //$NON-NLS-1$
    private static final String DLG_SORT_ALPH = "DLG_SORT_ALPHABETICALLY"; //$NON-NLS-1$
    private static final String DLG_USE_PERSISTED_SIZE = "com.excelsior.xds.ui.editor.modula.ModulaQuickOutlineDialog.DLG_USE_PERSISTED_SIZE"; //$NON-NLS-1$
    private static final String DLG_USE_PERSISTED_LOCATION = "com.excelsior.xds.ui.editor.modula.ModulaQuickOutlineDialog.DLG_USE_PERSISTED_LOCATION"; //$NON-NLS-1$


    public ModulaQuickOutlineDialog(Shell parent) {
        super( parent, SWT.RESIZE, true
             , getStateForSizeOrLocation(true)
             , getStateForSizeOrLocation(false)
             , false, true, null, null );        
        filter = new ModulaOutlineFilter();
        filter.readFilters(XdsEditorsPlugin.getDefault().getPreferenceStore(), DLG_ID);
    }
    
    private static boolean getStateForSizeOrLocation(boolean size) {
        IDialogSettings settings = getDialogSettingsStatic();
        return settings.getBoolean(size ? DLG_USE_PERSISTED_SIZE : DLG_USE_PERSISTED_LOCATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        
        XdsElementPatternFilter patternFilter = new XdsElementPatternFilter();
        filteredTree = new MyFilteredTree(composite, patternFilter);
        treeViewer = filteredTree.getViewer();
        treeViewer.setUseHashlookup(true);
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        editor = (ITextEditor) WorkbenchUtils.getActiveEditor(false);
       
        contentProvider = new ModulaOutlinePageContentProvider(editor, filter);
        treeViewer.setContentProvider(contentProvider);

        XdsElementLabelProvider labelProvider = new XdsElementLabelProvider();
        IBaseLabelProvider decoratedLabelProvider = new DelegatingDecoratedXdsElementLabelProvider(labelProvider);
        treeViewer.setLabelProvider(decoratedLabelProvider);
        treeViewer.setComparator(new OutlineSorter(labelProvider));
        
        treeViewer.setAutoExpandLevel(0);
        try {
        	treeViewer.getControl().setRedraw(false);
        	treeViewer.setInput(editor.getEditorInput());
        }
        finally {
        	treeViewer.getControl().setRedraw(true);
        }
        
        treeViewer.getTree().addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                handleTreeSelection(e);
            }        
        });
        
        Listener eventListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.type == SWT.KeyDown && event.character == SWT.CR) {
                    handleTreeSelection();
                }
            }
        };
        
        treeViewer.getTree().addListener (SWT.Collapse, eventListener);
        treeViewer.getTree().addListener (SWT.Expand, eventListener);
        treeViewer.getTree().addListener (SWT.KeyDown, eventListener);
        treeViewer.getTree().addListener (SWT.MouseUp, eventListener);
        treeViewer.getTree().addListener (SWT.MouseDoubleClick, eventListener);
        
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            sortAlph = settings.getBoolean(getClass().getName() + DLG_SORT_ALPH);
        }


        return composite;
    }
    
    @Override public int open() {
        int res = super.open();
        selectElementUnderEditorCursor();
        return res;
    }
    
    @Override
    protected void adjustBounds() {
        
        if (!getPersistSize()) {
            Point prefSize;
            int gap5W = SwtUtils.getTextWidth(filteredTree, "WWWWW"); //$NON-NLS-1$
            prefSize = filteredTree.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            prefSize.x += gap5W;
            prefSize.y += SWTFactory.getCharHeight(filteredTree)*3;
            
            Rectangle recDisplay = filteredTree.getDisplay().getPrimaryMonitor().getClientArea();
            prefSize.x = Math.max(prefSize.x, SwtUtils.getTextWidth(filteredTree, "WWWWWWWWWWWWWWWWWWWWWWWWWWW")); //$NON-NLS-1$
            prefSize.x = Math.min(prefSize.x, (int)((double)recDisplay.width / 1.5));
            prefSize.y = Math.min(prefSize.y, (int)((double)recDisplay.height / 1.2));

            // prefSize now is calculateg for all whole content. Clip it by decreased editor window size:
            Control edCtrl = (Control) editor.getAdapter(Control.class);
            Point edSize = edCtrl.getSize();
            prefSize.x = Math.min(prefSize.x, Math.max(edSize.x - gap5W*5, gap5W*5));
            prefSize.y = Math.min(prefSize.y, Math.max(edSize.y - gap5W*2, gap5W*5));

            getShell().setSize(prefSize);
            
            if (!this.getPersistLocation()) {
                int xx = (edSize.x - prefSize.x) / 2;
                int yy = (edSize.y - prefSize.y) / 2;
                Point edPos = edCtrl.toDisplay(new Point(0,0));
                getShell().setLocation(edPos.x + xx, edPos.y + yy);
            }
        }
        
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return getDialogSettingsStatic();
    }

    private static IDialogSettings getDialogSettingsStatic() {
        String sectionName= DLG_ID;

        IDialogSettings settings = XdsEditorsPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null) {
            settings = XdsEditorsPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }
        return settings;
    }

    private void selectElementUnderEditorCursor() {
        try {
            ISelection selection = editor.getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection)selection;
                IXdsElement child = XdsElementUtils.findBottomostChildCoveringPosition(contentProvider.getRoot(), textSelection.getOffset());
			    if (child != null) {
			    	treeViewer.setSelection(new StructuredSelection(child));
			    }
            }
        } catch (Exception e) {
            // NPE: not parsed yet?
        }
    }

    @Override
    protected Control getFocusControl() {
        return filteredTree.getFilterControl();
    }
    
    @Override
    protected void fillDialogMenu(IMenuManager dialogMenu) {
        Action actSortAlph = new Action (Messages.XdsQuickOutlineDialog_SortAlphabetically, IAction.AS_CHECK_BOX) {
            public void run() {
                reSort(this.isChecked());
            }
        }; 
        
        actSortAlph.setImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.SORT_ALPHA)));
        actSortAlph.setChecked(sortAlph);
        dialogMenu.add(actSortAlph);
        
        Action actFilters = new Action (Messages.XdsQuickOutlineDialog_Filters, IAction.AS_PUSH_BUTTON) {
            public void run() {
                filtersDialog();
            }
        }; 
        actFilters.setImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(ImageUtils.FILTERS_ICON)));
        dialogMenu.add(actFilters);
        
        dialogMenu.add(new Separator("XdsMenuEnd")); //$NON-NLS-1$
        super.fillDialogMenu(dialogMenu);
    }
    

    @Override 
    protected void showDialogMenu() {  // *FSA: hack to show menu w/o title bar.
        if (menuManager == null) {
            menuManager = new MenuManager();
            fillDialogMenu(menuManager);
        }
        
        // Setting this flag works around a problem that remains on X only,
        // whereby activating the menu deactivates our shell.
        
        // listenToDeactivate = !Util.isGtk(); // *FSA: hz. listenToDeactivate is private. seems that "true := true" here and all works ok...

        Menu menu = menuManager.createContextMenu(getShell());
        Rectangle bounds = toolBar.getBounds();
        Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
        topLeft = getShell().toDisplay(topLeft);
        menu.setLocation(topLeft.x, topLeft.y);
        menu.setVisible(true);
    }

    

    @Override 
    protected void saveDialogBounds(Shell shell) {
        super.saveDialogBounds(shell);
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            settings.put(getClass().getName() + DLG_SORT_ALPH, sortAlph);
        }
        // hacked menu button (w/o title bar) turns OFF restore of this states so save/restore it here: 
        settings.put(DLG_USE_PERSISTED_SIZE, getPersistSize());
        settings.put(DLG_USE_PERSISTED_LOCATION, getPersistLocation());
    }


    private void handleTreeSelection() {
        ISelection selection = treeViewer.getSelection();
        if (!selection.isEmpty()) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof ISourceBound) {
                ISourceBound sourceBound = (ISourceBound) element;
                ITextBinding sourceBinding = sourceBound.getSourceBinding();
                if (sourceBinding != null) {
                    ITextRegion identifierRegion = sourceBinding.getNameTextRegion();
                    if (identifierRegion != null) {
                        int start = identifierRegion.getOffset();
                        int length = identifierRegion.getLength();
                        editor.selectAndReveal(start, length);
                    }

                    ITextRegion elementRegion = sourceBinding.getDeclarationTextRegion();
                    if (elementRegion != null) {
                        int start = elementRegion.getOffset();
                        int length = elementRegion.getLength();
                        editor.setHighlightRange(start, length, identifierRegion == null);
                        if ((identifierRegion == null) && (element instanceof IXdsSyntheticElement)) {
                            // syntactic elements like "import list" have not name text region at all. 
                            editor.selectAndReveal(start, length);
                        }
                    }
                    
                    close();
                }
            }
        }
    }

    private void handleTreeSelection(SelectionEvent e) {
        if ((e.stateMask & SWT.BUTTON1) != 0) {
            handleTreeSelection();
        }
    }
    
    private void reSort(boolean sortAlph) {
        this.sortAlph = sortAlph;
        treeViewer.refresh();
    }
    
    private void filtersDialog(){
        if (ModulaOutlineFiltersDialog.playDialog(getShell(), filter.getElementFilters())) { 
            try {
                treeViewer.getControl().setRedraw(false);
                treeViewer.setInput(editor.getEditorInput());
            }
            finally {
                treeViewer.getControl().setRedraw(true);
            }
            filter.saveFilters(XdsEditorsPlugin.getDefault().getPreferenceStore(), DLG_ID);
        }
    }
    
    
    private class OutlineSorter extends XdsElementViewerComparator 
    {
        public OutlineSorter(ILabelProvider labelProvider) {
            super(labelProvider);
        }

        @Override
        public boolean isSortAlphabetically() {
            return sortAlph;
        }
    }
    
    
    private class MyFilteredTree extends FilteredTree 
    {
        public MyFilteredTree(Composite parent, PatternFilter filter) {
            super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE, filter, true);
        }
        
        @Override
        protected Composite createFilterControls(Composite parent) {
            GridLayout gl = (GridLayout)parent.getLayout();
            ++ gl.numColumns; // add menu button
            super.createFilterControls(parent);
            createDialogMenu(parent);
            return parent;
        }

        
        private void createDialogMenu(Composite parent) {
            toolBar = new ToolBar(parent, SWT.FLAT);
            ToolItem viewMenuButton = new ToolItem(toolBar, SWT.PUSH, 0);

            GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(toolBar);
            viewMenuButton.setImage(JFaceResources.getImage(POPUP_IMG_MENU));
            viewMenuButton.setDisabledImage(JFaceResources.getImage(POPUP_IMG_MENU_DISABLED));
            viewMenuButton.setToolTipText("Menu"); //$NON-NLS-1$
            viewMenuButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    showDialogMenu();
                }
            });
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=177183
            toolBar.addMouseListener(new MouseAdapter() {
                public void mouseDown(MouseEvent e) {
                    showDialogMenu();
                }
            });
        }

    }

}
