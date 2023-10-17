package DOMViewer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import presentables.Presentable;

public class XMLElement {
	public ArrayList<XMLElement> children 		= new ArrayList<>();
	
	public HashMap<String, String> attributes 	= new HashMap<>();
	
	public String
		elementType = null,
		data 		= null;
	
	
	public XMLElement() {}
	public XMLElement(String elementType) {
		this.elementType = elementType;
	}
	public XMLElement(String elementType, String data) {
		this(elementType);
		this.data = data;
	}
	public XMLElement(XMLElement[] children) {
		for(var v : children) 
			this.children.add(v);
	}
	
	public static XMLElement getElement(String src) {
		if(src == null || src.isBlank() || src.isEmpty() || !src.startsWith("<")) return null;
		
		XMLElement container = new XMLElement();
		
		ArrayList<String> sets = new ArrayList<>();
		for(int idx = 0, end = 0;idx < src.length();) {			
			String set = src.substring(idx, (end = getStrEle(idx, src, new char[]{' '})));
			sets.add(set);
			idx = end;
		}
			
		for(var v : sets) {
			System.out.println("\tset:"+v);
		}
		
		return container;
	}
	
	//can throw exception if given invalid xml
	private static int getStrEle(int i, String src, char[] whitespace) throws IndexOutOfBoundsException{
		var whiteSpace = Arrays.asList(whitespace);
		HashMap<Character, Character> delims = new HashMap<>();
			delims.put('<','>');
			delims.put('"', '"');
			var arr_keys = delims.keySet();
			
		char
			deil = 0, //set id
			charAt;
		
		int orgIdx = i; //the starting index. used to tell when to read "</" as a end or start of a set
		
		do {
			charAt = src.charAt(i);
			
			if(deil == 0) {	//if not in a set
				if(whiteSpace.contains(charAt)) { //if it hits white space => break
					return ++i;
				}
				
				if(arr_keys.contains(charAt)) { //if it is the start of a new set
					deil = charAt;
					
					//if it is a ending element of a alligator ["</"]
					if(deil == '<' && src.charAt(i+1) == '/' && orgIdx+1 < i) return i; //possibly exception source 
				}
			}else{ // it is in a set
				if(delims.get(deil) == charAt) { //if is it the end of the set
					//end of set means end of data
					return ++i;
				}
			}
			
			i++;
		}while(true);
	}
	
	public static XMLElement getElement(Path path) {
		if(!path.toFile().isFile() || !path.getFileName().endsWith(".xml"))
			return null;
		
		StringBuilder src = new StringBuilder();
		Presentable.readFromPath(path, br -> { 
				String line;
				try { 
					while((line = br.readLine()) != null) 
						 src.append(line);
				} 
				catch (IOException e) {
					e.printStackTrace(); 
				}
			});
		return getElement(src.toString());
	}		
	
	//this is just printing
	public String prettyPrint(int idx) {
		StringBuilder sb = new StringBuilder();
		String s = getSpacing(idx+1);
		
		//element type
		sb.append(getSpacing(idx)+"ele:\t" + this.elementType+ "\n");
		
		//attributes
		for(var v : this.attributes.keySet())
			sb.append(s+v+"\t:\t"+this.attributes.get(v)+"\n");
		
		//data
		sb.append(s+"data:\t"+this.data+"\n");
		
		//children
		for(var v : this.children)
			sb.append(s+v.prettyPrint(idx+1));
		
		//end
		sb.append(getSpacing(idx)+"end ele:\t"+this.elementType+"\n");
		
		return sb.toString();
	}
	private String getSpacing(int idx) {
		String s = "";
		while(idx > 0) {
			s += "\t";
			idx--;
		}
		return s;
	}
}
