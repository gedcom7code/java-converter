package edu.virginia.ged5to7.pipeline;
import edu.virginia.ged5to7.GedStruct;
import edu.virginia.ged5to7.GedAge;
import edu.virginia.ged5to7.GedDateValue;

public class AgeDateFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        String phrase = null;
        if (s.tag.equals("AGE") && s.payload != null) {
            GedAge age = GedAge.from551(s.payload);
            s.payload = age.getPayload();
            phrase = age.getPhrase();
        } else if (s.tag.equals("DATE") && s.payload != null) {
            GedDateValue date = GedDateValue.from551(s.payload);
            s.payload = date.getPayload();
            phrase = date.getPhrase();
        }
        if (phrase != null && phrase.length() > 0)
            new GedStruct(s, "PHRASE", phrase);
        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
