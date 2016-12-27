package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

/**
 *
 * @author Lot
 */
public class StopElement extends AbstractFlowElement {

    private static final long serialVersionUID = 6413310267574358529L;

    public StopElement() {
        super("stop_" + IDUtility.generateID());
    }

    @Override
    public String toString() {
        return getId() + " [label=\"\" shape=doublecircle style=filled fillcolor=black fixedsize=shape width=0.2 height=0.2]";
    }
}
