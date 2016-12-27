package org.netbeans.module.sandbox.model.graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.netbeans.module.sandbox.model.graph.Edge.Direction;

/**
 *
 * @author Lot
 * @param <N> Node's payload type
 */
public class Node<N> implements Serializable {

    public static final Node UNDEFINED_NODE = new Node("d4e71863c0fc", Graph.UNDEFINED_GRAPH);

    private static final long serialVersionUID = -9115175656874322412L;

    private final Graph<N, ?, ?> graph;

    //TODO: Decide whether it should be a List instead of a Set. Should we consider completely identical edges as alternatives?
    private final Collection<Edge<N, ?>> edges = new LinkedHashSet<>();

    private N payload;

    Node(N payload, Graph<N, ?, ?> graph) {
        this.payload = payload;
        this.graph = graph;
    }

    enum InOut {
        IN, OUT
    };

    public Collection<Edge<N, ?>> getEdges() {
        return Collections.unmodifiableCollection(edges);
    }

    /**
     * Returns a collection of the node's incoming edges.
     *
     * @param includeUndirected Should the undirected edges be considered as incoming
     *                          either?
     *
     * @return an unmodifiable collection of edges.
     */
    public Collection<Edge<N, ?>> getIncomingEdges(boolean includeUndirected) {
        return getEdges(InOut.IN, includeUndirected);
    }

    /**
     * Returns a collection of the node's outgoing edges.
     *
     * @param includeUndirected Should the undirected edges be considered as outgoing
     *                          either?
     *
     * @return an unmodifiable collection of edges.
     */
    public Collection<Edge<N, ?>> getOutgoingEdges(boolean includeUndirected) {
        return getEdges(InOut.OUT, includeUndirected);
    }

    /**
     * Returns a collection of edges by their direction towards the node.
     *
     * @param inOut             Incoming or outgoing edges filter.
     * @param includeUndirected Should the undirected edges be considered as
     *                          <code>inOut</code> either?
     *
     * @return an unmodifiable collection of edges.
     */
    Collection<Edge<N, ?>> getEdges(InOut inOut, boolean includeUndirected) {

        return edges.stream().filter(edge -> {
            boolean filter = false;
            switch (inOut) {
                case IN:
                    filter = (edge.getRight() == this);
                    break;
                case OUT:
                    filter = (edge.getLeft() == this);
                    break;
            }
            return filter;
        }).filter(edge -> {
            return (edge.getDirection() == Direction.DIRECT || (edge.getDirection() == Direction.UNDIRECT && includeUndirected));
        }).collect(Collector.of(LinkedHashSet<Edge<N, ?>>::new, Set::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableSet));
    }

    public N getPayload() {
        return payload;
    }

    public void setPayload(N payload) {
        this.payload = payload;
    }

    public Graph<N, ?, ?> getGraph() {
        return graph;
    }

    public <E> Edge<N, E> connectNodeFromRight(Node<N> rightNode) {
        return Graph.<N, E>connectNodes(this, rightNode, Direction.DIRECT);
    }

    public <E> Edge<N, E> connectNodeFromLeft(Node<N> leftNode) {
        return Graph.<N, E>connectNodes(leftNode, this, Direction.DIRECT);
    }

    public <E> Edge<N, E> connect(Node<N> otherNode) {
        return Graph.<N, E>connectNodes(this, otherNode, Direction.UNDIRECT);
    }

    boolean linkEdge(Edge e) {
        return edges.add(e);
    }

    boolean unLinkEdge(Edge e) {
        if (e == null || !edges.contains(e)) {
            return false;
        }
        return edges.remove(e);
    }

    public Set<Node> getLinkedNodes() {
        if (edges.isEmpty()) {
            return Collections.emptySet();
        }

        return edges.stream()
                .map((Edge edge) -> edge.getOpposite(Node.this))
                // avoid multiple node copies if there are more than one linked edges
                .distinct()
                .collect(Collectors.toSet());
    }

    //TODO: Decide whether it should be a List instead of a Set. Should we consider completely identical edges as alternatives?
    public Set<Edge> getEdgesToNode(Node destination) {
        return edges.stream()
                .filter((Edge edge) -> (edge.getOpposite(Node.this).equals(destination)))
                .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.payload);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node<?> other = (Node<?>) obj;
        //TODO: Consider nodes equality in the comparison, but avoid endless recursion!
        if (!Objects.equals(this.payload, other.payload)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return (payload == null) ? "node_" + hashCode() : payload.toString();
    }

}
