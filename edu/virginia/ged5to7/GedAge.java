package edu.virginia.ged5to7;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parses GEDCOM 5.5.1 age payloads and converts them into GEDCOM 7.0 age payloads.
 */
public class GedAge {
    private char modifier;
    private int year, month, week, day;
    private String phrase;
    
    private static final Pattern SYNTAX = Pattern.compile(
        "(?:\\s*(?:(CHILD)|(INFANT)|(STILLBORN))\\s*)|(?:\\s*([<>]))?((?:\\s*\\d+\\s*[ymwd]?)+)\\s*",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern SYNTAX_PART = Pattern.compile("(\\d+)\\s*([ymwd]?)",
        Pattern.CASE_INSENSITIVE);
    
    
    private GedAge() {
        modifier = '\0';
        year = month = week = day = -1;
        phrase = null;
    }
    private GedAge(char modifier, int year, int month, int week, int day, String phrase) {
        this.modifier = modifier;
        this.year = year;
        this.month = month;
        this.week = week;
        this.day = day;
        this.phrase = phrase;
    }
    
    public static GedAge from551(String payload) {
        Matcher m = SYNTAX.matcher(payload);
        if (!m.matches()) return new GedAge('\0',-1,-1,-1,-1,payload);
        if (m.group(1) != null) return new GedAge('<',8,-1,-1,-1, payload);
        if (m.group(2) != null) return new GedAge('<',1,-1,-1,-1, payload);
        if (m.group(3) != null) return new GedAge('\0',0,-1,-1,-1, payload);
        GedAge ans = new GedAge();
        if (m.group(4) != null) ans.modifier = m.group(4).charAt(0);
        m = SYNTAX_PART.matcher(m.group(5));
        while(m.find()) {
            String mode = m.group(2).toUpperCase();
            int value = Integer.parseInt(m.group(1));
            if (mode.equals("M")) ans.month = value;
            else if (mode.equals("W")) ans.week = value;
            else if (mode.equals("D")) ans.day = value;
            else ans.year = value;
        }
        return ans;
    }
    public String getPayload() {
        return (
            (modifier != '\0' ? modifier+" " : "") +
            (year >= 0 ? year+"y " : "") +
            (month >= 0 ? month+"m " : "") +
            (week >= 0 ? week+"w " : "") +
            (day >= 0 ? day+"d " : "")
        ).trim();
    }
    public String getPhrase() {
        return phrase;
    }
}
