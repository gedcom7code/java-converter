package edu.virginia.ged5to7;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.CharBuffer;

/**
 * Parses GEDCOM 5.5.1 date payloads and converts them into GEDCOM 7.0 date payloads and phrases.
 */
public class GedDateValue {
    private static class GedDate {
        private String calendar, month, epoch;
        private int day, year;
        public String toString() {
            return (calendar == null ? "" : calendar+" ") +
                   (day <= 0 ? "" : day+" ") +
                   (month == null ? "" : month+" ") +
                   year +
                   (epoch == null ? "" : " "+epoch);
        }
    }
    private GedDate d1, d2;
    private String modifier, phrase;
    public enum Type { VALUE, EXACT, PERIOD }
    private Type type;
    
    private enum TokenType { NONE, GREGMONTH, KEY1, KEY2, KEY12, EPOCH, WORD, NUMBER, OPAREN, CPAREN, SLASH, ERROR }
    private static class GedDateToken {
        TokenType type;
        String token;
        int number;
        GedDateToken() { type=TokenType.NONE; token=null; number=0; }

        void next(CharStackQueue b) {
            while(!b.isEmpty() && Character.isWhitespace(b.front())) b.shift();
            type=TokenType.NONE; token=null; number=0;
            if (b.isEmpty()) return;
            if (b.front() == '(') { type = TokenType.OPAREN; b.shift(); return; }
            if (b.front() == ')') { type = TokenType.CPAREN; b.shift(); return; }
            if (b.front() == '/') { type = TokenType.SLASH; b.shift(); return; }
            if (b.front() >= '0' && b.front() <= '9') {
                type = TokenType.NUMBER;
                while(!b.isEmpty() && b.front() >= '0' && b.front() <= '9') {
                    number *= 10;
                    number += b.shift() - '0';
                }
                return;
            }
            if ((b.front() >= 'A' && b.front() <= 'Z') || (b.front() >= 'a' && b.front() <= 'z') || (b.front() == '_')) {
                token = "";
                while(!b.isEmpty() && !Character.isWhitespace(b.front())) {
                    token += Character.toUpperCase(b.shift());
                }
                switch(token) {
                    case "BC": case "AD": case "BCE": case "ADE": case "B.C.": case "A.D.":
                        type = TokenType.EPOCH; break;
                    case "TO": type = TokenType.KEY12; break;
                    case "JAN": case "FEB": case "MAR": case "APR": case "MAY": case "JUN":
                    case "JUL": case "AUG": case "SEP": case "OCT": case "NOV": case "DEC":
                        type = TokenType.GREGMONTH; break;
                    case "BET": case "BEF": case "AFT": case "EST":
                    case "CAL": case "ABT": case "INT": case "FROM":
                        type = TokenType.KEY1; break;
                    case "AND": type = TokenType.KEY2; break;
                    default: type = TokenType.WORD;
                }
                return;
            }
            type = TokenType.ERROR;
            token = b.toString();
            b.clear();
            return;
        }
    }
    private static GedDateToken nextToken(CharStackQueue b) {
        GedDateToken ans = new GedDateToken();
        ans.next(b);
        return ans;
    }
    
