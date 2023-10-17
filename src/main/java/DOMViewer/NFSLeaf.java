package DOMViewer;

import java.nio.file.Path;

import javax.swing.JPopupMenu;

public class NFSLeaf extends NFSystem {
	public DOMParser parser;
	
	public NFSLeaf(String title, Path path) {
		super(title, path);
	}
	public NFSLeaf(NFSystem srcSystem) {
		super(srcSystem.title, srcSystem.path);
	}
	
	@Override public JPopupMenu getPopupMenu() {
		return super.getPopupMenu();
	}

}
