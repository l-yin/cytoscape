package csplugins.ucsd.rmkelley.GeneticInteractions;

//java import
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

//giny import
import giny.model.Node;
import giny.model.Edge;
import giny.model.GraphPerspective;


//cytoscape import
import cytoscape.data.GraphObjAttributes;
import cytoscape.view.CyWindow;


/**
 *This class is responsible for finding lethal CoPPs in the network. It does this by examining
 *each lethal node and the network, and finding any node with a genetic interaction that is near
 *that node. It then looks for genetic interactions that span two nodes that are close to the lethal
 *Each pair of paths ot those two nodes that do not overlap form a LethalCoPP
 *nodes with a lethal annotation.
 */
class LethalCoPPFinder{
  
  /**
   * Set to quickly determine if two nodes are involved in genetic interaction
   */
  private NodePairSet geneticPairs;
  /**
   * See constructor
   */
  private HashMap physicalInteractions;
  /**
   * Contains all nodes known to be involved in genetic interactions
   */
  private HashSet geneticInteractionNodes;
  /**
   * A list of all the lethal nodes in hte network
   */
  private List lethalNodes;
  /**
   * A set of all genetic interactions in this graph
   */
  private HashSet geneticInteractions;
  /**
   * Constructor.
   * @param lethalNodes a list of all lethal nodes in hte network
   * @param physicalInteractions a hashmap from node=> set of physical target
   * @param geneticInteractions a set of all lethal interactions in  the graph
   */
  public LethalCoPPFinder(List lethalNodes, HashMap physicalInteractions, HashSet geneticInteractions){
    this.physicalInteractions = physicalInteractions;
    this.lethalNodes = lethalNodes;
    this.geneticInteractions = geneticInteractions;	
    this.geneticInteractionNodes = getGeneticInteractionNodes(geneticInteractions);
    this.geneticPairs = new NodePairSet(geneticInteractions);
  }


  /**
   * Make a set containing all the nodes involved in genetic interactions.
   * @param geneticInteractions  A vector containing edges which are known to be genetic interactions
   * @return A HashSet containing all the nodes involved in those interactions
   */
  private HashSet getGeneticInteractionNodes(Set geneticInteractions){
    HashSet result = new HashSet();
    Iterator giIt = geneticInteractions.iterator();
    while(giIt.hasNext()){
      Edge currentEdge = (Edge)giIt.next();
      result.add(currentEdge.getSource());
      result.add(currentEdge.getTarget());
    }
    return result;
  }

  //hacking this together to make it compile, this doesn't really work, just making it 
  //return the correct data type
  //public RestrictedMinHeap findLethalCoPPs(int distance, int min,int thread_count){
  //the hashmap shoudl eventaully map each edge that is a genetic interaction to a list
  //of lethalCoPPs each of which represents the highest scoring
  public HashMap findLethalCoPPs(int distance, int min, int thread_count){
    HashMap result = new HashMap();
    for (Iterator edgeIt = geneticInteractions.iterator();edgeIt.hasNext();) {
      result.put(edgeIt.next(),new Vector());
    } // end of for ()
    
    Thread [] threads = new Thread [thread_count];
    Iterator lethalIt = lethalNodes.iterator();
    for(int i=0;i<thread_count;i++){
      threads[i] = new LethalCoPPFinderThread(geneticPairs, geneticInteractions,physicalInteractions,lethalIt,geneticInteractionNodes,result,distance,min);
    }
    for(int i=0;i<thread_count;i++){
      threads[i].start();
    }

    for(int i=0;i<thread_count;i++){
      try{
	threads[i].join();
      }
      catch(InterruptedException ie){
	ie.printStackTrace();
	System.exit(-1);
      }
    }
    //return result;
    return result;
  }
}

/**
 * This is a worker thread that will find paths from genetic interactions
 * to lethal nodes.
 */
class LethalCoPPFinderThread extends Thread{
  
  private NodePairSet geneticPairs;
  /**
   * Maps from a node to a set of physical neighbors 
   */
  private HashMap physicalInteractions;

  /**
   * See constructor
   */
  private Iterator lethalIt;

  /**
   * See constructor
   */
  private HashSet geneticInteractionNodes;
  /**
   * See constructor
   */
  private HashMap result;
  /**
   * See constructor
   */
  private int depth;
  /**
   * Keeps track of the nodes seen already in the search
   */
  private HashSet seenNodes;
  /**
   * HashSet of all the lethal nodes, hopefully faster than trying to do the lookup
   * in the reviled GraphObjAttributes every time
   */
  private HashSet lethalNodes;
  /**
   * Node that we are currently searching from, need this to be able to correctly associate
   * results
   */
  private Node currentSearch;
  /**
   * See constructor
   */
  private int min;
  /**
   * Maps from a giNode to a set of paths, used to store the found paths in the search
   * from a particular lethal node.
   */
  private HashMap node2Paths;
  /**
   * Set of all lethal interactions in the graph
   */
  private HashSet geneticInteractions;	
  /**
   * the window containing the graph
   */
  private CyWindow cyWindow;
  /**
   * Constructor
   * @param cyWindow the window containing the graph, need this to pass to constructor for LethalCoPP
   * @param geneticInteractions A set of all lethal interactions 
   * @param node2set hashmap from a node to list of nodes that physically interact
   * @param lethalIt An iterator over nodes with lethal deletion
   * @param geneticInteractionNodes a set of nodes that are involved in some genetic interaction
   * @param result A restricted min heap where the results will be stored 
   * @param depth The depth of the greedy search
   * @param min The minimum depth of the greedy search (ignore results closer than this)
   */
  public LethalCoPPFinderThread(NodePairSet geneticPairs, HashSet geneticInteractions,HashMap physicalInteractions,Iterator lethalIt, HashSet geneticInteractionNodes, HashMap result,int depth,int min){
    this.geneticPairs = geneticPairs;
    this.geneticInteractions = geneticInteractions;
    this.physicalInteractions = physicalInteractions; 
    this.lethalIt = lethalIt;
    this.geneticInteractionNodes = geneticInteractionNodes;
    this.result = result;
    this.depth = depth;
    this.min = min;
    this.node2Paths = new HashMap();
  }

