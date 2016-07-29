package com.excelsior.xds.builder.compile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;

import com.excelsior.xds.core.log.LogHelper;
import com.excelsior.xds.core.marker.XdsMarkerConstants;

public class MarkerMaker {
    
    public static class Location {
        public IResource resource;
        public int lineNumber;
        public int posInLine;
        public int length;
        public int severity;
        public int violation;
        public String message;
    }
    

    public IMarker makeMarker(Location loc) throws CoreException {
		IMarker marker = loc.resource.createMarker(XdsMarkerConstants.BUILD_PROBLEM_MARKER_TYPE);
        marker.setAttribute(IMarker.MESSAGE, loc.message);
        marker.setAttribute(IMarker.SEVERITY, loc.severity);
        if (loc.resource instanceof IFile) {
            marker.setAttribute(IMarker.LINE_NUMBER, loc.lineNumber);
            try {
                Document doc = getDoc((IFile)loc.resource);
                int lineOffs = doc.getLineOffset(loc.lineNumber-1);
                int lineLen  = doc.getLineLength(loc.lineNumber-1);
                int markOffs = lineOffs + loc.posInLine-1;
                int markEnd  = markOffs + getLexemLen(doc, markOffs);
                if (markEnd <= lineOffs + lineLen) { // hbz
                    marker.setAttribute(IMarker.CHAR_START, markOffs);
                    marker.setAttribute(IMarker.CHAR_END,   markEnd);
                }
            } catch (Exception e) {}
        }
        marker.setAttribute(XdsMarkerConstants.VIOLATION_ATTR, loc.violation);
        return marker;
    }
    
    private int getLexemLen(Document doc, int offs) throws Exception {
        char ch, beg = Character.toLowerCase(doc.getChar(offs));
        boolean isName = isAlpha(beg); 
        boolean isNum  = isDigit(beg);
        boolean isString = beg == '"' || beg == '\'';

        if (!isString && !isName && !isNum) {
            return 1;
        }
        
        int len = 1;
        for (; (ch = getch(doc, offs, len)) != 0; ++len) {
            ch = getch(doc, offs, len);
            if (isString && ch == beg) {
                break;
            } else if (isName && !isAlpha(ch) && !isDigit(ch)) {
                break;
            } else if (isNum && !isDigit(ch)) {
                break;
            }
        }
        return len;
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') || ch == '_';
    }

    private char getch(Document doc, int offset, int offs2) {
        try {
            char ch = Character.toLowerCase(doc.getChar(offset + offs2));
            if (ch == 0xd || ch == 0xa) {
                ch = 0;
            }
            return ch;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private Document getDoc(IFile file) throws Exception {
        if (cachedFile != null && cachedFile.getFullPath().equals(file.getFullPath())) {
            return cachedDoc;
        } else {
            return new Document(getText(file));
        }
    }
    
    
    public static String getText(IFile file) {
        try (InputStream in = file.getContents()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int read = in.read(buf);
            while (read > 0) {
                out.write(buf, 0, read);
                read = in.read(buf);
            }
            return out.toString();
        } 
        catch (CoreException | IOException e) {
        	LogHelper.logError(e);
        } 
        return StringUtils.EMPTY;
    }
    
    private IFile    cachedFile;
    private Document cachedDoc;
    
}
