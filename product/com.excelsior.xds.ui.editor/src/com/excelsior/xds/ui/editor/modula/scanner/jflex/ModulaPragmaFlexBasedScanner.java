package com.excelsior.xds.ui.editor.modula.scanner.jflex;

import com.excelsior.xds.ui.commons.syntaxcolor.TokenManager;
import com.excelsior.xds.ui.editor.commons.scanner.jflex.FlexAdapter;

public class ModulaPragmaFlexBasedScanner extends FlexAdapter {

    public ModulaPragmaFlexBasedScanner(TokenManager tokenManager) {
        super(new _ModulaPragmaFlexScanner(), tokenManager);
    }

}
