package com.excelsior.xds.ui.editor.commons.preferences;

import org.eclipse.swt.graphics.RGB;

import com.excelsior.xds.ui.editor.commons.ITokens;

public interface ITokenModification {
	ITokens getToken();
	
	int getStyle();
	RGB getRgb();
	boolean isDisabled();
}