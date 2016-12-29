package org.netbeans.module.sandbox;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.module.sandbox.model.alg.StatementElement;
import org.netbeans.module.sandbox.model.alg.CycleElement;
import org.netbeans.module.sandbox.model.alg.FlowElement;
import org.netbeans.module.sandbox.model.alg.ForkElement;
import org.netbeans.module.sandbox.model.alg.JoinElement;
import org.netbeans.module.sandbox.model.alg.StartBlockElement;
import org.netbeans.module.sandbox.model.alg.StartElement;
import org.netbeans.module.sandbox.model.alg.StopBlockElement;
import org.netbeans.module.sandbox.model.alg.StopElement;
import org.netbeans.module.sandbox.model.graph.Edge;
import org.netbeans.module.sandbox.model.graph.Node;
import org.openide.util.Exceptions;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * @author Lot
 */
public class CodeFlowScanner extends TreePathScanner<Edge, Edge<FlowElement, String>> {

    private static final InputOutput IO = IOProvider.getDefault().getIO("Sandbox", false);

    private final CompilationInfo compilationInfo;

    private final SourceCodeHelper sourceCodeHelper;

    private final CodeFlowGraph graph;
    private final Node<FlowElement> entryPoint;
    private final Node<FlowElement> exitPoint;
    private final Edge<FlowElement, String> initialFlow;

    public final ThreadLocal<AtomicBoolean> stopScan = new ThreadLocal<>();

    public CodeFlowScanner(CompilationInfo compilationInfo) {
        super();

        this.stopScan.set(new AtomicBoolean(false));

        this.compilationInfo = compilationInfo;
        this.sourceCodeHelper = new SourceCodeHelper(
                compilationInfo.getCompilationUnit(),
                compilationInfo.getTrees().getSourcePositions()
        );

        graph = new CodeFlowGraph();
        entryPoint = graph.createNode(new StartElement());

        exitPoint = graph.createNode(new StopElement());
        // Use "always-linked-insert-in-the-middle" approach.
        // ( ) --> (*)
        initialFlow = exitPoint.<String>connectNodeFromLeft(entryPoint);
        initialFlow.setPayload("initialFlow");
    }

    @Override
    public Edge<FlowElement, String> visitReturn(ReturnTree tree, Edge<FlowElement, String> contextEdge) {
        checkIsCancelled();

        Node<FlowElement> targetNode = contextEdge.getRight();
        if (targetNode == exitPoint) {
            return contextEdge;
        }

        // Reassign the source node to the exit point
        Node<FlowElement> sourceNode = contextEdge.getLeft();
        Edge<FlowElement, String> followToExit = sourceNode.connectNodeFromRight(exitPoint);
        followToExit.setPayload(contextEdge.getPayload());
        graph.breakEdge(contextEdge);

        return followToExit;

        // return super.visitReturn(tree, contextEdge);
    }

    @Override
    public Edge<FlowElement, String> visitIf(IfTree tree, Edge<FlowElement, String> flow) {
        checkIsCancelled();

        // flow.setPayload("flow");
        ForkElement fork = new ForkElement()
                .setCondition(tree.getCondition().toString())
                .setSourceLines(sourceCodeHelper.getSourceCodeBoundaries(tree));

        JoinElement join = new JoinElement();

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // BEGIN: (X) --[flow]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Edge.Split<FlowElement, String> forkEdges = flow.insertMiddleNode(graph.createNode(fork));
        Edge<FlowElement, String> edgeAfterFork = forkEdges.getRightEdge();
        //edgeAfterFork.setPayload("edgeAfterFork");

        Edge.Split<FlowElement, String> joinEdges = edgeAfterFork.insertMiddleNode(graph.createNode(join));

        Edge<FlowElement, String> thenFlow = joinEdges.getLeftEdge();
        thenFlow.setPayload("true");

        Edge<FlowElement, String> elseFlow = thenFlow.selfCopy();
        elseFlow.setPayload("false");

        Edge<FlowElement, String> edgeAfterJoin = joinEdges.getRightEdge();
        //edgeAfterJoin.setPayload("edgeAfterJoin");
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //                    /--[thenFlowEdge]-->\
        // END: (X) --> (fork)                     (join) --[edgeAfterJoin]--> (Y)
        //                    \--[elseFlowEdge]-->/
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        StatementTree thenStatement = tree.getThenStatement();
        if (thenStatement != null) {
            checkIsCancelled();
            super.scan(thenStatement, thenFlow);
        }

        StatementTree elseStatement = tree.getElseStatement();
        if (elseStatement != null) {
            checkIsCancelled();
            super.scan(elseStatement, elseFlow);
        }

        edgeAfterJoin.setPayload(null);
        return edgeAfterJoin;
    }

