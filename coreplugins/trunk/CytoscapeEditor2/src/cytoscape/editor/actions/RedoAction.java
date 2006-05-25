/*
 * Created on Jul 30, 2005
 *
 */
package cytoscape.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

import cytoscape.Cytoscape;
import cytoscape.editor.CytoscapeEditorManager;
import cytoscape.editor.impl.ShapePalette;

/**
 * redo an operation that has been undone
 * @author Allan Kuchinsky, Agilent Technologies
 *
 */
public class RedoAction extends AbstractAction {
	
	UndoManager undo;
	UndoAction undoAction;

	/**
	 * defines the method that is invoked when the user performs a "redo" of an undone operation.
	 * @param undo
	 */
	public RedoAction(UndoManager undo) {
		super("");
		this.undo = undo;
		setEnabled(false);
	}

	/**
	 * 
	 * method that is executed when the user performed an "redo delete" operation.
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			// AJK: 09/05/05 BEGIN
			// accommodate one UndoManager per NetworkView
//			undo.redo();
//			UndoManager undoMgr = CytoscapeEditorManager.getCurrentUndoManager();
			UndoManager undoMgr =
				CytoscapeEditorManager.getUndoManagerForView(Cytoscape.getCurrentNetworkView());
			undoMgr.redo();
			// AJK: 10/21/05 send end message to undo manager 
//			undoMgr.end();
			// AJK: 09/05/05 END			
		} catch (CannotRedoException ex) {
			System.out.println("Unable to redo: " + ex);
//			ex.printStackTrace();
		}

		
		update();
		undoAction.update();
	}
	

	/**
	 * enables and disables undo and redo operations, according to the last operation performed.
	 *
	 */
	public void update() {

    

		// accommodate one UndoManager per NetworkView
//		if (undo.canRedo()) {
		UndoManager undoMgr = CytoscapeEditorManager.getUndoManagerForView(
				Cytoscape.getCurrentNetworkView());
		ShapePalette palette = CytoscapeEditorManager.getShapePaletteForView(
				Cytoscape.getCurrentNetworkView());
		System.out.println ("for redo: " + this);
		System.out.println("REDO: " + undo.canRedo());
		if (undoMgr.canRedo()) {
		// AJK: 09/05/05 END			if (undo.canRedo()) {
			setEnabled(true);
			// AJK: 10/21/05 No name, just use button
//			putValue(Action.NAME, undo.getRedoPresentationName());
			if (palette != null)
			{
				palette.getRedoButton().setEnabled(true);
			}
		} else {
			setEnabled(false);
			// AJK: 10/21/05 
//			putValue(Action.NAME, "Redo");
			if (palette != null)
			{
				palette.getRedoButton().setEnabled(false);
			}
		}
	}
	
	public void update (boolean redoFlag)
	{
		UndoManager undoMgr = CytoscapeEditorManager.getUndoManagerForView(
				Cytoscape.getCurrentNetworkView());
		ShapePalette palette = CytoscapeEditorManager.getShapePaletteForView(
				Cytoscape.getCurrentNetworkView());
		System.out.println ("for redo: " + this);
		System.out.println("REDO: " + undo.canRedo());
		if ((undoMgr.canRedo() || (redoFlag))) {
		// AJK: 09/05/05 END			if (undo.canRedo()) {
			setEnabled(true);
			// AJK: 10/21/05 No name, just use button
//			putValue(Action.NAME, undo.getRedoPresentationName());
			if (palette != null)
			{
				palette.getRedoButton().setEnabled(true);
			}
		} else {
			setEnabled(false);
			// AJK: 10/21/05 
//			putValue(Action.NAME, "Redo");
			if (palette != null)
			{
				palette.getRedoButton().setEnabled(false);
			}
		}		
	}
	
	/**
	 * defines an undo action that corresponds with this RedoAction and is (or should be) the inverse of the 
	 * functionality of the redo operation.
	 * @param undoAction The UndoAction to set.
	 */
	public void setUndoAction(UndoAction undoAction) {
		this.undoAction = undoAction;
	}
}
