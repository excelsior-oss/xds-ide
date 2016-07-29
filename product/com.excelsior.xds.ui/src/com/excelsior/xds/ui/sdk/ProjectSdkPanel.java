package com.excelsior.xds.ui.sdk;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.excelsior.xds.core.sdk.Sdk;
import com.excelsior.xds.core.sdk.SdkManager;
import com.excelsior.xds.core.sdk.SdkRegistry;
import com.excelsior.xds.ui.commons.utils.SWTFactory;
import com.excelsior.xds.ui.images.ImageUtils;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.preferences.sdk.SDKsPreferencePage;

/**
 * A composite that displays project SDK. 
 *
 * Project SDK: [___________________[V] [Configure]
 *               Default SDK
 *               Registered SDK ...
 *               Invalid SDK 
 */
public class ProjectSdkPanel {

    private Label  labelSdk;
    private Label  labelInvalid;
	private Combo  comboSdk;
	private Button btnConfigSdk;
	
    private String invalidSdkName;
    private String invalidSdkItem;
    private String errorMessage;
	
	private static final int DEFAULT_SDK_INDEX = 0;
	
	/**
	 * Create a new instance of a project SDK composite.
	 * @param dialogPage that hosts the parent composite
	 * @param parent the parent composite to add this one to
	 * @param hspans[3] - hspans for label, combobox and button
	 */
	public ProjectSdkPanel (DialogPage dialogPage, Composite parent, int hspans[]) {
	    Assert.isTrue(hspans.length == 3, "ProjectSdkPanel() - wrong hspans[] array"); //$NON-NLS-1$
		Composite container = parent;
//		if (hspan < 3) {
//			container = SWTFactory.createComposite(parent, parent.getFont(), 3, hspan, GridData.FILL_HORIZONTAL);
//			hspan = 3;
//		}
		
		createControl(dialogPage, container, hspans);
	}
	
	/**
	 * Initialize the user interface.
	 */
	public void initContents() {
		initContents(null);
	}
	
	/**
	 * Initialize the user interface.
	 * @param sdkName name of initially selected SDK, null means default SDK 
	 */
	public void initContents(String sdkName) {
		if ((sdkName != null) && (SdkManager.getInstance().loadSdkRegistry().findSdk(sdkName) == null)) {
		    invalidSdkName = sdkName;
			invalidSdkItem = Messages.format(Messages.ProjectSdkBlock_InvalidSDK, sdkName);
			sdkName = invalidSdkItem;
		} else {
            invalidSdkName = null;
			invalidSdkItem = null;
		}
		refresh(sdkName);
		manageInvalidLabel();
	}
	
	/**
	 * Creates this block's control in the given control.
	 * Project SDK:      [___________________[V] [Configure]
	 * @param dialogPage that hosts the parent composite
	 * @param parent containing control
	 * @param hspan[3] the horizontal spans for controls in the parent composite
	 */
	private void createControl(final DialogPage dialogPage, Composite parent, int hspans[]) {
	    Composite labels = SWTFactory.createComposite(parent, parent.getFont(), 2, hspans[0], 0);
        GridLayout ld = (GridLayout)labels.getLayout();
        ld.marginWidth = 0;

        labelSdk = SWTFactory.createLabel(labels, Messages.ProjectSDKPanel_Label+':', 1);
        labelInvalid = SWTFactory.createLabel(labels, "", 1); //$NON-NLS-1$
        labelInvalid.setImage(ImageUtils.getImage(ImageUtils.EMPTY_16x16));

        comboSdk = SWTFactory.createCombo(parent, hspans[1], SWT.DROP_DOWN | SWT.READ_ONLY);
		comboSdk.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				onChanged();
			}

