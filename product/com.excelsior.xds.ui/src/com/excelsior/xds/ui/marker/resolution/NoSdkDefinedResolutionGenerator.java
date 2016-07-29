package com.excelsior.xds.ui.marker.resolution;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.excelsior.xds.core.marker.XdsMarkerConstants;
import com.excelsior.xds.ui.internal.nls.Messages;
import com.excelsior.xds.ui.preferences.project.ModulaProjectPreferencePage;

public class NoSdkDefinedResolutionGenerator implements IMarkerResolutionGenerator2 {
	
	@Override
	public boolean hasResolutions(IMarker marker) {
		boolean hasResolution = marker.getAttribute(XdsMarkerConstants.VIOLATION_ATTR, 0) == XdsMarkerConstants.NO_SDK_ERROR;
		return hasResolution;
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		return new IMarkerResolution[]{ new DefineSdkResolution() };
	}
	
	private class DefineSdkResolution implements IMarkerResolution {
		@Override
		public String getLabel() {
			return Messages.NoSdkDefinedResolutionGenerator_DefineSdk;
		}

		@Override
		public void run(IMarker marker) {
			PreferenceDialog dlg = PreferencesUtil.createPropertyDialogOn(null, marker.getResource(), ModulaProjectPreferencePage.ID, null, null);
			dlg.open();
		}
	}
}
