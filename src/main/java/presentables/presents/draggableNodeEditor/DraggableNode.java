package presentables.presents.draggableNodeEditor;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * a node object meant to be used by the {@code DraggableNodeEditor} class
 * @implNote Serializable
 */	
public class DraggableNode extends JPanel implements Serializable {
	public String title = "default title";
	
	private final static Border stdBorder = BorderFactory.createLineBorder(Color.black);
	private final static Color
		stdBackgroundColor 	= Color.LIGHT_GRAY;
	
	public DraggableNode() {
		super();
		this.setBorder(stdBorder);
		this.setBackground(stdBackgroundColor);
	}
	
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
