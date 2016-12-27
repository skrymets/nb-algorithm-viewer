package org.netbeans.module.sandbox.ui;

import java.awt.Color;
import javax.swing.GroupLayout;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.w3c.dom.svg.SVGDocument;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.netbeans.module.sandbox.ui//CodeFlowReview//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "CodeFlowReviewTopComponent",
        iconBase = "org/netbeans/module/sandbox/ui/arrows-split.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "rightSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "org.netbeans.module.sandbox.ui.CodeFlowReviewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_CodeFlowReviewAction",
        preferredID = "CodeFlowReviewTopComponent"
)
@Messages({
    "CTL_CodeFlowReviewAction=CodeFlowReview",
    "CTL_CodeFlowReviewTopComponent=CodeFlowReview Window",
    "HINT_CodeFlowReviewTopComponent=This is a CodeFlowReview window"
})
public final class CodeFlowReviewTopComponent extends TopComponent {

    private static final long serialVersionUID = 5219299625668111931L;

    public static final String TOP_COMPONENT_ID = "CodeFlowReviewTopComponent";

    private static CodeFlowReviewTopComponent instance;

    private final JSVGCanvas canvas;

    public CodeFlowReviewTopComponent() {
        canvas = new JSVGCanvas();
        canvas.setDocumentState(JSVGCanvas.ALWAYS_INTERACTIVE);

        initComponents();
        setName(Bundle.CTL_CodeFlowReviewTopComponent());
        setToolTipText(Bundle.HINT_CodeFlowReviewTopComponent());

        initAdditionalComponents();

    }

    public static synchronized CodeFlowReviewTopComponent getInstance() {
        TopComponent window = WindowManager.getDefault().findTopComponent(TOP_COMPONENT_ID);
        if (window == null) {
            return getDefault();
        }

        if (window instanceof CodeFlowReviewTopComponent) {
            return (CodeFlowReviewTopComponent) window;
        }

        return getDefault();
    }

    public static synchronized CodeFlowReviewTopComponent getDefault() {
        if (instance == null) {
            instance = new CodeFlowReviewTopComponent();
        }
        return instance;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private void initAdditionalComponents() {

        JSVGScrollPane canvasScrollPane = new JSVGScrollPane(canvas);
        canvasScrollPane.setBackground(Color.BLUE);

        GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(canvasScrollPane, GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(canvasScrollPane, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
        );

        this.add(canvasScrollPane);

    }

    public void setDocument(SVGDocument document) {
        canvas.setDocument(document);
    }
}