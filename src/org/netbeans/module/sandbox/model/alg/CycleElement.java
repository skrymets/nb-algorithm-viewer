package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

public class CycleElement extends AbstractFlowElement {

    private static final long serialVersionUID = -8394456798993031932L;

    public CycleElement() {
        super("cycle_" + IDUtility.generateID());
    }

    @Override
    public String toString() {
        return getId() + " [label=\"Cycle\" shape=rectangle style=\"filled,rounded\" fixedsize=shape width=1.0 height=0.5]";
    }

}
