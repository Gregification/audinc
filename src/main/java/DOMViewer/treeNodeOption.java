package DOMViewer;

/*
 * implementation in DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption)
 */
public enum treeNodeOption{	//keep ordering as least-to-most dependent. a higher element should never have a parent lower than it. java shouldn't let you do otherwise but...  (evaluation is based on this assumption)
	DELETE			("delete"),
	REFRESH			("refresh"),
	PARSE_d			("parse"),
		PARSE_SELF		("this", 		PARSE_d),
		PARSE_CHILDREN	("children", 	PARSE_d),
	SAVE_d			("save"),
		SAVE_AS			("save as ...",	SAVE_d),
		SAVE_OVERWRITE	("overwrite",	SAVE_d)
	;
	
	private String description;
	private treeNodeOption childOf;
	
	private treeNodeOption(String description) {
		this.description = description;
	}
	
	private treeNodeOption(String description, treeNodeOption parent) {
		this(description);
		this.childOf = parent;
	}
	
	@Override public String toString() {
		return this.description;
	}
	
	public treeNodeOption getChildOf() {
		return this.childOf;
	}
}
