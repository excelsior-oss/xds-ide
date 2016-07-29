package com.excelsior.xds.core.compiler.driver;

import com.excelsior.xds.core.process.InputStreamListener;
import com.excelsior.xds.core.sdk.Sdk;

public class XShell implements InputStreamListener {
    private InputStreamListener xShellImpl;
    
    public XShell(IXShellListener listener, Sdk.XShellFormat xShellFormat) {
        switch (xShellFormat) {
        case BINARY:
            xShellImpl = new XShellBinary(listener);
            break;
        default:
            xShellImpl = new XShellText(listener);
        }
    }
    
    @Override
    public void onHasData(byte[] buffer, int length) {
        xShellImpl.onHasData(buffer, length);
    }

    @Override
    public void onEndOfStreamReached() {
        xShellImpl.onEndOfStreamReached();
    }
    
    

}
