package edu.virginia.ged5to7.pipeline;

import edu.virginia.ged5to7.GedStruct;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Two changes to SOUR <Text>:
 * 
 * 1. Create a new SOUR record with a NOTE <Text>
 * 2. Put any TEXT substructures inside a new DATA substructure
 */
public class SourceFilter implements Filter {
    private void update(GedStruct s, LinkedList<GedStruct> newRecords) {
        if ("https://gedcom.io/terms/v7/SOUR".equals(s.uri) && s.payload != null) {
            GedStruct sour = new GedStruct(null, "https://gedcom.io/terms/v7/record-SOUR");
            GedStruct note = new GedStruct(sour, "https://gedcom.io/terms/v7/NOTE", s.payload);
            newRecords.add(sour);
            s.payload = null;
            s.pointTo(sour);
            GedStruct data = new GedStruct(null, "https://gedcom.io/terms/v7/SOUR-DATA");
            for(GedStruct s2 : s.sub) if ("TEXT".equals(s2.tag)) {
                s2.uri = "https://gedcom.io/terms/v7/TEXT";
                data.addSubstructure(s2);
            }
            if (data.sub.size() > 0) {
                s.sub.removeIf(s2 -> "TEXT".equals(s2.tag));
                s.addSubstructure(data);
            }
        }
        for(GedStruct s2 : s.sub) update(s2, newRecords);
    }
    public Collection<GedStruct> update(GedStruct s) {
        LinkedList<GedStruct> newRecords = new LinkedList<GedStruct>();
        update(s, newRecords);
        return newRecords;
    }
}
