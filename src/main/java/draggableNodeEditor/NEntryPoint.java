package draggableNodeEditor;

/**
 * the standard entry point for what ever draggable-node thing your trying to do
 */
public abstract class NEntryPoint extends DraggableNode {
	private static final long serialVersionUID = 1L;
	
	public NEntryPoint() {
		super();
		
		this.setBorder(DraggableNode.stdBorderEmphasis1);
		this.setBackground(DraggableNode.stdBackgroundColorEmphasis1);
	}
	
	
}
