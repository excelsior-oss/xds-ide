package com.excelsior.xds.core.model.internal;

import com.excelsior.xds.core.model.IXdsFolderContainer;
import com.excelsior.xds.core.model.IXdsResource;

public interface IEditableXdsFolderContainer extends IXdsFolderContainer {
    void addChild(IXdsResource e);
    void removeChild(IXdsResource e);
}
