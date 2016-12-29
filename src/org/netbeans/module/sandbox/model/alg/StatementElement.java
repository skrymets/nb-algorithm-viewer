package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

/**
 *
 * @author Lot
 */
public class StatementElement extends AbstractFlowElement {

    private static final long serialVersionUID = -4607983324082601217L;

    private String content;

    public StatementElement() {
        super("statement_" + IDUtility.generateID());
    }

    public String getContent() {
        return content;
    }

    public <T extends StatementElement> T setContent(String content) {
        this.content = content;
        return (T) this;
    }

    @Override
    public String toString() {
        return getId() + " [label=\"" + content + "\" shape=rect fixedsize=shape width=1.0 height=0.35]";
    }

}
