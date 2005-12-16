package cytoscape.render.stateful;

import cytoscape.geom.spacial.SpacialEntry2DEnumerator;
import cytoscape.geom.spacial.SpacialIndex2D;
import cytoscape.graph.fixed.FixedGraph;
import cytoscape.render.immed.EdgeAnchors;
import cytoscape.render.immed.GraphGraphics;
import cytoscape.util.intr.IntEnumerator;
import cytoscape.util.intr.IntHash;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

/**
 * This class contains a chunk of procedural code that stitches together
 * several external modules in an effort to efficiently render graphs.
 */
public final class GraphRenderer
{

  private final static int LOD_HIGH_DETAIL = 0x1;
  private final static int LOD_NODE_BORDERS = 0x2;
  private final static int LOD_NODE_LABELS = 0x4;
  private final static int LOD_EDGE_ARROWS = 0x8;
  private final static int LOD_DASHED_EDGES = 0x10;
  private final static int LOD_EDGE_ANCHORS = 0x20;
  private final static int LOD_EDGE_LABELS = 0x40;
  private final static int LOD_TEXT_AS_SHAPE = 0x80;
  private final static int LOD_CUSTOM_GRAPHICS = 0x100;

  // No constructor.
  private GraphRenderer() { }

  /**
   * Renders a graph.
   * @param graph the graph topology; nodes in this graph must correspond to
   *   objKeys in nodePositions (the SpacialIndex2D parameter) and vice versa.
   * @param nodePositions defines the positions and extents of nodes in graph;
   *   each entry (objKey) in this structure must correspond to a node in graph
   *   (the FixedGraph parameter) and vice versa; the order in which nodes are
   *   rendered is defined by a non-reversed overlap query on this structure.
   * @param lod defines the different levels of detail; an appropriate level
   *   of detail is chosen based on the results of method calls on this
   *   object.
   * @param nodeDetails defines details of nodes such as colors, node border
   *   thickness, and shape; the node arguments passed to methods on this
   *   object will be nodes in the graph parameter.
   * @param edgeDetails defines details of edges such as colors, thickness,
   *   and arrow type; the edge arguments passed to methods on this
   *   object will be edges in the graph parameter.
   * @param nodeBuff this is a computational helper that is required in the
   *   implementation of this method; when this method returns, nodeBuff is
   *   in a state such that an edge in graph has been rendered by this method
   *   if and only if it touches at least one node in this nodeBuff set;
   *   no guarantee made regarding edgeless nodes.
   * @param grafx the graphics context that is to render this graph.
   * @param bgPaint the background paint to use when calling grafx.clear().
   * @param xCenter the xCenter parameter to use when calling grafx.clear().
   * @param yCenter the yCenter parameter to use when calling grafx.clear().
   * @param scaleFactor the scaleFactor parameter to use when calling
   *   grafx.clear().
   */
  public final static void renderGraph(final FixedGraph graph,
                                       final SpacialIndex2D nodePositions,
                                       final GraphLOD lod,
                                       final NodeDetails nodeDetails,
                                       final EdgeDetails edgeDetails,
                                       final IntHash nodeBuff,
                                       final GraphGraphics grafx,
                                       final Paint bgPaint,
                                       final double xCenter,
                                       final double yCenter,
                                       final double scaleFactor)
  {
    nodeBuff.empty(); // Make sure we keep our promise.

    // Define the visible window in node coordinate space.
    final float xMin, yMin, xMax, yMax;
    {
      xMin = (float)
        (xCenter - 0.5d * grafx.image.getWidth(null) / scaleFactor);
      yMin = (float)
        (yCenter - 0.5d * grafx.image.getHeight(null) / scaleFactor);
      xMax = (float)
        (xCenter + 0.5d * grafx.image.getWidth(null) / scaleFactor);
      yMax = (float)
        (yCenter + 0.5d * grafx.image.getHeight(null) / scaleFactor);
    }

    // Determine the number of nodes and edges that we are about to render.
    final int renderNodeCount;
    final int renderEdgeCount;
    final byte renderEdges;
    SpacialEntry2DEnumerator nodeHits;
    {
      nodeHits = nodePositions.queryOverlap
        (xMin, yMin, xMax, yMax, null, 0, false);
      renderNodeCount = nodeHits.numRemaining();
      final int totalNodeCount = graph.nodes().numRemaining();
      final int totalEdgeCount = graph.edges().numRemaining();
      renderEdges = lod.renderEdges
        (renderNodeCount, totalNodeCount, totalEdgeCount);
      if (renderEdges > 0) { renderEdgeCount = totalEdgeCount; }
      else if (renderEdges < 0) { renderEdgeCount = 0; }
      else {
        int runningEdgeCount = 0;
        for (int i = 0; i < renderNodeCount; i++) {
          final int node = nodeHits.nextInt();
          final IntEnumerator touchingEdges =
            graph.edgesAdjacent(node, true, true, true);
          final int touchingEdgeCount = touchingEdges.numRemaining();
          for (int j = 0; j < touchingEdgeCount; j++) {
            final int edge = touchingEdges.nextInt();
            final int otherNode =
              node ^ graph.edgeSource(edge) ^ graph.edgeTarget(edge);
            if (nodeBuff.get(otherNode) < 0) { runningEdgeCount++; } }
          nodeBuff.put(node); }
        renderEdgeCount = runningEdgeCount;
        nodeHits = null;
        nodeBuff.empty(); }
    }

    // Based on number of objects we are going to render, determine LOD.
    final int lodBits;
    {
      int lodTemp = 0;
      if (lod.detail(renderNodeCount, renderEdgeCount)) {
        lodTemp |= LOD_HIGH_DETAIL;
        if (lod.nodeBorders(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_NODE_BORDERS; }
        if (lod.nodeLabels(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_NODE_LABELS; }
        if (lod.edgeArrows(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_EDGE_ARROWS; }
        if (lod.dashedEdges(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_DASHED_EDGES; }
        if (lod.edgeAnchors(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_EDGE_ANCHORS; }
        if (lod.edgeLabels(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_EDGE_LABELS; }
        if (((lodTemp & LOD_NODE_LABELS) != 0 ||
             (lodTemp & LOD_EDGE_LABELS) != 0) &&
            lod.textAsShape(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_TEXT_AS_SHAPE; }
        if (lod.customGraphics(renderNodeCount, renderEdgeCount)) {
          lodTemp |= LOD_CUSTOM_GRAPHICS; } }
      lodBits = lodTemp;
    }

    // Clear the background.
    {
      grafx.clear(bgPaint, xCenter, yCenter, scaleFactor);
    }

    // Define buffers.  These are of the few objects we're instantiating
    // directly in this method.
    final float[] floatBuff1, floatBuff2, floatBuff3, floatBuff4;
    final double[] doubleBuff1, doubleBuff2;
    final GeneralPath path2d;
    {
      floatBuff1 = new float[4];
      floatBuff2 = new float[4];
      floatBuff3 = new float[2];
      floatBuff4 = new float[8];
      doubleBuff1 = new double[4];
      doubleBuff2 = new double[2];
      path2d = new GeneralPath();
    }

    // Render the edges first.  No edge shall be rendered twice.  Render edge
    // labels.  A label is not necessarily on top of every edge; it is only
    // on top of the edge it belongs to.
    if (renderEdges >= 0) {
      final SpacialEntry2DEnumerator nodeHitsTemp;
      if (renderEdges > 0) {
        // We want to render edges in the same order (back to front) that
        // we would use to render just edges on visible nodes; this is assuming
        // that our spacial index has the subquery order-preserving property.
        nodeHitsTemp = nodePositions.queryOverlap
          (Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
           Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
           null, 0, false); }
      else { // renderEdges == 0.
        if (nodeHits == null) {
          nodeHitsTemp = nodePositions.queryOverlap
            (xMin, yMin, xMax, yMax, null, 0, false); }
        else {
          nodeHitsTemp = nodeHits;
          nodeHits = null; } }

      if ((lodBits & LOD_HIGH_DETAIL) == 0) { // Low detail.
        final int nodeHitCount = nodeHitsTemp.numRemaining();
        for (int i = 0; i < nodeHitCount; i++) {
          final int node = nodeHitsTemp.nextExtents(floatBuff1, 0);

          // Casting to double and then back we could achieve better accuracy
          // at the expense of performance.
          final float nodeX = (floatBuff1[0] + floatBuff1[2]) / 2;
          final float nodeY = (floatBuff1[1] + floatBuff1[3]) / 2;

          final IntEnumerator touchingEdges =
            graph.edgesAdjacent(node, true, true, true);
          final int touchingEdgeCount = touchingEdges.numRemaining();
          for (int j = 0; j < touchingEdgeCount; j++) {
            final int edge = touchingEdges.nextInt();
            final int otherNode =
              node ^ graph.edgeSource(edge) ^ graph.edgeTarget(edge);
            if (nodeBuff.get(otherNode) < 0) { // Has not yet been rendered.
              nodePositions.exists(otherNode, floatBuff2, 0);
              grafx.drawEdgeLow(nodeX, nodeY,
                                // Again, casting issue - tradeoff between
                                // accuracy and performance.
                                (floatBuff2[0] + floatBuff2[2]) / 2,
                                (floatBuff2[1] + floatBuff2[3]) / 2,
                                edgeDetails.colorLowDetail(edge)); } }
          nodeBuff.put(node); } }

      else { // High detail.
        while (nodeHitsTemp.numRemaining() > 0) {
          final int node = nodeHitsTemp.nextExtents(floatBuff1, 0);
          final byte nodeShape = nodeDetails.shape(node);
          final float nodeX = (float)
            ((((double) floatBuff1[0]) + floatBuff1[2]) / 2.0d);
          final float nodeY = (float)
            ((((double) floatBuff1[1]) + floatBuff1[3]) / 2.0d);
          final IntEnumerator touchingEdges =
            graph.edgesAdjacent(node, true, true, true);
          while (touchingEdges.numRemaining() > 0) {
            final int edge = touchingEdges.nextInt();
            final int otherNode =
              node ^ graph.edgeSource(edge) ^ graph.edgeTarget(edge);
            if (nodeBuff.get(otherNode) < 0) { // Has not yet been rendered.
              if (!nodePositions.exists(otherNode, floatBuff2, 0))
                throw new IllegalStateException
                  ("nodePositions not recognizing node that exists in graph");
              final byte otherNodeShape = nodeDetails.shape(otherNode);
              final float otherNodeX = (float)
                ((((double) floatBuff2[0]) + floatBuff2[2]) / 2.0d);
              final float otherNodeY = (float)
                ((((double) floatBuff2[1]) + floatBuff2[3]) / 2.0d);

              // Compute node shapes, center positions, and extents.
              final byte srcShape, trgShape;
              final float srcX, srcY, trgX, trgY;
              final float[] srcExtents, trgExtents;
              if (node == graph.edgeSource(edge)) {
                srcShape = nodeShape; trgShape = otherNodeShape;
                srcX = nodeX; srcY = nodeY;
                trgX = otherNodeX; trgY = otherNodeY;
                srcExtents = floatBuff1; trgExtents = floatBuff2; }
              else { // node == graph.edgeTarget(edge).
                srcShape = otherNodeShape; trgShape = nodeShape;
                srcX = otherNodeX; srcY = otherNodeY;
                trgX = nodeX; trgY = nodeY;
                srcExtents = floatBuff2; trgExtents = floatBuff1; }

              // Compute visual attributes that do not depend on LOD.
              final float thickness = edgeDetails.segmentThickness(edge);
              final Paint segPaint = edgeDetails.segmentPaint(edge);

              // Compute arrows.
              final byte srcArrow, trgArrow;
              final float srcArrowSize, trgArrowSize;
              final Paint srcArrowPaint, trgArrowPaint;
              if ((lodBits & LOD_EDGE_ARROWS) == 0) { // Not rendering arrows.
                trgArrow = srcArrow = GraphGraphics.ARROW_NONE;
                trgArrowSize = srcArrowSize = 0.0f;
                trgArrowPaint = srcArrowPaint = null; }
              else { // Rendering edge arrows.
                srcArrow = edgeDetails.sourceArrow(edge);
                trgArrow = edgeDetails.targetArrow(edge);
                if (srcArrow == GraphGraphics.ARROW_NONE) {
                  srcArrowSize = 0.0f; }
                else {
                  srcArrowSize = edgeDetails.sourceArrowSize(edge); }
                if (trgArrow == GraphGraphics.ARROW_NONE ||
                    trgArrow == GraphGraphics.ARROW_MONO) {
                  trgArrowSize = 0.0f; }
                else {
                  trgArrowSize = edgeDetails.targetArrowSize(edge); }
                if (srcArrow == GraphGraphics.ARROW_NONE ||
                    srcArrow == GraphGraphics.ARROW_BIDIRECTIONAL) {
                  srcArrowPaint = null; }
                else {
                  srcArrowPaint = edgeDetails.sourceArrowPaint(edge); }
                if (trgArrow == GraphGraphics.ARROW_NONE ||
                    trgArrow == GraphGraphics.ARROW_BIDIRECTIONAL ||
                    trgArrow == GraphGraphics.ARROW_MONO) {
                  trgArrowPaint = null; }
                else {
                  trgArrowPaint = edgeDetails.targetArrowPaint(edge); } }

              // Compute dash length.
              final float dashLength;
              if ((lodBits & LOD_DASHED_EDGES) == 0) { // Not rendering dashes.
                dashLength = 0.0f; }
              else {
                dashLength = edgeDetails.segmentDashLength(edge); }

              // Compute the anchors to use when rendering edge.
              final EdgeAnchors anchors;
              if ((lodBits & LOD_EDGE_ANCHORS) == 0) { anchors = null; }
              else {
                EdgeAnchors anchorsTemp = edgeDetails.anchors(edge);
                if (anchorsTemp != null && anchorsTemp.numAnchors() == 0) {
                  anchorsTemp = null; }
                anchors = anchorsTemp; }
              // Now anchors is null if and only if no anchors to be rendered.

              final float srcXOut, srcYOut, trgXOut, trgYOut;
              if (anchors == null) {
                srcXOut = trgX; srcYOut = trgY;
                trgXOut = srcX; trgYOut = srcY; }
              else {
                anchors.getAnchor(0, floatBuff3, 0);
                srcXOut = floatBuff3[0];
                srcYOut = floatBuff3[1];
                anchors.getAnchor(anchors.numAnchors() - 1, floatBuff3, 0);
                trgXOut = floatBuff3[0];
                trgYOut = floatBuff3[1]; }

              final float srcOffset;
              if (srcArrow == GraphGraphics.ARROW_DISC) {
                srcOffset = (float) (0.5d * srcArrowSize); }
              else if (srcArrow == GraphGraphics.ARROW_TEE) {
                srcOffset = (float) srcArrowSize; }
              else {
                srcOffset = 0.0f; }

              if (!grafx.computeEdgeIntersection
                  (srcShape,
                   srcExtents[0], srcExtents[1], srcExtents[2], srcExtents[3],
                   srcOffset, srcXOut, srcYOut, floatBuff3)) {
                continue; }
              final float srcXAdj = floatBuff3[0];
              final float srcYAdj = floatBuff3[1];

              final float trgOffset;
              if (trgArrow == GraphGraphics.ARROW_DISC) {
                trgOffset = (float) (0.5d * trgArrowSize); }
              else if (trgArrow == GraphGraphics.ARROW_TEE) {
                trgOffset = (float) trgArrowSize; }
              else {
                trgOffset = 0.0f; }

              if (!grafx.computeEdgeIntersection
                  (trgShape,
                   trgExtents[0], trgExtents[1], trgExtents[2], trgExtents[3],
                   trgOffset, trgXOut, trgYOut, floatBuff3)) {
                continue; }
              final float trgXAdj = floatBuff3[0];
              final float trgYAdj = floatBuff3[1];

              if (anchors == null &&
                  !((((double) srcX) - trgX) *
                    (((double) srcXAdj) - trgXAdj) +
                    (((double) srcY) - trgY) *
                    (((double) srcYAdj) - trgYAdj) > 0.0d)) {
                // The direction of the chopped segment has flipped.
                continue; }

              grafx.drawEdgeFull(srcArrow, srcArrowSize, srcArrowPaint,
                                 trgArrow, trgArrowSize, trgArrowPaint,
                                 srcXAdj, srcYAdj, anchors, trgXAdj, trgYAdj,
                                 thickness, segPaint, dashLength);

              // Take care of label rendering.
              if ((lodBits & LOD_EDGE_LABELS) != 0) {

                final int labelCount = edgeDetails.labelCount(edge);
                for (int labelInx = 0; labelInx < labelCount; labelInx++) {
                  final String text = edgeDetails.labelText(edge, labelInx);
                  final Font font = edgeDetails.labelFont(edge, labelInx);
                  final double fontScaleFactor =
                    edgeDetails.labelScaleFactor(edge, labelInx);
                  final Paint paint = edgeDetails.labelPaint(edge, labelInx);
                  final byte textAnchor =
                    edgeDetails.labelTextAnchor(edge, labelInx);
                  final byte edgeAnchor =
                    edgeDetails.labelEdgeAnchor(edge, labelInx);
                  final float offsetVectorX =
                    edgeDetails.labelOffsetVectorX(edge, labelInx);
                  final float offsetVectorY =
                    edgeDetails.labelOffsetVectorY(edge, labelInx);
                  final byte justify;
                  {
                    if (text.indexOf('\n') >= 0) {
                      justify = edgeDetails.labelJustify(edge, labelInx); }
                    else {
                      justify = NodeDetails.LABEL_WRAP_JUSTIFY_CENTER; }
                  }

                  final double edgeAnchorPointX;
                  final double edgeAnchorPointY;
                  { // Compute edgeAnchorPointX and edgeAnchorPointY.
                    if (edgeAnchor == EdgeDetails.EDGE_ANCHOR_SOURCE) {
                      edgeAnchorPointX = srcXAdj;
                      edgeAnchorPointY = srcYAdj; }
                    else if (edgeAnchor == EdgeDetails.EDGE_ANCHOR_TARGET) {
                      edgeAnchorPointX = trgXAdj;
                      edgeAnchorPointY = trgYAdj; }
                    else if (edgeAnchor == EdgeDetails.EDGE_ANCHOR_MIDPOINT)
                    {
                      grafx.getEdgePath(srcArrow, srcArrowSize,
                                        trgArrow, trgArrowSize,
                                        srcXAdj, srcYAdj, anchors,
                                        trgXAdj, trgYAdj, path2d);

                      // Count the number of path segments.  This count
                      // includes the initial SEG_MOVETO.  So, for example, a
                      // path composed of 2 cubic curves would have a numPaths
                      // of 3.  Note that numPaths will be at least 2 in all
                      // cases.
                      final int numPaths;
                      {
                        final PathIterator pathIter =
                          path2d.getPathIterator(null);
                        int numPathsTemp = 0;
                        while (!pathIter.isDone()) {
                          numPathsTemp++; // pathIter.currentSegment().
                          pathIter.next(); }
                        numPaths = numPathsTemp;
                      }

                      // Compute "midpoint" of edge.
                      if (numPaths % 2 != 0) {
                        final PathIterator pathIter =
                          path2d.getPathIterator(null);
                        for (int i = numPaths / 2; i > 0; i--) {
                          pathIter.next(); }
                        final int subPathType =
                          pathIter.currentSegment(floatBuff4);
                        if (subPathType == PathIterator.SEG_LINETO) {
                          edgeAnchorPointX = floatBuff4[0];
                          edgeAnchorPointY = floatBuff4[1]; }
                        else if (subPathType == PathIterator.SEG_QUADTO) {
                          edgeAnchorPointX = floatBuff4[2];
                          edgeAnchorPointY = floatBuff4[3]; }
                        else if (subPathType == PathIterator.SEG_CUBICTO) {
                          edgeAnchorPointX = floatBuff4[4];
                          edgeAnchorPointY = floatBuff4[5]; }
                        else {
                          throw new IllegalStateException
                            ("got unexpected PathIterator segment type: " +
                             subPathType); } }
                      else { // numPaths % 2 == 0.
                        final PathIterator pathIter =
                          path2d.getPathIterator(null);
                        for (int i = numPaths / 2; i > 0; i--) {
                          if (i == 1) {
                            final int subPathType =
                              pathIter.currentSegment(floatBuff4);
                            if (subPathType == PathIterator.SEG_MOVETO ||
                                subPathType == PathIterator.SEG_LINETO) {
                              floatBuff4[6] = floatBuff4[0];
                              floatBuff4[7] = floatBuff4[1]; }
                            else if (subPathType == PathIterator.SEG_QUADTO) {
                              floatBuff4[6] = floatBuff4[2];
                              floatBuff4[7] = floatBuff4[3]; }
                            else if (subPathType == PathIterator.SEG_CUBICTO) {
                              floatBuff4[6] = floatBuff4[4];
                              floatBuff4[7] = floatBuff4[5]; }
                            else {
                              throw new IllegalStateException
                                ("got unexpected PathIterator segment type: " +
                                 subPathType); } }
                          pathIter.next(); }
                        final int subPathType =
                          pathIter.currentSegment(floatBuff4);
                        if (subPathType == PathIterator.SEG_LINETO) {
                          edgeAnchorPointX =
                            0.5d * floatBuff4[6] + 0.5d * floatBuff4[0];
                          edgeAnchorPointY =
                            0.5d * floatBuff4[7] + 0.5d * floatBuff4[1]; }
                        else if (subPathType == PathIterator.SEG_QUADTO) {
                          edgeAnchorPointX =
                            0.25d * floatBuff4[6] + 0.5d * floatBuff4[0] +
                            0.25d * floatBuff4[2];
                          edgeAnchorPointY =
                            0.25d * floatBuff4[7] + 0.5d * floatBuff4[1] +
                            0.25d * floatBuff4[3]; }
                        else if (subPathType == PathIterator.SEG_CUBICTO) {
                          edgeAnchorPointX =
                            0.125d * floatBuff4[6] + 0.375d * floatBuff4[0] +
                            0.375d * floatBuff4[2] + 0.125d * floatBuff4[4];
                          edgeAnchorPointY =
                            0.125d * floatBuff4[7] + 0.375d * floatBuff4[1] +
                            0.375d * floatBuff4[3] + 0.125d * floatBuff4[5]; }
                        else {
                          throw new IllegalStateException
                            ("got unexpected PathIterator segment type: " +
                             subPathType); } }
                    }
                    else {
                      throw new IllegalStateException
                        ("encountered an invalid EDGE_ANCHOR_* constant: " +
                         edgeAnchor); }
                  } // End compute edgeAnchorPointX and edgeAnchorPointY.

                  TextRenderingUtils.computeTextDimensions
                    (grafx, text, font, fontScaleFactor,
                     (lodBits & LOD_TEXT_AS_SHAPE) != 0, floatBuff3);
                  doubleBuff1[0] = -0.5d * floatBuff3[0];
                  doubleBuff1[1] = -0.5d * floatBuff3[1];
                  doubleBuff1[2] = 0.5d * floatBuff3[0];
                  doubleBuff1[3] = 0.5d * floatBuff3[1];
                  lemma_computeAnchor(textAnchor, doubleBuff1, doubleBuff2);
                  final double textXCenter =
                    edgeAnchorPointX - doubleBuff2[0] + offsetVectorX;
                  final double textYCenter =
                    edgeAnchorPointY - doubleBuff2[1] + offsetVectorY;
                  TextRenderingUtils.renderHorizontalText
                    (grafx, text, font, fontScaleFactor,
                     (float) textXCenter, (float) textYCenter, justify, paint,
                     (lodBits & LOD_TEXT_AS_SHAPE) != 0);
                }
              }
            } }

          nodeBuff.put(node); } }
    }

    // Render nodes and labels.  A label is not necessarily on top of every
    // node; it is only on top of the node it belongs to.
    {
      if (nodeHits == null) {
        nodeHits = nodePositions.queryOverlap
          (xMin, yMin, xMax, yMax, null, 0, false); }

      if ((lodBits & LOD_HIGH_DETAIL) == 0) { // Low detail.
        final int nodeHitCount = nodeHits.numRemaining();
        for (int i = 0; i < nodeHitCount; i++) {
          final int node = nodeHits.nextExtents(floatBuff1, 0);
          grafx.drawNodeLow(floatBuff1[0], floatBuff1[1],
                            floatBuff1[2], floatBuff1[3],
                            nodeDetails.colorLowDetail(node)); } }

      else { // High detail.
        while (nodeHits.numRemaining() > 0) {
          final int node = nodeHits.nextExtents(floatBuff1, 0);

          // Compute visual attributes that do not depend on LOD.
          final byte shape = nodeDetails.shape(node);
          final Paint fillPaint = nodeDetails.fillPaint(node);

          // Compute node border information.
          final float borderWidth;
          final Paint borderPaint;
          if ((lodBits & LOD_NODE_BORDERS) == 0) { // Not rendering borders.
            borderWidth = 0.0f;
            borderPaint = null; }
          else { // Rendering node borders.
            borderWidth = nodeDetails.borderWidth(node);
            if (borderWidth == 0.0f) {
              borderPaint = null; }
            else {
              borderPaint = nodeDetails.borderPaint(node); } }

          // Draw the node.
          grafx.drawNodeFull(shape, floatBuff1[0], floatBuff1[1],
                             floatBuff1[2], floatBuff1[3], fillPaint,
                             borderWidth, borderPaint);

          // Take care of custom graphic rendering.
          if ((lodBits & LOD_CUSTOM_GRAPHICS) != 0) {

            final int graphicCount = nodeDetails.graphicCount(node);
            for (int graphicInx = 0; graphicInx < graphicCount; graphicInx++) {
              final Shape gShape = nodeDetails.graphicShape(node, graphicInx);
              final Paint paint = nodeDetails.graphicPaint(node, graphicInx);
              final byte anchor =
                nodeDetails.graphicNodeAnchor(node, graphicInx);
              final float offsetVectorX =
                nodeDetails.labelOffsetVectorX(node, graphicInx);
              final float offsetVectorY =
                nodeDetails.labelOffsetVectorY(node, graphicInx);
              doubleBuff1[0] = floatBuff1[0]; doubleBuff1[1] = floatBuff1[1];
              doubleBuff1[2] = floatBuff1[2]; doubleBuff1[3] = floatBuff1[3];
              lemma_computeAnchor(anchor, doubleBuff1, doubleBuff2);
              grafx.drawCustomGraphicFull
                (gShape,
                 (float) (doubleBuff2[0] + offsetVectorX),
                 (float) (doubleBuff2[1] + offsetVectorY), paint); } }

          // Take care of label rendering.
          if ((lodBits & LOD_NODE_LABELS) != 0) { // Potential label rendering.

            final int labelCount = nodeDetails.labelCount(node);
            for (int labelInx = 0; labelInx < labelCount; labelInx++) {
              final String text = nodeDetails.labelText(node, labelInx);
              final Font font = nodeDetails.labelFont(node, labelInx);
              final double fontScaleFactor =
                nodeDetails.labelScaleFactor(node, labelInx);
              final Paint paint = nodeDetails.labelPaint(node, labelInx);
              final byte textAnchor =
                nodeDetails.labelTextAnchor(node, labelInx);
              final byte nodeAnchor =
                nodeDetails.labelNodeAnchor(node, labelInx);
              final float offsetVectorX =
                nodeDetails.labelOffsetVectorX(node, labelInx);
              final float offsetVectorY =
                nodeDetails.labelOffsetVectorY(node, labelInx);
              final byte justify;
              {
                if (text.indexOf('\n') >= 0) {
                  justify = nodeDetails.labelJustify(node, labelInx); }
                else {
                  justify = NodeDetails.LABEL_WRAP_JUSTIFY_CENTER; }
              }
              doubleBuff1[0] = floatBuff1[0]; doubleBuff1[1] = floatBuff1[1];
              doubleBuff1[2] = floatBuff1[2]; doubleBuff1[3] = floatBuff1[3];
              lemma_computeAnchor(nodeAnchor, doubleBuff1, doubleBuff2);
              final double nodeAnchorPointX = doubleBuff2[0];
              final double nodeAnchorPointY = doubleBuff2[1];
              TextRenderingUtils.computeTextDimensions
                (grafx, text, font, fontScaleFactor,
                 (lodBits & LOD_TEXT_AS_SHAPE) != 0, floatBuff3);
              doubleBuff1[0] = -0.5d * floatBuff3[0];
              doubleBuff1[1] = -0.5d * floatBuff3[1];
              doubleBuff1[2] = 0.5d * floatBuff3[0];
              doubleBuff1[3] = 0.5d * floatBuff3[1];
              lemma_computeAnchor(textAnchor, doubleBuff1, doubleBuff2);
              final double textXCenter =
                nodeAnchorPointX - doubleBuff2[0] + offsetVectorX;
              final double textYCenter =
                nodeAnchorPointY - doubleBuff2[1] + offsetVectorY;
              TextRenderingUtils.renderHorizontalText
                (grafx, text, font, fontScaleFactor,
                 (float) textXCenter, (float) textYCenter, justify, paint,
                 (lodBits & LOD_TEXT_AS_SHAPE) != 0); } } } }
    }
  }

  private final static void lemma_computeAnchor(final byte anchor,
                                                final double[] input4x,
                                                final double[] rtrn2x)
  {
    switch (anchor) {
    case NodeDetails.ANCHOR_CENTER:
      rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
      rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;
      break;
    case NodeDetails.ANCHOR_NORTH:
      rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
      rtrn2x[1] = input4x[3];
      break;
    case NodeDetails.ANCHOR_NORTHEAST:
      rtrn2x[0] = input4x[2];
      rtrn2x[1] = input4x[3];
      break;
    case NodeDetails.ANCHOR_EAST:
      rtrn2x[0] = input4x[2];
      rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;
      break;
    case NodeDetails.ANCHOR_SOUTHEAST:
      rtrn2x[0] = input4x[2];
      rtrn2x[1] = input4x[1];
      break;
    case NodeDetails.ANCHOR_SOUTH:
      rtrn2x[0] = (input4x[0] + input4x[2]) / 2.0d;
      rtrn2x[1] = input4x[1];
      break;
    case NodeDetails.ANCHOR_SOUTHWEST:
      rtrn2x[0] = input4x[0];
      rtrn2x[1] = input4x[1];
      break;
    case NodeDetails.ANCHOR_WEST:
      rtrn2x[0] = input4x[0];
      rtrn2x[1] = (input4x[1] + input4x[3]) / 2.0d;
      break;
    case NodeDetails.ANCHOR_NORTHWEST:
      rtrn2x[0] = input4x[0];
      rtrn2x[1] = input4x[3];
      break;
    default:
      throw new IllegalStateException
        ("encoutered an invalid ANCHOR_* constant: " + anchor); }
  }

//   public final static boolean queryEdgeIntersect(
//                                             final GraphGraphics grafx,
//                                             final FixedGraph graph,
//                                             final SpacialIndex2D nodePositions,
//                                             final GraphLOD lod,
//                                             final NodeDetails nodeDetails,
//                                             final EdgeDetails edgeDetails,
//                                             final int edge,
//                                             final float xMinQuery,
//                                             final float yMinQuery,
//                                             final float xMaxQuery,
//                                             final float yMaxQuery)
//   {
//     return false;
//   }

}
