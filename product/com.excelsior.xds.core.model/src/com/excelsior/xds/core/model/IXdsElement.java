package com.excelsior.xds.core.model;

public interface IXdsElement 
{
    IXdsProject getXdsProject();
    
    String getElementName();
    IXdsContainer getParent();
}
