package org.netbeans.module.sandbox;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.module.sandbox.svg.SVGDocumentInteractionListener;
import org.netbeans.module.sandbox.svg.SVGGenerator;
import org.netbeans.module.sandbox.ui.CodeFlowReviewTopComponent;
import org.openide.filesystems.FileObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author Lot
 */
public class CodeFlowRepresentationTask implements CancellableTask<CompilationInfo> {

    //TODO: Get rid of this in production code
    private static final InputOutput IO = IOProvider.getDefault().getIO("Sandbox", false);

    private CodeFlowScanner scanner;

    private final FileObject file;

    CodeFlowRepresentationTask(FileObject file) {
        this.file = file;
    }

    @Override
    public void cancel() {
        if (scanner != null) {
            scanner.cancel();
        }
    }

    @Override
    public void run(CompilationInfo info) throws Exception {

        int initialCaretPosition = CodeFlowRepresentationTaskFactory.getLastPosition(file);

        // Serach for a method that encloses a statement at the caret position
        TreePath currentPath = info.getTreeUtilities().pathFor(initialCaretPosition);
        while ((currentPath.getLeaf().getKind() != Tree.Kind.METHOD)) {
            currentPath = currentPath.getParentPath();
            if (currentPath == null) {
                IO.getOut().append("Mehod node not found.\n");
                return; // there is nothing to process. How it's possible?
            }
        }

        scanner = new CodeFlowScanner(info);
        scanner.scan(currentPath, null);
        CodeFlowGraph flowGraph = scanner.getGraph();

        String dotString = flowGraph.toString();
        IO.getOut().print(dotString);

        SVGDocument document = SVGGenerator.convertDOT2SVG(dotString);

        if (document != null) {
            //TODO: Move this to the outer level. This is not the correct place to make this decision.
            attachSVGEventsProcessor(document);
            SwingUtilities.invokeLater(() -> {
                CodeFlowReviewTopComponent topComponent = CodeFlowReviewTopComponent.getInstance();
                topComponent.setDocument(document);
            });
        }

    }

    protected static void attachSVGEventsProcessor(SVGDocument document) {

        document.getDocumentElement().normalize();

        NodeList nodes = document.getElementsByTagName("g");
        System.out.println("Document nodes: " + nodes.getLength());

        for (int i = 0; i < nodes.getLength(); i++) {

            Element element = (Element) nodes.item(i);

            if (element.hasAttribute("id")) {
                String elementId = element.getAttribute("id");
                EventTarget target = (EventTarget) element;
                target.addEventListener(SVGDocumentInteractionListener.EVENT, new SVGDocumentInteractionListener(elementId), true);
            }
        }
    }

}
