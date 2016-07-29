package com.excelsior.xds.core.builders;

/** Copyright (c) 1993 xTech Ltd, Russia. All Rights Reserved. */
/** Utility library: regular expressions */
/* Leo 07-Oct-89 */
/* O2: Ned 10-Sep-92/01-Mar-93 */
/* Translated to Java *FSA 14-Mar-13 */

public class RegComp {

    public RegComp(String expr, boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        errPos = -1;
        parsePos = 0; // global
        
        if (!caseSensitive) {
            this.expr = expr.toUpperCase();
        }
        else {
            this.expr = expr;
        }

        // check that all chars are in 0..255 range:
        for (int i=0; i<expr.length(); ++i) {
            if (expr.charAt(i) > 255) {
                errPos = i;
                return;
            }
        }

        try {
            regExpr = re();
            regExpr.res = new Result();
        } catch (ArgException e) {
            // e.printStackTrace();
            errPos = parsePos;
        }
    }

    /**
     * @return -1 when Ok or error position in the expression string
     */
    public int getErrPos() {
        return errPos;
    }

    /**
     * Returns TRUE, iff expression matches with string "s" starting from position "pos".
     * @throws Exception
     *
     */
    public boolean match(String s, int pos) throws IllegalStateException {
        if (!caseSensitive) {
            s = s.toUpperCase();
        }
        for (int i = 0; i < regExpr.res.len.length; ++i) {
            regExpr.res.len[i] = 0;
            regExpr.res.pos[i] = 0;
        }
        if (errPos < 0) {
            return match0(regExpr, s, pos, new VarInt(0), regExpr.res);
        } else {
            return false; // bad idea to match with bad regExpr
        }
    }