			@Override public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		btnConfigSdk = SWTFactory.createPushButton(parent, Messages.Common_Configure, hspans[2], null);
		btnConfigSdk.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				PreferencesUtil.createPreferenceDialogOn(dialogPage.getShell(), SDKsPreferencePage.ID, new String[]{SDKsPreferencePage.ID}, null).open();
				refresh();
			}
		});
	}

	
	/**
	 * Returns the project specific SDK or null if the default SDK is selected.
	 * @return the SDK or null.  
	 */
	public Sdk getSelectedSdk() {
		int sel_idx = comboSdk.getSelectionIndex();
		if (sel_idx > 0) {
			SdkRegistry sr = SdkManager.getInstance().loadSdkRegistry();
			return sr.findSdk(comboSdk.getItem(sel_idx)); 
		}
		return null;
	}
	
	/**
	 * Returns the instance of selected SDK including default SDK.
	 * @return the SDK or null.
	 */  
	public Sdk getSelectedSdkInstance() {
		int sel_idx = comboSdk.getSelectionIndex();
		if (sel_idx >= 0) {
			SdkRegistry sr = SdkManager.getInstance().loadSdkRegistry();
			if (sel_idx == DEFAULT_SDK_INDEX) {
				return sr.getDefaultSdk();
			} else {
				return sr.findSdk(comboSdk.getItem(sel_idx)); 
			}
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if the receiver is in valid state. 
	 * Otherwise, <code>false</code> is returned. 
	 * @return the receiver's validity state
     */
	public boolean isValid() {
		if (comboSdk.getSelectionIndex() < 0) {
	    	if (comboSdk.getItemCount() > 0) {
	    		errorMessage = Messages.ProjectSdkBlock_Error_NoSelectedSDK;
	    	} else {
	    		errorMessage = Messages.ProjectSdkBlock_Error_NoInstalledSDK;
	    	}
	    	return false;
		} 
		if (getSelectedSdkInstance() == null) {
	    	errorMessage = Messages.ProjectSdkBlock_Error_InvalidSDK;
	    	return false;
	    }
		
		errorMessage = null;
		return true;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * Refreshes the content of project SDK block and preserves previously selected SDK if it is possible.
	 */
	private void refresh() {
		String sel_item = null;
		int sel_idx = comboSdk.getSelectionIndex();
		if (sel_idx >= 0) {
			sel_item = comboSdk.getItem(sel_idx);
		}

        boolean isSdkManuallyChanged = false;
		if (invalidSdkItem != null) {
	        if (SdkManager.getInstance().loadSdkRegistry().findSdk(invalidSdkName) != null) {
	            if ((sel_item != null) && (sel_item.equals(invalidSdkItem))) {
	                // Selected invalid SDK became valid now 
	                sel_item = invalidSdkName;
	                isSdkManuallyChanged = true;
	            }
	            invalidSdkName = null;
	            invalidSdkItem = null;
	        }
		}

		refresh(sel_item);

		if (isSdkManuallyChanged) {
            onChanged();
        }
	}
	
	/**
	 * Refreshes the content of project SDK block and select given SDK if it is possible.
	 * @param selectedItem item to select, null means default SDK
	 */
	private void refresh (String selectedItem) {
		boolean isSdkChanged = true;
		
		comboSdk.removeAll();
		int sel_idx = -1;
		
		SdkRegistry sr = SdkManager.getInstance().loadSdkRegistry();
		java.util.List<Sdk> sdks = sr.getRegisteredSDKs();
		Sdk defaultSdk = sr.getDefaultSdk();
		
		if (defaultSdk != null) {
			comboSdk.add(Messages.format(Messages.ProjectSdkBlock_DefaultSDK, defaultSdk.getName()));
			sel_idx = DEFAULT_SDK_INDEX;
			for (int i=0; i < sdks.size(); ++i) {
				Sdk sdk = sdks.get(i);
				String s = sdk.getName();
				if ((selectedItem != null) && (s.equals(selectedItem))) {
					sel_idx = i+1;
					isSdkChanged = false;
				}
				comboSdk.add(s);
			}
		}
		
		if (invalidSdkItem != null) {
			comboSdk.add(invalidSdkItem);
			if ((selectedItem != null) && (invalidSdkItem.equals(selectedItem))) {
				sel_idx = comboSdk.getItemCount()-1; 
			}
		}

		if (sel_idx >= 0) {
			comboSdk.select(sel_idx);
		}
		comboSdk.setEnabled(sel_idx >= 0);

		if (isSdkChanged) {
			onChanged();
		}
	}


	private void manageInvalidLabel() {
        Sdk sdk = getSelectedSdk();
        if (sdk == null) {
            sdk = SdkManager.getInstance().loadSdkRegistry().getDefaultSdk();
        }
        if (sdk != null && !sdk.isValid()) {
            labelInvalid.setImage(ImageUtils.getImage(ImageUtils.ERROR_16x16));
            labelInvalid.setToolTipText(Messages.ProjectSdkPanel_InvalidSdk);
        } else {
            labelInvalid.setImage(ImageUtils.getImage(ImageUtils.EMPTY_16x16));
            labelInvalid.setToolTipText(null);
        }
	}
	
	
	/**
	 * Invokes when selected SDK is changed.
	 */
	protected void onChanged() {
	    manageInvalidLabel();
	}
	
	/**
	 * Enables the receiver if the argument is <code>true</code>,
	 * and disables it otherwise. A disabled control is typically
	 * not selectable from the user interface and draws with an
	 * inactive or "grayed" look.
	 *
	 * @param enabled the new enabled state
	 */
	public void setEnabled (boolean enabled) {
        labelSdk.setEnabled(enabled);
        labelInvalid.setEnabled(enabled);
		comboSdk.setEnabled(enabled);
		btnConfigSdk.setEnabled(enabled);
	}
	
}
