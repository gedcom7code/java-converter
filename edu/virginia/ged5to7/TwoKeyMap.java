package edu.virginia.ged5to7;
import java.util.TreeMap;

public class TwoKeyMap<V> {
    private static class StringPair implements Comparable<StringPair> {
        final String a;
        final String b;
        public StringPair(String a, String b) {
            this.a = a; this.b = b;
        }
        public int hashCode() {
            return a.hashCode() ^ b.hashCode();
        }
        public boolean equals(Object that) {
            if (this==that) return true;
            if (!(that instanceof StringPair)) return false;
            StringPair other = (StringPair)that;
            return this.a.equals(other.a) && this.b.equals(other.a);
        }
        public int compareTo(StringPair that) {
            int tmp = a.compareTo(that.a);
            if (tmp != 0) return tmp;
            return b.compareTo(that.b);
        }
    }
    private TreeMap<StringPair, V> backing;
    private TreeMap<String, V> backup;
    
    public TwoKeyMap() {
        backing = new TreeMap<StringPair, V>();
        backup = new TreeMap<String, V>();
    }
    public V get(String a, String b) {
        V ans = backing.get(new StringPair(a,b));
        if (ans == null) return backup.get(b);
        else return ans;
    }
    public V get(String b) {
        return backup.get(b);
    }
    public V put(String b, V val) {
        return backup.put(b, val);
    }
    public V put(String a, String b, V val) {
        return backing.put(new StringPair(a,b), val);
    }
    public int size() { return backing.size() + backup.size(); }
    public boolean isEmpty() { return backing.isEmpty() && backup.isEmpty(); }
}
