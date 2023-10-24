package DOMViewer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import DOMViewer.parsers.*;
import presentables.presents.serialPoke.SerialPokeCommConnection;

/*
 * The type of parsing available
 * 
 * see DOMParser.class for where parsers are assigned.
 * - default models are the first of their variation
 */
public enum DOModel {
	TEXT	("TEXT"	, "",		//default
				TextParser.class,
				TextParser.getVariEnum(),
				"txt"),
//	CSV		("CSV"	, "",
//				"csv"),
	
//	XML		("XML"	,  "general usecase",		//default
//				"xml"),
//	XML_m1	("XML : method 1", XML,
//				"homemade parser, maybe faster, maybe slower, and - if your lucky - maybe it works"),
	
//	JSON	("JSON"	, "",						//default
//				"json"),
	
	SerialPokeSettings	("SP COMM","Serial Poke COMM Connection Setting",
				SPCParser.class,
				SPCParser.getVariEnum(),
				SerialPokeCommConnection.FileExtension_Settings)
	;

	private String 
		name,
		description;
	private Class<? extends DOMParser> parser;
	private EnumSet<? extends Enum> variEnum;
	private Set<String> fileExtensions; //lower case if possible
	
	private DOModel(String name, String description, Class<? extends DOMParser> parser, EnumSet<? extends Enum> variEnum, String... fileExtensions) {
		this.name = name;
		this.description = description;
		this.fileExtensions = Set.of(fileExtensions);
		this.parser = parser;
		this.variEnum = variEnum;
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
	
	public Class<? extends DOMParser> getParser() {
		return parser;
	}
	
	public EnumSet<? extends Enum> getVariEnum() {
		return variEnum;
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
