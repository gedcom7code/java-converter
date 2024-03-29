import java.net.URL;
import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;

public class DownloadDefinitions {
    
    private static void download(String urlname, String filename) {
        try {
            URL url = new URL(urlname);
            try(BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
                try(FileOutputStream fos = new FileOutputStream(filename)) {
                    byte[] buffer = new byte[1024];
                    int count=0;
                    while((count = bis.read(buffer,0,buffer.length)) != -1) {
                        fos.write(buffer, 0, count);
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("Unable to download\n  from: "+urlname+ "\n  to: "+filename+"\n  "+ex);
        }
    }
    
    private static void downloadIANALanguageSubtagRegistery(String filename) {
        try {
            URL url = new URL("https://www.iana.org/assignments/language-subtag-registry/language-subtag-registry");
            Scanner from = new Scanner(url.openStream());
            try(FileOutputStream fos = new FileOutputStream(filename)) {
                boolean isLang = false;
                String tag = null;
                while(from.hasNextLine()) {
                    String line = from.nextLine();
                    if (line.startsWith("Type: ")) isLang = line.equals("Type: language");
                    else if (line.startsWith("Subtag: ")) tag = line.substring(8);
                    else if (isLang && line.startsWith("Description: ")) {
                        String key = line.substring(13);
                        // key = key.replaceAll(" \\(.*", ""); // change "Modern Greek (1453-)" to "Modern Greek" but also conflates arr "Karo (Brazil)" and kxh "Karo (Ethiopia)" so disabled for now
                        key = key.replaceAll(" language.*", ""); // change "Bihari languages" to "Bihari"
                        fos.write((key+"\t"+tag+"\n").getBytes("UTF-8"));
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("Unable to download\n  from: https://www.iana.org/assignments/language-subtag-registry/language-subtag-registry\n  to: "+filename+"\n  "+ex);
        }
    }

    
    public static void main(String[] args) {
        download(
            "https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/enumerations.tsv",
            "ged5to7/config/enumerations.tsv"
        );
        download(
            "https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/enumerationsets.tsv",
            "ged5to7/config/enumerationsets.tsv"
        );
        download(
            "https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/payloads.tsv",
            "ged5to7/config/payloads.tsv"
        );
        download(
            "https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/substructures.tsv",
            "ged5to7/config/substructures.tsv"
        );
        download(
            "https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/cardinalities.tsv",
            "ged5to7/config/cardinalities.tsv"
        );
        download(
            "https://github.com/fhiso/legacy-format/raw/master/languages.tsv",
            "ged5to7/config/languages.tsv"
        );
        downloadIANALanguageSubtagRegistery(
            "ged5to7/config/all-languages.tsv"
        );
    }
}
