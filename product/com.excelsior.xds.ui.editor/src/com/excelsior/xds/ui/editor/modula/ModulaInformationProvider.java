package com.excelsior.xds.ui.editor.modula;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;

import com.excelsior.xds.ui.editor.commons.HoverInformationControl;

public class ModulaInformationProvider implements IInformationProvider, IInformationProviderExtension, IInformationProviderExtension2 {

    private ModulaSourceViewerConfiguration svConfig;
    private ITextHover hover;

    public ModulaInformationProvider(ModulaSourceViewerConfiguration svConfig) {
        this.svConfig = svConfig;
    }
    
    private void createHover(ITextViewer textViewer) {
        if (hover == null && textViewer instanceof ISourceViewer) {
            hover = svConfig.getTextHover((ISourceViewer)textViewer, IModulaPartitions.M2_CONTENT_TYPE_DEFAULT);
        }
    }

    /*
     * @see IInformationProvider#getSubject(ITextViewer, int)
     */
    public IRegion getSubject(ITextViewer textViewer, int offset) {
        createHover(textViewer);
        if (hover != null) {
            return hover.getHoverRegion(textViewer, offset);
        }
        return null;
    }

    /**
     * @see IInformationProvider#getInformation(ITextViewer, IRegion)
     * @deprecated
     */
    public String getInformation(ITextViewer textViewer, IRegion subject) {
        createHover(textViewer);
        if (hover != null) {
            String s = hover.getHoverInfo(textViewer, subject);
            if (s != null && s.trim().length() > 0) {
                return s;
            }
        }
        return null;
    }

    /*
     * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    public Object getInformation2(ITextViewer textViewer, IRegion subject) {
        createHover(textViewer);
        if (hover instanceof ITextHoverExtension2) {
            return ((ITextHoverExtension2)hover).getHoverInfo2(textViewer, subject);
        }
        return null;
    }

    /*
     * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
     * @since 3.1
     */
    public IInformationControlCreator getInformationPresenterControlCreator() {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                HoverInformationControl mic = new HoverInformationControl(parent, EditorsUI.getTooltipAffordanceString());
                IInformationControlCreator creatorWithFocus = mic.getInformationPresenterControlCreator();
                return creatorWithFocus.createInformationControl(parent);
            }
        };
    }
}
