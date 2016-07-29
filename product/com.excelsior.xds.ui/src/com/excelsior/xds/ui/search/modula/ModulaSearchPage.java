package com.excelsior.xds.ui.search.modula;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.excelsior.xds.core.search.modula.ModulaSearchInput;
import com.excelsior.xds.ui.internal.nls.Messages;


public class ModulaSearchPage extends DialogPage implements ISearchPage 
{
    public static final String PAGE_ID = "com.excelsior.xds.ui.ModulaSearchPage";   //$NON-NLS-1$

    private Button caseSensitive;
    private ISearchPageContainer container;
    private boolean firstTime = true;
    private ArrayList<Button> limitToButtons;
    private Combo patternCombo;
    private ArrayList<Button> searchForButtons;
    private ArrayList<Button> searchInCheckboxes;
    private volatile int ignoreSelectionEvents = 0;


    private static ArrayList<QueryData> previousQueries = new ArrayList<QueryData>();

    @Override
    public void createControl(Composite parent) {
        Composite result = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        result.setLayout(layout);
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        createPatternSection(result);
        createSearchForAndLimitTo(result);
        createSearchInCheckboxes(result);

        hookListeners();

        setControl(result);
        Dialog.applyDialogFont(result);
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(result, IHelpContextIds.SEARCH_PAGE);
    }

