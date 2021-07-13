package ged5to7.pipeline;
import ged5to7.GedStruct;

public class MediaTypeFilter implements Filter {
    public java.util.Collection<GedStruct> update(GedStruct s) {
        if ("https://gedcom.io/terms/v7/FORM".equals(s.uri)) {
            switch(s.payload.toLowerCase()) {
                case "gif": 
                s.payload = "image/gif"; break;
                case "jpg": case "jpeg": 
                s.payload = "image/jpeg"; break;
                case "tif": case "tiff": 
                s.payload = "image/tiff"; break;
                case "bmp": 
                s.payload = "image/bmp"; break;
                case "ole": 
                s.payload = "application/x-oleobject"; break;
                case "pcx": 
                s.payload = "application/vnd.zbrush.pcx"; break;
                case "wav": 
                s.payload = "audio/vnd.wave"; break;
                default:
                s.payload = "application/x-"+s.payload; break;
            }
        } 
        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
