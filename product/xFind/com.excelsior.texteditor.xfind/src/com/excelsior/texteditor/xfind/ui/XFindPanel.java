package com.excelsior.texteditor.xfind.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.swt.IFocusService;

import com.excelsior.texteditor.xfind.XFindPlugin;
import com.excelsior.texteditor.xfind.internal.ImageUtils;
import com.excelsior.texteditor.xfind.internal.nls.Messages;

public class XFindPanel extends Composite 
{
    public static final String CONTEXT_ID = "com.excelsior.texteditor.xfind.context";    //$NON-NLS-1$
    public static final String FOCUS_ID   = "com.excelsior.texteditor.xfind.focus.text"; //$NON-NLS-1$

    public static final String XFIND_PANEL_PLACEMENT = "com.excelsior.texteditor.xfind.PANEL_PLACEMENT"; //$NON-NLS-1$
    public static final String XFIND_PANEL_HISTORY = "com.excelsior.texteditor.xfind.PANEL_HISTORY"; //$NON-NLS-1$
    public static final String XFIND_PANEL_HISTORY_SEPARATOR = System.getProperty("line.separator");
    
    public static final int XFIND_PANEL_PLACEMENT_TOP    = 1;
    public static final int XFIND_PANEL_PLACEMENT_BOTTOM = 2;

    private static final int HISTORY_MAX_DEEP = 20;

    private static final String PREFERENCE_PAGE_ID = "com.excelsior.texteditor.xfind.ui.XFindPreferencePage_Id"; //$NON-NLS-1$

    private static final String XFIND_PREFIX = "xfind."; //$NON-NLS-1$
    private static final String FIND_CASE_SENSITIVE = XFIND_PREFIX + "caseSensitive"; //$NON-NLS-1$
    private static final String FIND_WHOLE_WORD     = XFIND_PREFIX + "wholeWords"; //$NON-NLS-1$
    private static final String FIND_REGEXP         = XFIND_PREFIX + "regExpr"; //$NON-NLS-1$
    private static final String FIND_INCREMENTAL    = XFIND_PREFIX + "incremental"; //$NON-NLS-1$

    private Text searchText;
    private ToolItem bHistory;
    private ToolItem bNext;
    private ToolItem bPrev;
    private ToolItem bSettings;
    private ToolBar toolBar;
    private Color bgColorOk, bgColorNotFound;
    private StatusLine statusLine;

    private static List<String> history = new ArrayList<String>();

    private IContextActivation contextActivation;
    private IFindReplaceTarget target;
    private int fromHistoryPos = 0;
    private boolean isRegExSupported;

    private Control targetFocusControl = null;
	
    public XFindPanel(final Composite parent, IEditorPart editorPart) {
        super(parent, SWT.NONE);
        statusLine = new StatusLine(editorPart.getEditorSite());
        setVisible(false);

        if (editorPart != null) {
            target = (IFindReplaceTarget) editorPart.getAdapter(IFindReplaceTarget.class);
            isRegExSupported = (target instanceof IFindReplaceTargetExtension3);
        }

        final IPreferenceStore store = XFindPlugin.getDefault().getPreferenceStore(); 
        if (!store.contains(XFIND_PANEL_PLACEMENT)) {
            store.setDefault(XFIND_PANEL_PLACEMENT, XFIND_PANEL_PLACEMENT_TOP);
        }

        if (store.getInt(XFIND_PANEL_PLACEMENT) == XFIND_PANEL_PLACEMENT_BOTTOM) {
            moveBelow(null);
        } else {
            moveAbove(null);
        }
        
        createContents();
        loadHistory(store);

        store.addPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (XFIND_PANEL_PLACEMENT.equals(event.getProperty())) {
                            if (store.getInt(XFIND_PANEL_PLACEMENT) == XFIND_PANEL_PLACEMENT_BOTTOM) {
                                moveBelow(null);
                            } else {
                                moveAbove(null);
                            }
                            parent.layout();
                        }
                    }
                });
            }
        });
        
        parent.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				saveHistory(store);
			}
		});
    }

	private void loadHistory(IPreferenceStore store) {
		if (history.isEmpty()) {
			String serializedHistory = store.getString(XFIND_PANEL_HISTORY);
			if (!serializedHistory.isEmpty()) {
				String[] historyItems = serializedHistory.split(XFIND_PANEL_HISTORY_SEPARATOR);
				for (String item : historyItems) {
					if (!item.isEmpty()) {
						history.add(item);
					}
				}
			}
		}
		bHistory.setEnabled(!history.isEmpty());
		if (!history.isEmpty()) {
			searchText.setText(history.get(0));
		}
	}
	
	private void saveHistory(IPreferenceStore store) {
		if (!history.isEmpty()) {
			StringBuilder valueSB = new StringBuilder();
			for (String item : history) {
				valueSB.append(item + XFIND_PANEL_HISTORY_SEPARATOR);
			}
			store.setValue(XFIND_PANEL_HISTORY, valueSB.toString());
		}
		else{
			store.setValue(XFIND_PANEL_HISTORY, "");
		}
	}

	private void createContents() {
        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layoutData.exclude = true;
        setLayout(layout);

        createXFindContent(this);
    }

