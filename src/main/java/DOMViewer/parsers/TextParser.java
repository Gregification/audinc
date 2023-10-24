package DOMViewer.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

import javax.swing.JTextField;

import DOMViewer.DOMParser;
import DOMViewer.parsers.SPCParser.Variations;

public class TextParser extends DOMParser<DOMViewer.parsers.TextParser.Variations>{
	private JTextField textContent;
	
	public TextParser(File file) {
		super(file);
	}

	@Override public void ParseFile() {
		var sb = new StringBuilder();
		
		try(var br = new BufferedReader(new FileReader(srcFile))){
			
			for(String line; (line = br.readLine()) != null;) {
				sb.append(line);
			}
			
			this.textContent.setText(sb.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void SaveToFile(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initGUI() {
		textContent = new JTextField();
		this.UITabbs = Map.of("content" , textContent);
	}

	@Override
	public void parsers() {
		// TODO Auto-generated method stub
		
	}
	
	enum Variations implements parserVariation{
			PLAIN_TEXT		("plain text file")
		;
		
		private String 
			title,
			description;
		
		private Variations(String description) {
			this.title = this.name().toLowerCase().replaceAll("_", " ");
			this.description = description;
		}

		@Override public String getTitle() {
			return title;
		}

		@Override public String getDescription() {
			return description;
		}
	}
	
	public static EnumSet<? extends Enum> getVariEnum() {
		return EnumSet.allOf(Variations.class);
	}
}
