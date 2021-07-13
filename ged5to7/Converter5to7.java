package ged5to7;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Stack;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;

import ged5to7.pipeline.*;

public class Converter5to7 {
    private int lastID;
    private final int ID_BASE;
    private final int ID_TO_SKIP;
    private LinkedList<GedStruct> records;
    private List<String> log;
    
    final static java.nio.charset.Charset UTF8 = java.nio.charset.Charset.forName("UTF-8");
    
    /**
     * Parses file using error-tolerant algorithm and performs full 5to7 conversion.
     */
    public Converter5to7(String filename) {
        this(filename, 10);
    }

    /**
     * Parses file using error-tolerant algorithm and performs full 5to7 conversion.
     * Record IDs are assigned as sequential base-<code>id_base</code> integers.
     */
    public Converter5to7(String filename, int id_base) {
        if (id_base < 2 || id_base > 36) throw new IllegalArgumentException("id_base must be between 2 and 36");
        ID_BASE = id_base;
        if (ID_BASE > 'V'-'A'+10) ID_TO_SKIP = Integer.parseInt("VOID", ID_BASE);
        else ID_TO_SKIP = -1;
        
        records = new LinkedList<GedStruct>();
        log = new LinkedList<String>();
        lastID = -1;
        
        fuzzyParse(filename);
        GedStruct trlr = records.removeLast();
        if (!"TRLR".equals(trlr.tag)) {
            records.add(trlr);
            trlr = new GedStruct(null, "TRLR");
        }
        for(GedStruct s : records) s.tag2uri();
        
        Filter[] filters = {
            new RenameFilter(),
            new AgeDateFilter(),
            new VersionFilter(),
            new NoteFilter(),
            new SourceFilter(),
            new ObjectFilter(),
            new LanguageFilter(),
            new TranFilter(),
            new EnumFilter(),
            new ExidFilter(),
            new FileFilter(),
            new MediaTypeFilter(),
        };
        for(Filter f : filters) {
            java.util.LinkedList<GedStruct> created = new java.util.LinkedList<GedStruct>();
            Iterator<GedStruct> it = records.iterator();
            while(it.hasNext()) {
                GedStruct s = it.next();
                java.util.Collection<GedStruct> r = f.update(s);
                if (r != null) created.addAll(r);
                if (s.sup != null) it.remove();
            }
            records.addAll(created);
        }

        for(GedStruct s : records) s.uri2tag();
        
        reID();
        records.add(trlr);
    }
    
    /**
     * Parses a file, logging but permitting errors, and converts cross-references to pointers.
     */
    private void fuzzyParse(String filename) {
        Stack<GedStruct> stack = new Stack<GedStruct>();
        Map<String,GedStruct> xref = new TreeMap<String,GedStruct>();
        try {
            Files.lines(Paths.get(filename), CharsetDetector.detect(filename)).forEach(line -> {
                try {
                    GedStruct got = new GedStruct(line);
                    while(got.level < stack.size()) stack.pop();
                    if (stack.empty()) records.add(got);
                    else stack.peek().addSubstructure(got);
                    stack.push(got);
                    if (got.id != null) xref.put(got.id, got);
                } catch (IllegalArgumentException ex) {
                    log.add(ex.toString());
                }
            });
            stack.clear();
            
            for(GedStruct record: records) record.convertPointers(xref, true, LinkedList.class);

        } catch(Exception ex) {
            log.add(ex.toString());
        }
    }
    
    /**
     * Outputs a parsed dataset as a GEDCOM file.
     */
    public void dumpTo(java.io.OutputStream out) throws IOException {
        out.write("\uFEFF".getBytes(UTF8));
        for(GedStruct rec : records) out.write(rec.toString().getBytes(UTF8));
    }
    
    /**
     * Allocates and returns the next available record ID.
     */
    private String nextID() {
        if (lastID == ID_TO_SKIP) lastID += 1;
        return "@" + Integer.toString(lastID++, ID_BASE).toUpperCase() + "@";
    }
    
    /**
     * Finds which anchors are actually used, renames those, and scraps unused anchors.
     * Unnecessary by itself, but useful before NOTE/SNOTE heuristic and after adding new records.
     */
    private void reID() {
        for(GedStruct record: records)
            if (record.incoming != null && !record.incoming.isEmpty()) {
                record.id = nextID();
            } else {
                record.id = null;
            }
    }
    
    public static void main(String[] args) {
        System.err.println();
        for(String path : args) {
            System.err.println("\nProcessing "+path+" ...");
            Converter5to7 conv = new Converter5to7(path);
            try { conv.dumpTo(System.out); } catch (IOException ex) { ex.printStackTrace(); }
            for(String err : conv.log) System.err.println("** "+err);
            System.err.println("    ... done with " + path+"\n");
        }
    }
}
