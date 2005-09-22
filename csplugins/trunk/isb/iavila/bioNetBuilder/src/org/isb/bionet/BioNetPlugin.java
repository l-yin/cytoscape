/**
 * 
 */
package org.isb.bionet;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import cytoscape.*;
import cytoscape.plugin.*;
import org.isb.xmlrpc.client.*;
import org.isb.bionet.datasource.interactions.*;
import org.isb.bionet.gui.wizard.*;
import org.isb.iavila.ontology.xmlrpc.*;
import java.lang.Exception;

/**
 * 
 * @author <a href="mailto:iavila@systemsbiology.org">Iliana Avila-Campillo</a>
 */
public class BioNetPlugin extends CytoscapePlugin {

    protected InteractionDataClient interactionsClient;
    protected GOClient goClient;

    protected NetworkBuilderWizard wizard;

    /**
     * Constructor
     */
    public BioNetPlugin() {

        try {

            this.interactionsClient = (InteractionDataClient) DataClientFactory
                    .getClient("interactions");
            

            if (this.interactionsClient != null) {
                System.out
                        .println("Successfully got an InteractionDataClient!!!");
            } else {
                System.out
                        .println("Could not get an InteractionDataClient!!!");
            }
            
            this.goClient = (GOClient)DataClientFactory.getClient("geneOntology");
            if (this.goClient != null) {
                System.out
                        .println("Successfully got a GOClient!!!");
            } else {
                System.out
                        .println("Could not get a GOClient!!!");
            }
 
            this.interactionsClient
                    .addSource("org.isb.bionet.datasource.interactions.ProlinksInteractionsSource");
        
            this.interactionsClient
            .addSource("org.isb.bionet.datasource.interactions.KeggInteractionsSource");
            System.out.println(interactionsClient.getSources());
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.wizard = new NetworkBuilderWizard(this.interactionsClient);

        Cytoscape.getDesktop().getCyMenus().getOperationsMenu().add(
                new AbstractAction("Build a Biological Network...") {
                    public void actionPerformed(ActionEvent e) {
                        wizard.startWizard();
                    }// actionPerformed
                });
    }// BioNetPlugin

}// BioNetPlugin
