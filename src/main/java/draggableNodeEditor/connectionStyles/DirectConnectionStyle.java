package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

import draggableNodeEditor.ConnectionStyle;
import draggableNodeEditor.NodeConnection;
import draggableNodeEditor.TerminalPoint;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	protected boolean objsticalNavication = false; //TODO
	
	@Override public <T> void genConnections(NodeConnection<T> nodeConnection, Rectangle[] obstacles) {
		var it = nodeConnection.iterator();
		
		if(!it.hasNext()) return;
		
		TerminalPoint<T> 
				next = it.next(),	//next
				curr = next;		//current
		while(it.hasNext()) {
			curr = next;
			next = it.next();
			
			curr.pathToNext = new Point[] {next.getPoint()};
			curr.needsRepathed = false;
		}
		
		curr.pathToNext = new Point[0];
		curr.needsRepathed = false;
		
	}

	@SuppressWarnings("unchecked")
	@Override public <T> void genConnection(NodeConnection<T> nodeConnection, Rectangle[] obstacles, Set<TerminalPoint<T>> terminalsToReconnect) {
		var arr = nodeConnection.toArray(TerminalPoint[]::new);//apparently including a generic in the array type dosen't fly
		
		for(int i = 0; i < arr.length - 1; i++) {
			TerminalPoint<T> tp = arr[i];
			if(terminalsToReconnect.contains(tp)) {
				terminalsToReconnect.remove(tp);
				
				tp.pathToNext = new Point[] {arr[i+1].getPoint()};
				tp.needsRepathed = false;
			}
		}
		
		var lastEle = arr[arr.length - 1];
		lastEle.pathToNext = new Point[0];
		lastEle.needsRepathed = false;
		
	}
}
