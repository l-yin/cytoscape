package org.cytoscape.coreplugin.cpath2.view;

import org.cytoscape.coreplugin.cpath2.task.ExecuteGetRecordByCPathId;
import org.cytoscape.coreplugin.cpath2.schemas.summary_response.BasicRecordType;
import org.cytoscape.coreplugin.cpath2.web_service.CPathWebService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import cytoscape.Cytoscape;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

/**
 * Download Details Frame.
 *
 * @author Ethan Cerami
 */
public class DownloadDetails extends JDialog {

    /**
     * Constructor.
     * @param passedRecordList      List of Records that Passed over Filter.
     * @param peName                Name of Physical Entity.
     */
    public DownloadDetails(java.util.List<BasicRecordType> passedRecordList,
            String peName) {
        super();
        this.setTitle("Retrieval Confirmation");
        this.setModal(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        GradientHeader header = new GradientHeader("Confirm Retrieval: "+ passedRecordList.size()
                + " records");
        contentPane.add(header, BorderLayout.NORTH);

        DefaultTableModel tableModel = new NonEditableTableModel();
        Vector headerList = new Vector();
        headerList.add("Name (if available)");
        headerList.add("Type");
        headerList.add("Data Source");
        tableModel.setColumnIdentifiers(headerList);
        tableModel.setRowCount(passedRecordList.size());
        JTable table = new JTable(tableModel);

        //  Adjust width / height of viewport;  fixes bug #1620.
        Dimension d = table.getPreferredSize();
        d.width = d.width * 2;
        if (d.height > 200) {
            d.height = 200;
        }
        table.setPreferredScrollableViewportSize(d);
        table.setAutoCreateColumnsFromModel(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final long ids[] = new long[passedRecordList.size()];
        int i = 0;
        for (BasicRecordType record : passedRecordList) {
            if (record.getName().equalsIgnoreCase("N/A")) {
                record.setName("---");
            }
            tableModel.setValueAt(record.getName(), i, 0);
            tableModel.setValueAt(record.getEntityType(), i, 1);
            if (record.getDataSource() != null) {
                tableModel.setValueAt(record.getDataSource().getName(), i, 2);
            } else {
                tableModel.setValueAt("---", i, 3);
            }
            ids[i++] = record.getPrimaryId();
        }
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel(ids, peName, this);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(Cytoscape.getDesktop());
    }

    /**
     * Button Panel.
     */
    private JPanel createButtonPanel(final long[] ids, final String peName,
            final JDialog dialog) {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
                dialog.dispose();
                downloadInteractions(ids, peName);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Downloads interaction bundles in a new thread.
     */
    private void downloadInteractions(long ids[], String peName) {
        String networkTitle = peName + ":  Network";
        CPathWebService webApi = CPathWebService.getInstance();
        ExecuteGetRecordByCPathId task = new ExecuteGetRecordByCPathId(webApi, ids, networkTitle);
        JTaskConfig jTaskConfig = new JTaskConfig();
        jTaskConfig.setOwner(Cytoscape.getDesktop());
        jTaskConfig.displayStatus(true);
        jTaskConfig.setAutoDispose(true);
        jTaskConfig.displayCancelButton(true);
        TaskManager.executeTask(task, jTaskConfig);
    }
}

class NonEditableTableModel extends DefaultTableModel {

    /**
     * Constructor.
     */
    public NonEditableTableModel() {
        super();
    }

    /**
     * Is the specified cell editable?  Never!
     *
     * @param row row index.
     * @param col col index.
     * @return false.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
