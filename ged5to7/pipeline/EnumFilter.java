package ged5to7.pipeline;
import ged5to7.GedStruct;
import ged5to7.GedcomDefinitions;

public class EnumFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        GedcomDefinitions def = GedcomDefinitions.getDefinitions();
        String pu = def.payloadURI(s.uri);
        if ("https://gedcom.io/terms/v7/type-Enum".equals(pu)) {
            String bit = s.payload.trim().toUpperCase().replaceAll("[- ]+","_");
            String uri = def.enumURI(s.uri, bit);
            if (uri == null) {
                new GedStruct(s, "https://gedcom.io/terms/v7/PHRASE", s.payload);
                s.payload = def.enumURI(s.uri, "OTHER") == null ? "_OTHER" : "OTHER";
            } else {
                s.payload = bit;
            }
        } else if ("https://gedcom.io/terms/v7/type-List#Enum".equals(pu)) {
            String[] bits = s.payload.split(",");
            boolean other = false;
            String others = s.payload;
            s.payload = "";
            for(String bit : bits) {
                bit = bit.trim().toUpperCase();
                if (def.enumURI(s.uri, bit) != null) {
                    if (s.payload.length() > 0) s.payload += ", ";
                    s.payload += bit;
                } else {
                    other = true;
                }
            }
            if (other) {
                if (s.payload.length() > 0) s.payload += ", ";
                s.payload = def.enumURI(s.uri, "OTHER") == null ? "_OTHER" : "OTHER";
                new GedStruct(s, "https://gedcom.io/terms/v7/PHRASE", others);
            }
        }

        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
