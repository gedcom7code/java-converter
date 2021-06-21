package edu.virginia.ged5to7;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class CharsetDetector {
    private byte[] buffer;
    private int pos;
    private int cap;
    private FileInputStream f;
    
    private CharsetDetector(FileInputStream f) {
        buffer = new byte[1024];
        pos = 0;
        cap = 0;
        this.f = f;
    }
    private int nextByte() throws IOException {
        if (pos < cap) return buffer[pos++] & 0xFF;
        cap = f.read(buffer);
        pos = 0;
        return pos < cap ? buffer[pos++] & 0xFF : -1;
    }
    /**
     * A kind of half-way multi-string regex. Given a set of templates, read
     * a byte at a time until one of them is found, returning the index of
     * which was found or -1 if none were. Treats '\n' as /[\n\r]+/ and
     * ' ' as /[ \t]+/ and lower-case ASCII as upper-case.
     */
    private int findFirstOf(String... needles) throws IOException {
        @SuppressWarnings({"unchecked"})
        java.util.List<Integer>[] dots = new java.util.List[2];
        java.util.List<Integer> tmp;
        dots[0] = new java.util.ArrayList<Integer>();
        dots[1] = new java.util.ArrayList<Integer>();
        int b = nextByte();
        while(b >= 0) {
            char c1 = (char)b;
            for(int i=0; i<needles.length; i+=1) {
                String n = needles[i];
                char c = n.charAt(0);
                if (c == '\r') c = '\n';
                if (c == '\t') c = ' ';
                if (c >= 'a' && c <= 'z') c ^= 0x20;
                if (c == c1) dots[1].add((i<<16)|1);
            }
            for(int dot : dots[0]) {
                int i = dot>>16;
                int j = dot&0xFFFF;
                String n = needles[i];
                char c = n.charAt(j);
                if (c == '\r') c = '\n';
                if (c == '\t') c = ' ';
                if (c >= 'a' && c <= 'z') c ^= 0x20;
                if (c == c1) {
                    if (c == ' ' || c == '\n') dots[1].add((i<<16)|(j));
                    dots[1].add((i<<16)|(1+j));
                }
            }
            tmp = dots[0];
            dots[0] = dots[1];
            tmp.clear();
            dots[1] = tmp;
            for(int dot : dots[0]) {
                int i = dot>>16;
                int j = dot&0xFFFF;
                String n = needles[i];
                if (j == n.length()) return i;
            }
            b = nextByte();
        }
        return -1;
    }


    public static Charset detect(String filename) {
        try (FileInputStream f = new FileInputStream(filename)) {
            byte[] m = new byte[4];
            f.read(m);
            if ((m[0]&0xff) == 0xEF && (m[1]&0xff) == 0xBB && (m[2]&0xff) == 0xBF) return Charset.forName("UTF-8");
            if ((m[0]&0xff) == 0xFF && (m[1]&0xff) == 0xFE) return Charset.forName("UTF-16LE");
            if ((m[0]&0xff) == 0xFE && (m[1]&0xff) == 0xFF) return Charset.forName("UTF-16BE");
            if (m[0] == 0) return Charset.forName("UTF-16BE");
            if (m[1] == 0) return Charset.forName("UTF-16LE");
            CharsetDetector d = new CharsetDetector(f);
            int got = d.findFirstOf("\n0", "\n1 CHAR");
            if (got < 1) return Charset.forName("UTF-8");
            got = d.findFirstOf("\n", "ASCII", "ANSEL");
            if (got < 1) return Charset.forName("UTF-8");
            if (got == 1) return Charset.forName("ISO-8859-1"); // a non-standard but popular superset of ASCII
            return new AnselCharset();
        } catch (IOException ex) { 
            ex.printStackTrace();
            return Charset.forName("UTF-8");
        }
    }
}
