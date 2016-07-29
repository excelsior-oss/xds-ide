package com.excelsior.xds.ui.editor.commons.text;

import java.util.Collection;

import org.eclipse.ui.IEditorPart;

import com.excelsior.xds.core.text.ITextRegion;

public interface IInactiveCodeMatcher
{
    /**
     * Returns the list of inactive source code regions.
     *
     * @param editor the editor part
     * @return list of inactive source code regions.
     */
    public Collection<ITextRegion> getInactiveCodeRegions(IEditorPart editor);

}
