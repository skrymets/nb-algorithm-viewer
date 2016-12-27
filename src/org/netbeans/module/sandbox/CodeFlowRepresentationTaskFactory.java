package org.netbeans.module.sandbox;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Lot
 */
public class CodeFlowRepresentationTaskFactory extends CaretAwareJavaSourceTaskFactory {

    public CodeFlowRepresentationTaskFactory() {
        super(
                JavaSource.Phase.ELEMENTS_RESOLVED,
                JavaSource.Priority.LOW,
                "text/x-java"
        );
    }

    @Override
    protected CancellableTask<CompilationInfo> createTask(FileObject file) {
        return new CodeFlowRepresentationTask(file);
    }

}
