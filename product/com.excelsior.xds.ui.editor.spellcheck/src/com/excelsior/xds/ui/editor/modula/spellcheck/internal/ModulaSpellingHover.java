package com.excelsior.xds.ui.editor.modula.spellcheck.internal;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

import com.excelsior.xds.ui.editor.commons.HoverInformationControl;

public class ModulaSpellingHover implements ITextHover, ITextHoverExtension,
		ITextHoverExtension2 {
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new HoverInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		};
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return null;
	}
	
	@Override
	public HoverInfoWithSpellingAnnotation getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		return getSpellingHover(textViewer, hoverRegion);
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return findWord(textViewer.getDocument(), offset);
	}

	private IRegion findWord(IDocument document, int offset) {
		int start = -2;
		int end = -1;

		try {

			int pos = offset;
			char c;

			while (pos >= 0) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c)) {
					break;
				}
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c)) {
					break;
				}
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset) {
				return new Region(offset, 0);
			} else if (start == offset) {
				return new Region(start, end - start);
			} else {
				return new Region(start + 1, end - start - 1);
			}
		}

		return null;
	}

	private HoverInfoWithSpellingAnnotation getSpellingHover(ITextViewer textViewer, IRegion hoverRegion) {
        IAnnotationModel model= null;
        if (textViewer instanceof ISourceViewerExtension2) {
            model = ((ISourceViewerExtension2)textViewer).getVisualAnnotationModel();
        } else if (textViewer instanceof SourceViewer) {
            model= ((SourceViewer)textViewer).getAnnotationModel();
        }
        if (model != null) {
            @SuppressWarnings("rawtypes")
            Iterator e= model.getAnnotationIterator();
            while (e.hasNext()) {
                Annotation a= (Annotation) e.next();
                if (a instanceof SpellingAnnotation) {
                    Position p= model.getPosition(a);
                    if (p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
                        return new HoverInfoWithSpellingAnnotation((SpellingAnnotation)a, textViewer, p.getOffset());
                    }
                }
            }
        }
        return null;
    }
}