//	private void createXSearchContent(Composite parent) {
//		GridLayout layout = new GridLayout(2, false);
//		parent.setLayout(layout);
//		Label searchLabel = new Label(parent, SWT.NONE);
//		searchLabel.setText("Search: ");
//		final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
//		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
//				| GridData.HORIZONTAL_ALIGN_FILL));        
//	}	
//

    protected void createXFindContent(Composite parent) {
        GridLayout layout = new GridLayout(3, false);
        parent.setLayout(layout);

        Label searchLabel = new Label(parent, SWT.NONE);
        searchLabel.setText(Messages.XFindPanel_Search_label + ": "); //$NON-NLS-1$
        searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
        searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        createToolBar(parent);

        searchText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                activateContext();
            }

            @Override
            public void focusLost(FocusEvent e) {
                deactivateContext();
            }
        });
        bgColorOk = searchText.getBackground();
        bgColorNotFound = new Color(bgColorOk.getDevice(), 255, 221, 221);

        searchText.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            	// Shift + up/down:
                // System.out.println(String.format("-- keycode = %x   satatemask = %x",
                // e.keyCode, e.stateMask));
                if ((e.stateMask & SWT.SHIFT) != 0) {
                    if ((e.keyCode & SWT.KEY_MASK) == (SWT.ARROW_UP | SWT.KEYCODE_BIT)) {
                        getFromHistory(true);
                        e.doit = false;
                    } else if ((e.keyCode & SWT.KEY_MASK) == (SWT.ARROW_DOWN | SWT.KEYCODE_BIT)) {
                        getFromHistory(false);
                        e.doit = false;
                    }
                }
                else if ((e.stateMask & SWT.CONTROL) != 0) {
                	if ((e.keyCode & SWT.KEY_MASK) == (SWT.ARROW_UP | SWT.KEYCODE_BIT)) {
                        e.doit = false;
                    } else if ((e.keyCode & SWT.KEY_MASK) == (SWT.ARROW_DOWN | SWT.KEYCODE_BIT)) {
                    	showHistoryMenu();
                        e.doit = false;
                    }
                }
            }
        });

        searchText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String s = searchText.getText();
                boolean en = s != null && !s.trim().isEmpty();
                bNext.setEnabled(en);
                bPrev.setEnabled(en);
                findIncremental();
            }
        });

        IFocusService focusService = getService(IFocusService.class);
        if (focusService != null) {
            focusService.addFocusTracker(searchText, FOCUS_ID);
        }
    
    }
	
    private void activateContext() {
        IContextService contextService = getService(IContextService.class);
        if (contextService != null) {
            contextActivation = contextService.activateContext(CONTEXT_ID);
        }
    }

    private void deactivateContext() {
        if (contextActivation != null) {
            IContextService contextService = getService(IContextService.class);
            if (contextService != null) {
                contextService.deactivateContext(contextActivation);
            }
            contextActivation = null;
        }
    }
	
    private void setFocusToTargetControl() {
        boolean canBePerformed = (targetFocusControl != null)
                              && targetFocusControl.isEnabled()
                              && targetFocusControl.isVisible();
        if (canBePerformed) {
            targetFocusControl.setFocus();
        }
    }

    
    /**
     * Navigates to the start position of the searched text 
     */
    public void gotoStartLine() {
        searchText.setSelection(0);
    }
    
    /**
     * Navigates to the end position of the searched text 
     */
    public void gotoEndLine() {
        searchText.setSelection(searchText.getCharCount());
    }

    
    /**
     * Selects searched text from current position to the start position 
     */
    public void selectLineStart() {
        searchText.setSelection(0, searchText.getCaretPosition());
    }
    
    /**
     * Selects searched text from current position to the end position 
     */
    public void selectLineEnd() {
        searchText.setSelection(searchText.getCaretPosition(), searchText.getCharCount());
    }

    
    public void hidePanel() {
        ((GridData) getLayoutData()).exclude = true;
        setVisible(false);
        getParent().layout(true);
        setFocusToTargetControl();
    }
	
    public void showPanel() {
        String text = target.getSelectionText();
    	if (!text.isEmpty()) {
            searchText.setText(text);
            searchText.setSelection(0, text.length());
            toHistory(text);
    	}
    	else {
    		if (!history.isEmpty()) {
    			searchText.setText(history.get(0));
    		}
    		searchText.setSelection(0, searchText.getText().length());
    	}

        if (!searchText.isVisible()) {
            ((GridData) getLayoutData()).exclude = false;
            setVisible(true);

            getParent().layout(true);

            targetFocusControl = Display.getCurrent().getFocusControl();
        }
        
        searchText.setFocus();
    }

    protected ToolBar createToolBar(final Composite parent) {
        toolBar = new ToolBar(parent, SWT.FLAT);
        GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(toolBar);
        createSearchHistoryMenu(toolBar);
        createNextItem(toolBar);
        createPreviousItem(toolBar);
        createSettingsMenu(toolBar);
        createClose(toolBar);
        return toolBar;
    }

    protected ToolItem createSearchHistoryMenu(final ToolBar bar) {
        bHistory = new ToolItem(bar, SWT.PUSH);
        bHistory.setImage(ImageUtils.getImage(ImageUtils.FIND_HISTORY));
        bHistory.setToolTipText(Messages.XFindPanel_ShowHistory_tooltip);
        bHistory.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                showHistoryMenu();
            }
        });
        bHistory.setEnabled(!history.isEmpty());
        return bHistory;
    }

    protected ToolItem createNextItem(final ToolBar bar) {
        bNext = createTool(bar, Messages.XFindPanel_FindNext_tooltip,
                ImageUtils.FIND_NEXT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(final SelectionEvent e) {
                        findNext();
                    }
                });
        bNext.setEnabled(false);
        return bNext;
    }

    protected ToolItem createPreviousItem(final ToolBar bar) {
        bPrev = createTool(bar, Messages.XFindPanel_FindPrevious_tooltip,
                ImageUtils.FIND_PREV, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(final SelectionEvent e) {
                        findPrevious();
                    }
                });
        bPrev.setEnabled(false);
        return bPrev;
    }

    protected ToolItem createSettingsMenu(final ToolBar bar) {
        bSettings = new ToolItem(bar, SWT.PUSH);
        bSettings.setImage(JFaceResources.getImage(PopupDialog.POPUP_IMG_MENU));
        bSettings.setDisabledImage(JFaceResources.getImage(PopupDialog.POPUP_IMG_MENU_DISABLED));
        bSettings.setToolTipText(Messages.XFindPanel_ShowSettings_tooltip); //$NON-NLS-1$
        bSettings.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                showSettings();
            }
        });
        return bSettings;
    }

    protected ToolItem createClose(final ToolBar bar) {
        final ToolItem close = new ToolItem(bar, SWT.PUSH);
        final ImageDescriptor image = PlatformUI.getWorkbench()
                .getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
        if (image != null)
            close.setImage(image.createImage());
        close.setToolTipText(Messages.XFindPanel_Close_tooltip); //$NON-NLS-1$
        close.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                hidePanel();
            }
        });
        return close;
    }

    private ToolItem createTool( final ToolBar bar, final String tip
                               , final String image, final SelectionAdapter listener) 
    {
        final ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setToolTipText(tip);
        item.setImage(ImageUtils.getImage(image));
        item.addSelectionListener(listener);
        return item;
    }

    public void showSettings() {
        final MenuManager manager = new MenuManager();
        fillSettingsMenu(manager);
        final Menu menu = manager.createContextMenu(toolBar);

//        menu.setLocation(toolBar.getDisplay().getCursorLocation());
        menu.setLocation(getToolItemLocation(bSettings));
        
        menu.setVisible(true);
    }

    protected void fillSettingsMenu(final IMenuManager menu) {
        menu.add(new Separator());
        newCheckAction( menu, FIND_CASE_SENSITIVE, Messages.XFindPanel_Settings_MatchCase_label
                      , true );
        newCheckAction( menu, FIND_WHOLE_WORD, Messages.XFindPanel_Settings_WholeWord_label
                      , isWholeWordSearchEnabled(searchText.getText()) );
        newCheckAction( menu, FIND_REGEXP, Messages.XFindPanel_Settings_RegExpr_label
                      , isRegExSupported );
        newCheckAction( menu, FIND_INCREMENTAL, Messages.XFindPanel_Settings_Incremental
                      , isIncrementalSearchEnabled() );

        menu.add(new Separator());
        menu.add(new Action(Messages.XFindPanel_Settings_ClearHistory_label) {
            @Override
            public void run() {
                clearHistory();
            }
        });
        menu.add(new Separator());
        menu.add(new Action(Messages.XFindPanel_Settings_ShowPreferences_label) {
            @Override
            public void run() {
                PreferencesUtil.createPreferenceDialogOn(toolBar.getShell(),
                        PREFERENCE_PAGE_ID, null, null).open();
            }
        });
    }

    private CheckAction newCheckAction( IMenuManager menu, String name
                                      , String label, boolean enable ) 
    {
        final CheckAction action = new CheckAction(name, label, enable);
        action.setEnabled(enable);
        menu.add(action);
        return action;
    }

    public IPreferenceStore getStore() {
        return XFindPlugin.getDefault().getPreferenceStore();
    }

    public class CheckAction extends Action {

        public CheckAction(String name, String label, boolean enable) {
            super(label, AS_CHECK_BOX);
            this.name = name;
            setChecked(enable && getStore().getBoolean(name));
        }

        @Override
        public void run() {
            getStore().setValue(name, isChecked());
        }

        private String name;
    }

    //--------------------------------------------------------------------------
	// Search History
    //--------------------------------------------------------------------------

    private void toHistory(String s) {
        int idx = history.indexOf(s);
        if (idx >= 0) {
            history.remove(idx);
        }
        history.add(0, s);
        while (history.size() > HISTORY_MAX_DEEP) {
            history.remove(HISTORY_MAX_DEEP);
        }
        bHistory.setEnabled(true);
        fromHistoryPos = 0;
    }

    private void clearHistory() {
        history.clear();
        bHistory.setEnabled(false);
        fromHistoryPos = 0;
    }

    private void getFromHistory(boolean up) {
        if (!history.isEmpty()) {
            fromHistoryPos += up ? -1 : 1;
            fromHistoryPos = Math.max(0, fromHistoryPos);
            fromHistoryPos = Math.min(fromHistoryPos, history.size() - 1);
            String s = history.get(fromHistoryPos);
            searchText.setText(s);
            searchText.setSelection(s.length(), s.length());
        }
    }

    public void showHistoryMenu() {
        if (bHistory.isEnabled()) {
            final MenuManager manager = new MenuManager();
            fillHistoryMenu(manager);
            final Menu menu = manager.createContextMenu(toolBar);

//          menu.setLocation(toolBar.getDisplay().getCursorLocation());
            menu.setLocation(getToolItemLocation(bHistory));

            menu.setVisible(true);
        }
    }

    protected void fillHistoryMenu(final IMenuManager menu) {
        for (int i = 0; i < history.size(); ++i) {
            menu.add(new HistoryAction(i));
        }
        fromHistoryPos = 0;
    }

    private class HistoryAction extends Action {
        private int idx;

        public HistoryAction(int idx) {
            super(history.get(idx));
            this.idx = idx;
        }

        @Override
        public void run() {
            String item = history.get(idx);
            searchText.setText(item);
            toHistory(item);
        }
    }
	
    private void setStatusLine(boolean clean) {
        if (clean) {
            statusLine.cleanStatusLine();
        } else {
            statusLine.showMessage(Messages.XFindPanel_Status_NotFound, ImageUtils.getImage(ImageUtils.FIND_STATUS));
        }
    }
	
    //--------------------------------------------------------------------------
    // Search actions
    //--------------------------------------------------------------------------
    enum SearchMode {
        Next, Prev, NextNoSkip, FromBeginning
    };

    public void findPrevious() {
        setStatusLine(true);
        if (!performSearch(SearchMode.Prev)) {
            setStatusLine(false);
        }
    }

    public void findNext() {
        setStatusLine(true);
        if (!performSearch(SearchMode.Next)) {
            setStatusLine(false);
        }
    }

    private void findIncremental() {
        setStatusLine(true);
        if (getStore().getBoolean(FIND_INCREMENTAL)) {
            if (performSearch(SearchMode.NextNoSkip) || performSearch(SearchMode.FromBeginning)) 
            {
                searchText.setBackground(bgColorOk);
            } else {
                searchText.setBackground(bgColorNotFound);
                setStatusLine(false);
            }
        }
    }

    private boolean performSearch(SearchMode mode) {
        String findString = searchText.getText();
        if (findString == null || findString.isEmpty()) {
            return true;
        }
        toHistory(findString);

        boolean caseSensitive = isIncrementalSearch();
        boolean wholeWord     = isWholeWordSearch(findString);
        boolean regExpSearch  = isRegExSearch();

        Point r = target.getSelection();
        int searchPosition = r.x;
        switch (mode) {
        case Next:
            searchPosition += r.y;
            break;
        case Prev:
            searchPosition--;
            break;
        case FromBeginning:
            searchPosition = 0;
            break;
		default:
			break;
        }
        searchPosition = Math.max(searchPosition, 0);
        
        int offset = findAndSelect( searchPosition, findString
                                  , mode != SearchMode.Prev
                                  , caseSensitive, wholeWord, regExpSearch );

        return (offset >= 0);
    }

    
    /**
     * Returns <code>true</code> if searching should be restricted to entire
     * words, <code>false</code> if not. This is the case if the respective
     * preference is turned on, regex is off, and the operation is enabled, i.e.
     * the current find string is an entire word.
     *
     * @param findString the string which should be found

     * @return <code>true</code> if the search is restricted to whole words
     */
    private boolean isWholeWordSearch(String findString) {
        return getStore().getBoolean(FIND_WHOLE_WORD) 
            && isWholeWordSearchEnabled(findString);
    }
    
    /**
     * Returns <code>true</code> if searching of entire words is enabled, 
     * <code>false</code> if not. This is the case regex is off, and  
     * the current find string is an entire word.
     *
     * @param findString the string which should be found

     * @return <code>true</code> if the search of whole words is enabled
     */
    private boolean isWholeWordSearchEnabled(String findString) {
        return (XFindUtils.isWord(findString) || (findString == null) || (findString.length() == 0))
            && !isRegExSearch();
    }
    
    /**
     * Retrieves and returns the option incremental search from the appropriate property.
     * @return <code>true</code> if incremental search
     */
    private boolean isIncrementalSearch() {
        return getStore().getBoolean(FIND_CASE_SENSITIVE)
            && isIncrementalSearchEnabled();   
    }

    /**
     * Retrieves and returns the option incremental search from the appropriate check box.
     * @return <code>true</code> if incremental search
     * @since 2.0
     */
    private boolean isIncrementalSearchEnabled() {
        return !isRegExSearch();
    }
    
    /**
     * Retrieves and returns the regEx option from the appropriate check box.
     *
     * @return <code>true</code> if case sensitive
     * @since 3.0
     */
    private boolean isRegExSearch() {
        return isRegExSupported && getStore().getBoolean(FIND_REGEXP);
    }
    
    /**
     * Searches for a string starting at the given offset and using the specified search
     * directives. If a string has been found it is selected and its start offset is
     * returned.
     *
     * @param offset the offset at which searching starts
     * @param findString the string which should be found
     * @param forwardSearch the direction of the search
     * @param caseSensitive <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
     * @param wholeWord if <code>true</code> only occurrences are reported in which the findString stands as a word by itself
     * @param regExSearch if <code>true</code> findString represents a regular expression
     * @return the position of the specified string, or -1 if the string has not been found
     */
    private int findAndSelect( int offset, String findString, boolean forwardSearch
                             , boolean caseSensitive, boolean wholeWord, boolean regExSearch ) 
    {
        try {
            if (target instanceof IFindReplaceTargetExtension3) {
                return ((IFindReplaceTargetExtension3)target).findAndSelect(
                    offset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch
                );
            }
            return target.findAndSelect(
                offset, findString, forwardSearch, caseSensitive, wholeWord
            );
        } catch (Exception e){
            return -1;
        }
    }

    
    private Point getToolItemLocation(ToolItem item) {
        Rectangle rect = item.getBounds();
        Point point = new Point(rect.x, rect.y + rect.height);
        point = toolBar.toDisplay(point);
        if (Util.isMac()) {
            point.y += 5;
        }
        return point;
    }

    /**
     * Retrieves the service corresponding to the given API.
     * 
     * @param api This is the interface that the service implements. 
     *        Must not be <code>null</code>.
     *        
     * @return The service, or <code>null</code> if no such service could be found.
     */
    @SuppressWarnings("unchecked")
    public static final <T> T getService(Class<T> api) {
        return (T) PlatformUI.getWorkbench().getService(api);
    }
    
}
