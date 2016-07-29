package com.excelsior.xds.ui.editor.commons;

import org.eclipse.swt.widgets.Composite;


public interface IHoverInfo {
    
    public void deferredCreateContent(Composite parent, HoverInformationControl miControl);
    
    public void setVisible(boolean visible);
    
}
