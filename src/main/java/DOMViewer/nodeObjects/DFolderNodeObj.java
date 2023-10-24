package DOMViewer.nodeObjects;

import java.nio.file.Path;

import DOMViewer.DOMParser;
import DOMViewer.DOMView;
import DOMViewer.DOModel;
import DOMViewer.Views.DOMViewFolder;

public record DFolderNodeObj(
			String title,
			Path path,
			DOMParser parser
		) {

	public DFolderNodeObj(String title, Path path, DOMParser parser) {
		this.title 	= title;
		this.path 	= path;
		this.parser = parser;
	}
	
	public DFolderNodeObj(String title, Path path) {
		this(title, path, null);
	}
	
	@Override public String toString() {
		return title;
	}
	
	public Path getPath() {
		return path;
	}
	
	public DOMParser getParser() {
		return parser;
	}
}
