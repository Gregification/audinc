package DOMViewer;

/*
 * implementation in DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption)
 */
public enum treeNodeOption{	//keep ordering as least-to-most dependent. a higher element should never have a parent lower than it. java shouldn't let you do otherwise but...  (evaluation is based on this assumption)
	CLEAR			("CLEAR",
						"remove view of this node, does effect the file system"),
	DELETE			("DELETE",
						"removes from file system (cannotbe undone)"),
	REFRESH			("refresh",
						"ignore local changes, rebase from the file system"),
	PARSE_d			("parse"),
		PARSE_SELF		("this", 
							"parse this node (includes children)",
							PARSE_d),
		PARSE_CHILDREN	("children",
							"parse children only",
							PARSE_d),
		PARSE_CUSTOM	("custom",
							"view parsing options",
							PARSE_d),
	SAVE_d			("save"),
		SAVE_OVERWRITE	("overwrite",	
							"overwrite the selected file/folder(s)",
							SAVE_d),
		SAVE_AS			("save as ...",
							SAVE_d)
	;
	
	private String 
		description,
		tooltiptext;
	private treeNodeOption childOf;
	
	private treeNodeOption(String description) {
		this(description, "");
	}	
	private treeNodeOption(String description, String tooltiptext) {
		this(description, tooltiptext, null);
	}	
	private treeNodeOption(String description, treeNodeOption parent) {
		this(description, "", parent);
	}	
	private treeNodeOption(String description, String tooltiptext, treeNodeOption parent) {
		this.description = description;
		this.tooltiptext = tooltiptext;
		this.childOf = parent;
	}
	
	@Override public String toString() {
		return this.description;
	}
	
	public treeNodeOption getChildOf() {
		return this.childOf;
	}

	public String getTooltipText() {
		return this.tooltiptext;
	}
}
