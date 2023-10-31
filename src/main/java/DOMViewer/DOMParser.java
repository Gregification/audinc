package DOMViewer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//import org.apache.commons.io.FilenameUtils;	//see Maven pom.xml dependency, groupID & artifactDI:"commons-io"

import DOMViewer.parsers.*;

/*
 * file parser. is not directly involved in UI operations or creation(that is handled by the [DOMView] class)
 * NOTE: this is not required to get a {DOMView] to work (especially if its single purpose like the [DOMView] 
 * 		used by [SerialPoke]), but if your trying to make something compatible and can be used by the rest 
 * 		of the program for other purposes then please use this class.
 */
public abstract class DOMParser<Variations extends Enum<Variations> & parserVariation> {		
	public Variations variation = null;
	
	public final File srcFile;
	protected boolean isModified = false;
	protected HashMap<String, JPanel> UITabbs = new HashMap<>();
	
	public DOMParser(File file) {
		this.srcFile = file;
		if(file == null) return;
		
		init();
		initGUI();
	};
	
	//file is assumed to be a valid file for the relevant type
	public abstract void ParseFile();
	public abstract void SaveToFile(File file);
	public void init() {}
	public abstract void initGUI(); 				//populate [UITabbs] with [JPanel] tabs
	public abstract void updateGUI();
	public abstract JPanel getMetaPanel();
	
	public void setVariation(Object variation) { //the magic of generic Enums
		this.variation = (Variations)variation;	//trust me bro
	}
	
////////////////////////
// getters / setters
////////////////////////
	public boolean isModified() {
		return isModified;
	}
	public Map<String, JPanel> getUITabbs() {		
		return UITabbs;
	}
	
	public Path getPath() {
		return srcFile.toPath();
	}

////////////////////////
//parsers / writers / readers
////////////////////////
	/*
	 * things that can be written-to(with toString) and read-form a string with bufferedReader.nextLine (without any line breaks)
	 */
	public final static Map<Class<? extends Object>, Function<String, Object>> parseValue_stringers = Map.of(	
			Boolean.class	, Boolean	:: parseBoolean,
			Integer.class	, Integer	:: parseInt,
			Double.class	, Double	:: parseDouble,
			Float.class		, Float		:: parseFloat,
			String.class	, s 		-> s
		);
	
	/*
	 * things w/ custom parsers
	 */
	public final static Map<Class<? extends Object>, Function<BufferedReader, Object>> parseValue = Map.of(
			
		);
	
	/*
	 * things w/ custom writers
	 */
	public final static Map<Class<? extends Object>, Function<BufferedWriter, Object>> writeValue = Map.of(
				
		);
}
