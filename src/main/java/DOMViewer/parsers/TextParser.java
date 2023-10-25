package DOMViewer.parsers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import DOMViewer.DOMParser;

public class TextParser extends DOMParser<DOMViewer.parsers.TextParser.Variations>{
	private JTextArea textContent;
	
	public TextParser(File file) {
		super(file);
		setVariation(Variations.PLAIN_TEXT);
	}

	@Override public void ParseFile() {
		System.out.println("text parser > parsefile");
		
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
		//System.out.println("DOMParser>TextParser>ParseFile, parsed :" + textContent.getText());
	}

	@Override
	public void SaveToFile(File file) {
		// TODO Auto-generated method stub
		
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
		
		var c = new GridBagConstraints();
		c.weightx = c.weighty = 1.0;	
		c.fill = GridBagConstraints.BOTH;
		
		textContent = new JTextArea();
			textContent.setLineWrap(false);
		
		textContent.setBackground(Color.LIGHT_GRAY);
		
		content.add(textContent, c);
		
		//System.out.println("TextParser>initGUI, content:" + content);
		
		this.UITabbs = new HashMap<String, JPanel>(Map.of("content" , content));
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
