package com.excelsior.xds.core.utils;

import org.eclipse.swt.graphics.RGB;

/**
 * Graphics related stuff
 * <br>
 * @author lsa80
 */
public final class GraphicUtils {
	/**
	 * Makes an SWT color lighter
	 * @param c
	 * @param factor float in [0..1]. if 0 - color will be unchanged, if 1 - color become white. 
	 * @return
	 */
	public static RGB lighter(RGB c, float factor) {
		float rComponent = (255 - c.red)*factor + c.red;
		float gComponent = (255 - c.green)*factor + c.green;
		float bComponent = (255 - c.blue)*factor + c.blue;
		return new RGB((int)rComponent, (int)gComponent, (int)bComponent);
	}
	
	/**
	 * Makes an SWT color darker
	 * @param c
	 * @param factor float in [0..1]. if 0 - color will be unchanged, if 1 - color become black. 
	 * @return
	 */
	public static RGB darker(RGB c, float factor) {
		float rComponent = (1 - factor)* c.red;
		float gComponent = (1 - factor)* c.green;
		float bComponent = (1 - factor)* c.blue;
		return new RGB((int)rComponent, (int)gComponent, (int)bComponent);
	}
	
	
	/**
     * Cannot instantiate this class, static methods only 
     */
	private GraphicUtils(){
	}
}
