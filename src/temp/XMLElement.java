package temp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;

import presentables.Presentable;

public class XMLElement {
	public ArrayList<XMLElement> children 		= new ArrayList<>();
	
	public HashMap<String, String> attributes 	= new HashMap<>();
	
	public String
		elementType = null,
		data 		= null;
	
	public XMLElement() {
		
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
	
	private static int getStrEle(int i, String src, char[] whitespace) {
		HashMap<Character, Character> delims = new HashMap<>();
			delims.put('<','>');
			delims.put('"', '"');
			var arr_keys = delims.keySet();
		
		char
			deil = 0, //there can only be one 1 at a time
			charAt;
		
		int orgIdx = i;
		
		do {
			charAt = src.charAt(i);
//			System.out.println("\t\""+charAt+"\"");
			
			if(deil == 0) {	//if not in a set
				if(Arrays.asList(whitespace).contains(charAt)) { //if it hits white space => break
//					System.out.println("\t[found white space]");
					return ++i;
				}
				
				if(arr_keys.contains(charAt)) {
//					System.out.println("\t[DATASET FOUND]");
					deil = charAt;
					
					if(deil == '<' && src.charAt(i+1) == '/' && orgIdx+1 < i) return i;
				}
			}else{
				if(delims.get(deil) == charAt) { //it is in a set -> is it the end of the set?
					//end of set means end of data
//					System.out.println("\t[end of dataset]");
					return ++i;
				}
			}
			
			i++;
		}while(i < src.length());
		
		return i;//something went wrong
	}
	
	public static XMLElement getElement(Path path) {
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
