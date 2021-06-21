package edu.virginia.ged5to7.pipeline;

import edu.virginia.ged5to7.GedStruct;
import java.util.Collection;
import java.util.Iterator;

/**
 * Convert NOTE records with 0 or 2+ incoming pointers to SNOTE records.
 * Convert NOTE substructures pointing to SNOTE records to SNOTE substructures.
 * Convert NOTE substructures pointing to NOTE records to inlined copy of the NOTE record.
 */
public class NoteFilter implements Filter {
    public Collection<GedStruct> update(GedStruct s) {
        if ("NOTE".equals(s.tag)) {
            if (s.incoming == null || s.incoming.size() != 1) {
                s.tag = "SNOTE";
                s.tag2uri();
                if (s.incoming != null) for(GedStruct ref : s.incoming) {
                    ref.tag = "SNOTE";
                    ref.tag2uri();
                }
            } else {
                GedStruct ref = s.incoming.iterator().next();
                ref.payload = s.payload;
                ref.pointsTo = null;

                for(GedStruct s2 : s.sub) ref.addSubstructure(s2);
                ref.uri = null;
                ref.tag2uri();

                s.sup = s; // because s.sup!=null causes it to be removed in Converter5to7
                s.sub.clear();
            }
        }
        return null;
    }
}
