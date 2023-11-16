package presentables.presents.draggableNodeEditor;

public abstract class NodeComponent<T extends Object>{
	//node stuff
	public volatile T value = null;
	
	//meta-ish stuff
	public volatile boolean 
		needsRedrawn	= true,		
		needsNewValue 	= false;	//requires a value
	public NodeComponentImportance importance;//purely UI related only
	
	public abstract void setValue();
	public abstract void getValue();
	public abstract boolean hasConnection();
}