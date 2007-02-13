
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

package cytoscape.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 *
 */
public class EditNetworkTitleDialog extends JDialog implements ActionListener {
	/**
	 * Creates a new EditNetworkTitleDialog object.
	 *
	 * @param parent  DOCUMENT ME!
	 * @param modal  DOCUMENT ME!
	 * @param pName  DOCUMENT ME!
	 */
	public EditNetworkTitleDialog(Component parent, boolean modal, String pName) {
		super((JFrame) parent, modal);
		initComponents();
		tfNetworkTitle.setText(pName);
		tfNetworkTitle.setSelectionStart(0);
		tfNetworkTitle.setSelectionEnd(pName.length());

		setSize(new java.awt.Dimension(300, 170));
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		jLabel1 = new JLabel();
		tfNetworkTitle = new JTextField();
		jPanel1 = new JPanel();
		btnOK = new JButton();
		btnCancel = new JButton();

		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);

		getContentPane().setLayout(new GridBagLayout());

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Edit Network Title");
		jLabel1.setText("Please enter new network title:");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(15, 10, 0, 0);
		getContentPane().add(jLabel1, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(10, 10, 10, 10);
		getContentPane().add(tfNetworkTitle, gridBagConstraints);

		btnOK.setText("OK");
		btnOK.setPreferredSize(new Dimension(65, 23));
		btnCancel.setPreferredSize(new Dimension(65, 23));

		jPanel1.add(btnOK);

		btnCancel.setText("Cancel");
		jPanel1.add(btnCancel);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridy = 2;
		gridBagConstraints.insets = new Insets(10, 0, 10, 0);
		getContentPane().add(jPanel1, gridBagConstraints);

		pack();
	} // </editor-fold>

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		Object _actionObject = e.getSource();

		// handle Button events
		if (_actionObject instanceof JButton) {
			JButton _btn = (JButton) _actionObject;

			if (_btn == btnOK) {
				networkTitle = tfNetworkTitle.getText();
				this.dispose();
			} else if (_btn == btnCancel) {
				this.dispose();
			}
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getNewNetworkTitle() {
		return networkTitle;
	}

	private String networkTitle = "";

	// Variables declaration - do not modify
	private JButton btnCancel;
	private JButton btnOK;
	private JLabel jLabel1;
	private JPanel jPanel1;
	private JTextField tfNetworkTitle;

	// End of variables declaration
}
