package edu.virginia.ged5to7.pipeline;
import edu.virginia.ged5to7.GedStruct;

public interface Filter {
    /**
     * Given a GedStruct, apply the filter to it, returning
     * any new records that need to be applied as a result.
     * If the filter can apply to the structures substructures,
     * this method is responsible for making the recursive changes too.
     */
    public java.util.Collection<GedStruct> update(GedStruct s);
}
