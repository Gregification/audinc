package DOMViewer;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import presentables.presents.serialPoke.SerialPokeCommConnection;

/*
 * The type of parsing available
 * 
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
	
	JSON	("JSON"	, "",						//default
				"json"),
	
	SerialPokeSettings	("SP COMM","Serial Poke COMM Connection Setting",
			SerialPokeCommConnection.FileExtension_Settings)
	;
	
	private String 
		name,
		description;
	private Set<String> fileExtensions; //lower case if possible
	
	private DOModel(String name, String description, String... fileExtensions) {
		this.name = name;
		this.description = description;
		this.fileExtensions = Set.of(fileExtensions);
	}
	
	private DOModel(String name, DOModel parent, String description) {
		this.name = name;
		this.description = description;
		this.fileExtensions = parent.extensions();
	}
	
	public Set<String> extensions() {
		return this.fileExtensions;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	@Override public String toString() {
		return this.name;
	}
	
	public static boolean isModelApplicableTo(DOModel model, File file) { return isModelApplicableTo(model, FilenameUtils.getExtension(file.toString()));	}	
	public static boolean isModelApplicableTo(DOModel model, String ext) {
		return model.extensions().contains(ext);
	}
	
	public static EnumSet<DOModel> getApplicableModels(File file){ return getApplicableModels(FilenameUtils.getExtension(file.toString()));}
	public static EnumSet<DOModel> getApplicableModels(String ext){
		if(DOModel.applicableModels.containsKey(ext))
			return DOModel.applicableModels.get(ext);
		
		//find matching set
		var ret = EnumSet.allOf(DOModel.class).stream()
				.filter(mod -> isModelApplicableTo(mod, ext))
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(DOModel.class)));
		
		ret.add(DOModel.defaultUniversalModel);
		//memo
		DOModel.applicableModels.put(ext, ret);
		
		return ret;
	}
	
	public static final DOModel defaultUniversalModel = TEXT;
	private static Map<String, EnumSet<DOModel>> applicableModels = new ConcurrentHashMap<>();	//memoized
}
