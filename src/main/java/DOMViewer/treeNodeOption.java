package DOMViewer;

/*
 * implementation in DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption)
 */
public enum treeNodeOption {
	DELETE			("delete"),
	PARSE			("parse"),
	PARSE_SELF		("parse: this"),
	PARSE_CHILDREN	("parse: children"),
	REFRESH			("refresh"),
	SAVE			("save to file")
	;
	
	private String description;
	
	private treeNodeOption(String description) {
		this.description = description;
	}
	
	@Override public String toString() {
		return this.description;
	}
}
