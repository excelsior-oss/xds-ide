/**
 * Push or 2-state  button for view toolbar (like "Sort alphabetically" in outline view)
 */
package com.excelsior.xds.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.excelsior.xds.core.utils.IClosure;
import com.excelsior.xds.ui.images.ImageUtils;

public class ToolbarActionButton extends Action implements IWorkbenchAction{
    private IDialogSettings settings;
    private final String id;
    private final IClosure<Boolean> action;
    private final boolean isCheckBox;
      
    public ToolbarActionButton(String tooltip, String id, boolean isCheckBox, String imgId, boolean defaultChecked,
                             IDialogSettings settings, IClosure<Boolean> action) 
    {
        super(tooltip, isCheckBox ? AS_CHECK_BOX : AS_PUSH_BUTTON);
        this.id = id;
        this.action = action;
        this.isCheckBox = isCheckBox;
        this.settings = settings;
        setId(id);
        setImageDescriptor(ImageDescriptor.createFromImage(ImageUtils.getImage(imgId)));

        if (isCheckBox) {
            boolean turnOn = defaultChecked;
            if (settings != null) {
                turnOn = settings.getBoolean(getClass().getName() + id); 
            }
            super.setChecked(turnOn);
        }
    }
    
    public void run() {
        boolean turnOn = false;
        if (isCheckBox) {
            turnOn = isChecked();
            if (settings != null) {
                settings.put(getClass().getName() + id, turnOn); 
            }
        }
        action.execute(turnOn);
    }  

    @Override
    public void setChecked(boolean b) {
        if (isCheckBox && settings != null) {
            settings.put(getClass().getName() + id, b); 
        }
        super.setChecked(b);
    }
    
    public void dispose() {}  
      
}
