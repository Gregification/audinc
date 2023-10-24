package DOMViewer.parsers;

import java.io.BufferedReader;
import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import DOMViewer.DOMParser;

public class FolderParser  extends DOMParser{
	//this only thing this cares about is the name
	
	@Override public void ParseFile(File file, DefaultMutableTreeNode node) {
		assert file.isDirectory() : "expected directory, given file";
		
	}

	@Override public void ParseFile(BufferedReader br, DefaultMutableTreeNode node) {
		throw new RuntimeException("bruh");
	}

	@Override public void SaveToFile(File output, DefaultMutableTreeNode node) {
		assert output.isDirectory() : "expected directory, given file";
		
	}

}