    public static GedDateValue from551(String payload) {
        GedDateValue ans = new GedDateValue();
        GedDateToken tok = new GedDateToken();
        CharStackQueue p = new CharStackQueue(payload);
        tok.next(p);
        
        if (tok.type == TokenType.OPAREN) { // "n DATE (text)"
            while(!p.isEmpty() && Character.isWhitespace(p.peek())) p.pop();
            if (p.peek() == ')') p.pop();
            ans.phrase = p.unused();
            return ans;
        }

        // optional starting keyword
        if (tok.type == TokenType.KEY1 || tok.type == TokenType.KEY12) {
            ans.modifier = tok.token;
            tok.next(p);
        }

        // required date
        GedDate d = new GedDate();
        ans.d1 = d;
        // calendar?
        if (tok.type == TokenType.WORD) { d.calendar = tok.token; tok.next(p); }
        // month?
        if (tok.type == TokenType.GREGMONTH) {
            d.month = tok.token; tok.next(p); 
            if (tok.type == TokenType.SLASH) {
                ans.phrase = payload;
                tok.next(p); tok.next(p);
            }
        }
        // day or year
        if (tok.type == TokenType.NUMBER) {
            d.year = tok.number;
            tok.next(p);
            
            // special undocumented "34/5" as shorthand for "BET 34 AND 35"
            if (ans.modifier == null && d.month == null && ans.phrase == null && tok.type == TokenType.SLASH) {
                ans.phrase = payload;
                tok.next(p);
                if (tok.type == TokenType.NUMBER) {
                    int num = tok.number;
                    tok.next(p);
                    if (tok.type == TokenType.NONE) {
                        ans.modifier = "BET";
                        ans.d2 = new GedDate();
                        ans.d2.calendar = ans.d1.calendar;
                        ans.d2.year = d.year;
                        int mod = 10;
                        while(mod*10 > 0 && mod < num) mod *= 10;
                        if ((ans.d2.year % mod) > num) {
                            ans.d2.year -= ans.d2.year % mod;
                            ans.d2.year += mod;
                            ans.d2.year += num;
                        } else {
                            ans.d2.year -= ans.d2.year % mod;
                            ans.d2.year += num;
                        }
                        return ans;
                    }
                } else {
                    tok.next(p);
                }
            } else if (tok.type == TokenType.SLASH) { // end special undocumented shorthand
                ans.phrase = payload;
                tok.next(p); tok.next(p);
            }
        } 
        // (day) month?
        if (d.month == null 
        && (tok.type == TokenType.GREGMONTH || d.calendar != null && tok.type == TokenType.WORD)) {
            d.day = d.year;
            d.year = 0;
            d.month = tok.token; tok.next(p); 
            if (tok.type == TokenType.SLASH) {
                ans.phrase = payload;
                tok.next(p); tok.next(p);
            }
        }
        // (month) year?
        if (d.month != null && d.year <= 0) {
            if (tok.type == TokenType.NUMBER) {
                d.year = tok.number; tok.next(p);
                if (tok.type == TokenType.SLASH) {
                    ans.phrase = payload;
                    tok.next(p); tok.next(p);
                }
            } else { // month without year illegal
                ans.d1 = null;
                ans.modifier = null;
                ans.phrase = payload;
                return ans;
            }
        }
        // epoch?
        if (tok.type == TokenType.EPOCH) {
            if (tok.token.startsWith("B")) d.epoch = "BCE";
            tok.next(p);
        }
        
        // what's next depends on starting keyword
        
        if (ans.modifier == null // no keyword, so only this date
        || ans.modifier.equals("TO") // single-date keywords
        || ans.modifier.equals("BEF")
        || ans.modifier.equals("AFT")
        || ans.modifier.equals("EST")
        || ans.modifier.equals("ABT")
        || ans.modifier.equals("CAL")) {
            if (tok.type != TokenType.NONE) ans.phrase = payload;
            return ans;
        }
        
        if (ans.modifier.equals("INT")) { // INT <date> (<date phrase>)
            ans.modifier = null; // not used as a modifier in GEDCOM 7
            if (tok.type == TokenType.OPAREN && ans.phrase == null) {
                while(!p.isEmpty() && Character.isWhitespace(p.peek())) p.pop();
                if (p.peek() == ')') p.pop();
                ans.phrase = p.unused();
                return ans;
            } else {
                ans.phrase = payload;
            }
            return ans;
        }
        
        if (ans.modifier.equals("FROM")) { // FROM <date> -or- FROM <date> TO <date>
            if (tok.type == TokenType.NONE) return ans;
            if (tok.type == TokenType.KEY12
            && tok.token.equals("TO")) {
                ans.d2 = d = new GedDate();
                tok.next(p);
                // fall through
            } else {
                ans.phrase = payload;
                return ans;
            }
        } else if (tok.type != TokenType.KEY2 
        || !(tok.token.equals("AND") && ans.modifier.equals("BET"))) {
            ans.phrase = payload;
            return ans;
        } else {
            ans.d2 = d = new GedDate();
            tok.next(p);
        }

        // if haven't returned yet, need a second date
        // similar to first, but without the special undocumented shorthand 
        
        // calendar?
        if (tok.type == TokenType.WORD) { d.calendar = tok.token; tok.next(p); }
        // month?
        if (tok.type == TokenType.GREGMONTH) {
            d.month = tok.token; tok.next(p); 
            if (tok.type == TokenType.SLASH) {
                ans.phrase = payload;
                tok.next(p); tok.next(p);
            }
        }
        // day or year
        if (tok.type == TokenType.NUMBER) {
            d.year = tok.number;
            tok.next(p);
            if (tok.type == TokenType.SLASH) {
                ans.phrase = payload;
                tok.next(p); tok.next(p);
            }
        }
        // (day) month?
        if (d.month == null 
        && (tok.type == TokenType.GREGMONTH || d.calendar != null && tok.type == TokenType.WORD)) {
            d.day = d.year;
            d.year = 0;
            d.month = tok.token; tok.next(p); 
            if (tok.type == TokenType.SLASH) {
                ans.phrase = payload;
                tok.next(p); tok.next(p);
            }
        }
        // (month) year?
        if (d.month != null && d.year <= 0) {
            if (tok.type == TokenType.NUMBER) {
                d.year = tok.number; tok.next(p);
                if (tok.type == TokenType.SLASH) {
                    ans.phrase = payload;
                    tok.next(p); tok.next(p);
                }
            } else { // month without year illegal
                ans.d1 = null;
                ans.modifier = null;
                ans.phrase = payload;
                return ans;
            }
        }
        // epoch?
        if (tok.type == TokenType.EPOCH) {
            if (tok.token.startsWith("B")) d.epoch = "BCE";
            tok.next(p);
        }
        
        // and now MUST end
        if (tok.type != TokenType.NONE) {
            ans.phrase = payload;
            return ans;
        }

        return ans;
    }


    public Type getType() {
        if (d1 != null && d2 == null && phrase == null && modifier == null
        && d1.calendar == null && d1.year > 0 && d1.month != null && d1.day > 0)
            return Type.EXACT;
        if (modifier != null && (modifier.equals("FROM") || modifier.equals("TO")))
            return Type.PERIOD;
        return Type.VALUE;
    }
    public String getPayload() {
        String ans = "";
        if (modifier != null) ans += modifier + " ";
        if (d1 != null) ans += d1.toString();
        if (d2 != null) {
            if (modifier.equals("FROM")) ans += " TO " + d2.toString();
            else if (modifier.equals("BET")) ans += " AND " + d2.toString();
            else assert(false);
        }
        return ans;
    }
    public String getPhrase() {
        return phrase;
    }
}