    /**
     * Returns the length of the substring matched to "$n" at last 
     * call of match()
     * 
     * @param n 0..9
     * @return
     */
    public int len(int n) {
        try {
            return regExpr.res.len[n];
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns the position of the  beginning  of  the  substring
     * matched to "$n" at last call of match()
     *
     * @param n 0..9
     * @return
     */
    public int pos(int n) {
        try {
            return regExpr.res.pos[n];
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * @return TRUE if expression does not contain wildcards
     */
    public boolean isConst() {
        Expr re = regExpr;
        while (re.kind==par) {
            re = re.next;
        }
        return re.kind==str && re.next==null;
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private final String expr;
    private final boolean caseSensitive;
    private int parsePos;
    private int errPos; // -1 or err position

    private Expr regExpr;


    private static final int str = 0;
    private static final int any = 1; // "?"
    private static final int set = 2; // "[]"
    private static final int bra = 3; // "{}"
    private static final int seq = 4; // "*"
    private static final int and = 5; // "&"
    private static final int or  = 6; // "|"
    private static final int not = 7; // "^"
    private static final int par = 8; // "()"

    private static class Expr {
        Expr next;
        int nres;
        Result res;
        int kind;
        Expr left; // right=next
        String pat;
        boolean leg[]; //[256]

        public Expr(int kind) {
            this.kind = kind;
            nres = -1;
        }
    }

    private static class Result {
        final int pos[]; // [10]
        final int len[]; // [10]

        public Result() {
            pos = new int[10];
            len = new int[10];
        }
    }

    private static class VarInt {
        public int val;
        public VarInt(int i) {val=i;}
    }


    /**
     * @param offset = 0 - next char (expr[i] in oberon prototype)
     * @return char ot 0C when out of range
     */
    private char peekChar(int offset) {
        return parsePos+offset < expr.length() ? expr.charAt(parsePos+offset) : 0;
    }

    /**
     * @param num drop this number of chars
     * @return 1st dropped char or 0C when EOL
     */
    private char dropChars(int num) {
        char ch = peekChar(0);
        parsePos = Math.min(parsePos+num, expr.length());
        return ch;
    }


    private Expr app_new(Expr reg[], int kind) throws ArgException {
        Expr n = new Expr(kind);
        if (reg[0] == null) {
            reg[0] = n;
        } else {
            Expr t = reg[0];
            while (t.next != null) {
                t = t.next;
            }
            if (kind == seq && t.kind == seq) {
                throw new ArgException();
            } else {
                t.next = n;
            }
        }
        return n;
    }

    private void dollar(Expr n) throws ArgException {
        if (peekChar(0) == '$') {
            char ch = peekChar(1);
            if ('0' <= ch && ch <= '9') {
                n.nres = ch - '0';
                dropChars(2);
            } else {
                throw new ArgException();
            }
        }
    }

    private char esc() throws ArgException {
        char c = dropChars(1);
        if (c == 0) {
            throw new ArgException();
        }
        if ('0'<=c && c<='7') {
            c -= '0';
            for (int n=0; n<=1; ++n) {
                char cc = dropChars(1);
                if ('0'<=cc && cc<='7') {
                    c = (char)(c*8 + cc - '0');
                } else {
                    throw new ArgException();
                }
            }
        }
        return c;
    }

    private void fill_set_incl(char ch, Expr n, char from, boolean range) throws ArgException {
        if (!range) {
            n.leg[ch] = true;
        } else {
            if (from>ch) {
                throw new ArgException();
            } else {
                for (int i=from; i<=ch; ++i) {
                    n.leg[i] = true;
                }
            }
        }
    }

    
    private void fill_set(Expr n) throws ArgException {
        n.leg = new boolean[256];
        for (int j=0; j<256; ++j) n.leg[j] = false;
        boolean range = false;
        char from = 0;
        char q;
        if (dropChars(1) == '[') {
            q = ']';
        } else {
            q = '}';
            n.kind = bra;
        }
        boolean inv = (peekChar(0) == '^');
        if (inv) dropChars(1);
        if (peekChar(0) == q) {
            throw new ArgException();
        }
        while (true) {
            char ch = peekChar(0);
            if (ch == 0 || ch == q) {
                break;
            } else if (ch=='\\' && peekChar(1)!=0) {
                dropChars(1);
                ch = esc();
                fill_set_incl(ch, n, from, range);
                range = false;
            } else if (ch=='-' && peekChar(1) != q) {
                range = true; // next char will be right bound of the range
                dropChars(1);
            } else {
                // btw: "[1-5-7]" seems equal to "[1-7]" :) (in RegComp.ob2 too)
                fill_set_incl(ch, n, from, range);
                from = ch;    // save prev char
                range = false;
                dropChars(1);
            }
        }
        if (dropChars(1) !=q || range) {
            throw new ArgException();
        } else {
            if (inv) {
                for (int j=0; j<n.leg.length; ++j) {
                    n.leg[j] = !n.leg[j];
                }
            }
        }
    }

    private void fill_str(Expr n) throws ArgException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char ch = peekChar(0);
            if (ch == 0) {
                break;
            }
            if ("[*?{)&|^$".indexOf(ch) >= 0) {
                break;
            }
            if (ch=='\\' && peekChar(1)!=0) {
                dropChars(1);
                ch = esc();
                sb.append(ch);
            } else {
                sb.append(ch);
                dropChars(1);
            }
        }
        n.pat = sb.toString();
    }


    private Expr simple() throws ArgException {
        if (peekChar(0) == 0) {
            throw new ArgException();
        }
        Expr reg[] = new Expr[1];
        while(true) {
            char ch = peekChar(0);
            if (ch == 0) {
                break;
            }
            if (("()|&^").indexOf(ch) >= 0) break;
            Expr n;
            if      (ch == '*') { n = app_new(reg, seq); dropChars(1); }
            else if (ch == '?') { n = app_new(reg, any); dropChars(1); }
            else if (ch == '{') { n = app_new(reg, set); fill_set(n); }
            else if (ch == '[') { n = app_new(reg, set); fill_set(n); }
            else                { n = app_new(reg, str); fill_str(n); }
            dollar(n);
        }
        return reg[0];
    }

    private Expr factor() throws ArgException {
        Expr reg;
        char ch = peekChar(0);
        if (ch == 0) {
            throw new ArgException();
        } else if (ch == '(') {
            dropChars(1);
            reg = new Expr(par);
            reg.next = re();
            if (peekChar(0) == ')') {
                dropChars(1);
                dollar(reg);
            } else {
                throw new ArgException();
            }
        } else if (ch == '^') {
            dropChars(1);
            reg = new Expr(not);
            reg.next = factor();
            if (reg.next.nres>=0) {
                reg.nres = reg.next.nres;
                reg.next.nres = -1;
            }
        } else {
            reg = simple();
            char cc = peekChar(0);
            if (cc=='^' || cc=='(') {
                Expr last = reg;
                while (last.next != null && (last.kind==str || last.kind==any || last.kind==set || last.kind==bra)) {
                    last = last.next;
                }
                last.next = factor();
            }
        }
        return reg;
    }

    private Expr term() throws ArgException {
        Expr reg = factor();
        if (peekChar(0)=='&') {
            dropChars(1);
            Expr t = new Expr(and);
            t.left = reg;
            reg = t;
            reg.next = term();
        }
        return reg;
    }

    private Expr re() throws ArgException {
        Expr reg = term();
        if (peekChar(0)=='|') {
            dropChars(1);
            Expr t = new Expr(or);
            t.left = reg;
            reg = t;
            reg.next = re();
        }
        return reg;
    }


    // matcher ////////////////////////////////////////////////////////////

    private boolean bra_seq_end(Expr reg, int n, Result rs, int p, VarInt stop) {
        while (reg != null && (reg.kind==bra || reg.kind==seq)) {
            reg=reg.next;
        }
        if (reg != null) {
            return false;
        } else {
            if (n>=0) {
                rs.len[n] = p - rs.pos[n];
            }
            stop.val = p;
            return true;
        }
    }

    private boolean match0(Expr reg, String s, int p, VarInt stop, Result rs) throws IllegalStateException {
        stop.val = p;
        if (reg == null) {
            return p >= s.length();
        }
        int n = reg.nres;
        if (p >= s.length()) {
            return (reg.kind == seq || reg.kind == bra) && reg.next == null;
        }
        if (reg.kind == any) {
            if (n >= 0) {
                rs.pos[n] = p;
                rs.len[n] = 1;
            }
            return match0(reg.next, s, p+1, stop, rs);
        } else if (reg.kind == seq) {
            if (n >= 0) {
                rs.pos[n] = p;
            }
            while (p < s.length()) {
                if (match0(reg.next, s, p, stop, rs)) {
                    if (n >= 0) {
                        rs.len[n] = p-rs.pos[n];
                    }
                    return true;
                }
                ++p;
            }
            return bra_seq_end(reg, n, rs, p, stop);
        } else if (reg.kind == set) {
            char ch = s.charAt(p);
            if (!reg.leg[ch]) {
                return false;
            }
            if (n >= 0) {
                rs.pos[n] = p;
                rs.len[n] = 1;
            }
            return match0(reg.next, s, p+1, stop, rs);
        } else if (reg.kind == bra) {
            if (n >= 0) {
                rs.pos[n] = p;
            }
            while (p < s.length()) {
                if (match0(reg.next, s, p, stop, rs)) {
                    if (n >= 0) {
                        rs.len[n] = p - rs.pos[n];
                    }
                    return true;
                }
                char ch = s.charAt(p);
                if (!reg.leg[ch]) {
                    return false;
                }
                ++p;
            }
            return bra_seq_end(reg, n, rs, p, stop);
        } else if (reg.kind == str) {
            if (n >= 0) {
                rs.pos[n] = p;
            }
            for (int i=0; i < reg.pat.length(); ++i) {
                if (s.charAt(p) != reg.pat.charAt(i)) {
                    return false;
                }
                ++p;
            }
            if (n >= 0) {
                rs.len[n] = p - rs.pos[n];
            }
            return match0(reg.next, s, p, stop, rs);
        } else if (reg.kind == and) {
            return match0(reg.left, s, p, stop, rs) && match0(reg.next, s, p, stop, rs);
        } else if (reg.kind == or) {
            return match0(reg.left, s, p, stop, rs) || match0(reg.next, s, p, stop, rs);
        } else if (reg.kind == not) {
            if (n >= 0) {
                rs.pos[n] = p;
            }
            if (match0(reg.next, s, p, stop, rs)) {
                return false;
            } else {
                p = s.length();
                stop.val = p;
            }
            if (n >= 0) {
                rs.len[n] = p - rs.pos[n];
            }
            return true;
        } else if (reg.kind == par) {
            if (n >= 0) {
                rs.pos[n] = p;
            }
            if (match0(reg.next, s, p, stop, rs)) {
                if (n >= 0) {
                    rs.len[n] = stop.val - rs.pos[n];
                }
                return true;
            } else {
                return false;
            }
        } else {
            throw new IllegalStateException("match0(): Internal error");
        }
    }

// // Not translated to java yet:
//    
// PROCEDURE Substitute*(re: Expr; s-,m: ARRAY OF CHAR; VAR d: ARRAY OF CHAR);
// (** Substitutes  the substrings of "s" matched with "re" instead
//   of "$digit" in the "m" and copies the resulting string into "d".
// *)
// (* NB: 'm' cant be VAL because 'm' & 'd' may be the same variable *)
//   VAR i,j,k,n,l: LONGINT;
// BEGIN
//   i:=0; j:=0;
//   IF LEN(d)=0 THEN RETURN END;
//   WHILE (i<LEN(m)) & (m[i]#0C) DO
//     IF j=LEN(d)-1 THEN d[j]:=0C; RETURN END;
//     IF (m[i]='\') & (i<LEN(m)-1) & (m[i+1]='$') THEN
//       d[j]:='$'; INC(i,2); INC(j)
//     ELSIF (m[i]='$') & (i<LEN(m)-1) & (ORD(m[i+1])-ORD("0") IN {0..9}) THEN
//       n:=ORD(m[i+1])-ORD("0");
//       k:=re^.res^.pos[n];
//       l:=re^.res^.len[n];
//       IF j+l>LEN(d)-1 THEN l:=LEN(d)-j-1 END;
//       IF k+l>LEN(s)   THEN l:=LEN(s)-k   END;
//       WHILE (l>0) DO d[j]:=s[k]; INC(k); INC(j); DEC(l); END;
//       INC(i,2)
//     ELSE
//       d[j]:=m[i]; INC(i); INC(j)
//     END
//   END;
//   IF j<LEN(d) THEN d[j]:=0C END
// END Substitute;


    private static class ArgException extends Exception {
        private static final long serialVersionUID = 110825815L;
    }
    

// // Test:
//    
//    public static void main(String args[]) throws Exception {
//        RegComp rc = new RegComp("a*$0.xx", false);
//        int ep = rc.getErrPos();
//        if (ep >= 0) {
//            System.out.println("ErrPos = " + ep);
//            return;
//        }
//
//        String tsts[] = {
//                "azz",
//                "a23.xx",
//                "azzc",
//                "zzc",
//                "bzzc",
//                ""
//        };
//        for (String s : tsts) {
//            boolean b = rc.match(s, 0);
//            System.out.println("s=\"" + s + "\"    res=" + b);
//            for (int i=0; i<=9; ++i) {
//                System.out.print(String.format("%d:[%d,%d] ", i, rc.pos(i), rc.len(i)));
//            }
//            System.out.println("\n---------------------------------------------");
//        }
//        
//    }
}


