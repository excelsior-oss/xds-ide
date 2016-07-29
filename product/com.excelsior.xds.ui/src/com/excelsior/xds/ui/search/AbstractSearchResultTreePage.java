//see package org.eclipse.pde.internal.ui.search.AbstractSearchResultPage

package com.excelsior.xds.ui.search;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

import com.excelsior.xds.ui.search.ResultModel.ModelItem;

public abstract class AbstractSearchResultTreePage extends AbstractTextSearchViewPage 
{
    private ContentProvider fContentProvider;

    public AbstractSearchResultTreePage() {
        super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
    }
    

    protected abstract IBaseLabelProvider createLabelProvider();

    protected abstract ViewerComparator createViewerComparator();

    
    /* In our case it will be IFile[]
     */
    protected void elementsChanged(Object[] objects) {
        if (fContentProvider != null && fContentProvider.fSearchResult != null)
            fContentProvider.elementsChanged(objects);
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#clear()
     */
    protected void clear() {
        if (fContentProvider != null)
            fContentProvider.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTreeViewer(org.eclipse.jface.viewers.TreeViewer)
     */
    protected void configureTreeViewer(TreeViewer viewer) {
        viewer.setComparator(createViewerComparator());
        viewer.setLabelProvider(createLabelProvider());
        fContentProvider = new ContentProvider();
        viewer.setContentProvider(fContentProvider);
    }

    @Override
    protected TreeViewer createTreeViewer(Composite parent) {
        return new MyTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchViewPage#configureTableViewer(org.eclipse.jface.viewers.TableViewer)
     */
    protected void configureTableViewer(TableViewer viewer) {
        throw new IllegalStateException("Doesn't support table mode."); //$NON-NLS-1$
    }
    
    protected void setGroupByProject(boolean on) {
        fContentProvider.setGroupByProject(on);
    }
    
    @Override
    public Match getCurrentMatch() {
        IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
        if (selection.size() > 0) {
            Object o = selection.getFirstElement();
            if (o instanceof Match) {
                return (Match)o;
            }
        }
        return null;
    }

    @Override
    public void gotoPreviousMatch() {
        gotoPrevNextMatch(false);
    }

    @Override
    public void gotoNextMatch() {
        gotoPrevNextMatch(true);
    }
    
    private void gotoPrevNextMatch(boolean next) {
        MyTreeViewer tv = (MyTreeViewer)getViewer();
        ContentProvider cp = (ContentProvider)tv.getContentProvider();
        Match match = null;
        Object sel = null;
        IStructuredSelection selection = (IStructuredSelection)tv.getSelection();
        if (!selection.isEmpty()) {
            sel = selection.getFirstElement();
            if (sel instanceof Match) {
                // try fast search near 'sel'
                match = findPrevNextMatch(new Object[]{cp.getParent(sel)}, sel, next, tv);
            }
        }
        if (match == null) {
            Object roots[] = cp.getElements(null);
            if (roots == null || roots.length == 0) {
                return; // no items in the tree
            }
            if (sel == null) {
                sel = roots[0];
            }
            match = findPrevNextMatch(roots, sel, next, tv);
        }
        if (match != null) {
            expandParents(match, tv);
            StructuredSelection ss = new StructuredSelection(match);
            tv.setSelection(ss, true);
            showMatch(match, true);
        }
    }
    
    private void showMatch(final Match match, final boolean activateEditor) {
        ISafeRunnable runnable = new ISafeRunnable() {
            public void handleException(Throwable exception) {
                if (exception instanceof PartInitException) {
                    PartInitException pie = (PartInitException) exception;
                    ErrorDialog.openError(getSite().getShell(), "Show match", "Could not find an editor for the current match", pie.getStatus());
                }
            }

            public void run() throws Exception {
                IRegion location= getCurrentMatchLocation(match);
                showMatch(match, location.getOffset(), location.getLength(), activateEditor);
            }
        };
        SafeRunner.run(runnable);
    }

    
    private void expandParents(Object o, TreeViewer tv) {
        if (o != null) {
            expandParents(((ContentProvider)tv.getContentProvider()).getParent(o), tv);
            tv.setExpandedState(o, true);
        }
    }
    
    private Match findPrevNextMatch(Object[] roots, Object sel, boolean next, MyTreeViewer tv) {
        ArrayList<Object> al = new ArrayList<Object>();
        collectContent(al, roots, tv);
        boolean selFound = false;
        Match last = null;
        for (Object o : al) {
            if (o == sel) {
                if (!next) {
                    return last;
                }
                selFound = true;
                continue;
            }
            if (o instanceof Match) {
                last = (Match)o;
                if (selFound) {
                    return last;
                }
            }
        }
        return null;
    }
    
    private void collectContent(ArrayList<Object>dest, Object[]roots, MyTreeViewer tv) {
        if (roots != null) {
            for (Object r : roots) {
                dest.add(r);
                collectContent(dest, tv.getSortedChildren(r), tv);
            }
        }
    }


    class ContentProvider implements ITreeContentProvider 
    {
        private volatile TreeViewer fTreeViewer;
        private AbstractTextSearchResult fSearchResult;
        private boolean showProjectTree = true;
        private ResultModel resultModel = new ResultModel();
        private UIJob refreshTreeJob = new UIJob("Refresh results tree") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (fTreeViewer != null && !fTreeViewer.getTree().isDisposed()) {
                    fTreeViewer.refresh();
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        
        /* 
         * Root elements of the tree
         */
        @Override
        public Object[] getElements(Object inputElement) {
            if (showProjectTree) {
                // Tree will contain ModelElement-s (for IProject, String, IFile) and Match-es
                return stripIFiles(resultModel.getRootElements().toArray());
            } else {
                // Tree will contain IFile-s and Match-es
                return fSearchResult == null ? new Object[0] : fSearchResult.getElements();
            }
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            IFile iFile = null;
            if (parentElement instanceof IFile) {
                iFile = (IFile)parentElement;
            } else if (parentElement instanceof ModelItem ) {
                iFile = (IFile)((ModelItem)parentElement).getIFile();
            }
            
            if (iFile != null) {
                Match[] matches = fSearchResult.getMatches(iFile);
                return matches;
            } else if (parentElement instanceof ModelItem) {
                return stripIFiles(((ModelItem)parentElement).getChildren().toArray());
            }
            return new Object[0];
        }
        
        private Object[] stripIFiles(Object[] arr) {
            for (int i=0; i<arr.length; ++i) {
                Object o = arr[i];
                if (o instanceof ModelItem) {
                    // ModelItems with IFile are replaced with this IFile in the tree -
                    // it allows to use default decorators. (Our tree may show such ModelItem as is,
                    // but it will not be decorated)
                    IFile iFile = ((ModelItem)o).getIFile();
                    if (iFile != null) {
                        arr[i] = iFile;
                    }
                }
            }
            return arr;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
        public void dispose() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            fTreeViewer = (TreeViewer) viewer;
            fSearchResult = (AbstractTextSearchResult) newInput;
            resultModel.clearAll();
            if (fSearchResult != null) {
                for (Object o : fSearchResult.getElements()) {
                    resultModel.addIFile((IFile)o);
                
                }
            }
            fTreeViewer.refresh();
        }

        public void clear() {
            resultModel.clearAll();
            fTreeViewer.getTree().removeAll();
            fTreeViewer.refresh();
        }

        public void elementsChanged(Object[] updatedElements) {
            if (updatedElements.length > 0) {
                // all this elements are IFile-s
                for (Object elt :updatedElements) {
                    if (fSearchResult.getMatchCount(elt) > 0) {
                        ModelItem mi = resultModel.searchModelItemForFile((IFile)elt);
                        if (mi == null) {
                            mi = resultModel.addIFile((IFile)elt);
                        }
                    }
                }
                refreshTreeJob.schedule(300);
            }
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof Match) {
                Object f = ((Match)element).getElement();
                if (f instanceof IFile) {
                    if (showProjectTree) {
                        return resultModel.searchModelItemForFile((IFile)f);
                    } else {
                        return f;
                    }
                }
            } else if (element instanceof ModelItem) {
                return ((ModelItem)element).getParent();
            } else if ((element instanceof IFile)) {
                ModelItem mit = resultModel.searchModelItemForFile((IFile)element);
                if (mit != null) {
                    // it was stripped IFile (see stripIFiles() )
                    return mit.getParent();
                }
            }
            // else - it is IFile with showProjectTree==false => it is root
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return (element instanceof IFile || element instanceof ModelItem);
        }
        
        public void setGroupByProject(boolean on) {
            showProjectTree = on;
            if (fTreeViewer != null) {
                fTreeViewer.refresh();
            }
        }

        // Dirs model
        

    } // class ContentProvider
    
    private static class MyTreeViewer extends TreeViewer {
        public MyTreeViewer(Composite parent, int style) {
            super(parent, style); 
        }
        
        public Object[] getSortedChildren(Object parentElementOrTreePath) {
            return super.getSortedChildren(parentElementOrTreePath);
        }
    }

}
