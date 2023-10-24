package DOMViewer.nodeObjects;

import java.nio.file.Path;

import DOMViewer.DOMParser;
import DOMViewer.DOMView;
import DOMViewer.DOModel;
import DOMViewer.Views.DOMViewFolder;

public record DFolderNodeObj(
			String title,
			Path path,
			DOMView domView
		) {

	public DFolderNodeObj(String title, Path path, DOMView domView) {
		this.title 	= title;
		this.path 	= path;
		this.domView = domView;
	}
	
	public DFolderNodeObj(String title, Path path) {
		this(title, path, new DOMViewFolder(path));
	}
	
	@Override public String toString() {
		return title;
	}
	
	public Path getPath() {
		return path;
	}
	
	public DOMView getDOMView() {
		return domView;
	}
}
