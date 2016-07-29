package com.excelsior.xds.ui.decorators;

import org.eclipse.core.resources.IResource;


public interface IXdsDecorator {
    void refresh(IResource[] changedElements);
}
