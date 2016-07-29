package com.excelsior.xds.core.text;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import com.excelsior.xds.core.resource.EncodingUtils;

public class TextEncoding {
	private static final int DEFAULT_BUFFER_SIZE = 1024*32;
	
	/**
	 * maximum number of chars to examine when  guessing the codepage.
	 */
	private static int MAX_CODEPAGE_GUESS_CHAR_COUNT = 1024 * 128;

    public enum CodepageId {

        CODEPAGE_1251("CP1251"), //$NON-NLS-1$
        CODEPAGE_866(EncodingUtils.DOS_ENCODING);   //$NON-NLS-1$

        final public String charsetName;

        private CodepageId (String name) {
            charsetName = name;
        }

    };

    /**
     * 
     * @param is - stream to read file contents from
     * @return     CodepageId (or null if the determined CP is not supported)
     */
    private static CodepageId determineCodepage (InputStream is) throws IOException {
        int cnt1251 = 0;
        int cnt866  = 0;

        byte buf[] = new byte[32768];
        int len;
        
        try{
        	while ((len = is.read(buf)) >= 0) {
                for (int i=0; i<len; ++i) {
                    int ch = buf[i] & 0x000000ff;
                    if (ch >= 0x80 && ch <= 0xAF || // 'A'..'ï'
                        ch >= 0xE0 && ch <= 0xEF || // 'ð'..'ÿ'
                        ch == 0xB3 || ch == 0xBA )  // '|' or '||'
                    {
                        ++cnt866;
                    }
                    if(ch >= 0xC0) { // 'A'...
                        ++cnt1251;
                    }
                }
            }
        }
        finally{
        	is.close();
        }
        
        CodepageId codepage = (cnt866 >= cnt1251) ? CodepageId.CODEPAGE_866 
                                                  : CodepageId.CODEPAGE_1251;
        try {
            Charset.forName(codepage.charsetName);
        } catch (Exception e) {
            codepage = null;
        }

        return codepage;
    }

    /**
     * @param f    file to read
     * @param sb   (out) StringBuilder to read file in or null to determine codepage only
     * @param cpId (out) if cpId != null in cpId[0] returns determined codepage or NULL when the determoned codepage is not supported in jvm  
     * @return     false when I/O error occured, true means that 'sb' contains file content 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static void readFileAndCodepage(File f, StringBuilder sb, CodepageId cpId[]) throws FileNotFoundException, IOException {
        try(FileInputStream fis = new FileInputStream(f)){
        	readFileAndCodepage(fis, sb, cpId);
        }
    }
    
    /**
     * First {@link #MAX_CODEPAGE_GUESS_CHAR_COUNT} bytes of the {@link is} are used to determine code page
     * @param is   input stream to read file from
     * @param sb   (out) StringBuilder to read file in or null to determine codepage only
     * @param cpId (out) if cpId != null in cpId[0] returns determined codepage or NULL when the determoned codepage is not supported in jvm  
     * @return     contents of the file
     * @throws IOException 
     */
    public static void readFileAndCodepage(InputStream is, StringBuilder sb, CodepageId cpId[]) throws IOException {
        CodepageId cpi = null;
        byte[] streamBytes = new byte[MAX_CODEPAGE_GUESS_CHAR_COUNT];
        int prefixSize = is.read(streamBytes);
        if (prefixSize > 0) {
        	cpi = determineCodepage(new ByteArrayInputStream(streamBytes, 0, prefixSize)); // may be null
        }

        if (sb != null && prefixSize > 0) {
            Charset cs;
            try { 
                cs = Charset.forName(cpi.charsetName);
            } catch (UnsupportedCharsetException e) {
                cs = Charset.defaultCharset();
                cpi = null;
            }
            InputStream compositeStream = new SequenceInputStream(new ByteArrayInputStream(streamBytes, 0, prefixSize), is);
			InputStreamReader ir = new InputStreamReader(compositeStream, cs);
			
			try{
				char buf[] = new char[DEFAULT_BUFFER_SIZE];
                int len;
                while((len = ir.read(buf)) >= 0) {
                    sb.append(buf, 0, len);
                }
			}
			finally{
				ir.close();
			}
        }
        if (cpId != null) {
            cpId[0] = cpi;
        }
    }


    public static InputStreamReader getInputStreamReader (File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
        return getInputStreamReader(fis);
    }

    private static InputStreamReader getInputStreamReader (InputStream is) throws IOException {
    	byte[] streamBytes = new byte[MAX_CODEPAGE_GUESS_CHAR_COUNT];
    	int prefixSize = is.read(streamBytes);
    	
    	InputStream resultStream;
    	Charset cs;
    	if (prefixSize > 0) {
    		CodepageId codepage = determineCodepage(new ByteArrayInputStream(streamBytes, 0, prefixSize));
    		cs = Charset.forName(codepage.charsetName);
    		resultStream = new SequenceInputStream(new ByteArrayInputStream(streamBytes, 0, prefixSize), is);
    	}
    	else {
    		resultStream = new ByteArrayInputStream(new byte[0]);
    		cs = Charset.defaultCharset();
    	}

    	return new InputStreamReader(resultStream, cs);
    }
    
    /**
     * 
     * @param codepageName - name like "CP866", "Cp1251" etc. May be null (will return false).
     * @return true if this encoding is supported
     */
    public static boolean isCodepageSupported(String codepageName) {
        try {
            Charset.forName(codepageName);
        } catch (UnsupportedCharsetException e) {
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @param preferredCodepageName - name like "CP866", "Cp1251" etc. 
     * @return charset to use (charset for this name or Default charset when can't resolve the name)
     */
    public static Charset whatCharsetToUse(String preferredCodepageName) {
        try {
            return Charset.forName(preferredCodepageName);
        } 
        catch (UnsupportedCharsetException e) {
        }
        return Charset.defaultCharset(); 
    }    

}
