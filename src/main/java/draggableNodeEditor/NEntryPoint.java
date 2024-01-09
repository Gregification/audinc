package draggableNodeEditor;

/**
 * the standard entry point for what ever draggable-node thing your trying to do.
 * honestly i forgot what I was doing with this class
 */
public abstract class NEntryPoint<T> extends DraggableNode<T> {
	private static final long serialVersionUID = 1L;
	
	public NEntryPoint(T context) {
		super(context);
		
		this.setBorder(DraggableNode.stdBorderEmphasis1);
		this.setBackground(DraggableNode.stdBackgroundColorEmphasis1);
	}
	
	
}
