package com.excelsior.xds.ui.editor.commons.scanner.jflex;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;

public class FlexPartitionAdapter extends FlexAggregateAdapter 
                                  implements IPartitionTokenScanner 
{

    public FlexPartitionAdapter(IFlexPartitionScanner flex, IToken defaultReturnToken, TokenManager tokenManager) {
        super(flex, defaultReturnToken, tokenManager);
    }

    @Override
    public void setPartialRange( IDocument document, int offset, int length
                               , String contentType, int partitionOffset ) 
    {
        int initial_state = ((IFlexPartitionScanner)flex).getState(contentType);
        
        if (partitionOffset > -1) {
            int delta = offset - partitionOffset;
            if (delta > 0) {
                reset(document, partitionOffset, length+delta, delta, initial_state);
//                setRange(document, partitionOffset, length+delta);
            }
        }
        reset(document, offset, length, 0, initial_state);
    }

}
