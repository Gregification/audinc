package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Polygon;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import draggableNodeEditor.ConnectionStyle;
import draggableNodeEditor.LineAnchor;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	public volatile boolean 			//TODO
		objsticalNavication = false,
		considerAnchors 	= false;

	@Override public CompletableFuture<LinkedBlockingQueue<Point>> genConnections(
			LinkedBlockingQueue<Point> output,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Polygon[] obsticals) {
		return CompletableFuture.supplyAsync(() -> {
			for(var t : terminals) {
				output.add(new Point(t.x(), t.y()));
			}
			
			return output;
		});
	}
}
