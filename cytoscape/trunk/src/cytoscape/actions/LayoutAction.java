//-------------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
//-------------------------------------------------------------------------
package cytoscape.actions;
//-------------------------------------------------------------------------
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import cytoscape.view.NetworkView;
//-------------------------------------------------------------------------
public class LayoutAction extends AbstractAction   {
    NetworkView networkView;
    
    public LayoutAction(NetworkView networkView) {
        super("Layout whole graph");
        this.networkView = networkView;
    }
    
    public void actionPerformed(ActionEvent e) {
	if ( networkView.getCytoscapeObj().getConfiguration().isYFiles() ) {    
		/* this forces a layout, but doesn't reapply the appearances */
		networkView.redrawGraph(true, false);
	}
	else { //for giny apply layout for the whole graph view
		networkView.applyLayout(networkView.getView());
	}
    }
}

