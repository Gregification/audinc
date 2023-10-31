package DOMViewer.parsers;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.EnumSet;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import DOMViewer.DOMParser;
import presentables.Presentable;
import presentables.presents.serialPoke.SPCSettings;

/*
 * serial port communication connection parser (.spccS extension)
 */
public class SPCParser extends DOMParser<DOMViewer.parsers.SPCParser.Variations>{
	public SPCSettings settings;
	private JPanel settingsPanel;
	
	public SPCParser(File file) {
		super(file);
	}

	@Override public void ParseFile() {
		try(var br = new BufferedReader(new FileReader(srcFile))){
			settings.rebaseFrom(br);
		} catch (IOException e) { e.printStackTrace(); }
	}

	@Override
	public void SaveToFile(File file) {
		// TODO Auto-generated method stub
		
	}
	
	@Override public void init() {
		settings = new SPCSettings();
	}

	@Override public void initGUI() {
		settingsPanel = new JPanel(new GridBagLayout());
		
		int offx = 0;
		for(var setting : SPCSettings.AvaliableSettings) {
			int y = setting.ordinal();
			
			//add label
			settingsPanel.add(
					new JLabel(setting.toString()),
					Presentable.createGbc(offx, y));
			
			JComponent comp = null;
			
			assert settings != null : "top setting";
			assert settings.settings != null : "sub setting";
			
			Object value = settings.settings.get(setting);
			
			//add value display
			if(!setting.isEditable()) {
				comp = new JLabel(value == null ? "undecalred" : value.toString()); //10/10 scalability
			}else {
				var clas = setting.clas;
				
				//my fellow Americans we, uhh, this is a great standard , nothing needs corrected
				if(clas == Boolean.class) {
					var c = new JCheckBox("enabled");
					boolean val = value == null ? false : (Boolean)value;
					
					c.setEnabled(val);
					c.addItemListener(e -> {
							settings.setSetting(setting, e.getStateChange() == ItemEvent.SELECTED);
						});
					comp = c;
				} else if(clas == Integer.class) {
					JSpinner spinner = new JSpinner(
							new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
						spinner.setValue(value == null ? 0 : value);
					  	spinner.addChangeListener(e -> {
					  			settings.setSetting(setting, (int)spinner.getModel().getValue());
					  		});
					comp = spinner;
				} else if(clas == Double.class || clas == Float.class) {
					JSpinner spinner = new JSpinner(
								new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, .1)); //no point in this being big
							spinner.setValue(value == null ? 0 : value);
							spinner.addChangeListener(e -> {
						  			settings.setSetting(setting, (Double)spinner.getModel().getValue());
						  		});
					comp = spinner;
				} else if(clas == String.class) {
					var field = new JTextField(10);
						field.setText(value == null ? "undefined" : value.toString());
						field.addActionListener(e ->{
								settings.setSetting(setting, field.getText());
							});
					comp = field;
				} else {
					throw new RuntimeException("non-implimented class in SPCSetting:"  + clas);
				}
			}
			
			
			//disable if not hot swappable
			comp.setEnabled(!setting.isHotSwappable);
			
			settingsPanel.add(
					comp,
					Presentable.createGbc(offx+1, y));
		}
		
		UITabbs.clear();
		UITabbs.put("Fazecast/jSerialComm", settingsPanel);
		System.out.println("womp womp, init gui");
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
