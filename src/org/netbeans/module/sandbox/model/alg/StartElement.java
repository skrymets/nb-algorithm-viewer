package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

/**
 *
 * @author Lot
 */
public class StartElement extends AbstractFlowElement implements FlowElement {

    private static final long serialVersionUID = 8296622452214940910L;

    public StartElement() {
        super("start_" + IDUtility.generateID());
    }

    @Override
    public String toString() {
        return getId() + " [label=\"\" shape=circle style=filled fillcolor=black fixedsize=shape width=0.2 height=0.2]";
    }

}
