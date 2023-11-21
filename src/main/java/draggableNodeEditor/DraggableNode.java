package draggableNodeEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import presentables.presents.serialPoke.Wrappable;

/**
 * <b>ALL CHILDREN OF THIS CLASS MUST HAVE A DEFAULT(ZERO PARAMATER) CONSTRUCTOR!!! (because i cheesed the new-node-dialog)</b>
 * 
 * <br>a node object meant to be used by the {@code DraggableNodeEditor} class.
 */	
public abstract class DraggableNode<T> extends JPanel implements Serializable{
	public final static Border 
		stdBorder 			= BorderFactory.createLineBorder(Color.black),
		stdBorderEmphasis1 	= BorderFactory.createBevelBorder(BevelBorder.RAISED);
	
	private static int count = 0;
	protected int index;
	
	protected final static Color
		stdBackgroundColor 			= Color.LIGHT_GRAY,
		stdBackgroundColorEmphasis1 = Color.LIGHT_GRAY;
	
	private static final long serialVersionUID = 1L;
	
	public volatile boolean isDraggable = true;
	
	protected volatile T context;
	
	public DraggableNode(T context) {
		super();
		
		index = count;
		count++;
		
		this.setBorder(stdBorder);
		this.setBackground(stdBackgroundColor);
		this.setContext(context);
	}
	
	public abstract String getTitle();
	public abstract void initGUI();
	public abstract void initNode();
	public abstract List<NodeComponent> getNodeComponents();
	public abstract JComponent getInspector();
	public void setContext(T newContext) {
		this.context = newContext;
	}
	
	public T getContext() { return context; }
}
