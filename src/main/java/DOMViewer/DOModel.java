package DOMViewer;

/*
 * see DOMParser.class for where parsers are assigned.
 * - default models are the first of their variation
 */
public enum DOModel {	
	TEXT	("TEXT"	, "",		//default 
				"txt"),
	CSV		("CSV"	, "",
				"csv"),
	
	XML		("XML"	,  "general usecase",		//default
				"xml"),
	XML_m1	("XML : method 1", XML,
				"homemade parser, maybe faster, maybe slower, and - if your lucky - maybe it works"),
	
	JSON	("JSON"	, "",		//default
				"json")
	;
	
	private String 
		name,
		description;
	private String[] fileExtensions; //lower case if possible
	
	private DOModel(String name, String description, String... fileExtensions) {
		this.name = name;
		this.description = description;
		this.fileExtensions = fileExtensions;
	}
	
	private DOModel(String name, DOModel parent, String description) {
		this.name = name;
		this.description = description;
		this.fileExtensions = parent.extensions();
	}
	
	public String[] extensions() {
		return this.fileExtensions;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	@Override public String toString() {
		return this.name;
	}
}
