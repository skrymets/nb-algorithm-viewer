package org.netbeans.module.sandbox.model.alg;

import org.netbeans.module.sandbox.utils.IDUtility;

/**
 *
 * @author Lot
 */
public abstract class AbstractFlowElement implements FlowElement {

    private static final long serialVersionUID = 493026565296419143L;

    protected final String id;

    protected SourceCodeBoundaries sourceCodeBoundaries = SourceCodeBoundaries.UNDEFINED;

    public AbstractFlowElement() {
        this(null);
    }

    public AbstractFlowElement(String id) {
        this.id = (id == null) ? IDUtility.generateID() : id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public SourceCodeBoundaries getSourceCodeBoundaries() {
        return sourceCodeBoundaries;
    }

    public <T extends AbstractFlowElement> T setSourceLines(SourceCodeBoundaries sourceLines) {
        this.sourceCodeBoundaries = sourceLines;
        return (T) this;
    }

    @Override
    public String toString() {
        return getId();
    }

}
