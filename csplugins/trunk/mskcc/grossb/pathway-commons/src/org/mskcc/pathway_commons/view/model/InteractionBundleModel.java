package org.mskcc.pathway_commons.view.model;

import org.mskcc.pathway_commons.schemas.summary_response.SummaryResponseType;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Observable;

/**
 * Contains information regarding the currently selected set of interaction bundles.
 *
 * @author Ethan Cerami
 */
public class InteractionBundleModel extends Observable {
    private RecordList recordList;

    /**
     * Sets the SummaryResponse Object.
     * @param recordList Record List.
     */
    public void setRecordList (RecordList recordList) {
        this.recordList = recordList;
        this.setChanged();
        this.notifyObservers();
    }

    /**
     * Gets the Record List.
     * @return RecordList Object.
     */
    public RecordList getRecordList() {
        return recordList;
    }
}