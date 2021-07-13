package ged5to7.pipeline;
import ged5to7.GedStruct;

/**
 * Updates required HEAD fields.
 * In 5.5.1 that was GEDC.VERS, GEDC.FORM, and CHAR.
 * In 7.0 it's just GEDC.VERS, which is recommended to come first.
 */
public class VersionFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        if (s.tag.equals("HEAD")) {
            s.sub.removeIf(s2 -> s2.tag.equals("GEDC") || s2.tag.equals("CHAR") || s2.tag.equals("SUBN") || s2.tag.equals("FILE"));
            GedStruct gedc = new GedStruct(null, "https://gedcom.io/terms/v7/GEDC", (String)null);
            GedStruct vers = new GedStruct(gedc, "https://gedcom.io/terms/v7/GEDC-VERS", "7.0");
            
            s.sub.addFirst(gedc); gedc.sup = s;
        }
        if (s.tag.equals("SUBN")) { s.sup = s; } // delete SUBN records
        return null;
    }
}
