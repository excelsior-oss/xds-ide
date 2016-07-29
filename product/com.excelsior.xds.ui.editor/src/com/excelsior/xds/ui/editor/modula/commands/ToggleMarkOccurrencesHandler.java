package com.excelsior.xds.ui.editor.modula.commands;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import com.excelsior.xds.core.preferences.PreferenceKeys;

public class ToggleMarkOccurrencesHandler extends    AbstractHandler 
                                          implements IPreferenceChangeListener
                                                   , IElementUpdater
{
    private static final String COMMAND_ID = "com.excelsior.xds.ui.commands.toggleMarkOccurrences";    //$NON-NLS-1$

    private volatile boolean isExecuting;
    private boolean isTurnedOn;
    
    public ToggleMarkOccurrencesHandler() {
    	PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.addChangeListener(this);
        isTurnedOn = PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getStoredBoolean();
        setToggleState(isTurnedOn);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            isExecuting = true;
            boolean on = PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getStoredBoolean();
            PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.setStoredBoolean(!on);
        }        
        finally {
            isExecuting = false;
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
        boolean on = PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getStoredBoolean();
        element.setChecked(on);
    }

    
    private static void setToggleState(boolean toggleState) {
        ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        service.refreshElements(COMMAND_ID, null);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
        if (isExecuting)
            return; 
                    
        if (PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getKey().equals(event.getKey())) {
            isTurnedOn = PreferenceKeys.PKEY_HIGHLIGHT_OCCURENCES.getStoredBoolean();
            setToggleState(isTurnedOn);
        }
    }
    
}
