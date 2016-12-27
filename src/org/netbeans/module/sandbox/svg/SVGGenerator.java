package org.netbeans.module.sandbox.svg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.util.XMLResourceDescriptor;
import org.netbeans.module.sandbox.dot.DotBridge;
import org.w3c.dom.svg.SVGDocument;

public class SVGGenerator {

    private SVGGenerator() {
    }

    public static SVGDocument convertDOT2SVG(String dotInput) {

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

        try {
            String svgSource = new DotBridge().convertDOT2SVG(dotInput);
            SVGOMDocument document = (SVGOMDocument) factory.createSVGDocument(null, new ByteArrayInputStream(svgSource.getBytes("UTF-8")));
            return document;
        } catch (IOException ex) {
            return null;
        }
    }

}
