package com.excelsior.xds.ui.editor.modula.contentassist2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import com.excelsior.xds.builder.buildsettings.BuildSettingsCache;
import com.excelsior.xds.core.builders.BuildSettings;
import com.excelsior.xds.core.compiler.driver.CompileDriver;
import com.excelsior.xds.core.utils.XdsFileUtils;
import com.excelsior.xds.parser.modula.XdsParserManager;
import com.excelsior.xds.parser.modula.XdsSourceType;
import com.excelsior.xds.parser.modula.utils.ModulaFileUtils;
import com.excelsior.xds.ui.commons.utils.WorkbenchUtils;
import com.excelsior.xds.ui.editor.modula.contentassist2.ActiveCodeContentAssistProcessor.ModuleLocation;

class ModuleImportContextualCompletionProposal extends ModulaContextualCompletionProposal {
    
    private File module;
    private ModuleLocation location;
	private EnumSet<CommaPosition> commas;
    
    public ModuleImportContextualCompletionProposal(int replacementOffset, int replacementLength, 
                                                    EnumSet<CommaPosition> commas, Image image, StyledString displaySString, File module, ModuleLocation location) 
    {
        super("", replacementOffset, replacementLength, 0, image, displaySString, null, ""); //$NON-NLS-1$ //$NON-NLS-2$
        this.module = module;
        this.location = location;
        this.commas = commas;
    }
    
    enum CommaPosition {
    	BEFORE,
    	AFTER,
    	NONE
    }

    @Override
    public void apply(IDocument document) {
        String s = null;
        try {
            s = getTrueCaseModuleName(module);
        } catch (Exception x) {}

        if (s == null) {
            s = FilenameUtils.getBaseName(module.getName()); // use as is..
        }
        
        StringBuilder sb = new StringBuilder();
        
        int cursorAdv = s.length();
        
        if (commas.contains(CommaPosition.BEFORE)) {
        	cursorAdv += 2;
        	sb.append(", "); //$NON-NLS-1$
        }
        
        sb.append(StringUtils.defaultString(s));
        
        if (commas.contains(CommaPosition.AFTER)) {
        	cursorAdv += 2;
        	sb.append(", "); //$NON-NLS-1$
        }

        try {
            document.replace(fReplacementOffset, fReplacementLength, sb.toString());
            fCursorPosition = Math.max(fReplacementLength, cursorAdv);
        } catch (BadLocationException x) {
        }
    }
    
    @Override
	public int hashCode() {
		return Objects.hashCode(module);
	}

	@Override 
    public boolean equals(Object o) {
        // not full equals, used to hide duplicated proposals only
        if (o instanceof ModuleImportContextualCompletionProposal) {
            ModuleImportContextualCompletionProposal p = (ModuleImportContextualCompletionProposal)o;
            return  module.equals(p.module);
        }
        return false;
    }
    
    ModuleLocation getLocation() { 
        // used to sort proposal list
        return location;
    }
    
    /**
     * Module file name may have wrong upper/lower case characters.
     * Here we try to get true name from the module itself.
     */
    private String getTrueCaseModuleName(File f) throws IOException {
        String res = null;
        
        String fileContents = null;
        if (XdsFileUtils.isSymbolFile(f.getName())) {
            res = tryGetModuleNameFromSym(f);
            if (res == null) { // oops. can't read it fast
                fileContents = CompileDriver.decodeSymFile(f.getAbsolutePath()); // convert to .ODF ...
            }
        } else {
            fileContents = FileUtils.readFileToString(f);
        }
        
        if (res == null && fileContents != null) {
            BuildSettings buildSettings = BuildSettingsCache.createBuildSettings(WorkbenchUtils.getActiveFile()); 
            XdsSourceType sourceType = ModulaFileUtils.getSourceType(f.getAbsolutePath());
            res = XdsParserManager.getModuleName(sourceType, fileContents, buildSettings); 
        }
        return res;
    }
    
    
    /////// Read module name from SYM file. ////////////
    
    private String tryGetModuleNameFromSym(File f) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(f);
            
            if (readInt(is) != sym_magic) {
                return null;
            }
            readInt(is); // current version
            readInt(is); // sym_ident
            int tag = is.read();
            if (tag != sym_hook) {
                return null; // hz
            }
            readInt(is); // some number
            
            String fnameLc = FilenameUtils.getBaseName(f.getName()).toLowerCase(); 
            String modName = readString(is, fnameLc.length()+1);
            
            // 'modName' must be true-case module name. 
            // Check that it is case-insensitive same with the file name:
            if (fnameLc.equals(modName.toLowerCase())) {
                return modName; // Ok 
            }
        } catch (Exception e) {
        } finally {
            if (is != null) {
            	IOUtils.closeQuietly(is);
            }
        }
        return null;
    }

    private final static int sym_magic = 0x4F4D53;
    private final static int sym_hook  = 2;
    private final static int EOS       = 0xA;
  
    // Reads packed integer
    private static int readInt(FileInputStream is) throws IOException {
        int x = is.read();
        if (x > 0x80 || x == -1) {
            return (x - 192) & 0xff;
        }
        int shift = 7;
        int n = x;
        x = is.read();
        while (x < 0x80 && x != -1) {
            n |= (x << shift);
            shift += 7;
            x = is.read();
        }
        return n | (((x - 192) & 0xff) << shift);
    }

    private static String readString(FileInputStream is, int maxLen) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ich = is.read();
            char ch = (char)ich;
            if (ch == EOS || ich < 0) {
                break;
            }
            sb.append(ch);
            if (sb.length() >= maxLen) {
                throw new IOException("Sym file reader: string length > " + maxLen);
            }
        }
        return sb.toString();
    }
    
}
