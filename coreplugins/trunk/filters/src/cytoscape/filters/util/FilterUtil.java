package cytoscape.filters.util;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.filters.CompositeFilter;
import giny.model.Edge;
import giny.model.Node;
import java.util.ArrayList;
import java.util.List;
import csplugins.quickfind.util.QuickFind;
import csplugins.quickfind.util.QuickFindFactory;
import csplugins.test.quickfind.test.TaskMonitorBase;
import cytoscape.data.CyAttributes;
import cytoscape.filters.FilterPlugin;
import csplugins.widgets.autocomplete.index.GenericIndex;

public class FilterUtil {
		
	// do selection on given network
	public static void doSelection(CompositeFilter pFilter) {
		//System.out.println("Entering FilterUtil.doSelection() ...");
		
		pFilter.apply();
		
		CyNetwork network = Cytoscape.getCurrentNetwork(); 

		network.unselectAllNodes();
		network.unselectAllEdges();
		
		final List<Node> nodes_list = network.nodesList();
		final List<Edge> edges_list = network.edgesList();

		if (pFilter.getAdvancedSetting().isNodeChecked()&& (pFilter.getNodeBits() != null)) {
			// Select nodes
			final List<Node> passedNodes = new ArrayList<Node>();

			Node node = null;

			for (int i=0; i< pFilter.getNodeBits().length(); i++) {
				int next_set_bit = pFilter.getNodeBits().nextSetBit(i);
				
				node = nodes_list.get(next_set_bit);
								
				passedNodes.add(node);
				i = next_set_bit;
			}
			network.setSelectedNodeState(passedNodes, true);
		}
		if (pFilter.getAdvancedSetting().isEdgeChecked()&& (pFilter.getEdgeBits() != null)) {
			// Select edges
			final List<Edge> passedEdges = new ArrayList<Edge>();

			Edge edge = null;
			for (int i=0; i< edges_list.size(); i++) {
				int next_set_bit = pFilter.getEdgeBits().nextSetBit(i);
				if (next_set_bit == -1) {
					break;
				}
				edge = edges_list.get(next_set_bit);
				passedEdges.add(edge);
				i = next_set_bit;
			}
			network.setSelectedEdgeState(passedEdges, true);
		}
		Cytoscape.getCurrentNetworkView().updateView();		
	}
	
	
	public static boolean isFilterNameDuplicated(String pFilterName) {
		if (FilterPlugin.getAllFilterVect() == null || FilterPlugin.getAllFilterVect().size() == 0)
			return false;
		
		for (int i=0; i<FilterPlugin.getAllFilterVect().size(); i++) {
			CompositeFilter theFilter = (CompositeFilter) FilterPlugin.getAllFilterVect().elementAt(i);
			if (pFilterName.equalsIgnoreCase(theFilter.getName().trim())) {
				return true;
			}
		}
		return false;
	}
	
	
	public static GenericIndex getQuickFindIndex(String pCtrlAttribute, CyNetwork pNetwork, int pIndexType) {
		final QuickFind quickFind = QuickFindFactory.getGlobalQuickFindInstance();
		quickFind.reindexNetwork(pNetwork, pIndexType, pCtrlAttribute, new TaskMonitorBase());
		
		return quickFind.getIndex(pNetwork);		
	}
	
	
	public static boolean hasSuchAttribute(String pAttribute, int pType) {
		int attributeType = CyAttributes.TYPE_UNDEFINED;
		if (pType == QuickFind.INDEX_NODES) {
			attributeType = Cytoscape.getNodeAttributes().getType(pAttribute);
		}
		else if (pType == QuickFind.INDEX_EDGES) {
			attributeType = Cytoscape.getEdgeAttributes().getType(pAttribute);					
		}
		
		if (attributeType == CyAttributes.TYPE_UNDEFINED)  {
			return false;
		}
		return true;
	}

}


