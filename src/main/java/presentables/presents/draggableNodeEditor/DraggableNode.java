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
 */	
public abstract class DraggableNode extends JPanel implements Serializable, Runnable{
	public volatile String 
		title 		= "default title",
		description = "default descrpition";
	public volatile boolean isDraggable = true;
	
	protected final static Border 
		stdBorder 			= BorderFactory.createLineBorder(Color.black),
		stdBorderEmphasis1 	= BorderFactory.createBevelBorder(BevelBorder.RAISED);
	
	protected final static Color
		stdBackgroundColor 			= Color.LIGHT_GRAY,
		stdBackgroundColorEmphasis1 = Color.LIGHT_GRAY;
	
	public DraggableNode() {
		super();
		this.setBorder(stdBorder);
		this.setBackground(stdBackgroundColor);
	}
	
	public abstract void init();
	
	@Override public String toString() {
		return title;
	}
	
	private static final long serialVersionUID = 1L;

}