    @Override
    public Edge<FlowElement, String> visitTry(TryTree node, Edge<FlowElement, String> flow) {

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // (X) --[flow]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Node<FlowElement> startTryNode = graph.createNode(new StartBlockElement().setContent("try"));
        Edge.Split<FlowElement, String> tryFlows = flow.insertMiddleNode(startTryNode);
        Edge<FlowElement, String> tryActionsEdge;
        tryActionsEdge = tryFlows.getRightEdge().setPayload(null);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // (X) --[flow]--> (startTryNode) --[tryActionsEdge]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Node<FlowElement> endTryNode = graph.createNode(new StopBlockElement().setContent(""));
        Edge.Split<FlowElement, String> endTryEdges = tryActionsEdge.insertMiddleNode(endTryNode);
        tryActionsEdge = endTryEdges.getLeftEdge();
        Edge<FlowElement, String> flowAfterTry = endTryEdges.getRightEdge();
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // (X) --[flow]--> (startTryNode) --[tryActionsEdge]--> (endTryNode) --[flowAfterTry]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Node<FlowElement> catchNode = graph.createNode(new StartBlockElement().setContent("catch"));
        Edge.Split<FlowElement, String> catchEdges = tryActionsEdge.insertMiddleNode(catchNode);
        tryActionsEdge = catchEdges.getLeftEdge();
        Edge<FlowElement, String> catchEdge = catchEdges.getRightEdge();
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // (X) --[flow]--> (startTryNode) --[tryActionsEdge]--> (catchNode) --[catchEdge]--> (endTryNode) --[flowAfterTry]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BlockTree finallyBlock = node.getFinallyBlock();
        if (finallyBlock != null) {
            Node<FlowElement> finallyNode = graph.createNode(new StartBlockElement().setContent("finally"));
            Edge.Split<FlowElement, String> finallyEdges = catchEdge.insertMiddleNode(finallyNode);
            catchEdge = finallyEdges.getLeftEdge();
            Edge<FlowElement, String> finallyEdge = finallyEdges.getRightEdge();
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // ... --> (catchNode) --[catchEdge]--> (finallyNode) --[finallyEdge]--> (endTryNode) --[flowAfterTry]--> (Y)
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            checkIsCancelled();
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            super.scan(finallyBlock, finallyEdge);
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BlockTree actions = node.getBlock();
        if (actions != null) {
            checkIsCancelled();
            super.scan(actions, tryActionsEdge);
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        List<? extends CatchTree> catches = node.getCatches();
        if (catches == null || catches.isEmpty()) {

        } else {
            final Edge<FlowElement, String> holder = catchEdge;
            Iterator<Edge<FlowElement, String>> clones = (catches.size() > 1
                    ? new ArrayList<Edge<FlowElement, String>>() {
                private static final long serialVersionUID = 1L;

                {
                    add(holder);
                    addAll(holder.selfCopy(catches.size() - 1));
                }
            }
                    : Collections.singletonList(catchEdge)).iterator();
            for (CatchTree ct : catches) {
                BlockTree catchBlock = ct.getBlock();
                if (catchBlock != null) {
                    checkIsCancelled();
                    Edge<FlowElement, String> catchEdgeCopy = clones.next();
                    catchEdgeCopy.setPayload("exception");
                    super.scan(catchBlock, catchEdgeCopy);
                }
            }
        }

        return flowAfterTry;
    }

    @Override
    public Edge<FlowElement, String> visitThrow(ThrowTree node, Edge<FlowElement, String> flow) {

        //TODO: Show/create exceptionPoint only if there is at least one throw statement
        //TODO: Change shape/color of the throw's exceptionPoint
        Node<FlowElement> throwNode = graph.createNode(new StatementElement().setContent(node.getExpression().toString()));
        Edge.Split<FlowElement, String> throwFlows = flow.insertMiddleNode(throwNode);
        // graph.breakEdge(throwFlows.getRightEdge());
        Node<FlowElement> exceptionPoint = graph.createNode(new StopElement());
        Edge<FlowElement, String> exceptionFlow = throwNode.connectNodeFromRight(exceptionPoint);

        return exceptionFlow;
    }

    @Override
    public Edge<FlowElement, String> visitForLoop(ForLoopTree node, Edge<FlowElement, String> flow) {
        checkIsCancelled();
        StatementTree statement = node.getStatement();
        Edge<FlowElement, String> afterCycleFlow = visitCycleUnified(flow, statement);
        return afterCycleFlow;
    }

    @Override
    public Edge<FlowElement, String> visitEnhancedForLoop(EnhancedForLoopTree node, Edge<FlowElement, String> flow) {
        checkIsCancelled();
        StatementTree statement = node.getStatement();
        Edge<FlowElement, String> afterCycleFlow = visitCycleUnified(flow, statement);
        return afterCycleFlow;
    }

    @Override
    public Edge<FlowElement, String> visitWhileLoop(WhileLoopTree node, Edge<FlowElement, String> flow) {
        checkIsCancelled();
        StatementTree statement = node.getStatement();
        Edge<FlowElement, String> afterCycleFlow = visitCycleUnified(flow, statement);
        return afterCycleFlow;
    }

    @Override
    public Edge<FlowElement, String> visitDoWhileLoop(DoWhileLoopTree node, Edge<FlowElement, String> flow) {
        checkIsCancelled();
        StatementTree statement = node.getStatement();
        Edge<FlowElement, String> afterCycleFlow = visitCycleUnified(flow, statement);
        return afterCycleFlow;
    }

    protected Edge<FlowElement, String> visitCycleUnified(Edge<FlowElement, String> flow, StatementTree statement) {
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // BEGIN: (X) --[flow]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Node<FlowElement> cycleNode = graph.createNode(new CycleElement());
        Edge.Split<FlowElement, String> cycleEdges = flow.insertMiddleNode(cycleNode);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // (X) --[cycleEdges.left]--> (cycleNode) --[cycleEdges.right]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Edge<FlowElement, String> loopFlow = cycleNode.connectNodeFromRight(cycleNode);
        loopFlow.setPayload("Loop"); // Self-closure
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // (X) --[cycleEdges.left]--> (  cycleNode  ) --[cycleEdges.right]--> (Y)
        //                           ^__[loopFlow]__|
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Edge<FlowElement, String> afterCycleFlow = cycleEdges.getRightEdge();
        afterCycleFlow.setPayload("done");
        checkIsCancelled();
        visitStatement(statement, loopFlow);

        return afterCycleFlow;
    }

    @Override
    public Edge<FlowElement, String> visitMethodInvocation(MethodInvocationTree node, Edge<FlowElement, String> flow) {
        checkIsCancelled();

        String callMethodName = node.getMethodSelect().toString();

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // BEGIN: (X) --[flow]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //TODO: MOVE COLLAPSE TO AN OUTER PROCESSOR!!!!
        FlowElement flowElement = flow.getLeft().getPayload();
        if (flowElement instanceof StatementElement) {
            StatementElement block = ((StatementElement) flowElement);
            String newContent = String.valueOf(block.getContent()) + "\n" + callMethodName;
            block.setContent(newContent);
            return flow;
        } else {
            Node<FlowElement> methodCall = graph.createNode(new StatementElement().setContent(callMethodName));
            Edge.Split<FlowElement, String> methodCallEdges = flow.insertMiddleNode(methodCall);
            Edge<FlowElement, String> currentFlow = methodCallEdges.getRightEdge().setPayload(null);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            // END: (X) --[flow]--> (methodCall) ----> (Y)
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            return currentFlow; // super.visitMethodInvocation(node, currentFlow);
        }

    }

    @Override
    public Edge<FlowElement, String> visitOther(Tree node, Edge<FlowElement, String> flow) {
        checkIsCancelled();

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // BEGIN: (X) --[flow]--> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Node<FlowElement> other = graph.createNode(new StatementElement().setContent(node.toString()));
        Edge.Split<FlowElement, String> otherEdges = flow.insertMiddleNode(other);
        Edge<FlowElement, String> currentFlow = otherEdges.getRightEdge().setPayload(null);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // END: (X) --[flow]--> (other) ----> (Y)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        checkIsCancelled();
        return super.visitOther(node, currentFlow);
    }

    @Override
    public Edge<FlowElement, String> visitBlock(BlockTree node, Edge<FlowElement, String> flow) {

        Edge<FlowElement, String> currentFlow = flow;
        for (StatementTree statement : node.getStatements()) {
//            if (statement == null) {
//                continue;
//            }
//
//            Edge<FlowElement, String> newFlow = scan(statement, currentFlow);
//            if (newFlow == null) {
//                continue;
//            }
//            currentFlow = newFlow;

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            Edge<FlowElement, String> newFlow = visitStatement(statement, currentFlow);
            currentFlow = (newFlow == null) ? flow : newFlow;
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 

        }

        return currentFlow;
    }

    @Override
    public Edge<FlowElement, String> visitMethod(MethodTree node, Edge<FlowElement, String> flow) {
        // By default the initial flow is the flow between start and end nodes: ( ) --> (*) 
        return super.visitMethod(node, (flow == null) ? initialFlow : flow);
    }

    protected Edge<FlowElement, String> visitStatement(StatementTree statement, Edge<FlowElement, String> flow) {
        checkIsCancelled();

        if (statement == null) {
            return flow;
        }

        Edge<FlowElement, String> newFlow = scan(statement, flow);
        return (newFlow == null) ? flow : newFlow;

    }

    private void _INFO(final String msg) throws NumberFormatException {
        try {
            IOColorLines.println(IO, msg, Color.decode("#00CC33"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static final Set<Tree.Kind> PROCESSING_FILTER = new HashSet<>(Arrays.asList(
            new Tree.Kind[]{
                Tree.Kind.CLASS,
                Tree.Kind.RETURN,
                Tree.Kind.CONTINUE,
                Tree.Kind.BREAK,
                Tree.Kind.BLOCK,
                Tree.Kind.METHOD,
                Tree.Kind.METHOD_INVOCATION,
                Tree.Kind.ASSIGNMENT,
                Tree.Kind.IF,
                Tree.Kind.WHILE_LOOP,
                Tree.Kind.FOR_LOOP
            }
    ));

    @Override
    public Edge<FlowElement, String> scan(Tree tree, Edge<FlowElement, String> flow) {
        checkIsCancelled();

        if (tree == null) {
            return flow;
        }

        return super.scan(tree, flow);
    }

    @Override
    public Edge<FlowElement, String> scan(TreePath treePath, Edge<FlowElement, String> flow) {
        checkIsCancelled();

        return super.scan(treePath, flow);
    }

    public CodeFlowGraph getGraph() {
        return graph;
    }

    public final synchronized void cancel() {
        this.stopScan.set(new AtomicBoolean(true));
    }

    protected void checkIsCancelled() throws RuntimeException {
        if (stopScan.get() != null && stopScan.get().get()) {
            throw new RuntimeException(new InterruptedException("The code flow scanning has been cancelled."));
        }
    }

}
