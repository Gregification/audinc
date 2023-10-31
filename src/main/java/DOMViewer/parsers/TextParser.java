package DOMViewer.parsers;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumSet;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import DOMViewer.DOMParser;
import presentables.Presentable;

public class TextParser extends DOMParser<DOMViewer.parsers.TextParser.Variations>{
	private JTextArea textContent;
	
	public TextParser(File file) {
		super(file);
		setVariation(Variations.PLAIN_TEXT);
	}

	@Override public void ParseFile() {		
		try(var br = new BufferedReader(new FileReader(srcFile))){
			var sb = new StringBuilder();
			
			for(String line; (line = br.readLine()) != null;) {
				sb.append(line);
				sb.append('\n');
			}
			
			this.textContent.setText(sb.toString());
		} catch (FileNotFoundException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override public void SaveToFile(File file){		
		try(FileWriter fw = new FileWriter(file, false);) {//overwrites
			for(var s : textContent.getText().split("\\n")) {
				fw.write(s);
				fw.write('\n');	//this works, but textContent dosen't actually display the whitespace when its reloaded... y :(
			}
		} catch (IOException e) { e.printStackTrace(); } //this entire function should throw some custom exception so the user can be prompted for a try but eh. todo it later... eventually
	}
	
	@Override
	public void updateGUI() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JPanel getMetaPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initGUI() {
		var content = new JPanel(new GridBagLayout());
			textContent = new JTextArea();
				textContent.setLineWrap(false);
				textContent.setBackground(new Color(150, 200,200));//color.teal. DO NOT CHANGE ALPHA else swing goes full R. 
		
		content.add(textContent, Presentable.createGbc(0, 0));		
		
		UITabbs.clear();
		UITabbs.put("content", content);
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
