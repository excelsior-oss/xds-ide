package com.excelsior.xds.ui.editor.modula.outline;

import com.excelsior.xds.core.model.IXdsElement;

public interface IXdsElementFilter 
{
    public boolean accept(IXdsElement xdsElement);
}
