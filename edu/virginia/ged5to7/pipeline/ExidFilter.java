package edu.virginia.ged5to7.pipeline;
import edu.virginia.ged5to7.GedStruct;
import edu.virginia.ged5to7.GedcomDefinitions;

public class ExidFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        boolean changed = true;
        switch(s.tag) {
            case "AFN":
            s.uri = "https://gedcom.io/terms/v7/EXID";
            new GedStruct(s, "https://gedcom.io/terms/v7/TYPE", "https://www.familysearch.org/wiki/en/Ancestral_File");
            break;
            case "RFN":
            s.uri = "https://gedcom.io/terms/v7/EXID";
            int colon = s.payload.indexOf(':');
            if (colon < 0) {
                new GedStruct(s, "https://gedcom.io/terms/v7/TYPE", "https://gedcom.io/terms/v7/RFN");
            } else {
                new GedStruct(s, "https://gedcom.io/terms/v7/TYPE", "https://gedcom.io/terms/v7/RFN#"+s.payload.substring(0,colon));
                s.payload = s.payload.substring(colon+1);
            }
            break;
            case "RIN":
            s.uri = "https://gedcom.io/terms/v7/REFN";
            new GedStruct(s, "https://gedcom.io/terms/v7/TYPE", "RIN");
            break;
            case "_FSFTID": case "_FID": case "FSFTID":
            s.uri = "https://gedcom.io/terms/v7/EXID";
            new GedStruct(s, "https://gedcom.io/terms/v7/TYPE", "https://www.familysearch.org/tree/person/");
            break;
            case "_APID":
            s.uri = "https://gedcom.io/terms/v7/EXID";
            new GedStruct(s, "https://gedcom.io/terms/v7/TYPE", "https://www.ancestry.com/family-tree/");
            break;
            // case "HISTID": // unclear what TYPE to give it
            default: changed = false;
        }
        if (changed) s.tag2uri(false);
        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
