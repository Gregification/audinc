package presentables.presents.draggableNodeEditor;

/**
 * the standard entry point for what ever draggable-node thing your trying to do
 */
public class DragNEntryPoint extends DraggableNode {
	private static final long serialVersionUID = 1L;
	
	public DragNEntryPoint() {
		super();
		
		this.title = "entry point";
		this.setBorder(DraggableNode.stdBorderEmphasis1);
		this.setBackground(DraggableNode.stdBackgroundColorEmphasis1);
	}
	
	
}
