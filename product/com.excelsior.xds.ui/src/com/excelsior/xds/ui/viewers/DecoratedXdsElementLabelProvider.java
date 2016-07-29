package com.excelsior.xds.ui.viewers;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.excelsior.xds.core.model.IXdsAliasQualifiedImportElement;
import com.excelsior.xds.core.model.IXdsElementWithSymbol;
import com.excelsior.xds.core.model.IXdsProcedure;
import com.excelsior.xds.core.model.IXdsRecordVariant;
import com.excelsior.xds.core.model.IXdsRecordVariantLabel;
import com.excelsior.xds.core.model.IXdsSetElement;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;

public class DecoratedXdsElementLabelProvider extends    ColumnLabelProvider 
                                              implements IStyledLabelProvider 
{
    private final ILabelProvider decoratedLabelProvider;
    
    public DecoratedXdsElementLabelProvider(ILabelProvider decoratedLabelProvider) {
        this.decoratedLabelProvider = decoratedLabelProvider;
    }

    @Override
    public StyledString getStyledText(Object element) {
        StyledString styledString = new StyledString();
        String text = decoratedLabelProvider.getText(element);
        styledString.append(text != null ? text : ""); //$NON-NLS-1$

        if (element instanceof IXdsRecordVariant) {
            IXdsRecordVariant xdsRecordVariant = (IXdsRecordVariant) element;
            if (xdsRecordVariant.isElseVariant()) {
                appendStringDecoration(styledString, "", " ELSE");    //$NON-NLS-1$  //$NON-NLS-2$
            } 
            else {
                Collection<IXdsRecordVariantLabel> labels = xdsRecordVariant.getLabels();
                String separator = " | ";    //$NON-NLS-1$
                String decoration = "";     //$NON-NLS-1$
                for (IXdsRecordVariantLabel label : labels) {
                    decoration += separator + label.getElementName(); 
                    separator = ", ";       //$NON-NLS-1$
                }
                appendStringDecoration(styledString, "", decoration);    //$NON-NLS-1$
            }
        }
        else if (element instanceof IXdsSetElement) {
        }
        else if (element instanceof IXdsElementWithSymbol) {
            IModulaSymbol symbol = ((IXdsElementWithSymbol)element).getSymbol();
            if (element instanceof IXdsAliasQualifiedImportElement) 
                symbol = ((IXdsAliasQualifiedImportElement)element).getAliasSymbol();
            appendStringDecoration(styledString, "", ModulaSymbolDescriptions.getSymbolDescription(symbol));
        }
        
        if (element instanceof IXdsProcedure) {
			IXdsProcedure xdsProcedure = (IXdsProcedure) element;
			if (xdsProcedure.isForwardDeclaration()) {
				appendStringDecoration(styledString, "", " FORWARD"); //$NON-NLS-1$
			}
		}
        
        return styledString;
    }
    

    @Override
    public Image getImage(Object element) {
        return decoratedLabelProvider.getImage(element);
    }
    
    @Override
    public String getText(Object element) {
        return decoratedLabelProvider.getText(element);
    }

    private void appendStringDecoration(StyledString styledString, String prefix, String decoration) {
        if (!StringUtils.isEmpty(decoration)) {
            styledString.append(prefix + decoration, StyledString.DECORATIONS_STYLER); 
        }
    }
    
    @Override
    public void addListener(ILabelProviderListener listener) {
        decoratedLabelProvider.addListener(listener);
    }

    @Override
    public void dispose() {
        decoratedLabelProvider.dispose();
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return decoratedLabelProvider.isLabelProperty(element, property);
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        decoratedLabelProvider.removeListener(listener);
    }

    
    /**
     *  treeViewer.setLabelProvider() for styled labels (with colored texts) expects DelegatingStyledCellLabelProvider class.
     *  But it does not implements ILabelProvider required sometimes (f. ex. in Outline view)
     *  
     *  This class may be used in the same cases.
     *
     */
    public static class DelegatingDecoratedXdsElementLabelProvider 
                  extends    DelegatingStyledCellLabelProvider 
                  implements ILabelProvider 
    {
        public DelegatingDecoratedXdsElementLabelProvider(ILabelProvider decoratedLabelProvider) {
            super(new DecoratedXdsElementLabelProvider(decoratedLabelProvider));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DecoratedXdsElementLabelProvider getStyledStringProvider() {
            return (DecoratedXdsElementLabelProvider)super.getStyledStringProvider();
        }
        
        /**
         * {@inheritDoc}
         * Used by the pattern <tt>PatternFilter</tt> in the <tt>FilteredTree</tt>.
         */
        @Override
        public String getText(Object element) {
            return getStyledStringProvider().getText(element);
        }
    }

}
