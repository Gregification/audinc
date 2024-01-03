package draggableNodeEditor.NodeConnectionDrawer;

import java.awt.Component;

import javax.swing.SwingUtilities;

import draggableNodeEditor.AnchorPoint;
import draggableNodeEditor.NodeComponent;
import draggableNodeEditor.NodeSupplier;

/**
 * a anchor point for ConnectionStyle to consider.
 * values do not have to be normalized.
 * 
 * <br><b>NOTE! :</b><u> the exact effect of each value is determined by the ConnectionStyle using it.</u> the following are guidelines of what they should cause 
 * @pram x , y : position. 
 * @pram weight : the scale of the effect on the line. higher scale => more influence. can be negative. 
 * @pram bias : a modifier for the weight. IN and OUT are, respectively, before and after the line "passes" the anchor. 
 */
public record LineAnchor(
			int x,
			int y,
			float weight,
			float IN_biasX,
			float IN_biasY,
			float OUT_biasX,
			float OUT_biasY
		) {
	
	public static LineAnchor getFromAnchorPoint(AnchorPoint anc) {
		return new LineAnchor(
				anc.getX(),
				anc.getY(),
				anc.weight,
				anc.getIncommingBiasX(),
				anc.getIncommingBiasY(),
				anc.getOutgoingBiasX(),
				anc.getOutgoingBiasY()
			);
	}

	public static LineAnchor getFromNodeComponent(NodeComponent<?> comp, Component hostComp) {
		int bias = (comp instanceof NodeSupplier) ? 1  : -1;//if is not supplier then is consumer
		
		var conPoint = SwingUtilities.convertPoint(comp.hostNode, comp.connectionPoint, hostComp);
		System.out.println("line ancor > get from node component : ");
		return new LineAnchor(
					conPoint.x,
					conPoint.y,
					1,
					bias,
					0,
					bias,
					0
				);
	}
	
	/**
	 * finds the cost(in terms of distance) of traversing from this node to the given node. override as needed for custom lines.
	 * the cost is not necessarily the same as distance.
	 * @param other : the other point
	 * @return a float between -inf and +inf , inclusive, with large positive numbers representing large distances, and large negative numbers representing small distances
	 */
	public float costTo(LineAnchor other) {
		int dy = other.y - this.y, 
			dx = other.x - this.x; 
		return dy*dy + dx*dx;
	}
}
