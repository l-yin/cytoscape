
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.ding.impl.events;

import org.cytoscape.model.CyNode;
import org.cytoscape.ding.GraphView;

import java.util.List;

public final class GraphViewNodesRestoredEvent extends GraphViewChangeEventAdapter {
	private final static long serialVersionUID = 1202416512142917L;
	private final GraphView m_view;
	private final List<CyNode> m_restoredNodeInx;

	public GraphViewNodesRestoredEvent(GraphView view, List<CyNode> restoredNodeInx) {
		super(view);
		m_view = view;
		m_restoredNodeInx = restoredNodeInx;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final int getType() {
		return NODES_RESTORED_TYPE;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final CyNode[] getRestoredNodes() {
		final CyNode[] returnThis = new CyNode[m_restoredNodeInx.size()];

		for (int i = 0; i < returnThis.length; i++)
			returnThis[i] = m_restoredNodeInx.get(i);

		return returnThis;
	}
}
