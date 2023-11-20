package draggableNodeEditor;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * <b>ALL CHILDREN OF THIS CLASS MUST HAVE A DEFAULT(ZERO PARAMATER) CONSTRUCTOR!!! (because i cheesed the new-node-dialog)</b>
 * 
 * <br>a node object meant to be used by the {@code DraggableNodeEditor} class.
 */	
public abstract class DraggableNode extends JPanel implements Serializable{
	public volatile boolean isDraggable = true;
	
	public final static Border 
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
	
	public abstract String getTitle();
	public abstract void initGUI();
	public abstract void initNode();
	public abstract List<NodeComponent> getNodeComponents();
	public abstract JComponent getInspector();
	
	private static final long serialVersionUID = 1L;
}
