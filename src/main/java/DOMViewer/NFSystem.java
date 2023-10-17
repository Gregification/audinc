package DOMViewer;

import java.nio.file.Path;

import javax.swing.JPopupMenu;

public class NFSystem extends DOMNode {

	public Path path;
	
	public NFSystem(String title, Path path) {
		super(title);
		this.path = path;
	}
	
	@Override public JPopupMenu getPopupMenu() {
		return null;
	}

}
