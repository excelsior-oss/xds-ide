package com.excelsior.xds.core.builders;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public final class LookupSettings {

    private final List<Lookup> lookups = new ArrayList<Lookup>();

    /**
     * @param name - file or module name ("zz.def" or "zz" to search "zz.def" | "zz.ob2" | "zz.sym")
     * @return
     */
    public File lookup(String name) {
        File res;
        if (name.contains(".")) {
            res = lookupFile(name);
        } else {
            res = lookupFile(name + ".def");        //$NON-NLS-1$
            if (res == null) {
                res = lookupFile(name + ".ob2");    //$NON-NLS-1$
            }
            if (res == null) {
                res = lookupFile(name + ".sym");    //$NON-NLS-1$
            }
        }
        return res;
    }
    
    
    public void setLookupEquations(String lookupEquations, File workDirectory) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new StringReader(lookupEquations));
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            int eqpos = line.indexOf("=");
            String patt = line.substring(0, eqpos);
            String sdirs = line.substring(eqpos+1).trim();
            if (sdirs.isEmpty()) {
                continue;
            }
            
            Lookup lookup = new Lookup(patt);

            String[] dirs = StringUtils.split(sdirs, File.pathSeparatorChar);
            for (String dir : dirs) {
                File f = new File(dir.trim()); 
                if (!f.isAbsolute()) {
                    f = new File(workDirectory, dir);
                }
                try {
                    String s = f.getCanonicalPath();
                    f = new File(s);
                    lookup.dirs.add(f);
                } catch (IOException e) {
                }
            }
            
            if (!lookup.dirs.isEmpty()) {
                lookups.add(lookup);
            }
        }
    }
    
    /**
     * Returns all lookup directories to search for files with given file name pattern.
     * 
     * @param fileName - file name to search for (something like "zz.def")
     * @return - list of all lookup directories for the given file name pattern. 
     */
    public List<File> getLookupDirs(String fileName) {
        List<File> res = new ArrayList<File>();
        for (Lookup lookup : lookups) {
            try {
                if (lookup.regComp.match(fileName, 0)) {
                    for (File dir : lookup.dirs) {
                        res.add(dir);
                    }
                }
            } catch (IllegalStateException e) {
            }
        }
        return res;
    }
    
    public List<File> getLookupDirs(){
    	return lookups.stream().flatMap(l -> l.dirs.stream()).distinct().collect(Collectors.toList());
    }
    
    private File lookupFile(String name) {
        for (Lookup lookup : lookups) {
            try {
                if (lookup.regComp.match(name, 0)) {
                    for (File dir : lookup.dirs) {
                        File f = new File(dir, name);
                        if (f.isFile()) {
                            return f;
                        }
                    }
                }
            } catch (IllegalStateException e) {
            }
        }
        return null;
    }

    private final static class Lookup {
        RegComp regComp;
        List<File> dirs;
        
        public Lookup(String regExpr) {
            regComp = new RegComp(regExpr, File.pathSeparatorChar != ';');
            dirs = new ArrayList<File>(3);
        }
    }
    
    void copy(LookupSettings that) {
    	lookups.clear();
    	lookups.addAll(that.lookups);
    }
}
