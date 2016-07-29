package com.excelsior.xds.ui.project.wizard;

import com.excelsior.xds.core.project.NewProjectCreator.IUserInteraction;
import com.excelsior.xds.ui.commons.utils.SWTFactory;

public class DialogInteraction implements IUserInteraction {
	
	public static DialogInteraction INSTANCE = new DialogInteraction();

	@Override
	public void showOkNotification(String title, String message) {
		SWTFactory.OkMessageBox(null, title, message);
	}

	@Override
	public boolean askYesNoQuestion(String title, String message) {
		return SWTFactory.YesNoQuestion(null, title, message );
	}

}
