//-------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//-------------------------------------------------------------------------
package cytoscape.actions;
//-------------------------------------------------------------------------
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyNetworkView;
import giny.model.Node;

import java.awt.event.ActionEvent;
import java.util.Iterator;

public class NewWindowSelectedNodesOnlyAction extends CytoscapeAction {

    public NewWindowSelectedNodesOnlyAction () {
        super("Selected nodes, All edges");
        setPreferredMenu( "Select.To New Network" );
        setAcceleratorCombo(java.awt.event. KeyEvent.VK_N, ActionEvent.CTRL_MASK );
    }

    public void actionPerformed(ActionEvent e) {
        //save the vizmapper catalog

      CyNetworkView current_network_view = Cytoscape.getCurrentNetworkView();
      CyNetwork current_network = current_network_view.getNetwork();

      int [] nodes = current_network_view.getSelectedNodeIndices();

      CyNetwork new_network = Cytoscape.createNetwork( nodes, current_network.getConnectingEdgeIndicesArray( nodes ) , current_network.getTitle()+"->child", current_network );
      new_network.setExpressionData( current_network.getExpressionData() );

      CyNetworkView new_view = Cytoscape.getNetworkView( new_network.getIdentifier() );
      if ( new_view == null )
        return;

      Iterator i = new_network.nodesIterator();
      while ( i.hasNext() ) {
        Node node = ( Node )i.next();
        new_view.getNodeView( node ).setOffset( current_network_view.getNodeDoubleProperty( node.getRootGraphIndex(), CyNetworkView.NODE_X_POSITION ),
                                                current_network_view.getNodeDoubleProperty( node.getRootGraphIndex(), CyNetworkView.NODE_Y_POSITION ) );
      }


    }


}


