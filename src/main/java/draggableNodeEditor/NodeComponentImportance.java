package draggableNodeEditor;

import java.awt.Color;

public enum NodeComponentImportance {
		OPTIONAL	(Color.black),
		REQUIRED	(Color.red),
		SUGGESTED	(Color.yellow),
		SUPPLIER	(Color.black)
	;
	
	public Color color;
	public String description;
	
	private NodeComponentImportance(Color color) {
		this.color = color;
		this.description = this.name().toLowerCase().replace('_', ' ');
	}
	private NodeComponentImportance(Color color, String description) {
		this.color = color;
		this.description = description;
	}
}
