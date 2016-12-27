package org.netbeans.module.sandbox.svg;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author Lot
 */
public class SVGDocumentInteractionListener implements EventListener {

    public static final String EVENT = "click";

    private final String nodeId;

    public SVGDocumentInteractionListener(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void handleEvent(Event evt) {
        // if (evt.getType().equals(processedEventName)) {
        //Here is where I want to get the clicked path from the interaction with the DOM document
        // Element el = (Element) evt.getTarget();
        System.out.println("Click on: " + nodeId);
        // evt.stopPropagation();

        //}
    }
}
