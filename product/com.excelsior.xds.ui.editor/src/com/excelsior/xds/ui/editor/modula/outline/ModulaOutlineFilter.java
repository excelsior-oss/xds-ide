package com.excelsior.xds.ui.editor.modula.outline;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;

import com.excelsior.xds.core.model.IXdsConstant;
import com.excelsior.xds.core.model.IXdsElement;
import com.excelsior.xds.core.model.IXdsFormalParameter;
import com.excelsior.xds.core.model.IXdsImportElement;
import com.excelsior.xds.core.model.IXdsModule;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsRecordField;
import com.excelsior.xds.core.model.IXdsType;
import com.excelsior.xds.core.model.IXdsVariable;
import com.excelsior.xds.parser.modula.symbol.IVariableSymbol;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

public class ModulaOutlineFilter implements IXdsElementFilter
{
    private final Set<OutlineElementFilter> state;
    private final Set<OutlineElementFilter> savedState; // used in XdsOutlineFiltersDialog for 'Cancel'
    
    private final OutlineElementFilter[] filters;

    public ModulaOutlineFilter() {
        state = new HashSet<OutlineElementFilter>();
        savedState = new HashSet<OutlineElementFilter>();
        filters = new OutlineElementFilter[] {

                new InstanceofElementFilter<IXdsImportElement>(IXdsImportElement.class,
                        Messages.XdsOutlineFilter_ImportName, 
                        Messages.XdsOutlineFilter_ImportDesc,
                        ".FILTER_IMPORT"),    //$NON-NLS-1$

                new InstanceofElementFilter<IXdsConstant>(IXdsConstant.class,
                        Messages.XdsOutlineFilter_ConstantsName,
                        Messages.XdsOutlineFilter_ConstantsDesc, 
                        ".FILTER_CONSTANTS"),    //$NON-NLS-1$

                new InstanceofElementFilter<IXdsType>(IXdsType.class,
                        Messages.XdsOutlineFilter_TypesName, Messages.XdsOutlineFilter_TypesDesc,
                        ".FILTER_TYPES"),    //$NON-NLS-1$

                new InstanceofElementFilter<IXdsRecordField>(IXdsRecordField.class,
                        Messages.XdsOutlineFilter_RecordFieldsName,
                        Messages.XdsOutlineFilter_RecordFieldsDesc, 
                        ".FILTER_RECORD_FIELDS"),    //$NON-NLS-1$

                new VariableElementFitler( false,
                        Messages.XdsOutlineFilter_GlobalVariablesName,
                        Messages.XdsOutlineFilter_GlobalVariablesDesc,
                        ".FILTER_GLOB_VARIABLES" ),    //$NON-NLS-1$
                                                   
                new VariableElementFitler( true,
                       Messages.XdsOutlineFilter_LocalVariablesName,
                       Messages.XdsOutlineFilter_LocalVariablesDesc,
                       ".FILTER_LOC_VARIABLES" ),    //$NON-NLS-1$
                                                   
                new InstanceofElementFilter<IXdsProcedure>(IXdsProcedure.class,
                        Messages.XdsOutlineFilter_ProceduresName,
                        Messages.XdsOutlineFilter_ProceduresDesc, 
                        ".FILTER_PROCEDURES"),     //$NON-NLS-1$
                                 
                new InstanceofElementFilter<IXdsFormalParameter>(IXdsFormalParameter.class,
                        Messages.XdsOutlineFilter_FormalParametersName,
                        Messages.XdsOutlineFilter_FormalParametersDesc, 
                        ".FILTER_FORAML_PARAMETERS"),     //$NON-NLS-1$

                new InstanceofElementFilter<IXdsModule>( IXdsModule.class,
                        Messages.XdsOutlineFilter_LocModulesName,
                        Messages.XdsOutlineFilter_LocModulesDesc,
                        ".FILTER_LOCAL_MODULES" ),    //$NON-NLS-1$
       };
        for (OutlineElementFilter filter : filters) {
            state.add(filter);
        }
    }

    
    public OutlineElementFilter[] getElementFilters() {
        return filters;
    }

    
    public void saveFilters(IPreferenceStore store, String dlgId) {
        for (OutlineElementFilter f : filters) {
            f.save(store, dlgId);
        }
    }

    public void readFilters(IPreferenceStore store, String dlgId) {
        for (OutlineElementFilter f : filters) {
            f.read(store, dlgId);
        }
    }
    
    
    abstract class OutlineElementFilter
    {        
        private final String name;
        private final String description;
        private final String id;
        
        private OutlineElementFilter( String name, String description
                                    , String id ) 
        {
            this.name = name;
            this.description = description;
            this.id   = id;
        }
       
        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
        
        public void setCheckState(boolean b) {
            if (b) {
                state.add(this);
            } 
            else {
                state.remove(this);
            }
        }
        
        public boolean getCheckState() {
            return state.contains(this);
        }

        public void setSavedCheckState(boolean b) {
            if (b) {
                savedState.add(this);
            } 
            else {
                savedState.remove(this);
            }
        }
        
        public boolean getSavedCheckState() {
            return savedState.contains(this);
        }

        
        private void save(IPreferenceStore store, String dlgId) {
            store.setValue(dlgId + id, getCheckState());
        }

        private void read(IPreferenceStore store, String dlgId) {
            String preferenceName = dlgId + id;
//            store.setDefault(preferenceName, true);
            setCheckState(store.getBoolean(preferenceName));
        }
        
        public abstract boolean accept(IXdsElement xdsElement);
    }
    
    
    private class InstanceofElementFilter<T extends IXdsElement> extends OutlineElementFilter 
    {
        private Class<T> classFilter;
        
        public InstanceofElementFilter(Class<T> classFilter, String name, String description, String id ) {
            super(name, description, id);
            this.classFilter = classFilter;
        }
        
        @Override
        public boolean accept(IXdsElement xdsElement) {
            return classFilter.isAssignableFrom(xdsElement.getClass());
        }
    }
    
    private class VariableElementFitler extends OutlineElementFilter 
    {
        private boolean isLocal;
        
        public VariableElementFitler(boolean isLocal, String name, String description, String id ) {
            super(name, description, id);
            this.isLocal = isLocal;
        }
        
        @Override
        public boolean accept(IXdsElement xdsElement) {
            if (xdsElement instanceof IXdsVariable)  {
                IXdsVariable xdsVariable = (IXdsVariable) xdsElement;
                IVariableSymbol symbol = xdsVariable.getSymbol();
                if (symbol != null) {
                    return isLocal == symbol.isLocal();
                }
            }
            return false;
        }
    }

    @Override
    public boolean accept(IXdsElement xdsElement) {
        // TODO : implement filtering using EnumSet and XdsElementTypes
        for (Iterator<OutlineElementFilter> iterator = state.iterator(); iterator.hasNext();) {
            OutlineElementFilter filter = (OutlineElementFilter) iterator.next();
            if (filter.accept(xdsElement)) {
                return false;
            }
        }
        return true;
    }
}