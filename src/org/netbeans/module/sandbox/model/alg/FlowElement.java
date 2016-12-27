package org.netbeans.module.sandbox.model.alg;

import java.io.Serializable;

/**
 * @author Lot
 */
public interface FlowElement extends Serializable {

    String getId();

    SourceCodeBoundaries getSourceCodeBoundaries();

    @Override
    String toString();

}
