package DOMViewer;

import javax.swing.JPopupMenu;

public abstract class DOMNode {
	public String title;
	
	public abstract JPopupMenu getPopupMenu();
	
	public DOMNode(String title) {
		this.title = title;
	}
	
	@Override public String toString() {
		return title;
	}
}
