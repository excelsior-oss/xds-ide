package com.excelsior.xds.ui.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.eclipse.core.resources.IFile;

import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.XdsModelManager;

public class ResultModel {
    private HashMap<IFile, ModelItem> hmIFileToModel = new HashMap<IFile, ModelItem>();
    private HashMap<IXdsElement, ModelItem> hmIXdsToModel = new HashMap<IXdsElement, ModelItem>();
    private HashSet<ModelItem> hsModelRoots = new HashSet<ResultModel.ModelItem>();

    
    public ModelItem addIFile(IFile iFile) {
        IXdsElement xdsEl = XdsModelManager.getModel().getXdsElement(iFile);
        ModelItem mitForFile;
        if (xdsEl == null) {
            // hz. add it to the root of model as IFile
            mitForFile = hmIFileToModel.get(iFile);
            if (mitForFile == null) {
                mitForFile = new ModelItem(null, iFile, null);
                hsModelRoots.add(mitForFile);
            }
        } else {
            IXdsElement xdsElForFile = xdsEl;
            mitForFile = hmIXdsToModel.get(xdsElForFile);
            if (mitForFile == null) {
                // hang on the model new subtree with IXdsElement elements
                Stack<IXdsElement> stack = new Stack<IXdsElement>();
                do{
                    stack.push(xdsEl);
                    xdsEl = xdsEl.getParent();
                } while (xdsEl != null && !hmIXdsToModel.containsKey(xdsEl));
                
                ModelItem mit = (xdsEl == null) ? null : hmIXdsToModel.get(xdsEl);
                do {
                    xdsEl = stack.pop();
                    mitForFile = new ModelItem(xdsEl, xdsEl == xdsElForFile ? iFile : null, mit);
                    if (mit == null) {
                        hsModelRoots.add(mitForFile);
                    }
                    mit = mitForFile;
                } while(xdsEl != xdsElForFile);
            }
        }
            
        return mitForFile;
    }
    
    public HashSet<ModelItem> getRootElements() {
        return hsModelRoots;
    }
    
    public ModelItem searchModelItemForFile(IFile iFile) {
        return hmIFileToModel.get(iFile);
    }
    
    public void clearAll() {
        hsModelRoots.clear();
        hmIFileToModel.clear();
        hmIXdsToModel.clear();
    }

    
    
    ////////////////////////////////////////////
    
    public class ModelItem {
        private IXdsElement xdsElement;
        private IFile iFile;
        private ModelItem parent;
        private HashSet<ModelItem> children;

        /**
         * @param xdsElement - null for IFiles taht can't be found in XDS model (int. error?)
         * @param iFile - not null for file nodes (this nodes has matches associated wit this file)
         * @param parent - null for model roots
         */
        private ModelItem(IXdsElement xdsElement, IFile iFile, ModelItem parent) {
            this.xdsElement = xdsElement;
            this.iFile = iFile;
            this.parent = parent;
            this.children = new HashSet<ResultModel.ModelItem>();
            if (parent != null) {
                parent.children.add(this);
            }
            if (xdsElement != null) {
                hmIXdsToModel.put(xdsElement, this);
            }
            if (iFile != null) {
                hmIFileToModel.put(iFile, this);
            }
        }
        
        public ModelItem getParent() {
            return parent;
        }
        
        public HashSet<ModelItem> getChildren() {
            return children;
        }
        
        public IXdsElement getIXdsElement() {
            return xdsElement;
        }

        public IFile getIFile() {
            return iFile;
        }

        @Override
        public boolean equals(Object o) {
            try {
                ModelItem mi = (ModelItem)o;
                if (xdsElement != null) {
                    return xdsElement.equals(mi.xdsElement); 
                } else if (iFile != null) {
                    return iFile.equals(mi.iFile); 
                } else {
                    return mi.iFile == null && mi.xdsElement == null; // hmm. incorrect ModelItem?
                }
            } catch (Exception e) {}
            return false;
        }
    } // class ModelItem {

    
}
