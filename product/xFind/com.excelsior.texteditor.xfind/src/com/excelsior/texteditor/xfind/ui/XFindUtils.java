package com.excelsior.texteditor.xfind.ui;

public abstract class XFindUtils 
{
    /**
     * Tests whether each character in the given string is a letter.
     *
     * @param str the string to check
     * @return <code>true</code> if the given string is a word
     */
    public static boolean isWord(String str) {
        if (str == null || str.length() == 0)
            return false;

        for (int i= 0; i < str.length(); i++) {
            if (!Character.isJavaIdentifierPart(str.charAt(i)))
                return false;
        }
        return true;
    }

}
