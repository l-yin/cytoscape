package csplugins.jActiveModules;
//------------------------------------------------------------------------------

import java.io.*;
import java.util.*;
import giny.model.*;
import cytoscape.data.*;
import cytoscape.data.servers.*;
import cytoscape.data.readers.*;
//import cytoscape.undo.*;
import csplugins.jActiveModules.data.*;
import csplugins.jActiveModules.dialogs.*;
//import cytoscape.vizmap.*;
//import cytoscape.layout.*;
import cytoscape.*;

/**
 * This class is used to find components. I can't use the one in y.algo, because
 * I need to find components seeded off a certain set of nodes. This class extends
 * the Dfs class, so that we can use the super class functions to do most of the
 * grunt work in the depth first search to find the components (just override the
 * callback functions, like postVisit()
 */
public class ComponentFinder{
    /**
     *The graph which contains the nodes we are searching on
     */
     GraphPerspective graph;
    /**
     *A list of nodes that have been found in the current component search.
     */
    List current;
    /**
     *Quickly determine if we have seen a node in our search yet. I think there is
     *also some way to do this with the fields in the super class, but I didn't 
     *seem to quite work so I just did it this way.
     */
    HashSet reached;
    /**
     * Make a new component finder with the specified graph
     * @param g the graph on which to search
     */
    public ComponentFinder(GraphPerspective g){
	graph = g;
    }

    /**
     * Return a vector of components reachable from these nodes.
     * @param The nodes which we base our search off of
     */
    public Vector getComponents(Vector nodes){
	Vector result = new Vector();
	boolean done = false;
	int start = 0;

	int initialSize = nodes.size();
	int finalSize = 0;

	//check to see if there is anything to do
	if(nodes.size() == 0){
	    done = true;
	}
	Iterator it = nodes.iterator();
	reached = new HashSet(2*nodes.size());
	//while there are nodes that we haven't seen in our search yet
	while(!done){
	    //make a new list
	    current = new Vector();
	    //start searching at the current node, the start method will make a 
	    //call back to postVisit after it is done searching any node. This is
	    //where we will update the status of that node
	    search((Node)nodes.get(start));
	    result.add(new Component(current));
	    finalSize += current.size();
	    while(start<nodes.size() && reached.contains(nodes.get(start))){
		start++;
	    }
	    //check if we are out of nodes to examine
	    if(start == nodes.size()){
		done = true;
	    }
	}
	return result;
    }

    private void search(Node root){
	current.add(root);
	reached.add(root);
	Iterator nodeIt = graph.neighborsList(root).iterator();
	while(nodeIt.hasNext()){
	    Node myNode = (Node)nodeIt.next();
	    if(!reached.contains(myNode)){
		search(myNode);
	    }
	}
    }

}