  /**
   * This will start the pathfinding procedure and store any results
   * found into "result";
   */
  public void run(){
    boolean done = true;
    Node current = null;
    synchronized(lethalIt){
      if(lethalIt.hasNext()){
	current = (Node)lethalIt.next();
	if(GeneticInteractions.DEBUG){
	  System.out.println(current);
	}
	done = false;
      }
    }
    while(!done){
      done = true;
      //search from current and store the results
      seenNodes = new HashSet();
      currentSearch = current;
      node2Paths.clear();
      findPaths(current,depth,min);
      //using the paths we just found, go through and indentify
      //lethal CoPPs. iteratte through the geneteic interactions,
      //and see which ones have both a source and target in the paths
      //hashmap, try to create CoPPs from these nodes
      Iterator giIt = geneticInteractions.iterator();
      Set reachedNodes = node2Paths.keySet();
      while(giIt.hasNext()){
	Edge lethal = (Edge)giIt.next();
	Node source = lethal.getSource();
	Node target = lethal.getTarget();
	if(reachedNodes.contains(source) && reachedNodes.contains(target)){
	  findCoPPs(lethal,source,target);
	}
      }

      //check to see if there is another node from which
      //to search
      synchronized(lethalIt){
	if(lethalIt.hasNext()){
	  current = (Node)lethalIt.next();
	  if(GeneticInteractions.DEBUG){
	    System.out.println(current);
	  }
	  done = false;
	}
      }			
    }
  }

  /**
   * Given a source and target node, find all combinations
   * of paths from a single lethal node that do not overlap
   * and therefore form a lethal CoPP. This adds the identified
   * lethal CoPPs to result.
   * @param source the source node of a lethal interaction
   * @param target the target node of a genetic interaction
   */
  private void findCoPPs(Edge lethal, Node source, Node target){
    List sourcePaths = (List)node2Paths.get(source);	
    List targetPaths = (List)node2Paths.get(target);
    //Iteratore through each possible combination of paths
    //from the node and source
    Iterator sourcePathsIt = sourcePaths.iterator();
    while(sourcePathsIt.hasNext()){
      HashSet sourcePath = (HashSet)sourcePathsIt.next();
      Iterator targetPathsIt = targetPaths.iterator();
      while(targetPathsIt.hasNext()){
	HashSet targetPath = (HashSet)targetPathsIt.next();
	//check to see if there is overlap between the 
	//two paths
	Iterator sourceNodeIt = sourcePath.iterator();
	boolean overlap = false;
	while(sourceNodeIt.hasNext() && !overlap){
	  if(targetPath.contains(sourceNodeIt.next())){
	    overlap = true;
	  }
	}
	if(!overlap){
	  //there is no overlap between these two paths
	  //create a new lethal copp and add it to the list
	  //of results
	  LethalCoPP lethalCoPP = new LethalCoPP(geneticPairs,currentSearch,sourcePath,targetPath);
	  //synchronized(result){
	  List bestCoPPs = (List)result.get(lethal);
	  synchronized(bestCoPPs){
	    if (bestCoPPs.size() == 0) {
	      bestCoPPs.add(lethalCoPP);
	    } // end of if ()
	    else {
	      int compare = lethalCoPP.compareTo(bestCoPPs.get(0));	      
	      if (compare == 0) {
		bestCoPPs.add(lethalCoPP);
	      } // end of if ()
	      else {
		if (compare > 0) {
		  bestCoPPs.clear();
		  bestCoPPs.add(lethalCoPP);
		}
	      }
	    }
	  }
	}
      }
    }
  }
  /**
   * Recursive function to find genetic interaction nodes
   * @param current The current location of the search
   * @param distance The remaining depth in the search
   */
  private void findPaths(Node current, int distance, int min){
    //base case, no more depth left in search
    if(distance > 0){
      seenNodes.add(current);
      //check to see if the current node is a genetic interaction 
      if(min <= 1 && geneticInteractionNodes.contains(current)){
	//add the current path to the results
	List pathList = (List)node2Paths.get(current);
	if(pathList == null){
	  pathList = new Vector();
	  node2Paths.put(current,pathList);
	}
	//create a copy of the current path so far
	HashSet currentPath = new HashSet(seenNodes);
	currentPath.remove(currentSearch);
	pathList.add(currentPath);
      }
      //get the list of next nodes and initiate a
      //search from each of those, if they have not 
      //already been seen
      Iterator neighborIt = ((Set)physicalInteractions.get(current)).iterator();
      while(neighborIt.hasNext()){
	Node neighbor = (Node)neighborIt.next();
	if(!seenNodes.contains(neighbor)){
	  findPaths(neighbor,distance-1,min-1);
	}
      }
      //remove the current
      seenNodes.remove(current);
    }
  }
}
