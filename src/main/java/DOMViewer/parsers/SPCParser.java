package DOMViewer.parsers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import DOMViewer.DOMParser;
import audinc.gui.MainWin;
import presentables.Presentable;
import presentables.presents.serialPoke.SPCSetting;
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
		var comboBoxFunctions = new HashMap<Class<? extends Object>, Function<Object, Void>>();
		
		for(var setting : SPCSettings.AvaliableSettings) {
			int y = setting.ordinal();
			//add label
			var label = new JLabel(setting.toString());
				if(!setting.description.isBlank())
					label.setToolTipText(setting.description);
				
			settingsPanel.add(
					label,
					Presentable.createGbc(offx, y));
			
			JComponent comp = null;
			
			assert settings != null : "top setting";
			assert settings.settings != null : "sub setting";
			
			Object value = settings.settings.get(setting);
			
			//add value display
			if(!setting.isEditable()) {
				var field = new JTextField(value == null ? "undefined" : value.toString()); //10/10 scalability
					field.setEditable(false);
				comp = field;
			}else {	//my fellow Americans we, uhh, this is a great standard , nothing needs corrected
				var clas = setting.clas;
				
				if(clas == Boolean.class) {
					var c = new JCheckBox();
					boolean val = value == null ? false : (Boolean)value;
					
					c.setEnabled(val);
					c.addItemListener(e -> {
							settings.setSetting(setting, e.getStateChange() == ItemEvent.SELECTED);
						});
					
					comboBoxFunctions.put(clas, i -> {
							throw new UnsupportedOperationException("please dont have a drop down menu for booleans.. correct it in SPCSetting");
						});
					
					comp = c;
				} else if(clas == Integer.class) {
					JSpinner spinner = new JSpinner(
							new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
						spinner.setValue(value == null ? 0 : value);
					  	spinner.addChangeListener(e -> {
					  			settings.setSetting(setting, (int)spinner.getModel().getValue());
					  		});
					  	
					comboBoxFunctions.put(clas, i -> {
							var s = i.toString();
							if(!s.isBlank())
								try { spinner.setValue(DOMParser.parseValue_stringers.get(clas).apply(s));
								}catch(NumberFormatException e) {}
							
							return null;
						});
					  	
					comp = spinner;
				} else if(clas == Double.class || clas == Float.class) {
					JSpinner spinner = new JSpinner(
								new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, .1)); //no point in this being big
							spinner.setValue(value == null ? 0 : value);
							spinner.addChangeListener(e -> {
						  			settings.setSetting(setting, (Double)spinner.getModel().getValue());
						  		});
							
					comboBoxFunctions.put(clas, i -> {
							var s = i.toString();
							if(!s.isBlank())
								try { spinner.setValue(DOMParser.parseValue_stringers.get(clas).apply(s));
								}catch(NumberFormatException e) {}
								
								return null;
						});
							
					comp = spinner;
				} else if(clas == String.class) {
					var field = new JTextField();
						field.setText(value == null ? "undefined" : value.toString());
					
					Class<? extends Object> ref;
					if((ref = setting.choosableValues[0].getClass()).isEnum()) {
						field.setEditable(false);
						
						comboBoxFunctions.put(clas, i -> {
							try {
								Enum selectedEnum;
								if(SPCSetting.ParityOptions == ref) {
									selectedEnum = SPCSetting.ParityOptions.cast(i);
								}else if(SPCSetting.StopBitOptions == ref) {
									selectedEnum = SPCSetting.StopBitOptions.cast(i);
								} else {
									throw new UnsupportedOperationException("enum of class | " + ref + " | not implimented in SPCParser.clases are listed form SPCSetting.choosableValues"); 
								}

								field.setText(selectedEnum.toString());
								settings.setSetting(setting, selectedEnum);
							}catch(ClassCastException cce) { }//do nothing
							
							return null;
						});
						
					}else {
						field.addActionListener(e ->{
								settings.setSetting(setting, field.getText());
							});
						
						comboBoxFunctions.put(clas, i -> {
								var s = i.toString();
								if(!s.isBlank())	field.setText(s);
								
								return null;
							});
					}
					
					comp = field;
				} else {
					throw new RuntimeException("non-implimented class in SPCSetting:"  + clas);
				}
				
				if(setting.choosableValues != null && setting.choosableValues.length > 1) {
					assert comboBoxFunctions.containsKey(clas) 
						: "cannot create a combobox without a function. you forgot to make a function for objects of class: " + clas;
					
					JButton comboBtn = getComboButton(setting, comboBoxFunctions.get(clas));
					
					/*
					 * switch-er-roo, to make [comp] contain [comboBtn] and the original value of itself([comp])
					 * TODO: make [comboBtn]'s size constant regardless of how the other component get resized 
					 */
					var newComp = new JPanel(new GridBagLayout());
					float scale = 1f;
					comboBtn.setPreferredSize(new Dimension((int)(MainWin.stdtabIconSize.width * scale), (int)(MainWin.stdtabIconSize.height * scale)));
					newComp.add(comboBtn, Presentable.createGbc(0, 0));
					newComp.add(comp, Presentable.createGbc(1, 0));
					comp = newComp;
				}
				
			}
			
			
			//disable if not hot swappable
			if(setting.isHotSwappable) {
				var highlight = new Color(107,62,3);
				label.setForeground(highlight);
			}
			
			settingsPanel.add(
					comp,
					Presentable.createGbc(offx+1, y));
		}
		
		UITabbs.clear();
		UITabbs.put("Fazecast/jSerialComm", settingsPanel);
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
	
	private JButton getComboButton(SPCSetting setting, Function<Object, Void> dollarStoreActionListener) {
		var btn = new JButton("...");
			btn.addActionListener(new ActionListener() {
				JPopupMenu menu = null;
				
				@Override public void actionPerformed(ActionEvent e) {
					if(menu == null) {
						menu = new JPopupMenu();
						
						for(var v : setting.choosableValues) {
							var mi = new JMenuItem(v.toString());
								mi.addActionListener(ev -> {
										dollarStoreActionListener.apply(v);
									});
								
							menu.add(mi);
						}
					}
					
					menu.show(btn, 0, 0);
				}
				
			});
		return btn;
	}
}
