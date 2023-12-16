package draggableNodeEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import audinc.gui.MainWin;

/**
 * a node object meant to be used by the {@code DraggableNodeEditor} class.
 */	
public abstract class DraggableNode<T> extends JPanel{
	private static final long serialVersionUID = 1L;
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
	
	public volatile boolean isDraggable = true;
	
	protected volatile T context;
	private volatile ArrayList<NodeComponent<?>> connectableNodeComponents = new ArrayList<>();
	
	public DraggableNode(T context) {
		super();
		
		index = count;
		count++;
		
		this.setBackground(stdBackgroundColor);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.setContext(context);
	}
	
	public abstract String getTitle();
	public abstract JComponent getInspector();
	public abstract void initNode();
	
	public void initGUI() { applyDefaultNamedBorder(); }
	public void setContext(T newContext) { this.context = newContext; }
	public T getContext() { return context; }
	
	protected void applyDefaultNamedBorder() { 
		this.setBorder(stdBorder);
		
		this.setBorder(new TitledBorder(
				this.getBorder(),
				this.getTitle()
			));
	}
	
	/**
	 * regenerates the connection points for the NodeComponent relative to this DraggableNode.
	 * connection point is where the connection line will be drawn to. this means its up against the side of the DraggableNode,
	 * either left or right depending on if its a consumer, supplier, or some secrete third thing.
	 * @param comp
	 */
	protected void genConnectionPoint(NodeComponent... comps) {
		SwingUtilities.invokeLater(() -> {
			for(var comp : comps) {
				if(comp == null) return;
				
				synchronized(comp.getTreeLock()) {
				
					var nodeBounds			= this.getBounds();
					var nodeBorderInsets 	= this.getBorder().getBorderInsets(comp);
					var compBounds 			= SwingUtilities.convertRectangle(comp, comp.getBounds(), this);//component bounds, relative to this
					var compCPDimension 	= comp.getConnecitonPointDimensions();
					Point point = new Point(
							0,
							(int)(comp.getY() + comp.getHeight()/2 - compCPDimension.height/2)
						);
					
					if(comp instanceof NodeSupplier) {
						//point goes on right side
						comp.setAlignmentX(Component.RIGHT_ALIGNMENT);
						
						point.x = (int)(nodeBounds.width);
						
					}else if(comp instanceof NodeConsumer) {
						//point goes on left side
						comp.setAlignmentX(Component.LEFT_ALIGNMENT);
						
					}else {
						throw new UnsupportedOperationException("idk where the point should go");
					}
					
					connectableNodeComponents.add(comp);
					comp.connectionPoint = point;
					
					//keep for debugging
//					System.out.println("draggable node > gen conneciton point; new connection point|" +comp.connectionPoint+"| of component("+(comp.getClass().getCanonicalName())+"):" + comp.name+""
//							+ "\n\t\tgenerated point|" + point+ "|"
//							+ "\n\t\tis supplier?" + (comp instanceof NodeSupplier)
//							+ "\n\t\tis consumer?" + (comp instanceof NodeConsumer)
//							+ "\n\t\talignmentX: " + comp.getAlignmentX()
//							+ "\n\t\tconnection point dimension:" + compCPDimension
//							+ "\n\torgional:"
//							+ "\n\t\tcomp bounds:\t\t" + comp.getBounds()
//							+ "\n\t\tcomp size:\t\t" + comp.getSize()
//							+ "\n\t\tcomp preferred size:\t" + comp.getPreferredSize()
//							+ "\n\t\tcomp border:\t\t" + comp.getBorder()
//							+ "\n\t\tcomp border insets@comp:\t" + ((comp.getBorder()==null)?"null":comp.getBorder().getBorderInsets(comp))
//							+ "\n\t\tnode bounds:\t\t" + this.getBounds()
//							+ "\n\t\tnode size:\t\t" + this.getSize()
//							+ "\n\t\tnode preferred size:\t" + this.getPreferredSize()
//							+ "\n\t\tnode border:\t\t" + this.getBorder()
//							+ "\n\t\tnode border insets@comp:\t" + nodeBorderInsets
//							+ "\n\t\tpoint relative to node:\t" + SwingUtilities.convertPoint(comp, comp.getLocation(), this)
//							+ "\n\t\tbounds relative to node:\t" + compBounds
//						);
				}
			}
			
			this.repaint();
		});
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
//				System.out.println("draggable node > paint component; drawing point|"+e.connectionPoint.toString()+"|node:"+ e.name);
				e.drawConnectionPoint(g, e.connectionPoint);
			}
		}
	}
	
	/**
	 * @param aPoint relative to this DraggableNode
	 * @return the selected component, or null is none are avaliable
	 */
	public NodeComponent getComponentForPoint(Point aPoint) {
		for(var comp : this.connectableNodeComponents) {
			if(comp.isInConnectionSeletionRegion(aPoint))
				return comp;
		}
		return null;
	}
}
