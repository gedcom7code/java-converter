package edu.virginia.ged5to7.pipeline;

import edu.virginia.ged5to7.GedStruct;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Convert OBJE substructures with no payload into a pointer to an OBJE record
 */
public class ObjectFilter implements Filter {
    private void update(GedStruct s, LinkedList<GedStruct> newRecords) {
        if ("https://gedcom.io/terms/v7/OBJE".equals(s.uri) && s.pointsTo == null && s.payload == null) {
            GedStruct obje = new GedStruct(null, "OBJE");
            newRecords.add(obje);
            Iterator<GedStruct> i = s.sub.iterator();
            while(i.hasNext()) {
                GedStruct s2 = i.next();
                if (s2.uri == null) {
                    obje.addSubstructure(s2);
                    i.remove();
                }
            }
            s.pointTo(obje);
            obje.tag2uri();
        }
        for(GedStruct s2 : s.sub) update(s2, newRecords);
    }
    public Collection<GedStruct> update(GedStruct s) {
        LinkedList<GedStruct> newRecords = new LinkedList<GedStruct>();
        update(s, newRecords);
        return newRecords;
    }
}
