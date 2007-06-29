
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

package ManualLayout.control.view;

import ManualLayout.control.actions.stack.*;

import cytoscape.view.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;


/**
 *
 */
public class StackPanel extends JPanel {
	/**
	 * Creates a new StackPanel object.
	 */
	public StackPanel() {

		ImageIcon vali = new ImageIcon(getClass().getResource("/V_STACK_LEFT.gif"));
		ImageIcon vaci = new ImageIcon(getClass().getResource("/V_STACK_CENTER.gif"));
		ImageIcon vari = new ImageIcon(getClass().getResource("/V_STACK_RIGHT.gif"));
		ImageIcon hati = new ImageIcon(getClass().getResource("/H_STACK_TOP.gif"));
		ImageIcon haci = new ImageIcon(getClass().getResource("/H_STACK_CENTER.gif"));
		ImageIcon habi = new ImageIcon(getClass().getResource("/H_STACK_BOTTOM.gif"));

		VStackLeft val = new VStackLeft(vali);
		VStackCenter vac = new VStackCenter(vaci);
		VStackRight var = new VStackRight(vari);

		HStackTop hat = new HStackTop(hati);
		HStackCenter hac = new HStackCenter(haci);
		HStackBottom hab = new HStackBottom(habi);

		setLayout(new java.awt.GridBagLayout());

		//JPanel h_panel = new JPanel();
		java.awt.GridBagConstraints gridBagConstraints;
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);

		add(createJButton(val, "Vertical Left"), gridBagConstraints);
		add(createJButton(vac, "Vertical Center"), gridBagConstraints);
		add(createJButton(var, "Vertical Right"), gridBagConstraints);
		//JPanel v_panel = new JPanel();
		add(createJButton(hat, "Horizontal Top"), gridBagConstraints);
		add(createJButton(hac, "Horizontal Center"), gridBagConstraints);
		add(createJButton(hab, "Horizontal Bottom"), gridBagConstraints);


		//setLayout( new BorderLayout() );
		//add( h_panel, BorderLayout.EAST );
		//add( v_panel, BorderLayout.WEST );
		setBorder(new TitledBorder("Stack"));
	}

	protected JButton createJButton(Action a, String tt) {
		JButton b = new JButton(a);
		b.setToolTipText(tt);
		b.setPreferredSize(new Dimension(27, 18));

		return b;
	}
}
