package DOMViewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.io.FilenameUtils;	//see Maven pom.xml dependency, groupID & artifactDI:"commons-io"

/*
 * file parser. is not directly involved in UI operations or creation(that is handled by the [DOMView] class)
 * NOTE: this is not required to get a {DOMView] to work (especially if its single purpose like the [DOMView] 
 * 		used by [SerialPoke]), but if your trying to make something compatible and can be used by the rest 
 * 		of the program for other purposes then please use this class.
 */
public abstract class DOMParser {
//	public abstract NodeList parse(File source); //eh
	
	public DOModel model;
	
	public DOMParser() {
		this.model 	= DOModel.defaultUniversalModel;
	};
	public DOMParser(DOModel model) {
		this.model 	= model;
	};
	
	//file is assumed to be a valid file for the relevant type
	public abstract void ParseFile(File file);
	public abstract void ParseFile(BufferedReader br);

	public abstract JPopupMenu getPopupMenu();
	
	public boolean canParse(String ext) {
		for(var v : model.extensions())
			if(v.equals(ext))
				return true;
		
		return false;
	}
	public boolean canParse(File file) {
		return canParse(FilenameUtils.getExtension(file.getPath()));
	}

	
////////////////////////
// private / protected
////////////////////////
}
