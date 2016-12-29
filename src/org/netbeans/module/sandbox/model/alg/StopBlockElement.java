package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

/**
 *
 * @author Lot
 */
public class StopBlockElement extends AbstractFlowElement {

    private static final long serialVersionUID = -5518826986816320735L;

    private String content;

    public StopBlockElement() {
        super("stop_block_" + IDUtility.generateID());
    }

    public String getContent() {
        return content;
    }

    public <T extends StopBlockElement> T setContent(String content) {
        this.content = content;
        return (T) this;
    }

    @Override
    public String toString() {
        return getId() + " [label=\"" + content + "\" shape=house fixedsize=shape width=1.0 height=0.20]";
    }

}
