package cytoscape.graph.dynamic.util;

import cytoscape.graph.dynamic.DynamicGraph;
import cytoscape.util.intr.IntEnumerator;
import cytoscape.util.intr.IntIterator;
import cytoscape.util.intr.IntStack;

final class DynamicGraphRepresentation
  implements DynamicGraph, java.io.Externalizable
{

  private int m_nodeCount;
  private int m_maxNode;
  private int m_edgeCount;
  private int m_maxEdge;
  private Node m_firstNode;
  private final NodeArray m_nodes;
  private final NodeDepot m_nodeDepot;
  private final EdgeArray m_edges;
  private final EdgeDepot m_edgeDepot;

  // Use this as a bag of integers in various operations.  Don't forget to
  // empty() it before using it.
  private final IntStack m_stack;

  DynamicGraphRepresentation()
  {
    m_nodeCount = 0;
    m_firstNode = null;
    m_maxNode = -1;
    m_edgeCount = 0;
    m_maxEdge = -1;
    m_nodes = new NodeArray();
    m_edges = new EdgeArray();
    m_edgeDepot = new EdgeDepot();
    m_nodeDepot = new NodeDepot();
    m_stack = new IntStack();
  }

  public final IntEnumerator nodes()
  {
    final int nodeCount = m_nodeCount;
    final Node firstNode = m_firstNode;
    return new IntEnumerator() {
        private int numRemaining = nodeCount;
        private Node node = firstNode;
        public final int numRemaining() { return numRemaining; }
        public final int nextInt() {
          final int returnThis = node.nodeId;
          node = node.nextNode;
          numRemaining--;
          return returnThis; } };
  }

  public final IntEnumerator edges()
  {
    final int edgeCount = m_edgeCount;
    final Node firstNode = m_firstNode;
    return new IntEnumerator() {
        private int numRemaining = edgeCount;
        private Node node = firstNode;
        private Edge edge = null;
        public final int numRemaining() { return numRemaining; }
        public final int nextInt() {
          final int returnThis;
          if (edge != null) returnThis = edge.edgeId;
          else {
            for (edge = node.firstOutEdge;
                 edge == null;
                 node = node.nextNode, edge = node.firstOutEdge) { }
            node = node.nextNode;
            returnThis = edge.edgeId; }
          edge = edge.nextOutEdge;
          numRemaining--;
          return returnThis; } };
  }

  public final int nodeCreate()
  {
    final Node n = m_nodeDepot.getNode();
    final int returnThis;
    if (n.nodeId < 0) returnThis = (n.nodeId = ++m_maxNode);
    else returnThis = n.nodeId;
    m_nodes.setNodeAtIndex(n, returnThis);
    m_nodeCount++;
    n.nextNode = m_firstNode;
    if (m_firstNode != null) m_firstNode.prevNode = n;
    m_firstNode = n;
    n.outDegree = 0; n.inDegree = 0; n.undDegree = 0; n.selfEdges = 0;
    return returnThis;
  }

  public final boolean nodeRemove(final int node)
  {
    final IntEnumerator edges = edgesAdjacent(node, true, true, true);
    if (edges == null) return false;
    m_stack.empty();
    while (edges.numRemaining() > 0) m_stack.push(edges.nextInt());
    while (m_stack.size() > 0) edgeRemove(m_stack.pop());
    final Node n = m_nodes.getNodeAtIndex(node);
    if (n.prevNode != null) n.prevNode.nextNode = n.nextNode;
    else m_firstNode = n.nextNode;
    if (n.nextNode != null) n.nextNode.prevNode = n.prevNode;
    m_nodes.setNodeAtIndex(null, node);
    n.prevNode = null; n.firstOutEdge = null; n.firstInEdge = null;
    m_nodeDepot.recycleNode(n);
    m_nodeCount--;
    return true;
  }

  public final int edgeCreate(final int sourceNode, final int targetNode,
                              final boolean directed)
  {
    if (sourceNode < 0 || sourceNode == Integer.MAX_VALUE) return -1;
    final Node source = m_nodes.getNodeAtIndex(sourceNode);
    if (targetNode < 0 || targetNode == Integer.MAX_VALUE) return -1;
    final Node target = m_nodes.getNodeAtIndex(targetNode);
    if (source == null || target == null) return -1;
    final Edge e = m_edgeDepot.getEdge();
    final int returnThis;
    if (e.edgeId < 0) returnThis = (e.edgeId = ++m_maxEdge);
    else returnThis = e.edgeId;
    m_edges.setEdgeAtIndex(e, returnThis);
    m_edgeCount++;
    if (directed) { source.outDegree++; target.inDegree++; }
    else { source.undDegree++; target.undDegree++; }
    if (source == target) { // Self-edge.
      if (directed) source.selfEdges++;
      else source.undDegree--; }
    e.nextOutEdge = source.firstOutEdge;
    if (source.firstOutEdge != null) source.firstOutEdge.prevOutEdge = e;
    source.firstOutEdge = e;
    e.nextInEdge = target.firstInEdge;
    if (target.firstInEdge != null) target.firstInEdge.prevInEdge = e;
    target.firstInEdge = e;
    e.directed = directed;
    e.sourceNode = sourceNode;
    e.targetNode = targetNode;
    return returnThis;
  }

  public final boolean edgeRemove(final int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return false;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return false;
    final Node source = m_nodes.getNodeAtIndex(e.sourceNode);
    final Node target = m_nodes.getNodeAtIndex(e.targetNode);
    if (e.prevOutEdge != null) e.prevOutEdge.nextOutEdge = e.nextOutEdge;
    else source.firstOutEdge = e.nextOutEdge;
    if (e.nextOutEdge != null) e.nextOutEdge.prevOutEdge = e.prevOutEdge;
    if (e.prevInEdge != null) e.prevInEdge.nextInEdge = e.nextInEdge;
    else target.firstInEdge = e.nextInEdge;
    if (e.nextInEdge != null) e.nextInEdge.prevInEdge = e.prevInEdge;
    if (e.directed) { source.outDegree--; target.inDegree--; }
    else { source.undDegree--; target.undDegree--; }
    if (source == target) { // Self-edge.
      if (e.directed) source.selfEdges--;
      else source.undDegree++; }
    m_edges.setEdgeAtIndex(null, edge);
    e.prevOutEdge = null; e.nextInEdge = null; e.prevInEdge = null;
    m_edgeDepot.recycleEdge(e);
    m_edgeCount--;
    return true;
  }

  public final boolean nodeExists(final int node)
  {
    if (node < 0 || node == Integer.MAX_VALUE) return false;
    return m_nodes.getNodeAtIndex(node) != null;
  }

  public final byte edgeType(final int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return -1;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return -1;
    if (e.directed) return 1; return 0;
  }

  public final int edgeSource(final int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return -1;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return -1;
    return e.sourceNode;
  }

  public final int edgeTarget(final int edge)
  {
    if (edge < 0 || edge == Integer.MAX_VALUE) return -1;
    final Edge e = m_edges.getEdgeAtIndex(edge);
    if (e == null) return -1;
    return e.targetNode;
  }

  public final IntEnumerator edgesAdjacent(final int node,
                                           final boolean outgoing,
                                           final boolean incoming,
                                           final boolean undirected)
  {
    if (node < 0 || node == Integer.MAX_VALUE) return null;
    final Node n = m_nodes.getNodeAtIndex(node);
    if (n == null) return null;
    final Edge[] edgeLists;
    if (undirected || (outgoing && incoming)) {
      edgeLists = new Edge[] { n.firstOutEdge, n.firstInEdge }; }
    else if (outgoing) { // Cannot also be incoming.
      edgeLists = new Edge[] { n.firstOutEdge, null }; }
    else if (incoming) { // Cannot also be outgoing.
      edgeLists = new Edge[] { null, n.firstInEdge }; }
    else { // All boolean input parameters are false.
      edgeLists = new Edge[] { null, null }; }
    int tentativeEdgeCount = 0;
    if (outgoing) tentativeEdgeCount += n.outDegree;
    if (incoming) tentativeEdgeCount += n.inDegree;
    if (undirected) tentativeEdgeCount += n.undDegree;
    if (outgoing && incoming) tentativeEdgeCount -= n.selfEdges;
    final int edgeCount = tentativeEdgeCount;
    return new IntEnumerator() {
        private int numRemaining = edgeCount;
        private int edgeListIndex = -1;
        private Edge edge = null;
        public final int numRemaining() { return numRemaining; }
        public final int nextInt() {
          while (edge == null) edge = edgeLists[++edgeListIndex];
          int returnThis = -1;
          if (edgeListIndex == 0) {
            while (edge != null &&
                   !((outgoing && edge.directed) ||
                     (undirected && !edge.directed))) {
              edge = edge.nextOutEdge;
              if (edge == null) {
                edge = edgeLists[++edgeListIndex];
                break; } }
            if (edge != null && edgeListIndex == 0) {
              returnThis = edge.edgeId;
              edge = edge.nextOutEdge; } }
          if (edgeListIndex == 1) {
            while ((edge.sourceNode == edge.targetNode &&
                    ((outgoing && edge.directed) ||
                     (undirected && !edge.directed))) ||
                   !((incoming && edge.directed) ||
                     (undirected && !edge.directed))) {
              edge = edge.nextInEdge; }
            returnThis = edge.edgeId;
            edge = edge.nextInEdge; }
          numRemaining--;
          return returnThis; } };   
  }

  public final IntIterator edgesConnecting(final int node0, final int node1,
                                           final boolean outgoing,
                                           final boolean incoming,
                                           final boolean undirected)
  {
    final IntEnumerator node0Adj = edgesAdjacent(node0, outgoing, incoming,
                                                 undirected);
    final IntEnumerator node1Adj = edgesAdjacent(node1, incoming, outgoing,
                                                 undirected);
    if (node0Adj == null || node1Adj == null) return null;
    final DynamicGraph graph = this;
    final IntEnumerator theAdj;
    final int nodeZero;
    final int nodeOne;
    if (node0Adj.numRemaining() <= node1Adj.numRemaining()) {
      theAdj = node0Adj; nodeZero = node0; nodeOne = node1; }
    else {
      theAdj = node1Adj; nodeZero = node1; nodeOne = node0; }
    return new IntIterator() {
        private int nextEdge = -1;
        private void ensureComputeNext() {
          if (nextEdge != -1) return;
          while (theAdj.numRemaining() > 0) {
            final int edge = theAdj.nextInt();
            if (nodeOne == (nodeZero ^ graph.edgeSource(edge) ^
                graph.edgeTarget(edge))) {
              nextEdge = edge; return; } }
          nextEdge = -2; }
        public final boolean hasNext() {
          ensureComputeNext();
          if (nextEdge < 0) return false;
          else return true; }
        public final int nextInt() {
          ensureComputeNext();
          final int returnThis = nextEdge;
          nextEdge = -1;
          return returnThis; } };
  }

  // Externalizable methods.

  public final void writeExternal(final java.io.ObjectOutput out)
    throws java.io.IOException
  {
    { // m_nodeDepot.
      for (Node currNode = m_nodeDepot.m_head.nextNode; currNode != null;
           currNode = currNode.nextNode) out.writeInt(currNode.nodeId);
      out.writeInt(-1);
    }
    { // m_edgeDepot.
      for (Edge currEdge = m_edgeDepot.m_head.nextOutEdge; currEdge != null;
           currEdge = currEdge.nextOutEdge) out.writeInt(currEdge.edgeId);
      out.writeInt(-1);
    }
    { // m_edges.
      final int arrLen = m_edges.m_edgeArr.length;
      out.writeInt(arrLen);
      for (int i = 0; i < arrLen; i++) {
        final Edge edge = m_edges.m_edgeArr[i];
        if (edge == null) { out.writeInt(-1); continue; }
        out.writeInt(edge.sourceNode);
        out.writeInt(edge.targetNode);
        out.writeBoolean(edge.directed); }
      for (int i = 0; i < arrLen; i++) {
        final Edge edge = m_edges.m_edgeArr[i];
        if (edge == null) continue;
        out.writeInt(edge.nextOutEdge == null ? -1 : edge.nextOutEdge.edgeId);
        out.writeInt(edge.prevOutEdge == null ? -1 : edge.prevOutEdge.edgeId);
        out.writeInt(edge.nextInEdge == null ? -1 : edge.nextInEdge.edgeId);
        out.writeInt(edge.prevInEdge == null ? -1 : edge.prevInEdge.edgeId); }
    }
  }

  public final void readExternal(final java.io.ObjectInput in)
    throws java.io.IOException
  {
    { // m_nodeDepot.
      Node currNode = m_nodeDepot.m_head;
      while (true) {
        final int id = in.readInt();
        if (id < 0) break;
        currNode.nextNode = new Node();
        currNode = currNode.nextNode;
        currNode.nodeId = id; }
    }
    { // m_edgeDepot.
      Edge currEdge = m_edgeDepot.m_head;
      while (true) {
        final int id = in.readInt();
        if (id < 0) break;
        currEdge.nextOutEdge = new Edge();
        currEdge = currEdge.nextOutEdge;
        currEdge.edgeId = id; }
    }
    { // m_edges.
      final int arrLen = in.readInt();
      final Edge[] arr = (m_edges.m_edgeArr = new Edge[arrLen]);
      for (int i = 0; i < arrLen; i++) {
        final int source = in.readInt();
        if (source < 0) continue;
        final Edge edge = (arr[i] = new Edge());
        edge.edgeId = i;
        edge.sourceNode = source;
        edge.targetNode = in.readInt();
        edge.directed = in.readBoolean(); }
      for (int i = 0; i < arrLen; i++) {
        final Edge edge = arr[i];
        if (edge == null) continue;
        final int nextOutEdge = in.readInt();
        final int prevOutEdge = in.readInt();
        final int nextInEdge = in.readInt();
        final int prevInEdge = in.readInt();
        if (nextOutEdge >= 0) edge.nextOutEdge = arr[nextOutEdge];
        if (prevOutEdge >= 0) edge.prevOutEdge = arr[prevOutEdge];
        if (nextInEdge >= 0) edge.nextInEdge = arr[nextInEdge];
        if (prevInEdge >= 0) edge.prevInEdge = arr[prevInEdge]; }
    }
  }

}