    private ArrayList<Button> createGroup(Composite parent, int columns, String groupLabel, 
            String[] buttonLabels, int[] buttonData, int defaultSelected) 
    {
        ArrayList<Button> buttons = new ArrayList<Button>();
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(columns, true));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        group.setText(groupLabel);
        for (int i = 0; i < buttonLabels.length; i++) {
            if (buttonLabels[i] != null) {
                Button b = new Button(group, SWT.RADIO);
                b.setData((Integer)buttonData[i]);
                b.setText(buttonLabels[i]);
                b.setSelection(i == defaultSelected);
                buttons.add(b);
            } else {
                new Label(group, SWT.NORMAL); // empty place
            }
        }
        return buttons;
    }

    private Button createButton(Composite parent, int style, String text, int data, boolean isSelected) {
        Button button= new Button(parent, style);
        button.setText(text);
        button.setData(new Integer(data));
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        button.setSelection(isSelected);
        return button;
    }

    
    private void createPatternSection(Composite parent) {
        Composite result = new Composite(parent, SWT.NONE);
        result.setLayout(new GridLayout(2, false));
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(result, SWT.NONE);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        label.setLayoutData(data);
        label.setText(Messages.ModulaSearchPage_SearchString);

        patternCombo = new Combo(result, SWT.SINGLE | SWT.BORDER);
        patternCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        caseSensitive = new Button(result, SWT.CHECK);
        caseSensitive.setText(Messages.ModulaSearchPage_CaseSensitive);
    }

    private void createSearchForAndLimitTo(Composite parent) {
        Composite result = new Composite(parent, SWT.NONE);
        result.setLayout(new GridLayout(2, true));
        result.setLayoutData(new GridData(GridData.FILL_BOTH));

        searchForButtons = createGroup(result, 3, Messages.ModulaSearchPage_SearchFor, 
                new String[] {
                    Messages.ModulaSearchPage_Procedure, 
                    Messages.ModulaSearchPage_Variable, 
                    Messages.ModulaSearchPage_Type, 
                    Messages.ModulaSearchPage_Field, 
                    Messages.ModulaSearchPage_Constant, 
                    Messages.ModulaSearchPage_Module, 
                    Messages.ModulaSearchPage_AnyElement},
                new int[] {
                    ModulaSearchInput.SEARCH_FOR_PROCEDURE,
                    ModulaSearchInput.SEARCH_FOR_VARIABLE,
                    ModulaSearchInput.SEARCH_FOR_TYPE,
                    ModulaSearchInput.SEARCH_FOR_FIELD,
                    ModulaSearchInput.SEARCH_FOR_CONSTANT,
                    ModulaSearchInput.SEARCH_FOR_MODULE,
                    ModulaSearchInput.SEARCH_FOR_ANY_ELEMENT},
                6);
        
        limitToButtons = createGroup(result, 1, Messages.ModulaSearchPage_LimitTo, 
                new String[] {
                    Messages.ModulaSearchPage_Declarations,
                    Messages.ModulaSearchPage_Usages, 
                    Messages.ModulaSearchPage_AllOccurencies}, 
                new int[] {
                    ModulaSearchInput.LIMIT_TO_DECLARATIONS,
                    ModulaSearchInput.LIMIT_TO_USAGES,
                    ModulaSearchInput.LIMIT_TO_ALL_OCCURENCES},
                2);
    }

    
    private void createSearchInCheckboxes(Composite parent) {
        Group result= new Group(parent, SWT.NONE);
        result.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        result.setText(Messages.ModulaSearchPage_SearchIn);
        result.setLayout(new GridLayout(3, false));
        final Button cbCompset = createButton(result, SWT.CHECK, Messages.ModulaSearchPage_CompilationSet, ModulaSearchInput.SEARCH_IN_COMP_SET, false); 
        final Button cbAllSrc  = createButton(result, SWT.CHECK, Messages.ModulaSearchPage_AllSources, ModulaSearchInput.SEARCH_IN_ALL_SOURCES, true);
        final Button cbSdkLib  = createButton(result, SWT.CHECK, Messages.ModulaSearchPage_SdkLibraries, ModulaSearchInput.SEARCH_IN_SDK_LIBRARIES, true);

        searchInCheckboxes = new ArrayList<Button>();
        searchInCheckboxes.add(cbCompset);
        searchInCheckboxes.add(cbAllSrc);
        searchInCheckboxes.add(cbSdkLib);

        SelectionAdapter listener= new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (ignoreSelectionEvents > 0) return;
                // only 0 or 1 checkbox may be checked:
                Button unsel = null;
                if (e.widget == cbCompset && cbCompset.getSelection()) {
                    unsel = cbAllSrc;
                } else if (e.widget == cbAllSrc && cbAllSrc.getSelection()) {
                    unsel = cbCompset;
                }
                if (unsel != null) {
                    ++ignoreSelectionEvents;
                    unsel.setSelection(false);
                    --ignoreSelectionEvents;
                }

                updateOKStatus();
            }
        };
        for (Button cb : searchInCheckboxes) {
            cb.addSelectionListener(listener);
        }
    }

    final void updateOKStatus() {
        boolean ok1 = getSelectedBtns(searchInCheckboxes, true) != 0;
        boolean ok2 = patternCombo.getText().trim().length() > 0; 
        container.setPerformActionEnabled(ok1 && ok2);
    }

    private void hookListeners() {
        patternCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int index = previousQueries.size() - patternCombo.getSelectionIndex() - 1;
                if (previousQueries.size() > index) {
                    QueryData data = (QueryData) previousQueries.get(index);
                    resetPage(data);
                }
                updateOKStatus();
            }
        });

        patternCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateOKStatus();
            }
        });
    }


    public void setVisible(boolean visible) {
        if (visible && patternCombo != null) {
            if (firstTime) {
                firstTime = false;
                String[] patterns = new String[previousQueries.size()];
                for (int i = previousQueries.size() - 1, j = 0; i >= 0; i--, j++) {
                    patterns[j] = ((QueryData) previousQueries.get(i)).text;
                }
                patternCombo.setItems(patterns);
                String sel = ""; //$NON-NLS-1$
                ISelection selection = container.getSelection();
                if (selection instanceof TextSelection) {
                    sel = ((TextSelection) selection).getText().trim();
                    // get ident if any
                    int i = 0;
                    for (; i<100 && i<sel.length(); ++i) {
                        char ch = Character.toUpperCase(sel.charAt(i));
                        if ((ch>='A' && ch<='Z') || ch == '_') {
                            continue;
                        }
                        if ((ch>='0' && ch<='9') && i>0) {
                            continue;
                        }
                        break;
                    }
                    sel = sel.substring(0, i);
                }
                if (!sel.isEmpty()) {
                    container.setSelectedScope(ISearchPageContainer.SELECTED_PROJECTS_SCOPE);
                    patternCombo.setText(sel);
                } else {
                    if (previousQueries.size() > 0) {
                        patternCombo.select(0);
                        resetPage(previousQueries.get(previousQueries.size()-1));
                    } else {
                        container.setSelectedScope(ISearchPageContainer.SELECTED_PROJECTS_SCOPE);
                    }
                    updateOKStatus();
                }
                container.setPerformActionEnabled(patternCombo.getText().length() > 0);
            }
            patternCombo.setFocus();
        }

        IEditorInput editorInput = container.getActiveEditorInput();
        container.setActiveEditorCanProvideScopeSelection(editorInput != null && editorInput.getAdapter(IFile.class) != null);

        super.setVisible(visible);
    }
    
    private void resetPage(QueryData data) {
        caseSensitive.setSelection(data.isCaseSensitive);
        setSelectedBtns(data.searchFor, searchForButtons, false);
        setSelectedBtns(data.limitTo, limitToButtons, false);
        setSelectedBtns(data.searchInFlags, searchInCheckboxes, true);

        container.setSelectedScope(data.workspaceScope);
        if (data.workingSets != null) {
            container.setSelectedWorkingSets(data.workingSets);
        }
    }

    
    
    @Override
    public boolean performAction() {
        saveQueryData();
        NewSearchUI.activateSearchResultView();
        NewSearchUI.runQueryInBackground(new ModulaSearchQuery(getInput()));
        return true;
     }
    
    private void saveQueryData() {
        QueryData data = new QueryData();
        data.text = patternCombo.getText();
        data.isCaseSensitive = caseSensitive.getSelection();
        data.searchFor = getSelectedBtns(searchForButtons, false);
        data.limitTo = getSelectedBtns(limitToButtons, false);
        data.searchInFlags = getSelectedBtns(searchInCheckboxes, true);
        data.workspaceScope = container.getSelectedScope();
        data.workingSets = container.getSelectedWorkingSets();

        if (previousQueries.contains(data))
            previousQueries.remove(data);

        previousQueries.add(data);
        if (previousQueries.size() > 10)
            previousQueries.remove(0);
    }


    private ModulaSearchInput getInput() {
        ModulaSearchInput input = new ModulaSearchInput(null); // we cannot know what project is the target project of the search
        input.setSearchFor(getSelectedBtns(searchForButtons, false));
        input.setLimitTo(getSelectedBtns(limitToButtons, false));
        input.setSearchInFlags(getSelectedBtns(searchInCheckboxes, true));

        String searchString = patternCombo.getText().trim();
        input.setSearchScope(createTextSearchScope());
        input.setSearchScopeId(container.getSelectedScope());
        input.setSearchString(searchString);
        input.setCaseSensitive(caseSensitive.getSelection());
        return input;
    }
    
    public FileTextSearchScope createTextSearchScope() {
        // Setup search scope
        switch (container.getSelectedScope()) {
            case ISearchPageContainer.WORKSPACE_SCOPE:
                return FileTextSearchScope.newWorkspaceScope(new String[]{"*"}, false); //$NON-NLS-1$
            case ISearchPageContainer.SELECTION_SCOPE:
                return getSelectedResourcesScope();
            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                return getEnclosingProjectScope();
            case ISearchPageContainer.WORKING_SET_SCOPE:
                IWorkingSet[] workingSets= container.getSelectedWorkingSets();
                return FileTextSearchScope.newSearchScope(workingSets, new String[]{"*"}, false); //$NON-NLS-1$
            default:
                // unknown scope
                return FileTextSearchScope.newWorkspaceScope(new String[]{"*"}, false); //$NON-NLS-1$
        }
    }
    
    private FileTextSearchScope getSelectedResourcesScope() {
        HashSet<IResource> resources= new HashSet<IResource>();
        ISelection sel= container.getSelection();
        if (sel instanceof IStructuredSelection && !sel.isEmpty()) {
            @SuppressWarnings("rawtypes")
            Iterator iter= ((IStructuredSelection) sel).iterator();
            while (iter.hasNext()) {
                Object curr= iter.next();
                if (curr instanceof IWorkingSet) {
                    IWorkingSet workingSet= (IWorkingSet) curr;
                    if (workingSet.isAggregateWorkingSet() && workingSet.isEmpty()) {
                        return FileTextSearchScope.newWorkspaceScope(new String[]{"*"}, false); //$NON-NLS-1$
                    }
                    IAdaptable[] elements= workingSet.getElements();
                    for (int i= 0; i < elements.length; i++) {
                        IResource resource= (IResource)elements[i].getAdapter(IResource.class);
                        if (resource != null && resource.isAccessible()) {
                            resources.add(resource);
                        }
                    }
                } else if (curr instanceof IAdaptable) {
                    IResource resource= (IResource) ((IAdaptable)curr).getAdapter(IResource.class);
                    if (resource != null && resource.isAccessible()) {
                        resources.add(resource);
                    }
                }
            }
        } else {
            // Can't use container.getActiveEditorInput() - it always returns null when dailog is shown. So:
            try {    
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IEditorPart activeEditor = activePage.getActiveEditor();
                if (activeEditor instanceof MultiPageEditorPart) {
                    Object page= ((MultiPageEditorPart)activeEditor).getSelectedPage();
                    if (page instanceof IEditorPart) {
                        activeEditor= (IEditorPart)page;
                    } else {
                        activeEditor = null;
                    }
                }
                resources.add((IFile)activeEditor.getEditorInput().getAdapter(IFile.class));
            } catch (Exception e) {}
        }
        IResource[] arr= (IResource[]) resources.toArray(new IResource[resources.size()]);
        return FileTextSearchScope.newSearchScope(arr, new String[]{"*"}, false); //$NON-NLS-1$
    }

    private FileTextSearchScope getEnclosingProjectScope() {
        String[] enclosingProjectName= container.getSelectedProjectNames();
        if (enclosingProjectName == null) {
            return FileTextSearchScope.newWorkspaceScope(new String[]{"*"}, false); //$NON-NLS-1$
        }

        IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
        IResource[] res= new IResource[enclosingProjectName.length];
        for (int i= 0; i < res.length; i++) {
            res[i]= root.getProject(enclosingProjectName[i]);
        }

        return FileTextSearchScope.newSearchScope(res, new String[]{"*"}, false); //$NON-NLS-1$
    }


    private int getSelectedBtns(ArrayList<Button> btns, boolean combineBits) {
        int res = 0;
        for (Button btn : btns) {
            if (btn.getSelection()) {
                res |= (Integer)btn.getData();
                if (!combineBits) {
                    return res;
                }
            }
        }
        return res;
    }
    
    private void setSelectedBtns(int value, ArrayList<Button> btns, boolean combineBits) {
        ++ignoreSelectionEvents;
        for (Button btn : btns) {
            int d = (Integer)btn.getData();
            boolean sel = combineBits ? ( (d & value) != 0) : d == value;
            btn.setSelection(sel);
        }
        --ignoreSelectionEvents;
    }

    @Override
    public void setContainer(ISearchPageContainer container) {
        this.container= container;
    }

    private class QueryData {
        public String text;
        public boolean isCaseSensitive;
        public int searchFor;
        public int limitTo;
        public int searchInFlags;
        public int workspaceScope;
        public IWorkingSet[] workingSets;

        public boolean equals(Object obj) {
            if (obj instanceof QueryData) {
                if (((QueryData) obj).text.equals(text))
                    return true;
            }
            return false;
        }

    }

}
