package com.excelsior.xds.core.marker;

public final class XdsMarkerConstants 
{
	private XdsMarkerConstants(){
		super();
	}
	
    /**
     * Build problem marker type.
     */
    public static final String BUILD_PROBLEM_MARKER_TYPE = "com.excelsior.xds.core.build_problem";  //$NON-NLS-1$
    
    /**
     * Parser problem marker type.
     */
    public static final String PARSER_PROBLEM = "com.excelsior.xds.core.source_code_problem";  //$NON-NLS-1$
    
    /**
     * Parser problem marker severity attribute. It is used to cheat with eclipse markers. If file with errors does not belong 
     * to compilation set it should not be marked in the Problems View and Project Explorer. So we create it with IMarker.SEVERITY_INFO
     * but then draw correct icon using this artificial attribute.
     * {@link com.excelsior.xds.ui.editor.modula.ModulaEditor.NotInCompilationSetMarkerHandler}
     */
    public static final String PARSER_PROBLEM_SEVERITY_ATTRIBUTE = PARSER_PROBLEM + ".severity.attribute"; //$NON-NLS-1$
    
    // -------------------------------------------------------------------------
    // Marker attributes:
    // -------------------------------------------------------------------------
    /** 
     * Violation marker attribute. An integer value indicating problem with SDK.
     * 
     * @see #NO_VIOLATION
     * @see #NO_SDK_ERROR
     */
    public static final String VIOLATION_ATTR = "xds.violation"; //$NON-NLS-1$

    /**
     * Violation constant (value 0) indicating a no error state.
     */
    public static final int NO_VIOLATION = 0;

    /**
     * Violation constant (value -1) indicating that SDK was not specified.
     */
    public static final int NO_SDK_ERROR = -1;
    
    /**
     * Attribute MARKER_GRAY_STATE for BUILD_PROBLEM_MARKER_TYPE
     * 
     *  Hanged on marker when its line was changed in the editor  
     */
    public static final String MARKER_GRAY_STATE = "xds.marker.gray.state";
    
}
