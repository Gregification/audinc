package draggableNodeEditor;

import java.awt.Color;

//i wanted to do something more with this but eh. if your reading this and feel like it then consider ...
public enum NodeComponentStatus {
		SUGESTED		(Color.green),
		AVAILABLE		(Color.yellow),
		NOT_AVAILABLE	(Color.red),
		NETURAL			(Color.cyan),
		WARNING			(Color.orange)
	;
	
	public Color color;
	public String description;
	
	private NodeComponentStatus(Color color) {
		this.color = color;
		this.description = this.name().toLowerCase().replace('_', ' ');
	}
	private NodeComponentStatus(Color color, String description) {
		this.color = color;
		this.description = description;
	}
}
