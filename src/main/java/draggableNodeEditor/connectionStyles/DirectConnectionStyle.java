package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import draggableNodeEditor.ConnectionStyle;
import draggableNodeEditor.LineAnchor;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	protected boolean objsticalNavication = false; //TODO

	@Override public CompletableFuture<LinkedBlockingQueue<Point>> genConnections(
			LinkedBlockingQueue<Point> output,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Rectangle[] obsticals) {
		return CompletableFuture.supplyAsync(() -> {
			for(var t : terminals) {
				output.add(new Point(t.x(), t.y()));
			}
			
			return output;
		});
	}
}
