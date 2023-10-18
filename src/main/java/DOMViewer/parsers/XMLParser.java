package DOMViewer.parsers;

import java.io.File;
import java.io.InputStream;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import DOMViewer.DOMParser;
import DOMViewer.DOModel;

public class XMLParser extends DOMParser {
	
	public XMLParser(DefaultMutableTreeNode root) {
		this(DOModel.TEXT, root);//default model
	}
	
	public XMLParser(DOModel model, DefaultMutableTreeNode root) {
		super(model, root);
		// TODO Auto-generated constructor stub
	}

	@Override
	public JPopupMenu getPopupMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void ParseFile(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ParseFile(InputStream is) {
		// TODO Auto-generated method stub
		
	}
	
}
