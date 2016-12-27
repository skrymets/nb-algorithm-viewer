package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

public class ForkElement extends AbstractFlowElement {

    private static final long serialVersionUID = 2740519371507667871L;

    private String condition;

    public ForkElement() {
        super("if_" + IDUtility.generateID());
    }

    public ForkElement setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public String getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return getId() + " [label=\"" + condition + "\" shape=diamond fixedsize=shape width=1.0 height=0.5]";
    }

}
