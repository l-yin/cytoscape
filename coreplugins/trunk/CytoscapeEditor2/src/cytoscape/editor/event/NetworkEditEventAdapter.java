/*
 * Created on May 17, 2005
 *
 */
package cytoscape.editor.event;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import cytoscape.editor.CytoscapeEditor;

import phoebe.PGraphView;
import phoebe.PhoebeCanvasDropEvent;
import phoebe.PhoebeCanvasDropListener;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.util.PNodeLocator;

/**
 * 
 * The <b>NetworkEditEventAdapter</b> class provides stub methods for specialized 
 * network edit event handlers, as part of the graph editing framework.
 * The specialized network edit event handler is the key class in the Cytoscape editor for defining the 
 * behavior of the editor.  The behavior is defined in terms of how the event handler responds to mouse events, drag/drop events,
 * and button press events.  All editors must include a network edit event handler class that extends the
 * <b>NetworkEditEventAdapter</b> class.  
 * 
 * @author Allan Kuchinsky
 * @version 1.0
 * 
 */
public class NetworkEditEventAdapter 
   extends PBasicInputEventHandler implements
		ActionListener,
		PhoebeCanvasDropListener,
		cytoscape.data.attr.MultiHashMapListener
		{

	protected PCanvas canvas;

	protected PGraphView view;

	PNodeLocator locator;

	CytoscapeEditor _caller;
	
	
	public NetworkEditEventAdapter() {
	}
	

	/**
	 * starts up the event handler on the input network view
	 * adds an input event listener to the view's canvas
	 * @param view a Cytoscape network view
	 */
	public void start(PGraphView view) {
		this.view = view;
		this.canvas = view.getCanvas();
		canvas.addInputEventListener(this);
	}

	/**
	 * stops the event handler by removing the input event listener from the canvas
	 * this is called when the user switches between editors
	 *
	 */
	public void stop() {
		if (canvas != null) {
			canvas.removeInputEventListener(this);
			this.view = null;
			this.canvas = null;
		}
	}


	/**
	 * 
	 * @return the current canvas
	 */
	public PCanvas getCanvas() {
		return canvas;
	}


	public void mousePressed(PInputEvent e) {
//		System.out.println ("Calling mousePressed on: " + super.getClass());
		super.mousePressed(e);
	}

	public void mouseMoved(PInputEvent e) {
		super.mouseMoved(e);

	}

	public void mouseEntered(PInputEvent e) {
		super.mouseEntered(e);
	}

	public void mouseExited(PInputEvent e) {
		super.mouseExited(e);
	}

	public void mouseDragged(PInputEvent e) {
		super.mouseDragged (e);

	}


	/**
	 * method for rendering an edge under construction as the user moves the mouse
	 * typically this may be done via a rubberband-line that udpates as the mouse position changes
	 *
	 */
	public void updateEdge() {

	}



	public void actionPerformed(ActionEvent evt) {

	}
	
	/**
	 * method for responding when an item is dropped onto the canvas.
	 * typically this would result in the addition of a node or an edge to the 
	 * current Cytoscape network.
	 */
	public void itemDropped (PhoebeCanvasDropEvent dte)
	{
		
	}

	
	/**
	 * 
	 * MultiHashMapListener methods
	 */
	public void attributeValueAssigned(java.lang.String objectKey,
			java.lang.String attributeName, java.lang.Object[] keyIntoValue,
			java.lang.Object oldAttributeValue,
			java.lang.Object newAttributeValue) {
//		System.out.println("attributeValueAssigned");

	}

	public void attributeValueRemoved(java.lang.String objectKey,
			java.lang.String attributeName, java.lang.Object[] keyIntoValue,
			java.lang.Object attributeValue) {

	}

	public void allAttributeValuesRemoved(java.lang.String objectKey,
			java.lang.String attributeName) {

	}

	/**
	 * @return Returns the _caller.
	 */
	public CytoscapeEditor get_caller() {
		return _caller;
	}

	/**
	 * @param _caller The _caller to set.
	 */
	public void set_caller(CytoscapeEditor _caller) {
		this._caller = _caller;
	}
	
	
	/**
	 * @return Returns the view.
	 */
	public PGraphView getView() {
		return view;
	}


	/**
	 * @param view The view to set.
	 */
	public void setView(PGraphView view) {
		this.view = view;
	}

}