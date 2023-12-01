package draggableNodeEditor;

import java.awt.Point;

/**
 * a cheesy way to make terminal points
 */
public class TerminalPoint<T> {
	public NodeComponent<T> targetComponent;
	
	private Point point = new Point(0,0);
	public Point[] pathToNext = new Point[0];
	
	public TerminalPoint() {}
	
	public Point getPoint() {
		if(this.targetComponent != null) {
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
}
