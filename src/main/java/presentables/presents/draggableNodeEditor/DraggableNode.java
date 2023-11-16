package presentables.presents.draggableNodeEditor;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * a node object meant to be used by the {@code DraggableNodeEditor} class
 * @implNote Serializable
 */	
public class DraggableNode extends JPanel implements Serializable {
	
	
	public volatile String 
		title 		= "default title",
		description = "default descrpition";
	public volatile boolean isDraggable = true;
	
	protected final static Border 
		stdBorder = BorderFactory.createLineBorder(Color.black),
		stdBorderEmphasis1 = BorderFactory.createBevelBorder(BevelBorder.RAISED);
	
	protected final static Color
		stdBackgroundColor 			= Color.LIGHT_GRAY,
		stdBackgroundColorEmphasis1 = Color.LIGHT_GRAY;
	
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
