package DOMViewer;

public enum DOModel {
	XML		("XML"	, "xml"),
	JSON	("JSON"	, "json"),
	CSV		("CSV"	, "csv", "txt")
	;
	
	private String name;
	private String[] fileExtensions; //lowercase is possible
	
	private DOModel(String name, String... fileExtensions) {
		this.name = name;
		this.fileExtensions = fileExtensions;
	}
	
	public String[] extensions() {
		return this.fileExtensions;
	}
	
	@Override public String toString() {
		return this.name;
	}
}
