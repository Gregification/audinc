package draggableNodeEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
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
	protected volatile ArrayList<NodeComponent> nodeComponents = new ArrayList<>();
	
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
	
	public void updateNodeComponents() {
		
	}
	
	/**
	 * generates the connection-regions, and connection-points for each available connection.
	 * <br>done with consideration of the preffered connection regions of the NodeComponent.
	 * @return
	 */
	protected List<Rectangle> genConnectionRegion(NodeComponent comp) {
		assert comp != null : "theres no point to it";
		//make a connection point
		genPoint(comp);
		
		//make a connection region for the connection point
		List<Rectangle> regions = List.of();
		
		if(comp.connectionPoint == null) return List.of();
		
		return List.of(
				new Rectangle(
				comp.connectionPoint.x,
				comp.connectionPoint.y,
				connectionPointSize.width,
				connectionPointSize.height
			));
	}
	
	/**
	 * draws the terminal points
	 */
	public void drawConnectionPoints() {
		
	}
	
	/**
	 * regenerates all the connection points for this object
	 * @param comp
	 */
	private void genPoint(NodeComponent comp) {			
		Point point = new Point(
				0,
				comp.getY()
			);
		
		if(comp instanceof NodeSupplier) {
			//point goes on  right side
			
		}else if(comp instanceof NodeConsumer) {
			//point goes on left side
			point.x = (int)(this.getBounds().getWidth() - connectionPointSize.getWidth());
		}else {
			throw new UnsupportedOperationException("idk where the point should go");
		}
		
		comp.connectionPoint = point;
		}
	}
