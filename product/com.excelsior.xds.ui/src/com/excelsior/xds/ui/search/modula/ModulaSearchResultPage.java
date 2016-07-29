// see package org.eclipse.pde.internal.ui.search.PluginSearchResultPage
package com.excelsior.xds.ui.search.modula;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsExternalDependenciesContainer;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsSdkLibraryContainer;
import com.excelsior.xds.core.search.modula.ModulaSymbolMatch;
import com.excelsior.xds.core.utils.IClosure;
import com.excelsior.xds.ui.XdsPlugin;
import com.excelsior.xds.ui.actions.ToolbarActionButton;
import com.excelsior.xds.ui.commons.swt.resources.ResourceRegistry;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.search.AbstractSearchResultTreePage;
import com.excelsior.xds.ui.search.ResultModel.ModelItem;

public class ModulaSearchResultPage extends AbstractSearchResultTreePage 
{
    private static final String DLG_ID = "com.excelsior.xds.ui.ModulaSearchResultPage_ID";   //$NON-NLS-1$ 
    private boolean sortAlph = true;
    
    private ToolbarActionButton tbaGroupByProject;
    private ToolbarActionButton tbaGroupByFile;
    
    private final ResourceRegistry resourceRegistry = new ResourceRegistry();
    
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        ToolbarActionButton tbaSortAlph = new ToolbarActionButton(
                Messages.ModulaSearchResultPage_SortAlphabetically, 
                DLG_ID + "DIALOG_SORT_ALPHABETICALLY", true,      //$NON-NLS-1$
                ImageUtils.SORT_ALPHA, false, getDialogSettings(),  
                new IClosure<Boolean>(){   
                    @Override
                    public void execute(Boolean isChecked) {
                        reSort(isChecked);
                    }
                });
        this.getSite().getActionBars().getToolBarManager().add(tbaSortAlph);
        sortAlph = tbaSortAlph.isChecked();

        tbaGroupByFile = new ToolbarActionButton(
                Messages.ModulaSearchResultPage_GroupByFile, 
                DLG_ID + "DIALOG_GROUP_BY_FILE_", true,      //$NON-NLS-1$
                ImageUtils.GROUP_BY_FILE, false, getDialogSettings(),  
                new IClosure<Boolean>(){   
                    @Override
                    public void execute(Boolean isChecked) {
                        setGroupByProject(!isChecked);
                    }
                });
        
        tbaGroupByProject = new ToolbarActionButton(
                Messages.ModulaSearchResultPage_GroupByProject, 
                DLG_ID + "DIALOG_GROUP_BY_PROJECT_", true,      //$NON-NLS-1$
                ImageUtils.GROUP_BY_PROJECT, !tbaGroupByFile.isChecked(), null,  
                new IClosure<Boolean>(){   
                    @Override
                    public void execute(Boolean isChecked) {
                        setGroupByProject(isChecked);
                    }
                });

