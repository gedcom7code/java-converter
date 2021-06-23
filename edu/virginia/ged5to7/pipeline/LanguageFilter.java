package edu.virginia.ged5to7.pipeline;
import edu.virginia.ged5to7.GedStruct;
import edu.virginia.ged5to7.GedcomDefinitions;

public class LanguageFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        String phrase = null;
        if ((s.tag != null) && s.tag.equals("LANG")) {
            String lang = GedcomDefinitions.langTag(s.payload);
            if (lang == null) new GedStruct(s, "https://gedcom.io/terms/v7/PHRASE", s.payload);
            s.payload = (lang == null) ? "und" : lang;
        }
        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
