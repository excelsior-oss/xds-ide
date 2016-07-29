package com.excelsior.xds.ui.project.wizard;

public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ValidationException (String msg, boolean isError) {
        super(msg);
        this.isError = isError;
    }
    
    public boolean isError() {
        return isError;
    }
    
    private boolean isError;

}
