package DOMViewer;

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

public abstract class DOMParser {
//	public abstract NodeList parse(File source); //eh
	
	public DefaultMutableTreeNode root;
	
	public static DOMParser Parse(File file, DefaultMutableTreeNode treenode) {
		var models = getModels(file);
		DOModel model = models.length != 0 ? models[0] : DOModel.TEXT;
		
		return Parse(file, model, treenode);
	}
	public static DOMParser Parse(File file, DOModel model, DefaultMutableTreeNode treenode) {
		try {
			var v = parsers.get(model).getDeclaredConstructor().newInstance();
			v.ParseFile(file, treenode);
			return v;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public static DOMParser Parse(InputStream is, DOModel model, DefaultMutableTreeNode treenode) {
		try {
			var v = parsers.get(model).getDeclaredConstructor().newInstance();
			v.ParseFile(is, treenode);
			return v;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static DOModel[] getModels(File file) {
		return getModels(FilenameUtils.getExtension(file.getPath())); 
	}
	public static DOModel[] getModels(String ext) { 
		if(modelMap.containsKey(ext))
			return modelMap.get(ext);
			
		ArrayList<DOModel> models = new ArrayList<>();
		for(DOModel m : DOModel.values())
			if(Arrays.stream(m.extensions()).anyMatch(e -> e.equals(ext)))
				models.add(m);
		
		var arr = models.toArray(new DOModel[] {});
		
		return arr;	
	}
	
	//file is assumed to be a valid file for the relevant type
	public abstract void ParseFile(File file, DefaultMutableTreeNode root);
	public abstract void ParseFile(InputStream is, DefaultMutableTreeNode root);

	public abstract JPopupMenu getPopupMenu();
	
	public boolean canParse(String ext) {
		//basically reverse map, ends up as model -> extension
		return Arrays.stream(getModels(ext))
				.anyMatch(e -> {
					for(var v : e.extensions())
						if(v.equals(ext))
							return true;
					return false;
				});
	}
	public boolean canParse(File file) {
		return canParse(FilenameUtils.getExtension(file.getPath()));
	}

	
////////////////////////
// private / protected
////////////////////////	
	protected static Map<DOModel, Class<? extends DOMParser>> parsers = Map.of(
			DOModel.TEXT, 	DOMVParsers.TextParser.class,	//full class path for tractability reasons
			DOModel.XML,	DOMVParsers.XMLParser.class
		);

	private static HashMap<String, DOModel[]> modelMap = new HashMap<>(); //memoized
}