        this.getSite().getActionBars().getToolBarManager().appendToGroup("group.properties", tbaGroupByProject);  //$NON-NLS-1$
        this.getSite().getActionBars().getToolBarManager().appendToGroup("group.properties", tbaGroupByFile);  //$NON-NLS-1$
        setGroupByProject(!tbaGroupByFile.isChecked());
    }
    
    @Override
    protected void setGroupByProject(boolean on) {
        tbaGroupByFile.setChecked(!on);
        tbaGroupByProject.setChecked(on);
        super.setGroupByProject(on);
    }
    
    private void reSort(boolean sortAlph) {
        this.sortAlph = sortAlph;
        getViewer().refresh();
    }

    private IDialogSettings getDialogSettings() {
        String sectionName= DLG_ID;

        IDialogSettings settings = XdsPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null) {
            settings = XdsPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }
        return settings;
    }


    public ModulaSearchResultPage() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    protected void fillContextMenu(IMenuManager mgr) {
        super.fillContextMenu(mgr);
//        mgr.add(new Separator());
//        IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
//        ActionContext context = new ActionContext(selection);
//        PluginSearchActionGroup actionGroup = new PluginSearchActionGroup();
//        actionGroup.setContext(context);
//        actionGroup.fillContextMenu(mgr);
//        if (ImportActionGroup.canImport(selection)) {
//            mgr.add(new Separator());
//            ImportActionGroup importActionGroup = new ImportActionGroup();
//            importActionGroup.setContext(context);
//            importActionGroup.fillContextMenu(mgr);
//        }
//        mgr.add(new Separator());
//
//        JavaSearchActionGroup jsActionGroup = new JavaSearchActionGroup();
//        jsActionGroup.setContext(new ActionContext(selection));
//        jsActionGroup.fillContextMenu(mgr);
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.pde.internal.ui.search.AbstractSearchResultPage#createLabelProvider()
     */
    protected IBaseLabelProvider createLabelProvider() {
        return new ModulaSearchLabelProvider(this, resourceRegistry);
    }


    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#showMatch(org.eclipse.search.ui.text.Match, int, int, boolean)
     */
    protected void showMatch( Match match, int currentOffset
                            , int currentLength, boolean activate 
                            ) throws PartInitException 
    {
        if (match instanceof ModulaSymbolMatch) {
            try {
                ModulaSymbolMatch em = (ModulaSymbolMatch)match;
                IFile f = em.getFile();
                IWorkbenchPage page = WorkbenchUtils.getActivePage();
                IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(f.getName());
                IEditorPart ep = page.openEditor(new FileEditorInput(f), desc.getId());
                ITextEditor te = (ITextEditor)ep;
                Control ctr = (Control)te.getAdapter(Control.class);
                ctr.setFocus();
                te.selectAndReveal(em.getOffset(), em.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#dispose()
     */
    public void dispose() {
        super.dispose();
        resourceRegistry.dispose();
    }

	@Override
	protected ViewerComparator createViewerComparator() {
		return new ResultPageComparator();
	}
	
	private class ResultPageComparator extends ViewerComparator 
	{

		public boolean isSortAlphabetically() {
			return sortAlph;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			try {
			    if (e1 instanceof ModulaSymbolMatch && e2 instanceof ModulaSymbolMatch) {
    				ModulaSymbolMatch t1 = (ModulaSymbolMatch) e1;
    				ModulaSymbolMatch t2 = (ModulaSymbolMatch) e2;
    				if (isSortAlphabetically()) {
                        String s1 = t1.getSymbol().getName();
                        String s2 = t2.getSymbol().getName();
    					return s1.toUpperCase().compareTo(s2.toUpperCase());
    				} else {
    					int pos1 = t1.getOffset();
    					int pos2 = t2.getOffset();
    					return pos1 - pos2;
    				}
			    } else {
			        String s1, s2;
			        if (e1 instanceof IResource && e2 instanceof IResource) {
                        s1 = ((IResource)e1).getName();
                        s2 = ((IResource)e2).getName();
			        } else if (e1 instanceof ModelItem && e2 instanceof ModelItem) {
                        s1 = getMIName((ModelItem)e1);
                        s2 = getMIName((ModelItem)e2);
			        } else {
	                    s1 = e1.toString();
	                    s2 = e2.toString();
			        }
                    return s1.toUpperCase().compareTo(s2.toUpperCase());
			    }
			} catch (Exception e) {
			}
			return 0;
		}
		
		private String getMIName(ModelItem mi) {
		    IFile iFile= mi.getIFile();
            IXdsElement ixe = mi.getIXdsElement();
            String res = "9";
            if (ixe != null) {
                if (ixe instanceof IXdsProject) {
                    res = "1";
                } else if (ixe instanceof IXdsExternalDependenciesContainer) {
                    res = "2";
                } else if (ixe instanceof IXdsSdkLibraryContainer) {
                    res = "3";
                } else if (ixe instanceof IXdsContainer) {
                    res = "4";
                }
            }
            
		    if (iFile != null) {
		        res += iFile.getName();
		    }
		    if (ixe != null) {
		        res += ixe.getElementName();
		    }
		    return res;
		}
	}
	
}
