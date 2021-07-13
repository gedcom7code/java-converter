package edu.virginia.ged5to7.pipeline;
import edu.virginia.ged5to7.GedStruct;

public class TranFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        if (s.sup != null && "TYPE".equals(s.tag)) {
            if ("FONE".equals(s.sup.tag)) {
                s.sup.tag = "TRAN";
                s.tag = "LANG";
                switch(s.payload.toLowerCase()) {
                    case "hangul": s.payload = "ko-hang"; break;
                    case "kana": s.payload = "jp-hrkt"; break;
                    case "pinyin": s.payload = "und-Latn-pinyin"; break;
                    default: s.payload = "x-phonetic-"+s.payload;
                }
                s.sup.tag2uri();
            } else if ("ROMN".equals(s.sup.tag)) {
                s.sup.tag = "TRAN";
                s.tag = "LANG";
                switch(s.payload.toLowerCase()) {
                    case "romanji": s.payload = "jp-Latn"; break;
                    case "wadegiles": s.payload = "zh-Latn-wadegile"; break;
                    default: s.payload = "und-Latn-x-"+s.payload;
                }
                s.sup.tag2uri();
            }
        }
        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
