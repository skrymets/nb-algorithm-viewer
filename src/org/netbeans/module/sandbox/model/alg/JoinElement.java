package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

public class JoinElement extends AbstractFlowElement {

    private static final long serialVersionUID = -3791412494127886651L;

    public JoinElement() {
        super("join_" + IDUtility.generateID());
    }

    @Override
    public String toString() {
        return getId() + " [label=\"\" shape=point]";
    }
}
