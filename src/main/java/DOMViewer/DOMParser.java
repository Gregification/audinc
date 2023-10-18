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
	public DOModel model;
	
	public static Class<? extends DOMParser> findParser(File file) {
		return parsers.get(getModel(file));
	}
	
	public static DOMParser Parse(File file, DefaultMutableTreeNode treenode) {
		return Parse(file, getModel(file), treenode);
	}
	public static DOMParser Parse(File file, DOModel model, DefaultMutableTreeNode treenode) {
		try {
			var v = parsers.get(model).getDeclaredConstructor(DOModel.class, DefaultMutableTreeNode.class).newInstance(model, treenode);
			v.ParseFile(file);
			return v;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public static DOMParser Parse(InputStream is, DOModel model) {
		try {
			var v = parsers.get(model).getDeclaredConstructor().newInstance();
			v.ParseFile(is);
			return v;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static DOModel getModel(File file) {
		DOModel[] models = getModels(FilenameUtils.getExtension(file.getPath()));
		return models.length != 0 ? models[0] : DOModel.TEXT;
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
	
	public DOMParser(DefaultMutableTreeNode root) {
		this.model 	= DOModel.TEXT;
		this.root 	= root;
	};
	public DOMParser(DOModel model, DefaultMutableTreeNode root) {
		this.model 	= model;
		this.root 	= root;
	};
	
	//file is assumed to be a valid file for the relevant type
	public abstract void ParseFile(File file);
	public abstract void ParseFile(InputStream is);

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
	protected static Map<DOModel, Class<? extends DOMParser>> parsers = Map.of(
			DOModel.TEXT, 	DOMViewer.parsers.TextParser.class,	//full class path for tractability reasons
			DOModel.XML,	DOMViewer.parsers.XMLParser.class
		);

	private static HashMap<String, DOModel[]> modelMap = new HashMap<>(); //memoized
}
