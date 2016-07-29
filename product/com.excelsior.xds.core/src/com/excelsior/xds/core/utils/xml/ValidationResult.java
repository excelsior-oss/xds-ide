package com.excelsior.xds.core.utils.xml;

public class ValidationResult {
	private boolean isValid;
	private String validationMessage;
	
	public ValidationResult(boolean isValid, String validationMessage) {
		this.isValid = isValid;
		this.validationMessage = validationMessage;
	}

	public boolean isValid() {
		return isValid;
	}

	public String getValidationMessage() {
		return validationMessage;
	}
}
