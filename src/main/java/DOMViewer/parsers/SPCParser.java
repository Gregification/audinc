package DOMViewer.parsers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
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
import javax.swing.SpinnerNumberModel;

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
	private HashMap<SPCSetting, Function<Object, Void>> functionsToUpdateUI;
	
	public SPCParser(File file) {
		super(file);
	}

	@Override public void ParseFile() {
		try(var br = new BufferedReader(new FileReader(srcFile))){
			settings.rebaseFrom(br);
		} catch (IOException e) { }
	}

	@Override
	public void SaveToFile(File file) {
		try(BufferedWriter br = new BufferedWriter(new FileWriter(file))){
			settings.writeTo(br);
		} catch(IOException e) { }	//not out problem anymore
	}
	
	@Override public void init() {
		settings = new SPCSettings();
	}

	@Override public void initGUI() {
		settingsPanel = new JPanel(new GridBagLayout());
		
		int offx = 0;
		functionsToUpdateUI 	= new HashMap<>();
		var comboBoxFunctions 	= new HashMap<Class<? extends Object>, Function<Object, Void>>(); //handles what happens when a ui element is selected. actually changes setting value
		
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
			
			//add value display
			if(!setting.isEditable()) {
				var field = new JTextField(); //10/10 scalability
					field.setEditable(false);
					
				functionsToUpdateUI.put(setting, o ->{
						if(o == null)
							throw new IllegalArgumentException("invalid value, is null?" + (o==null));
						
						field.setText(o.toString());
						return null;
					});
					
				comp = field;
			} else {	//my fellow Americans we, uhh, this is a great standard , nothing needs corrected
				var clas = setting.clas;
				
				if(clas == Boolean.class) {
					var c = new JCheckBox();
					c.addItemListener(e -> {
							settings.setSetting(setting, e.getStateChange() == ItemEvent.SELECTED);
						});
					
					//no combo box for booleans 
					
					functionsToUpdateUI.put(setting, o ->{
							if(o == null || !(o instanceof Boolean))
								throw new IllegalArgumentException("invalid value, is null?" + (o==null));
							
							c.setSelected((boolean)o);
							return null;
						});
					
					comp = c;
				} else if(clas == Integer.class) {
					JSpinner spinner = new JSpinner(
							new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
					  	spinner.addChangeListener(e -> {
					  			settings.setSetting(setting, (int)spinner.getModel().getValue());
					  		});
					  	
					comboBoxFunctions.putIfAbsent(clas, i -> {
							var s = i.toString();
							if(!s.isBlank())
								try { 
									Object value = DOMParser.parseValue_stringers.get(clas).apply(s);
									if(value != null)
										spinner.setValue(value);
								}catch(NumberFormatException e) {}
							
							return null;
						});
					
					functionsToUpdateUI.put(setting, o ->{
							if(o == null || !(o instanceof Integer))
								throw new IllegalArgumentException("invalid value, is null?" + (o==null));
							
							spinner.getModel().setValue(o);
							return null;
						});
					  	
					comp = spinner;
				} else if(clas == Double.class || clas == Float.class) {
					JSpinner spinner = new JSpinner(
								new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, .1)); //no point in this being big
							spinner.addChangeListener(e -> {
						  			settings.setSetting(setting, (Double)spinner.getModel().getValue());
						  		});
							
					comboBoxFunctions.putIfAbsent(clas, i -> {
							var s = i.toString();
							if(!s.isBlank())
								try { 
									spinner.setValue(DOMParser.parseValue_stringers.get(clas).apply(s));
								}catch(NumberFormatException e) {} //do nothing
								return null;
						});
					
					functionsToUpdateUI.put(setting, o ->{
							if(!(o instanceof Double || o instanceof Float))
								throw new IllegalArgumentException("invalid value, is null?" + (o==null));
							
							spinner.getModel().setValue(o);
							return null;
						});
							
					comp = spinner;
				} else if(clas == String.class) {
					var field = new JTextField();
					
					functionsToUpdateUI.put(setting, o ->{
							field.setText(o.toString());
							return null;
						});
						
					
					field.addActionListener(e ->{
							settings.setSetting(setting, field.getText());
						});
					
					comboBoxFunctions.putIfAbsent(clas, o -> {
							if(o == null) throw new IllegalArgumentException("invalid value, is null?" + (o==null));
						
							field.setText(o.toString());
							return null;
						});
					
					comp = field;
				}else if(clas.isEnum()){//this works. i keep . there's only a finite amount of these so it dosen't matter much
					var field = new JTextField();
						field.setEditable(false);
					functionsToUpdateUI.put(setting, o ->{ field.setText(o.toString()); return null; });
					
					//need it to be parsed as its specific class because these are passed around abstractly and checked using "instanceof"
					final Class<?> classToCastTo = List.of(	//gets the first matching class else null
									SPCSetting.parityOptions.class,
									SPCSetting.stopbitOptions.class,
									SPCSetting.timeoutOptions.class,
									SPCSetting.protocallOptions.class
								).stream()
							.filter(c -> c == clas)
							.findFirst().get();
					
					assert classToCastTo != null : "enum not listed!"; //list the new enum above
					
					comboBoxFunctions.putIfAbsent(clas, new Function<Object, Void>(){
						private final Class<?> caster = classToCastTo;
						@Override public Void apply(Object i) {
							try {
								var selectedEnum = this.caster.cast(i);	
		
								functionsToUpdateUI.get(setting).apply(selectedEnum);
								settings.setSetting(setting, selectedEnum);
							}catch(ClassCastException cce) { }//do nothing
							
							return null;
						}
						
					});
					
					comp = field;
				} else {
					throw new UnsupportedOperationException("non-implimented class in SPCSetting:"  + clas);
				}
				
				if(setting.choosableValues != null && setting.choosableValues.length > 1) {
					assert comp != null : "forgot to assign [comp] for objects of class:" + clas;
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
					var c = Presentable.createGbc(0, 0);
						c.fill = GridBagConstraints.NONE;
						
					newComp.add(comboBtn, c);
					newComp.add(comp, Presentable.createGbc(1, 0));
					
					comp = newComp;
				}
				
			}
			
			
			//make visual indication if a setting is hot swappable
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
		for(var setting : SPCSettings.AvaliableSettings) {
			Object value = settings.settings.get(setting);
			if(value != null) {
				assert functionsToUpdateUI.containsKey(setting) : "forgot to make a [functinosToUpdateUI] entry for settings: " + setting;
				
				functionsToUpdateUI.get(setting).apply(value);
			}
		}
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
	
	public static EnumSet<? extends Enum<?>> getVariEnum() {
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

	@Override public boolean isModified() {
		return this.isModified = this.settings.getModifiedSettings().size() != 0;
	}
}
