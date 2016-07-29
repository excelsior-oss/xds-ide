package com.excelsior.xds.xbookmarks;

import org.eclipse.core.runtime.Assert;


import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;


public class BookmarkColumn extends AbstractContributedRulerColumn {
    
    private BookmarkRulerColumn fDelegate;
        
    public BookmarkColumn() {
        super();
        fDelegate = new BookmarkRulerColumn();

       // some initialization (is it required? hz..) :
        new MarkerAnnotationPreferences().getAnnotationPreferences(); 
    }

    @Override
	public void columnRemoved() {
		fDelegate.aboutToDispose();
		super.columnRemoved();
	}


    @Override
    public void setModel(IAnnotationModel model) {
        fDelegate.setModel(model);
    }

    @Override
    public void redraw() {
        fDelegate.redraw();
    }

    @Override
    public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
        Assert.isTrue(fDelegate != null);
        return fDelegate.createControl(parentRuler, parentControl);
    }

    @Override
    public Control getControl() {
        return fDelegate.getControl();
    }

    @Override
    public int getWidth() {
        return fDelegate.getWidth();
    }

    @Override
    public void setFont(Font font) {
        fDelegate.setFont(font);
    }
    
    
}
