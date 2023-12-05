package draggableNodeEditor;

import java.awt.Point;

/**
 * a cheesy way to make terminal points
 */
public class TerminalPoint<T> {
	public final Class<T> type;
	
	public Point[] pathToNext = new Point[0];
	public boolean needsRepathed = true;
	
	public NodeComponent<T> targetComponent = null;
	
	private Point point = new Point(0,0);
	
	public TerminalPoint(Class<T> type) {
		this.type = type;
	}
	
	public Point getPoint() {
		if(this.targetComponent != null) {
			needsRepathed = !point.equals(targetComponent.connectionPoint);
			
			point.x = this.targetComponent.connectionPoint.x;
			point.y = this.targetComponent.connectionPoint.y;
		}
			
		return this.point;
	}
	
	public boolean pathEndsOn(TerminalPoint<T> otherTerminal) {
		if(pathIsEmpty()) return false;
		
		return pathToNext[pathToNext.length-1].equals(otherTerminal.getPoint());
	}
	
	public boolean pathIsEmpty() {
		return pathToNext == null || pathToNext.length == 0;
	}
	
	public void emptyPath() {
		this.pathToNext = new Point[0];
	}
	
	public void setPoint(Point point) {
		this.needsRepathed = !point.equals(this.point);
		
		this.point = point;
	}
	public void setPoint(int x, int y) {
		this.needsRepathed = point.x != x || point.y != y;
		
		point.x = x;
		point.y = y;
	}
}
