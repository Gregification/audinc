package temp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
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
		if(src == null || src.isBlank() || src.isEmpty()) return null;
		
		int idx = 0;
		XMLElement container = new XMLElement();
		
		
		
		System.out.println("src\n"+src);
		
		while(true) {
			XMLElement pointer = getElement(src.substring(idx));
			
			if(pointer != null)
				container.children.add(pointer);
			else break;
		}
		
		return container;
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
	
	//finds the end of the starting element ex:[<book id="10">], the start index gives the first '<' and returns the index of '>'
	public static int getEndOfEle(int start, String src) {
		Stack<Character> q = new Stack<Character>();
		HashMap<Character, Character> delims = new HashMap<>();
		int 
			idx = 0;
		
		//xml should only contain these two
		delims.put('<', '>');
		delims.put('"', '"');
		
		var arr_keys = delims.keySet();
		
		while(idx != src.length()) {
			char charAt = src.charAt(idx);
			
			//if charAt is a ending deliminator
			if(q.size() > 0 && charAt == q.peek()) {
				
			}
			
			//if it is a starting deliminator. this should alwayse trigger on the first loop
			else if(arr_keys.contains(charAt)) { 
				q.push(delims.get(charAt));
			}
			
			idx++;
			
			if(q.size() == 0) break; //successful! :D
		}		
		
		if(q.size() != 0) {
			//a element was misssing a closing statement
			System.out.println("index:"+idx + "\nSOMETHIGN BROKE :(");
		}
		
		return idx;
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
