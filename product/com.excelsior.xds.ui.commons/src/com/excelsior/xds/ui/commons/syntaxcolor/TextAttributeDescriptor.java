package com.excelsior.xds.ui.commons.syntaxcolor;

import org.eclipse.swt.graphics.RGB;

public class TextAttributeDescriptor {
	private RGB foreground;
	private RGB backrground;
	private int style;
	
	public TextAttributeDescriptor(RGB foreground, RGB backrground, int style) {
		this.foreground = foreground;
		this.backrground = backrground;
		this.style = style;
	}

	public RGB getForeground() {
		return foreground;
	}

	public void setForeground(RGB foreground) {
		this.foreground = foreground;
	}

	public RGB getBackground() {
		return backrground;
	}

	public void setBackrground(RGB backrground) {
		this.backrground = backrground;
	}

	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		this.style = style;
	}
}
