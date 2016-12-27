package org.netbeans.module.sandbox.model.graph;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Lot
 * @param <T> Edge's nodes payload type
 * @param <P> Edge's own payload type
 */
public class Edge<T, P> implements Serializable {

    public static class Split<T, P> implements Serializable {

        public static final Split UNDEFINED_SPLIT = new Split(UNDEFINED_EDGE, UNDEFINED_EDGE);

        private static final long serialVersionUID = -5130994142685336016L;

        private final Edge<T, P> leftEdge;
        private final Edge<T, P> rightEdge;

        public Split(Edge<T, P> leftEdge, Edge<T, P> rightEdge) {
            this.leftEdge = leftEdge;
            this.rightEdge = rightEdge;
        }

        public Edge<T, P> getLeftEdge() {
            return leftEdge;
        }

        public Edge<T, P> getRightEdge() {
            return rightEdge;
        }

    }

    private static final long serialVersionUID = 1858672683520768369L;

    public static enum Direction {
        /**
         * left-to-right
         */
        DIRECT,
        UNDIRECT
    }

    private final String id;

    private final Node<T> left;
    private final Node<T> right;

    private Direction direction;

    private P payload;

    public static final Edge UNDEFINED_EDGE = new Edge(Node.UNDEFINED_NODE, Node.UNDEFINED_NODE, Direction.UNDIRECT);

    Edge(Node<T> left, Node<T> right, Direction direction) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Neither left or right note can be undefined.");
        }

        this.id = UUID.randomUUID().toString();

        this.left = left;
        this.right = right;
        this.direction = (direction == null) ? Direction.UNDIRECT : direction;
    }

    public Node<T> getLeft() {
        return left;
    }

    public Node<T> getRight() {
        return right;
    }

    public Direction getDirection() {
        return direction;
    }

    public Edge<T, P> setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public P getPayload() {
        return payload;
    }

    public Edge<T, P> setPayload(P payload) {
        this.payload = payload;
        return this;
    }

    public Node<T> getOpposite(Node<T> node) {
        if (left.equals(node)) {
            return right;
        } else if (right.equals(node)) {
            return left;
        } else {
            return Node.UNDEFINED_NODE;
        }
    }

    public void collapse() {
    }

    public Edge<T, P> selfCopy() {
        return Graph.connectNodes(left, right, direction, payload);
    }

    public Split<T, P> insertMiddleNode(Node<T> middleNode) {
        if (middleNode == null || middleNode == Node.UNDEFINED_NODE) {
            return Split.UNDEFINED_SPLIT;
        }

        Graph.composite(left.getGraph(), right.getGraph()).breakEdge(this);

        Edge leftEdge = Graph.connectNodes(left, middleNode, direction, payload);
        Edge rightEdge = Graph.connectNodes(middleNode, right, direction, payload);
        return new Split(leftEdge, rightEdge);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        hash = 47 * hash + Objects.hashCode(this.left);
        hash = 47 * hash + Objects.hashCode(this.right);
        hash = 47 * hash + Objects.hashCode(this.direction);
        // Payload MUST NOT participate in the hash!
        // hash = 47 * hash + Objects.hashCode(this.payload); 
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
        final Edge<?, ?> other = (Edge<?, ?>) obj;
        //TODO: Consider ID in equality (from business point of view)
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.left, other.left)) {
            return false;
        }
        if (!Objects.equals(this.right, other.right)) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        // Payload MUST NOT participate in the eguals!
//        if (!Objects.equals(this.payload, other.payload)) {
//            return false;
//        }
        return true;
    }

    @Override
    public String toString() {
        return left.toString() + " -" + ((direction == Direction.DIRECT) ? '>' : '-') + ' ' + right.toString();
    }

}
