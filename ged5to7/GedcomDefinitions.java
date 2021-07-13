package ged5to7;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;
import java.io.IOException;


public class GedcomDefinitions {
    private HashMap<String,String> cards, enums, langs, morelangs, pays, subs, tagOf;
    private HashSet<String> structSet, enumSet, singular;
    private HashMap<String, HashSet<String>> required;
    private static GedcomDefinitions engine;
    
    private static HashMap<String,String> readTSV(Scanner s) {
        HashMap<String,String> ans = new HashMap<String, String>();
        while (s.hasNextLine()) {
            String line = s.nextLine();
            int lio = line.lastIndexOf('\t');
            if (lio < 0) continue;
            ans.put(line.substring(0, lio), line.substring(lio+1));
        }
        return ans;
    }
    
    private void addTags(HashMap<String,String> src) {
        for(String key : src.keySet()) {
            String tag = key.split("\t")[1];
            String val = src.get(key);
            String old = tagOf.get(val);
            if (old != null && !tag.equals(old))
                throw new RuntimeException("ERROR: uri "+val+" has multiple tags\n\t- "+old+"\n\t- "+tag);
            else if (old == null) tagOf.put(val, tag);
        }
    }

    
    private GedcomDefinitions() {
        tagOf = new HashMap<String,String>();
        
        cards = readTSV(new Scanner(getClass().getResourceAsStream("config/cardinalities.tsv")));
        required = new HashMap<String,HashSet<String>>();
        singular = new HashSet<String>();
        cards.forEach((k,v) -> {
            if (v.charAt(1) == '1') {
                String[] k2 = k.split("\t");
                required.putIfAbsent(k2[0], new HashSet<String>());
                required.get(k2[0]).add(k2[1]);
            }
            if (v.charAt(3) == '1') singular.add(k);
        });
        
        pays = readTSV(new Scanner(getClass().getResourceAsStream("config/payloads.tsv")));

        enums = readTSV(new Scanner(getClass().getResourceAsStream("config/enumerations.tsv")));
        enumSet = new HashSet<String>(enums.values());
        addTags(enums);
        
        subs = readTSV(new Scanner(getClass().getResourceAsStream("config/substructures.tsv")));
        subs.put("\tHEAD", "HEAD pseudostructure"); //// HARD-CODE based on substructures.tsv implementation
        structSet = new HashSet<String>(subs.values());
        addTags(subs);

        langs = readTSV(new Scanner(getClass().getResourceAsStream("config/languages.tsv")));
        for(String key : langs.keySet()) { // remove trailing '*' from ELF's tsv
            String val = langs.get(key);
            if (val.endsWith("*")) {
                langs.put(key, val.substring(0,val.length()-1));
            }
        }
        morelangs = readTSV(new Scanner(getClass().getResourceAsStream("config/all-languages.tsv")));
    }
    public static GedcomDefinitions getDefinitions() {
        if (engine == null) engine = new GedcomDefinitions();
        return engine;
    }

    public Collection<String> requiredSubstructures(String struct) {
        Collection<String> ans = required.get(struct);
        if (ans == null) ans = Collections.emptySet();
        return ans;
    }
    public boolean justOne(String ctx, String uri) {
        if (ctx == null) return false;
        return singular.contains(ctx+'\t'+uri);
    }

    /** Looks up the URI of an enumeration based on the GEDCOM 7 spec
     * @param ctx the URI of the containing structure.
     *            use <code>null</code> for an extension.
     * @param tag the enumeration value
     * @return the URI of the enumeration value, or <code>null</code> if unknown
     */
    public String enumURI(String ctx, String tag) {
        if (ctx == null) {
            String val = "https://gedcom.io/terms/v7/"+tag;
            if (enumSet.contains(val)) return val;
            return null;
        } else {
            String key = ctx+'\t'+tag;
            return enums.get(key);
        }
    }
    public boolean isStdEnum(String uri) {
        return enumSet.contains(uri);
    }

    /** Looks up the URI of an structure type based on the GEDCOM 7 spec
     * @param ctx the URI of the containing structure type
     *            use <code>""</code> for a record and <code>null</code> for an extension.
     * @param tag the tag of the structure
     * @return the URI of the structure type, or <code>null</code> if unknown
     */
    public String structURI(String ctx, String tag) {
        if (ctx == null) {
            String val = "https://gedcom.io/terms/v7/"+tag;
            if (structSet.contains(val)) return val;
            return null;
        } else {
            String key = ctx+'\t'+tag;
            return subs.get(key);
        }
    }
    public boolean isStdStruct(String uri) {
        return structSet.contains(uri);
    }

    /** Looks up the tag of a structure URI based on the GEDCOM 7 spec
     * @param uri the URI of the structure type
     * @return the tag of the structure type, or <code>null</code> if unknown
     */
    public String structTag(String uri) {
        if (uri == null) return null;
        return tagOf.get(uri);
    }
    /** Looks up the payload type of a structure based on the GEDCOM 7 spec
     * @param ctx the URI of the containing structure type
     * @return the type code (URI or <code>"Y|<NULL>"</code> or <code>""</code> or <code>"@XREF:</code>tag<code>"</code>) of the payload type, or <code>null</code> if unknown
     */
    public String payloadURI(String ctx) {
        if (ctx == null) return null;
        return pays.get(ctx);
    }
    /** Looks up the language tag type of a language based ELF's mapping
     * @param lang the 5.5.1 language name
     * @return the BCP-47 language tag, or <code>null</code> if unknown
     */
    public String langTag(String ctx) {
        if (ctx == null) return null;
        String ans = langs.get(ctx);
        if (ans == null) ans = morelangs.get(ctx);
        return ans;
    }
    
    public static void main(String[] args) {
        new GedcomDefinitions();
    }
}
