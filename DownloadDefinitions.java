import java.net.URL;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

public class DownloadDefinitions {
    public static void main(String[] args) throws IOException {
        try (PrintWriter dest = new PrintWriter(new File("edu/virginia/ged5to7/GedcomDefinitions.java"))) {
            dest.println("/* WARNING: This file is automatically generated and should not be edited by hand */");
            dest.println("package edu.virginia.ged5to7;");
            dest.println("import static java.util.Arrays.binarySearch;");
            dest.println("\n/** A container for the substructure, payload, and enumeration rules from gedcom.io */");
            dest.println("public class GedcomDefinitions {");
                

            Scanner s;
            char before;
            ArrayList<String> lines;
            
            // enumerations
            s = new Scanner(new URL("https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/enumerations.tsv").openStream());
            lines = new ArrayList<String>();
            while(s.hasNext()) lines.add(s.nextLine());
            lines.sort(null);
            dest.println("    private static final String[] enumKeys =");
            before = '{';
            for(String line : lines) {
                dest.println("        "+before+'"'+line.replaceAll("\t[^\t]*$","\""));
                before = ',';
            }
            dest.println("        };");
            dest.println("    private static final String[] enumVals =");
            before = '{';
            for(String line : lines) {
                dest.println("        "+before+line.replaceAll(".*\t","\"")+"\"");
                before = ',';
            }
            dest.println("        };");
            dest.println("    /** Looks up the URI of an enumeration based on the GEDCOM 7 spec");
            dest.println("     * @param ctx the URI of the containing structure.");
            dest.println("     *            use <code>null</code> for an extension.");
            dest.println("     * @param tag the enumeration value");
            dest.println("     * @return the URI of the enumeration value, or <code>null</code> if unknown");
            dest.println("     */");
            dest.println("    public static String enumURI(String ctx, String tag) {");
            dest.println("        if (ctx == null) {");
            dest.println("            String val = \"https://gedcom.io/terms/v7/\"+tag;");
            dest.println("            int idx = binarySearch(enumVals, val);");
            dest.println("            if (idx < 0) return null;");
            dest.println("            return enumVals[idx];");
            dest.println("        } else {");
            dest.println("            String key = ctx+'\\t'+tag;");
            dest.println("            int idx = binarySearch(enumKeys, key);");
            dest.println("            if (idx < 0) return null;");
            dest.println("            return enumVals[idx];");
            dest.println("        }");
            dest.println("    }");
            
            dest.println();
            
            // substructures
            s = new Scanner(new URL("https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/substructures.tsv").openStream());
            lines = new ArrayList<String>();
            while(s.hasNext()) lines.add(s.nextLine());
            lines.sort(null);
            dest.println("    private static final String[] structKeys =");
            before = '{';
            for(String line : lines) {
                dest.println("        "+before+'"'+line.replaceAll("\t[^\t]*$","\""));
                before = ',';
            }
            dest.println("        };");
            dest.println("    private static final String[] structVals =");
            before = '{';
            for(String line : lines) {
                dest.println("        "+before+line.replaceAll(".*\t","\"")+"\"");
                before = ',';
            }
            dest.println("        };");
            dest.println("    /** Looks up the URI of an structure type based on the GEDCOM 7 spec");
            dest.println("     * @param ctx the URI of the containing structure type");
            dest.println("     *            use <code>\"\"</code> for a record and <code>null</code> for an extension.");
            dest.println("     * @param tag the tag of the structure");
            dest.println("     * @return the URI of the structure type, or <code>null</code> if unknown");
            dest.println("     */");
            dest.println("    public static String structURI(String ctx, String tag) {");
            dest.println("        if (ctx == null) {");
            dest.println("            String val = \"https://gedcom.io/terms/v7/\"+tag;");
            dest.println("            int idx = binarySearch(structVals, val);");
            dest.println("            if (idx < 0) return null;");
            dest.println("            return structVals[idx];");
            dest.println("        } else {");
            dest.println("            String key = ctx+'\\t'+tag;");
            dest.println("            int idx = binarySearch(structKeys, key);");
            dest.println("            if (idx < 0) return null;");
            dest.println("            return structVals[idx];");
            dest.println("        }");
            dest.println("    }");
                  
            // structure types -- uses same file and substructures above
            TreeMap<String,String> knownStructs = new TreeMap<String, String>();
            for(String line : lines) {
                  knownStructs.put(line.replaceAll(".*\t",""), line.replaceAll("^[^\t]*\t|\t[^\t]*$",""));
            }
            dest.println("    private static final String[] knownStructs =");
            before = '{';
            for(String uri : knownStructs.keySet()) {
                dest.println("        "+before+'"'+uri+'"');
                before = ',';
            }
            dest.println("        };");
            dest.println("    private static final String[] uriTag =");
            before = '{';
            for(String tag : knownStructs.values()) {
                dest.println("        "+before+'"'+tag+'"');
                before = ',';
            }
            dest.println("        };");
            dest.println("    /** Looks up the tag of a structure URIbased on the GEDCOM 7 spec");
            dest.println("     * @param uri the URI of the structure type");
            dest.println("     * @return the tag of the structure type, or <code>null</code> if unknown");
            dest.println("     */");
            dest.println("    public static String structTag(String uri) {");
            dest.println("        if (uri == null) return null;");
            dest.println("        int idx = binarySearch(knownStructs, uri);");
            dest.println("        if (idx < 0) return null;");
            dest.println("        return uriTag[idx];");
            dest.println("    }");


            
            dest.println();
            
            // payloads
            s = new Scanner(new URL("https://github.com/FamilySearch/GEDCOM/raw/main/extracted-files/payloads.tsv").openStream());
            lines = new ArrayList<String>();
            while(s.hasNext()) lines.add(s.nextLine());
            lines.sort(null);
            dest.println("    private static final String[] payloadKeys =");
            before = '{';
            for(String line : lines) {
                dest.println("        "+before+'"'+line.replaceAll("\t[^\t]*$","\""));
                before = ',';
            }
            dest.println("        };");
            dest.println("    private static final String[] payloadVals =");
            before = '{';
            for(String line : lines) {
                dest.println("        "+before+line.replaceAll(".*\t","\"")+"\"");
                before = ',';
            }
            dest.println("        };");
            dest.println("    /** Looks up the payload type of a structure based on the GEDCOM 7 spec");
            dest.println("     * @param ctx the URI of the containing structure type");
            dest.println("     * @return the type code (URI or <code>\"Y|<NULL>\"</code> or <code>\"\"</code> or <code>\"@XREF:</code>tag<code>\"</code>) of the payload type, or <code>null</code> if unknown");
            dest.println("     */");
            dest.println("    public static String payloadURI(String ctx) {");
            dest.println("        if (ctx == null) return null;");
            dest.println("        int idx = binarySearch(payloadKeys, ctx);");
            dest.println("        if (idx < 0) return null;");
            dest.println("        return payloadVals[idx];");
            dest.println("    }");
            
            
            dest.println("}");
        }
    }
}