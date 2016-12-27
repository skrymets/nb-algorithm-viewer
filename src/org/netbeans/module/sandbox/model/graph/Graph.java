package org.netbeans.module.sandbox.model.graph;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import static org.netbeans.module.sandbox.model.graph.Edge.UNDEFINED_EDGE;

/**
 * @param <N> Graph's nodes payload type
 * @param <E> Graph's edges payload type
 * @param <E> Graph's own payload type
 */
//TODO: Extract interface from Graph!
public class Graph<N, E, G> implements Serializable {

    public static final Graph UNDEFINED_GRAPH = new Graph();

    private static final long serialVersionUID = -6831388494769859912L;

    protected E payload;

    protected final Set<Node<N>> nodes = new HashSet<>();

    protected final Set<Edge<N, E>> edges = new LinkedHashSet<Edge<N, E>>() {
        private static final long serialVersionUID = 3086904407674824236L;

        @Override
        public String toString() {
            return this.stream().map(e -> e.toString()).collect(Collectors.joining("\n"));
        }
    };

    public Graph() {
    }

    public Graph(E payload) {
        this();
        this.payload = payload;
    }

    public E getPayload() {
        return payload;
    }

    public void setPayload(E payload) {
        this.payload = payload;
    }

    public Node<N> createNode(N payload) {
        Node<N> node = new Node<>(payload, this);
        nodes.add(node);

        return node;
    }

    public Set<Node<N>> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    /**
     * Connects two nodes with a new edge, and registers the edge in the parent graph(s)
     *
     * @param <N>         Payload type of the left and right nodes
     * @param <E>         Payload type of the edge itself
     * @param left        Left node
     * @param right       Right node
     * @param direction   Edge's direction
     * @param edgePayload Edge's payload
     *
     * @return a newly created edge, or <code>UNDEFINED_EDGE</code> if either left or
     *         right node (or both) is <code>null</code>
     */
    public static <N, E> Edge<N, E> connectNodes(Node<N> left, Node<N> right, Edge.Direction direction, E edgePayload) {

        if (left == null || right == null) {
            return UNDEFINED_EDGE;
        }

        Edge<N, E> edge = new Edge<>(left, right, (direction == null) ? Edge.Direction.UNDIRECT : direction);
        edge.setPayload(edgePayload);

        left.linkEdge(edge);
        right.linkEdge(edge);

        //TODO: Should graphs be the same? If yes - only one side may provide a reference to the parent graph
        Graph.composite(left.getGraph(), right.getGraph()).registerEdge(edge);

        return edge;
    }

    public static <N, E> Edge<N, E> connectNodes(Node<N> left, Node<N> right, Edge.Direction direction) {
        return connectNodes(left, right, (direction == null) ? Edge.Direction.UNDIRECT : direction, null);
    }

    public static <N, E> Edge<N, E> connectNodes(Node<N> left, Node<N> right, E edgePayload) {
        return connectNodes(left, right, Edge.Direction.UNDIRECT, edgePayload);
    }

    public static <N, E> Edge<N, E> connectNodes(Node<N> left, Node<N> right) {
        return connectNodes(left, right, Edge.Direction.UNDIRECT, null);
    }

    public boolean breakEdge(Edge edge) {
        if (edge == null || !edges.contains(edge)) {
            return false;
        }

        edge.getLeft().unLinkEdge(edge);
        edge.getRight().unLinkEdge(edge);

        return edges.remove(edge);
    }

    boolean registerEdge(Edge e) {
        return edges.add(e);
    }

    public static <T> Graph<T, ?, ?> composite(Graph<T, ?, ?>... graphs) {
        return new CompositeGraph(graphs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("graph CodeFlow {\n");

        nodes.stream().forEach((node) -> {
            sb.append(node.toString()).append("\n");
        });

        edges.stream().forEach((edge) -> {
            sb.append(edge.toString()).append("\n");
        });

        sb.append("\n}");
        return sb.toString();
    }

}

class CompositeGraph<N, E, G> extends Graph<N, E, G> {

    private static final long serialVersionUID = 2244496582260428466L;

    private final Set<Graph<N, E, G>> composite = new HashSet<>();

    CompositeGraph(Graph<N, E, G>... graphs) {
        composite.addAll(Arrays.asList(graphs));
    }

    @Override
    public String toString() {
        return composite.stream()
                .map(graph -> graph.toString())
                .collect(Collectors.toList()).toString();
    }

    @Override
    boolean registerEdge(Edge edge) {
        boolean result = true;
        for (Graph graph : composite) {
            result = (result && graph.registerEdge(edge));
        }
        return result;
    }

    @Override
    public boolean breakEdge(Edge edge) {
        boolean result = true;
        for (Graph graph : composite) {
            result = (result && graph.breakEdge(edge));
        }
        return result;
    }

    @Override
    public Set<Edge> getEdges() {
        //TODO: Should be immutable?
        return composite.stream()
                .map(graph -> graph.getEdges())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Node<N>> getNodes() {
        //TODO: Should be immutable?
        return composite.stream()
                .map(graph -> graph.getNodes())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public Node<?> createNode(Object payload) {
        //TODO: Should we create a node in all graphs? Doesn't seem to be good idea so far.
        return Node.UNDEFINED_NODE;
    }

}
