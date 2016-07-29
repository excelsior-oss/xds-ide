package com.excelsior.xds.ui.editor.modula.hyperlink;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.excelsior.xds.core.ide.utils.CoreEditorUtils;
import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.resource.ResourceUtils;
import com.excelsior.xds.parser.commons.pst.PstNode;
import com.excelsior.xds.parser.modula.symbol.IModulaSymbol;
import com.excelsior.xds.ui.editor.internal.nls.Messages;

/**
 * Modula-2/Oberon-2 symbol declaration hyperlink.
 */
public class ModulaDeclarationHyperlink implements IHyperlink
{
    private final int offset, length;
    private final IRegion region;
    private final IFile efile;
    private final IFileStore fileStore;

    /**
     * Creates a new Modula-2/Oberon-2 symbol declaration element hyperlink.
     * 
     * @param symbol the Modula-2/Oberon-2 symbol to open .
     * @param region the region of the link
     * @param files a file which contains the symbol definition 
     */
    public ModulaDeclarationHyperlink( IModulaSymbol symbol, IRegion region  
                                     , IFile file )
    {
        this.offset = symbol.getPosition().getOffset();
        this.length = symbol.getName().length();
        this.region = region;
        this.efile  = file;
        this.fileStore   = ResourceUtils.toFileStore(file);
    }
    
    /**
     * Creates a new Modula-2/Oberon-2 symbol declaration element hyperlink.
     * 
     * @param symbol the Modula-2/Oberon-2 symbol to open .
     * @param region the region of the link
     * @param files a file which contains the symbol definition 
     */
    public ModulaDeclarationHyperlink( IModulaSymbol symbol, IRegion region  
                                     , IFileStore fileStore )
    {
        this.offset = symbol.getPosition().getOffset();
        this.length = symbol.getName().length();
        this.region = region;
        this.efile  = null;
        this.fileStore   = fileStore;
    }

    /**
     * Creates a new Modula-2/Oberon-2 symbol declaration element hyperlink.
     * 
     * @param node the Ast node to open .
     * @param region the region of the link
     * @param files a file which contains the symbol definition 
     */
    public ModulaDeclarationHyperlink( PstNode node, IRegion region  
                                     , IFile file )
    {
        this.offset = node.getOffset();
        this.length = node.getLength();
        this.region = region;
        this.efile  = file;
        this.fileStore   = ResourceUtils.toFileStore(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
    	try {
    		if (efile != null) {
    			CoreEditorUtils.openInEditor(efile, true, offset, length);
    		}
    		else if (fileStore != null) {
    			CoreEditorUtils.openInEditor(fileStore, true, offset, length);
    		}
    	} catch (CoreException e) {
    		LogHelper.logError(e);
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRegion getHyperlinkRegion() {
        return region;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHyperlinkText() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeLabel() {
        return Messages.HyperlinkText_ModulaDeclaration;
    }

}
