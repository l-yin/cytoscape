package csplugins.mcode;

import cytoscape.CyNetwork;
import giny.model.GraphPerspective;

import java.util.*;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 ** User: Gary Bader
 ** Date: Jan 20, 2004
 ** Time: 6:18:03 PM
 ** Description: An implementation of the MCODE algorithm
 **/

/**
 * An implementation of the MCODE algorithm
 */
public class MCODEAlgorithm {

    //data structure for storing information required for each node
	private class NodeInfo {
		double density;         //neighborhood density
		int numNodeNeighbors;   //number of node nieghbors
		int[] nodeNeighbors;    //stores node indices
		int coreLevel;          //e.g. 2 = a 2-core
		double coreDensity;     //density of the core neighborhood
		double score;           //node score

		public NodeInfo() {
			this.density = 0.0;
			this.numNodeNeighbors = 0;
			this.coreLevel = 0;
			this.coreDensity = 0.0;
		}
	}

	//data structures useful to have around for more than one complex finding iteration
	private HashMap nodeInfoHashMap; //key is the node index, value is a NodeInfo instance
	private TreeMap nodeScoreSortedMap; //key is node score, value is nodeIndex

    private MCODEParameterSet params;   //the parameters used for this instance of the algorithm
    //stats
    private long lastScoreTime;
    private long lastFindTime;

    /**
     * The constructor.  Use this to get an instance of MCODE to run.
     */
    public MCODEAlgorithm() {
        //init class members
        nodeInfoHashMap = null;
        nodeScoreSortedMap = null;
        //get current parameters
        params = MCODECurrentParameters.getInstance().getParamsCopy();
    }

    /**
     * Get the time taken by the last score operation in this instance of the algorithm
     */
    public long getLastScoreTime() {
        return lastScoreTime;
    }

    /**
     * Get the time taken by the last find operation in this instance of the algorithm
     */
    public long getLastFindTime() {
        return lastFindTime;
    }

    /**
     * Get the parameter set used for this instance of MCODEAlgorithm
     * @return The parameter set used
     */
    public MCODEParameterSet getParams() {
        return params;
    }

