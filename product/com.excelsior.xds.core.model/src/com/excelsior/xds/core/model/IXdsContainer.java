package com.excelsior.xds.core.model;

import java.util.Collection;

public interface IXdsContainer extends IXdsElement 
{
    public Collection<IXdsElement> getChildren();
}