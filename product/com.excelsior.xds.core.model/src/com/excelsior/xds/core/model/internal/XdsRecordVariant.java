package com.excelsior.xds.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.excelsior.xds.core.model.IXdsContainer;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsProject;
import com.excelsior.xds.core.model.IXdsRecordField;
import com.excelsior.xds.core.model.IXdsRecordVariant;
import com.excelsior.xds.core.model.IXdsRecordVariantLabel;
import com.excelsior.xds.core.model.SourceBinding;
import com.excelsior.xds.core.model.internal.nls.Messages;
import com.excelsior.xds.core.utils.collections.CollectionsUtils;

public class XdsRecordVariant extends    SourceBoundXdsElement 
                              implements IXdsRecordVariant
{
    private final List<IXdsRecordField> fields = new ArrayList<IXdsRecordField>();
    private final List<IXdsRecordVariantLabel> labels = new ArrayList<IXdsRecordVariantLabel>(2);
    private final boolean isElseVariant;
    
    public XdsRecordVariant( IXdsProject project
                           , IXdsContainer parent
                           , SourceBinding sourceBinding 
                           , boolean isElseVariant ) 
    {
        super(Messages.XdsRecordVariant_Name, project, parent, sourceBinding);        
        this.isElseVariant = isElseVariant;
    }
    
    public synchronized void addField(IXdsRecordField field) {
        fields.add(field);
    }

    @Override
    public synchronized Collection<IXdsElement> getChildren() {
    	return CollectionsUtils.unmodifiableArrayList(fields, IXdsElement.class);
    }

    
    @Override
    public boolean isElseVariant() {
        return isElseVariant;
    }
    
    
    public synchronized void addLabel(IXdsRecordVariantLabel label) {
        labels.add(label);
    }

    @Override
    public synchronized Collection<IXdsRecordVariantLabel> getLabels() {
    	return CollectionsUtils.unmodifiableArrayList(labels, IXdsRecordVariantLabel.class);
    }
    
}
