package DOMViewer.parsers;

import java.io.File;
import java.util.EnumSet;

import javax.swing.JPanel;

import DOMViewer.DOMParser;
import DOMViewer.parsers.TextParser.Variations;

public class SPCParser extends DOMParser<DOMViewer.parsers.SPCParser.Variations>{

	public SPCParser(File file) {
		super(file);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void ParseFile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SaveToFile(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initGUI() {
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

	enum Variations implements parserVariation{
			V1		("Serial Port Comm Parser, format 1")
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
