package presentables.presents.draggableNodeEditor;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JComponent;

/**
 * a node object meant to be used by the {@code DraggableNodeEditor} class
 * @implNote Serializable, ActionListener
 */	
public class DraggableNode extends JComponent implements Serializable, ActionListener{
	public Point startPoint;
	private boolean isBeingDragged = false;
	
	public void Input(Object[] data) {
		
	}
	
////////////////////////////////
//	getters / setters
////////////////////////////////
	public boolean isBeingDragged() {
		return false;
	}
	
	private static final long serialVersionUID = 1L;

	@Override public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
