package org.cytoscape.biopax.internal;

import org.cytoscape.work.TaskManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedListener;

import org.cytoscape.biopax.internal.BioPaxFilter;
import org.cytoscape.biopax.internal.action.BioPaxViewTracker;
import org.cytoscape.biopax.internal.action.ExportAsBioPAXAction;
import org.cytoscape.biopax.internal.BioPaxReaderTaskFactory;
import org.cytoscape.biopax.internal.util.BioPaxVisualStyleUtil;
import org.cytoscape.biopax.internal.view.BioPaxContainer;
import org.cytoscape.biopax.internal.view.BioPaxDetailsPanel;
import org.cytoscape.biopax.internal.view.BioPaxCytoPanelComponent;
import org.cytoscape.biopax.internal.action.LaunchExternalBrowser;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.io.read.InputStreamTaskFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
		TaskManager<?,?> taskManagerRef = getService(bc,TaskManager.class);
		OpenBrowser openBrowserRef = getService(bc,OpenBrowser.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkNaming cyNetworkNamingRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryRef = getService(bc,CyNetworkViewFactory.class);
		FileUtil fileUtilRef = getService(bc,FileUtil.class);
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
		VisualMappingManager visualMappingManagerRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryRef = getService(bc,VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFunctionFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFunctionFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
				
		BioPaxFilter bioPaxFilter = new BioPaxFilter(streamUtilRef);
		LaunchExternalBrowser launchExternalBrowser = new LaunchExternalBrowser(openBrowserRef);	
		BioPaxDetailsPanel bioPaxDetailsPanel = new BioPaxDetailsPanel(launchExternalBrowser);
		BioPaxContainer bioPaxContainer = new BioPaxContainer(launchExternalBrowser,cyApplicationManagerRef,cyNetworkViewManagerRef,bioPaxDetailsPanel,cySwingApplicationRef);
		BioPaxVisualStyleUtil bioPaxVisualStyleUtil = new BioPaxVisualStyleUtil(visualStyleFactoryRef,visualMappingManagerRef,discreteMappingFunctionFactoryRef,passthroughMappingFunctionFactoryRef);
		
		BioPaxViewTracker bioPaxViewTracker = new BioPaxViewTracker(bioPaxDetailsPanel,bioPaxContainer, cyApplicationManagerRef);
		InputStreamTaskFactory inputStreamTaskFactory = new BioPaxReaderTaskFactory(bioPaxFilter,cyNetworkFactoryRef,cyNetworkViewFactoryRef,cyNetworkNamingRef,bioPaxViewTracker,visualMappingManagerRef,bioPaxVisualStyleUtil);
		CytoPanelComponent cytoPanelComponent = new BioPaxCytoPanelComponent(bioPaxContainer);
		ExportAsBioPAXAction exportAsBioPAXAction = new ExportAsBioPAXAction(fileUtilRef, cyApplicationManagerRef, bioPaxFilter, taskManagerRef);
		
		// register/export core Cytoscape osgi service implementations
		registerService(bc,inputStreamTaskFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,cytoPanelComponent,CytoPanelComponent.class, new Properties());
		registerService(bc,(RowsSetListener)bioPaxViewTracker,RowsSetListener.class, new Properties());
		registerService(bc,(NetworkViewAddedListener)bioPaxViewTracker,NetworkViewAddedListener.class, new Properties());
		registerService(bc,(NetworkViewAboutToBeDestroyedListener)bioPaxViewTracker,NetworkViewAboutToBeDestroyedListener.class, new Properties());
		registerService(bc,(SetCurrentNetworkViewListener)bioPaxViewTracker,SetCurrentNetworkViewListener.class, new Properties());
		
		// the next CyAction service (export BioPAX) is disabled: TODO needs refactoring; use ExportAsBioPAXTaskFactory as well...
		//registerService(bc, exportAsBioPAXAction, CyAction.class,  new Properties());
	}
}

