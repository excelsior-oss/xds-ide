package com.excelsior.xds.ui.editor.commons.annotations;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;

/**
 * Used to custom-draw annotations in the editor
 * @author lsa80
 */
public interface IAnnotationPaintHandler {
	boolean paint(SourceCodeTextEditor editor, Annotation annotation, GC gc, Canvas canvas, Rectangle bounds);
	void dispose();
}
