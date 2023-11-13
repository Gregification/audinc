package presentables.presents.draggableNodeEditor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;

import javax.swing.JPanel;

import audinc.gui.MainWin;
/**
 * GUI editor for draggable and linkable nodes. encouraged to inherit this class for more specialized uses
 * <p>
 * <b>to use :</b>  this class inherits JPanel and displays the main content (where nodes are dragged around)
 * there are 2 JPanels (get using {@code getEditorPanel()} and {@code getIndexPanel}) that are also part of the UI
 * @implNote MouseListener, MouseMotionListener, Serializable
 */
public class DraggableNodeEditor extends JPanel implements MouseListener, MouseMotionListener, Serializable {
	private static final long serialVersionUID = 1L;//eclipse auto generated

	protected JPanel 
		indexPanel,		//options related to the entire content panel. add nodes, define nodes, ect.
		editorPanel;	//options related to a single node. view & edit details about a selected node
	
	public DraggableNodeEditor() {
		setPreferredSize(MainWin.stdDimension);
		
		genGUI();
	}
	
	protected void genGUI() {
		indexPanel = new JPanel();
		editorPanel = new JPanel();
	}
	
	@Override public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	
	public JPanel getIndexPanel() {
		return this.indexPanel;
	}
	public JPanel getEditorPanel() {
		return this.editorPanel;
	}
}
