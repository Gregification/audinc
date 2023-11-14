package presentables.presents.draggableNodeEditor;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JComponent;

/**
 * a node object meant to be used by the {@code DraggableNodeEditor} class
 * @implNote Serializable
 */	
public class DraggableNode extends JComponent implements Serializable{
	public Point startPoint;
	public String title;
	
	@Override public String toString() {
		return title;
	}
////////////////////////////////
//	getters / setters
////////////////////////////////
	public boolean isBeingDragged() {
		return false;
	}
	
	private static final long serialVersionUID = 1L;
}
