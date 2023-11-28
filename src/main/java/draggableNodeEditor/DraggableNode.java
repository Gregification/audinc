package draggableNodeEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import audinc.gui.MainWin;
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
	public final static Dimension
		connectionPointSize = MainWin.stdtabIconSize;
	
	private static int count = 0;
	protected int index;
	
	protected final static Color
		stdBackgroundColor 			= Color.LIGHT_GRAY,
		stdBackgroundColorEmphasis1 = Color.LIGHT_GRAY;

	public final static double terminalPointSelectionRadius = 3;
	
	private static final long serialVersionUID = 1L;
	
	public volatile boolean isDraggable = true;
	
	protected volatile T context;
	private volatile ArrayList<NodeComponent> connectableNodeComponents = new ArrayList<>();
	
	public DraggableNode(T context) {
		super();
		
		index = count;
		count++;
		
		this.setBorder(stdBorder);
		this.setBackground(stdBackgroundColor);
		
		genBorder();
		
		this.setContext(context);
	}
	
	public abstract String getTitle();
	public abstract void initGUI();
	public abstract void initNode();
	
	public abstract JComponent getInspector();
	public void setContext(T newContext) {
		this.context = newContext;
	}
	
	public T getContext() { return context; }
	
	protected void genBorder() { 
		this.setBorder(new TitledBorder(
				null,
				this.getTitle()
			));
	}
	
	/**
	 * regenerates the connection points for the NodeComponent relative to this DraggableNode
	 * @param comp
	 */
	protected void genConnectionPoint(NodeComponent comp) {			
		Point point = new Point(
				0,
				comp.getY() + comp.getWidth()/2
			);
		
		if(comp instanceof NodeSupplier) {
			//point goes on  right side
			
		}else if(comp instanceof NodeConsumer) {
			//point goes on left side
			point.x = (int)(this.getBounds().getWidth());
		}else {
			throw new UnsupportedOperationException("idk where the point should go");
		}
		
		connectableNodeComponents.add(comp);
		comp.connectionPoint = point;
		System.out.println("new connection point|" +comp.connectionPoint+"| of component:" + comp.name);
	}
	
	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for(int i = 0, size = connectableNodeComponents.size(); i < size; i++) {
			var e = connectableNodeComponents.get(i);
			
			if(e.getParent() != this) {
				connectableNodeComponents.remove(i);
				i--;
				size--;
			}else {
				System.out.println("draggable node > paint component; drawing point|"+e.connectionPoint.toString()+"|node:"+ e.name);
				e.drawConnectionPoint(g, e.connectionPoint);
			}
		}
	}
}