	/**
     * Step 1: Score the graph and save scores as node attributes.  Scores are also
     * saved internally in your instance of MCODEAlgorithm.
     * @param inputNetwork - The network that will be scored
     */
	public void scoreGraph(CyNetwork inputNetwork) {
		String callerID = "MCODEAlgorithm.MCODEAlgorithm";
		if (inputNetwork == null) {
			System.err.println("In " + callerID + ": inputNetwork was null.");
			return;
		}

		//initialize
        long msTimeBefore = System.currentTimeMillis();
		nodeInfoHashMap = new HashMap(inputNetwork.getNodeCount());
		nodeScoreSortedMap = new TreeMap(new Comparator() { //will store Doubles
			//sort Doubles in descending order
			public int compare(Object o1, Object o2) {
				double d1 = ((Double) o1).doubleValue();
				double d2 = ((Double) o2).doubleValue();
				if (d1 == d2) {
					return 0;
				} else if (d1 < d2) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		//iterate over all nodes and calculate MCODE score
		NodeInfo nodeInfo = null;
		double nodeScore;
		for (int i = 1; i <= inputNetwork.getNodeCount(); i++) {
			nodeInfo = calcNodeInfo(inputNetwork, i);
			nodeInfoHashMap.put(new Integer(i), nodeInfo);
			//score node TODO: add support for other scoring functions (low priority)
			nodeScore = scoreNode(nodeInfo);
			//record score as a nodeAttribute
            inputNetwork.setNodeAttributeValue(i, "MCODE_SCORE", new Double(nodeScore));
			//save score for later use in TreeMap
			nodeScoreSortedMap.put(new Double(nodeScore), new Integer(i));
		}
        long msTimeAfter = System.currentTimeMillis();
        lastScoreTime = msTimeAfter - msTimeBefore;
	}

	/**
     * Step 2: Find all complexes given a scored graph.  If the input network has not been scored,
     * this method will return null.
     * @param inputNetwork - The scored network to find complexes in.
     * @return An ArrayList containing an ArrayList for each complex. Each complex is stored as a simple list
     * of node IDs of the nodes in the input network that are part of the complex.
     */
	public ArrayList findComplexes(CyNetwork inputNetwork) {
		String callerID = "MCODEAlgorithm.findComplexes";
		if (inputNetwork == null) {
			System.err.println("In " + callerID + ": inputNetwork was null.");
			return (null);
		}
		if ((nodeInfoHashMap == null) || (nodeScoreSortedMap == null)) {
			System.err.println("In " + callerID + ": nodeInfoHashMap or nodeScoreSortedMap was null.");
			return (null);
		}

		//initialization
        long msTimeBefore = System.currentTimeMillis();
		boolean[] nodeSeenArray = new boolean[inputNetwork.getNodeCount() + 1]; //+1 since node indices start at 1
		Arrays.fill(nodeSeenArray, false);
		int currentNode = 0;
		Collection values = nodeScoreSortedMap.values();
		//stores the list of complexes as ArrayLists of node indices in the input Network
		ArrayList alComplexes = new ArrayList();
		//iterate over node indices sorted descending by their score
		for (Iterator iterator = values.iterator(); iterator.hasNext();) {
			currentNode = ((Integer) iterator.next()).intValue();
			if (nodeSeenArray[currentNode] == false) {
				ArrayList complex = getComplexCore(currentNode, nodeSeenArray);
				if (complex.size() > 0) {
					//make sure spawning node is part of complex, if not already in there
                    if(!complex.contains(new Integer(currentNode))) {
					    complex.add(new Integer(currentNode));
                    }
                    //create an input graph for the filter and haircut methods
                    //comvert Integer array to int array
                    int[] complexArray = new int[complex.size()];
                    for (int i = 0; i < complex.size(); i++) {
                        int nodeIndex = ((Integer) complex.get(i)).intValue();
                        complexArray[i] = nodeIndex;
                    }
                    GraphPerspective gpComplexGraph = inputNetwork.createGraphPerspective(complexArray);
					if (!filterComplex(gpComplexGraph)) {
                        if (params.isHaircut()) {
                            haircutComplex(gpComplexGraph, complex, inputNetwork);
                        }
                        if (params.isFluff()) {
                            fluffComplexBoundary(complex, nodeSeenArray);
                        }
                        //store detected complex for later
						alComplexes.add(complex);
                    }
				}
			}
		}
        long msTimeAfter = System.currentTimeMillis();
        lastFindTime = msTimeAfter - msTimeBefore;

		return (alComplexes);
	}

	/**
     * Score node using the formula from original MCODE paper.
     * This formula selects for larger, denser cores.
     * This is a utility function for the algorithm.
     * @param nodeInfo The internal data structure to fill with node information
     * @return The score of this node.
     */
	private double scoreNode(NodeInfo nodeInfo) {
		if (nodeInfo.numNodeNeighbors > params.getDegreeCutOff()) {
			nodeInfo.score = nodeInfo.coreDensity * (double) nodeInfo.coreLevel;
		} else {
			nodeInfo.score = 0.0;
		}
		return (nodeInfo.score);
	}

	/**
     * Score a complex.  Currently this ranks larger, denser complexes higher, although
     * in the future other scoring functions could be created
     * @param gpComplex - The GINY GraphPerspective version of the complex
     * @return The score of the complex
     */
	public double scoreComplex(GraphPerspective gpComplex) {
		int numNodes = 0;
		double density = 0.0, score = 0.0;

		numNodes = gpComplex.getNodeCount();
		density = calcDensity(gpComplex, true);
		score = density * numNodes;

		return (score);
	}

	/**
     * Calculates node information for each node according to the original MCODE publication.
     * This information is used to score the nodes in the scoring stage.
     * This is a utility function for the algorithm.
     * @param inputNetwork The input network for reference
     * @param nodeIndex The index of the node in the input network to score
     * @return A NodeInfo object containing node information required for the algorithm
     */
	private NodeInfo calcNodeInfo(CyNetwork inputNetwork, int nodeIndex) {
		int[] neighborhood;

		String callerID = "MCODEAlgorithm.calcNodeInfo";
		if (inputNetwork == null) {
			System.err.println("In " + callerID + ": gpInputGraph was null.");
			return null;
		}

		//get neighborhood of this node (including the node)
		int[] neighbors = inputNetwork.neighborsArray(nodeIndex);
		if (neighbors.length < 2) {
			//if there are no neighbors or just one neighbor, nodeInfo calculation is trivial
			NodeInfo nodeInfo = new NodeInfo();
			if (neighbors.length == 1) {
				nodeInfo.coreLevel = 1;
				nodeInfo.coreDensity = 1.0;
				nodeInfo.density = 1.0;
			}
			return (nodeInfo);
		}
		//add original node to extract complete neighborhood
		Arrays.sort(neighbors);
		if (Arrays.binarySearch(neighbors, nodeIndex) < 0) {
			neighborhood = new int[neighbors.length + 1];
			System.arraycopy(neighbors, 0, neighborhood, 1, neighbors.length);
			neighborhood[0] = nodeIndex;
		} else {
			neighborhood = neighbors;
		}

		//extract neighborhood subgraph
		GraphPerspective gpNodeNeighborhood = inputNetwork.createGraphPerspective(neighborhood);
		if (gpNodeNeighborhood == null) {
			//this shouldn't happen
			System.err.println("In " + callerID + ": gpNodeNeighborhood was null.");
			return null;
		}

		//calculate the node information for each node
		NodeInfo nodeInfo = new NodeInfo();
		//density
		if (gpNodeNeighborhood != null) {
			nodeInfo.density = calcDensity(gpNodeNeighborhood, params.isIncludeLoops());
		}
		nodeInfo.numNodeNeighbors = neighborhood.length;
		//calculate the highest k-core
		GraphPerspective gpCore = null;
		Integer k = null;
		Object[] returnArray = getHighestKCore(gpNodeNeighborhood);
		k = (Integer) returnArray[0];
		gpCore = (GraphPerspective) returnArray[1];
		nodeInfo.coreLevel = k.intValue();
		/*calculate the core density - amplifies the density of heavily interconnected regions and attenuates
		that of less connected regions*/
		if (gpCore != null) {
			nodeInfo.coreDensity = calcDensity(gpCore, params.isIncludeLoops());
		}
		//record neighbor array for later use in comkplex detection step
		nodeInfo.nodeNeighbors = neighborhood;

		return (nodeInfo);
	}

	/**
     * Find the high-scoring central region of the complex.
     * This is a utility function for the algorithm.
     * @param startNode The node that is the seed of the complex
     * @param nodeSeenArray The list of nodes seen already
     * @return A list of node IDs representing the core of the complex
     */
	private ArrayList getComplexCore(int startNode, boolean[] nodeSeenArray) {
		ArrayList complex = new ArrayList(); //stores Integer nodeIndices
		getComplexCoreInternal(startNode, nodeSeenArray, ((NodeInfo) nodeInfoHashMap.get(new Integer(startNode))).score, 1, complex);
		return (complex);
	}

    /**
     * An internal function that does the real work of getComplexCore, implemented to enable recursion.
     * @param startNode The node that is the seed of the complex
     * @param nodeSeenArray The list of nodes seen already
     * @param startNodeScore The score of the seed node
     * @param currentDepth The depth away from the seed node that we are currently at
     * @param complex The complex to add to if we find a complex node in this method
     * @return true
     */
	private boolean getComplexCoreInternal(int startNode, boolean[] nodeSeenArray, double startNodeScore, int currentDepth, ArrayList complex) {
		//base cases for recursion
		if (nodeSeenArray[startNode] == true) {
			return (true);  //don't recheck a node
		}
		if (currentDepth > params.getMaxDepthFromStart()) {
			return (true);  //don't exceed given depth from start node
		}

		//Initialization
		int currentNeighbor = 0, i = 0;

		nodeSeenArray[startNode] = true;
		for (i = 0; i < (((NodeInfo) nodeInfoHashMap.get(new Integer(startNode))).numNodeNeighbors); i++) {
			//go through all currentNode neighbors to check their core density for complex inclusion
			currentNeighbor = ((NodeInfo) nodeInfoHashMap.get(new Integer(startNode))).nodeNeighbors[i];
			if ((nodeSeenArray[currentNeighbor] == false) &&
			        (((NodeInfo) nodeInfoHashMap.get(new Integer(currentNeighbor))).score >=
                    (startNodeScore - startNodeScore * params.getNodeScoreCutOff()))) {
				//add current neighbor
                if (!complex.contains(new Integer(currentNeighbor))) {
				    complex.add(new Integer(currentNeighbor));
                }
				//try to extend complex at this node
				getComplexCoreInternal(currentNeighbor, nodeSeenArray, startNodeScore, currentDepth + 1, complex);
			}
		}

		return (true);
	}

	/**
     * Fluff up the complex at the boundary by adding lower scoring, non complex-core neighbors
     * This implements the complex fluff feature.
     * @param complex The complex to fluff
     * @param nodeSeenArray The list of nodes seen already
     * @return true
     */
	private boolean fluffComplexBoundary(ArrayList complex, boolean[] nodeSeenArray) {
		int currentNode = 0, nodeNeighbor = 0;
		//create a temp list of nodes to add to avoid concurrently modifying 'complex'
		ArrayList nodesToAdd = new ArrayList();

		//Copy the nodeSeeArray because nodes seen during a fluffing should not be marked as permanently seen,
		//they can be included in another complex's fluffing step.
		boolean[] nodeSeenArrayInternal = new boolean[nodeSeenArray.length];
		System.arraycopy(nodeSeenArray, 0, nodeSeenArrayInternal, 0, nodeSeenArray.length);

		//add all current neighbour's neighbours into complex (if they have high enough clustering coefficients) and mark them all as seen
		for (int i = 0; i < complex.size(); i++) {
			currentNode = ((Integer) complex.get(i)).intValue();
			for (int j = 0; j < ((NodeInfo) nodeInfoHashMap.get(new Integer(currentNode))).numNodeNeighbors; j++) {
				nodeNeighbor = ((NodeInfo) nodeInfoHashMap.get(new Integer(currentNode))).nodeNeighbors[j];
				if ((nodeSeenArrayInternal[nodeNeighbor] == false) &&
				        ((((NodeInfo) nodeInfoHashMap.get(new Integer(nodeNeighbor))).density) > params.getFluffNodeDensityCutOff())) {
					nodesToAdd.add(new Integer(nodeNeighbor));
					nodeSeenArrayInternal[nodeNeighbor] = true;
				}
			}
		}

		//Add fluffed nodes to complex
		if (nodesToAdd.size() > 0) {
			complex.addAll(nodesToAdd.subList(0, nodesToAdd.size()));
		}

		return (true);
	}

	/**
     * Checks if the complex needs to be filtered according to heuristics in this method
     * @param gpComplexGraph The complex to check if it passes the filter
     * @return true if complex should be filtered, false otherwise
     */
	private boolean filterComplex(GraphPerspective gpComplexGraph) {
		if (gpComplexGraph == null) {
			return (true);
		}

		//filter if not a 2-core
		GraphPerspective gpCore = getKCore(gpComplexGraph, 2);
		if (gpCore == null) {
			return (true);
		}

		return (false);
	}

    /**
     * Gives the complex a haircut (removed singly connected nodes by taking a 2-core)
     * @param gpComplexGraph The complex graph
     * @param complex The complex node ID list (in the original graph)
     * @param gpInputGraph The original input graph
     * @return true
     */
    private boolean haircutComplex(GraphPerspective gpComplexGraph, ArrayList complex, GraphPerspective gpInputGraph) {
        //get 2-core
        GraphPerspective gpCore = getKCore(gpComplexGraph, 2);
        if (gpCore != null) {
            //clear the complex and add all 2-core nodes back into it
            complex.clear();
            //must add back the nodes in a way that preserves gpInputGraph node indices
            int[] rootGraphIndices = gpCore.getNodeIndicesArray();
            for (int i = 0; i < rootGraphIndices.length; i++) {
                complex.add(new Integer(gpInputGraph.getNodeIndex(rootGraphIndices[i])));
            }
        }
        return (true);
    }

	/**
     * Calculate the density of a network
     * The density is defined as the number of edges/the number of possible edges
     * @param gpInputGraph The input graph to calculate the density of
     * @param includeLoops Include the possibility of loops when determining the number of
     * possible edges.
     * @return The density of the network
     */
	public double calcDensity(GraphPerspective gpInputGraph, boolean includeLoops) {
		int possibleEdgeNum = 0, actualEdgeNum = 0, loopCount = 0;
		double density = 0;

		String callerID = "MCODEAlgorithm.calcDensity";
		if (gpInputGraph == null) {
			System.err.println("In " + callerID + ": gpInputGraph was null.");
			return (-1.0);
		}

		if (includeLoops) {
			//count loops
			for (int i = 1; i <= gpInputGraph.getNodeCount(); i++) {
				if (gpInputGraph.isNeighbor(i, i)) {
					loopCount++;
				}
			}
			possibleEdgeNum = gpInputGraph.getNodeCount() * gpInputGraph.getNodeCount();
			actualEdgeNum = gpInputGraph.getEdgeCount() - loopCount;
		} else {
			possibleEdgeNum = gpInputGraph.getNodeCount() * gpInputGraph.getNodeCount();
			actualEdgeNum = gpInputGraph.getEdgeCount();
		}

		density = (double) actualEdgeNum / (double) possibleEdgeNum;
		return (density);
	}

	/**
     * Find a k-core of a network. A k-core is a subgraph of minimum degree k
     * @param gpInputGraph The input network
     * @param k The k of the k-core to find e.g. 4 will find a 4-core
     * @return Returns a subgraph with the core, if any was found at given k
     */
	public GraphPerspective getKCore(GraphPerspective gpInputGraph, int k) {
		String callerID = "MCODEAlgorithm.getKCore";
		if (gpInputGraph == null) {
			System.err.println("In " + callerID + ": gpInputGraph was null.");
			return (null);
		}

		//filter all nodes with degree less than k until convergence
		boolean firstLoop = true;
		int numDeleted;
		GraphPerspective gpOutputGraph = null;
		while (true) {
			numDeleted = 0;
			ArrayList alCoreNodeIndices = new ArrayList(gpInputGraph.getNodeCount());
			for (int i = 1; i <= gpInputGraph.getNodeCount(); i++) {
				if (gpInputGraph.getDegree(i) >= k) {
					alCoreNodeIndices.add(new Integer(i)); //contains all nodes with degree >= k
				} else {
					numDeleted++;
				}
			}
			if ((numDeleted > 0) || (firstLoop)) {
				//convert ArrayList to int[] for creation of a GraphPerspective for this core
				int[] outputNodeIndices = new int[alCoreNodeIndices.size()];
				int j = 0;
				for (Iterator i = alCoreNodeIndices.iterator(); i.hasNext(); j++) {
					outputNodeIndices[j] = ((Integer) i.next()).intValue();
				}
				gpOutputGraph = gpInputGraph.createGraphPerspective(outputNodeIndices);
				if (gpOutputGraph.getNodeCount() == 0) {
					return (null);
				}
				//iterate again, but with a new k-core input graph
				gpInputGraph = gpOutputGraph;
				if (firstLoop) {
					firstLoop = false;
				}
			} else {
				//stop the loop
				break;
			}
		}

		return (gpOutputGraph);
	}

	/**
     * Find the highest k-core in the input graph.
     * @param gpInputGraph The input network
     * @return Returns the k-value and the core as an Object array.
     * The first object is the highest k value i.e. objectArray[0]
     * The second object is the highest k-core as a GraphPerspective i.e. objectArray[1]
     */
	public Object[] getHighestKCore(GraphPerspective gpInputGraph) {
		String callerID = "MCODEAlgorithm.getHighestKCore";
		if (gpInputGraph == null) {
			System.err.println("In " + callerID + ": gpInputGraph was null.");
			return (null);
		}

		int i = 1;
		GraphPerspective gpCurCore = null, gpPrevCore = null;

		while ((gpCurCore = getKCore(gpInputGraph, i)) != null) {
			gpInputGraph = gpCurCore;
			gpPrevCore = gpCurCore;
			i++;
		}

		Integer k = new Integer(i - 1);
		Object[] returnArray = new Object[2];
		returnArray[0] = k;
		returnArray[1] = gpPrevCore;    //in the last iteration, gpCurCore is null (loop termination condition)

		return (returnArray);
	}
}
