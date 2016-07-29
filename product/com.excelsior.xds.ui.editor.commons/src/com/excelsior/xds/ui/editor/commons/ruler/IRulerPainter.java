package com.excelsior.xds.ui.editor.commons.ruler;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextEditor;

import com.excelsior.xds.core.utils.IDisposable;

public interface IRulerPainter extends IDisposable {
	void setTextEditor(TextEditor editor);
	void beforePaint();
	boolean paintLine(int line, Rectangle bounds, GC gc, Display display);
}