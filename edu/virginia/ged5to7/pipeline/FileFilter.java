package edu.virginia.ged5to7.pipeline;
import edu.virginia.ged5to7.GedStruct;

public class FileFilter implements Filter {
    private static String escapePath(String url) {
        url = url.replace("%", "%25");
        url = url.replace(":", "%3A");
        url = url.replace("?", "%3F");
        url = url.replace("#", "%23");
        url = url.replace("[", "%5B");
        url = url.replace("]", "%5D");
        url = url.replace("@", "%40");
        return url;
    }
    public java.util.Collection<GedStruct> update(GedStruct s) {
        if ("https://gedcom.io/terms/v7/FILE".equals(s.uri)) {
            String url = s.payload;
            
            if (url.contains("://")) {
                // URL schema://host.com/path/to/file.ext
            } else if (url.startsWith("\\\\")) {
                // Microsoft's network location notation \\server\path\to\file.ext
                url = "file:"+escapePath(url.replace('\\','/'));
            } else if (url.matches("[A-Za-z]:\\\\.*")) {
                // Microsoft's absolute c:\path\to\file.ext
                url = "file:///"+url.charAt(0)+":/"+escapePath(url.substring(3).replace('\\','/'));
            } else if (url.startsWith("/")) {
                // POSIX's absolute /path/to/file.ext
                url = "file://"+escapePath(url);
            } else {
                // Microsoft's relative path\to\file.ext
                // POSIX's relative path/to/file.ext
                url = escapePath(url.replace('\\','/'));
            }

            s.payload = url;
        }
        for(GedStruct s2 : s.sub) update(s2);
        return null;
    }
}
