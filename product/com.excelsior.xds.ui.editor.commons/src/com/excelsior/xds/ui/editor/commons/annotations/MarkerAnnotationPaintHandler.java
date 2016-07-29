package com.excelsior.xds.ui.editor.commons.annotations;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import com.excelsior.xds.ui.editor.commons.SourceCodeTextEditor;

/**
 * Use this annotation paint handler if you only interested in MarkerAnnotation annotations.
 * @author lsa80
 */
public abstract class MarkerAnnotationPaintHandler implements IAnnotationPaintHandler {

	@Override
	public boolean paint(SourceCodeTextEditor editor, Annotation annotation, GC gc, Canvas canvas, Rectangle bounds) {
		if (annotation instanceof MarkerAnnotation) {
			return paint(editor, (MarkerAnnotation)annotation, gc, canvas, bounds);
		}
		return false;
	}
	
	protected abstract boolean paint(SourceCodeTextEditor editor, MarkerAnnotation annotation, GC gc, Canvas canvas, Rectangle bounds);
}