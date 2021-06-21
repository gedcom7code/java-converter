package edu.virginia.ged5to7;

import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class AnselCharset extends Charset {
    public AnselCharset() {
        super("ANSEL", new String[0]);
    }
    public boolean contains(Charset that) {
        return that.displayName().equals("US-ASCII")
            || that.displayName().equals("ANSEL");
    }
    public CharsetEncoder newEncoder() {
        return new AnselEncoder(this);
    }
    public CharsetDecoder newDecoder() {
        return new AnselDecoder(this);
    }
    
    private static class AnselEncoder extends CharsetEncoder {
        public AnselEncoder(Charset me) {
            super(me, 1, 1); // 1 byte per character, always
        }
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            throw new UnsupportedOperationException("ASNEL Encoding not supported");
        }
    }
    private static class AnselDecoder extends CharsetDecoder {
        static boolean ishigh(int b) {
            if (b < 0xE0) return false;
            if (b <= 0xEF) return true;
            if (b < 0xFA) return false;
            if (b <= 0xFB) return true;
            return b == 0xFE;
        }
        static boolean isLow(int b) {
            return (b >= 0xF0) && (b <= 0xF9);
        }
        static boolean isCenter(int b) {
            return b == 0xFC;
        }
        static final int[] special = {
            0x141, 0xD8, 0x110, 0xDE, 0xC6, 0x152, 0x2B9, 
            0xB7, 0x266D, 0xAE, 0xB1, 0x1A0, 0x1AF, 0x2BE, -0xAF, 
            0x2BF, 0x142, 0xF8, 0x111, 0xFE, 0xE6, 0x153, 0x2BA, 
            0x131, 0xA3, 0xF0, -0xBB, 0x1A1, 0x1B0, 0x25A1, 0x25A0, 
            0xB0, 0x2113, 0x2117, 0xA9, 0x2667, 0xBF, 0xA1, 0xDF, 
            0x20AC, -0xC9, -0xCA, -0xCB, -0xCC, 0x65, 0x6F, 0xDF, 
            -0xD0, -0xD1, -0xD2, -0xD3, -0xD4, -0xD5, -0xD6, -0xD7,
            -0xD8, -0xD9, -0xDA, -0xDB, -0xDC, -0xDD, -0xDE, -0xDF, 
            0x309, 0x300, 0x301, 0x302, 0x303, 0x304, 0x306, 0x307, 
            0x308, 0x30C, 0x30A, 0xFE20, 0xFE21, 0x315, 0x30B, 0x310, 
            0x327, 0x328, 0x323, 0x324, 0x325, 0x333, 0x332, 0x326, 
            0x328, 0x32E, 0xFE22, 0xFE23, 0x338, -0xFD, 0x313, -0xFF
        };
        CharStackQueue high, low, center;
        boolean diacriticMode;
        
        public AnselDecoder(Charset me) {
            super(me, 1, 1); // 1 byte per character, always
            high = new CharStackQueue(20);
            low = new CharStackQueue(10);
            center = new CharStackQueue(1);
            diacriticMode = false;
        }
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while(true) {
                if (diacriticMode) {
                    if (!out.hasRemaining()) return CoderResult.OVERFLOW;
                    if (!center.isEmpty()) out.put(center.pop());
                    else if (!low.isEmpty()) out.put(low.shift());
                    else if (!high.isEmpty()) out.put(high.pop());
                    if (center.isEmpty() && low.isEmpty() && high.isEmpty())
                        diacriticMode = false;
                } else {
                    boolean haveDiacritics = !(center.isEmpty() && low.isEmpty() && high.isEmpty());
                    if (!in.hasRemaining()) return CoderResult.UNDERFLOW;
                    int b = in.get() & 0xFF;
                    if (b < 0x80) { out.put((char)b); diacriticMode = haveDiacritics; continue; }
                    if (b < 0xA1) return CoderResult.malformedForLength(1);
                    int c = special[b-0xA1];
                    if (c < 0) return CoderResult.malformedForLength(1);
                    if (ishigh(b)) { high.push((char)c); }
                    else if (isLow(b)) { low.push((char)c); }
                    else if (isCenter(b)) { center.push((char)c); }
                    else { out.put((char)c); diacriticMode = haveDiacritics; }
                }
            }
        }
    }
}
