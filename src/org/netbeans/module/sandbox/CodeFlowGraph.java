package org.netbeans.module.sandbox;

import org.netbeans.module.sandbox.model.alg.FlowElement;
import org.netbeans.module.sandbox.model.graph.Edge;
import org.netbeans.module.sandbox.model.graph.Graph;

public class CodeFlowGraph extends Graph<FlowElement, String, String> {

    private static final long serialVersionUID = -2644398931307833344L;

    public CodeFlowGraph() {
        super();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("digraph CodeFlow {\n");

        sb
                //.append("packMode=\"node,graph\"").append("\n")
                .append("nodesep=0.2").append("\n")
                .append("rankdir=TB").append("\n")
                .append("splines=spline").append("\n")
                .append("node [margin=0 color=\"#747474\" fontcolor=\"#11343E\" fillcolor=\"#FFFAF3\" fontsize=12 fontname=\"arial\" style=filled]")
                .append("\n")
                .append("edge [color=\"#747474\" fontcolor=\"#01465A\" fontsize=10 fontname=\"arial\" arrowsize=0.4]")
                .append("\n");

        nodes.stream().forEach((node) -> {
            sb.append(node.toString()).append("\n");
        });

        sb.append("\n");
        edges.stream().forEach((edge) -> {
            String leftId = edge.getLeft().getPayload().getId();
            String rightId = edge.getRight().getPayload().getId();

            sb.append(leftId)
                    .append(" -")
                    .append((edge.getDirection() == Edge.Direction.DIRECT) ? '>' : '-')
                    .append(" ")
                    .append(rightId)
                    .append((edge.getPayload() == null) ? "" : "[label=\"" + edge.getPayload() + "\"]")
                    .append("\n");
        });

        sb.append("}\n");
        return sb.toString();
    }

}
