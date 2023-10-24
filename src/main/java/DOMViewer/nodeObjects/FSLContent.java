package DOMViewer.nodeObjects;

import javax.swing.JComponent;

public abstract class FSLContent<T> {
	protected JComponent content;
	protected T value;
	protected Boolean isEditable = false;
	
	public FSLContent(T value) {
		this.value = value;
		this.initContent();
		this.setIsEditable(isEditable);
	}
	public JComponent getContent() {
		if(content == null)
			initContent();
		return content;
	}
	public T getValue() {
		return value;
	}
	
	public abstract void initContent();
	
	public Boolean getIsEditable() {
		return isEditable;
	}
	public abstract void setIsEditable(Boolean isEditable);
}
