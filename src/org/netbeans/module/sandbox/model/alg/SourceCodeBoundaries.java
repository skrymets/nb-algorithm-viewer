package org.netbeans.module.sandbox.model.alg;

import java.io.Serializable;

/**
 *
 * @author Lot
 */
public class SourceCodeBoundaries implements Serializable {

    private static final long serialVersionUID = 6231071895533490715L;

    public static final SourceCodeBoundaries UNDEFINED = new SourceCodeBoundaries(0L, 0L);

    private final long start;
    private final long end;

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public SourceCodeBoundaries(long start, long end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (int) (this.start ^ (this.start >>> 32));
        hash = 19 * hash + (int) (this.end ^ (this.end >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SourceCodeBoundaries other = (SourceCodeBoundaries) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + start + ':' + end + ']';
    }

}
