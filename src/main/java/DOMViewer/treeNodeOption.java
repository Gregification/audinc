package DOMViewer;

/*
 * implementation in DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption)
 */
public enum treeNodeOption {
	DELETE			("delete"),
	PARSE			("parse"),
	PARSE_SELF		("parse: only this element"),
	PARSE_CHILDREN	("parse: only the children of this element"),
	REFRESH			("refresh")
	;
	
	private String description;
	
	private treeNodeOption(String description) {
		this.description = description;
	}
	
	@Override public String toString() {
		return this.description;
	}
}
