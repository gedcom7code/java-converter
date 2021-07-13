package ged5to7.pipeline;
import ged5to7.GedStruct;

public class RenameFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        if ("EMAI".equals(s.tag) || "_EMAIL".equals(s.tag)) {
            s.uri = "https://gedcom.io/terms/v7/EMAIL";
            s.tag2uri(false);
        } else if ("TYPE".equals(s.tag) && s.sup != null && "https://gedcom.io/terms/v7/FORM".equals(s.sup.uri)) {
            s.uri = "https://gedcom.io/terms/v7/MEDI";
            s.tag2uri(false);
        } else if ("_UID".equals(s.tag)) {
            s.uri = "https://gedcom.io/terms/v7/UID";
            s.tag2uri(false);
        } else if ("_ASSO".equals(s.tag)) {
            if (s.pointsTo == null) {
                new GedStruct(s, "https://gedcom.io/terms/v7/PHRASE", s.payload);
                s.pointsTo = GedStruct.VOID;
            }
            s.uri = "https://gedcom.io/terms/v7/ASSO";
            s.tag2uri(false);
        } else if ("_CRE".equals(s.tag) || "_CREAT".equals(s.tag)) {
            s.uri = "https://gedcom.io/terms/v7/CREA";
            s.tag2uri(false);
        } else if ("_DATE".equals(s.tag)) {
            s.tag = "DATE"; // needed for AgeDateFilter
            s.uri = "https://gedcom.io/terms/v7/DATE";
            s.tag2uri(false);
        } else if ("RELA".equals(s.tag) && s.sup != null && "https://gedcom.io/terms/v7/ASSO".equals(s.sup.uri)) {
            s.uri = "https://gedcom.io/terms/v7/ROLE";
            s.tag2uri(false);
        }

        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
