package edu.virginia.ged5to7;

/**
 * Java's String, CharSequence, CharBuffer, and Stream all make various simple
 * character-level operations tedious. This class tries to approximate some of
 * the flexibility of C's char** in Java. It can be used like CharBuffer, or
 * a stack, or a queue.
 * 
 * Like C, this is unchecked; if you push to a full buffer it silently does
 * the wrong thing instead of throwing an exception.
 */
public class CharStackQueue {
    char[] backing;
    int i0, i1;
    public CharStackQueue(int capacity) {
        backing = new char[capacity+1];
        i0 = i1 = 0;
    }
    public CharStackQueue(String s) {
        backing = (s+"\0").toCharArray();
        i0 = 0;
        i1 = backing.length - 1;
    }
    public void push(char c) {
        backing[i1] = c;
        i1 += 1;
        i1 %= backing.length;
    }
    public char pop() {
        i1 += backing.length-1;
        i1 %= backing.length;
        return backing[i1];
    }
    public char peek() {
        return backing[(i1 + backing.length-1)%backing.length];
    }
    public char front() {
        return backing[i0];
    }
    public char shift() {
        char ans = backing[i0];
        i0 += 1;
        i0 %= backing.length;
        return ans;
    }
    public String unused() {
        if (i0 > i1) return String.copyValueOf(backing, i0, backing.length-i0) + String.copyValueOf(backing, 0, i1);
        else return String.copyValueOf(backing, i0, i1-i0);
    }
    public void clear() { i0 = i1 = 0; }
    public boolean isEmpty() { return i0 == i1; }
}
