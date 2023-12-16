package draggableNodeEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;

/**
 * a cheesy way to make terminal points
 */
public class TerminalPoint extends DraggableNode<Void> {
	private static final long serialVersionUID = -1554483802522425955L;
	
	public final static int 
		pointDiameter = 12,
		pointBorderThickness = 3;
	public static Color 
		defaultPointColor 		= Color.yellow,
		defaultPointBorderColor	= Color.black; 
	
	public Color 
		pointColor 		 = TerminalPoint.defaultPointColor,
		pointBorderColor = TerminalPoint.defaultPointBorderColor;
	
	public TerminalPoint() {
		super(null);
		
		this.setBorder(null);
		
		int diameter = pointDiameter + pointBorderThickness;
		var dim = new Dimension(diameter, diameter);
		this.setPreferredSize(dim);
		
//		this.setOpaque(true);
	}
	
	public Point getPoint() {
		var bounds = this.getBounds();
		return new Point((int)bounds.getCenterX(), (int)bounds.getCenterY());
	}

	@Override public String getTitle() {
		return "terminal point " + this.index;
	}

	@Override public void initNode() {
		
	}

	@Override public JComponent getInspector() {
		return null;
	}
	
	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int 
			R =	pointDiameter + 2 * pointBorderThickness;
		
		System.out.println("size: " + this.getSize()+""
				+ "\n\t R " + R);
		
		g.setColor(pointBorderColor); 
		g.fillOval(0, 0, R, R);
		g.setColor(pointColor);
		g.fillOval(pointBorderThickness,pointBorderThickness, pointDiameter, pointDiameter);
	}
}
