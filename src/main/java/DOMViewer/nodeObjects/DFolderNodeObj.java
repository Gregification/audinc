package DOMViewer.nodeObjects;

import java.nio.file.Path;

public record DFolderNodeObj(
			String title,
			Path path
		) {

	@Override public String toString() {
		return title;
	}
	
	public Path getPath() {
		return path;
	}
}
