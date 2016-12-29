package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

/**
 *
 * @author Lot
 */
public class StartBlockElement extends AbstractFlowElement {

    private static final long serialVersionUID = -6365182588707202550L;

    private String content;

    public StartBlockElement() {
        super("start_block_" + IDUtility.generateID());
    }

    public String getContent() {
        return content;
    }

    public <T extends StartBlockElement> T setContent(String content) {
        this.content = content;
        return (T) this;
    }

    @Override
    public String toString() {
        return getId() + " [label=\"" + content + "\" shape=invhouse fixedsize=shape width=1.0 height=0.20]";
    }

}
