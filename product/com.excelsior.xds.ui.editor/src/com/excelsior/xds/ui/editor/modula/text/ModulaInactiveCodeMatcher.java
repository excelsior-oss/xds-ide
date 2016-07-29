package com.excelsior.xds.ui.editor.modula.text;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.ui.IEditorPart;

import com.excelsior.xds.core.text.ITextRegion;
import com.excelsior.xds.parser.modula.ast.ModulaAst;
import com.excelsior.xds.ui.editor.commons.text.IInactiveCodeMatcher;
import com.excelsior.xds.ui.editor.modula.utils.ModulaEditorSymbolUtils;

public class ModulaInactiveCodeMatcher implements IInactiveCodeMatcher
{
    /** 
     * {@inheritDoc} 
     */
    @Override
    public Collection<ITextRegion> getInactiveCodeRegions(IEditorPart editor) {
        if (editor != null) {
            ModulaAst ast = ModulaEditorSymbolUtils.getModulaAst(editor.getEditorInput());
            if (ast != null) {
                return ast.getInactiveCodeRegions();
            }
        }
        return Collections.emptyList(); 
    }
    
}
