package DOMViewer;

import java.io.File;

import org.w3c.dom.NodeList;

public abstract class DOMParser {
	public abstract NodeList parse(File source);
}
