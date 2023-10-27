package DOMViewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

import FileMetadata.BasicFileAttribute;
import presentables.Presentable;

public class FileViewer extends JTabbedPane{
	protected DOMParser parser;
	private JPanel metaTab, thisPanel, parserPanel;
	
	public FileViewer(DOMParser parser) {
		super();
		setParser(parser);
		initGUI();
	}

	public DOMParser getParser() {
		return parser;
	}

	public void setParser(DOMParser parser) {
		while (getTabCount() > 1) {
			remove(1);//meta tab is the first (i = 0) and we dont ever want to remove that
		}
		
		if(parser == null || parser == this.parser) return;
		
		this.parser = parser;
		updateMeta(parser.getPath());
		
		parser.ParseFile();
		var tabs = parser.getUITabbs();
		for(var v : tabs.keySet()) {
			String title = v.toString();
			var content = (JPanel)tabs.get(title);
			var wrapper = new JScrollPane(content,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			addTab(v.toString(), wrapper);
		}
	}
	
	public void updateGUI() {
		parser.updateGUI();
		updateMeta(parser.getPath());
	}
	
	private void initGUI() {
		//meta panels
		metaTab = new JPanel(new GridBagLayout());
			System.out.println("FileViewer>initGUI, background; metapanel:PINK");
			metaTab.setBackground(Color.PINK);
		
		var c = new GridBagConstraints();
			c.weightx = c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
		
		thisPanel = 	getTitledPanel("System");
		parserPanel = 	getTitledPanel("Parser Specific");
			
		metaTab.add(
				new JSplitPane(SwingConstants.HORIZONTAL,
						new JScrollPane(parserPanel,
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
						new JScrollPane(thisPanel,
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)),
				c);
		
		addTab("Meta", metaTab);
	}
	
	public void updateMeta(Path path) {
		thisPanel.removeAll();	
		
		int x = 0, y = 0;
		
		var basic_p = getTitledPanel("Basic");
			try {
				BasicFileAttributes FileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
					
				for(var attr : BasicFileAttribute.values()) {
					
					JComponent display = null;
					
					if(		attr == BasicFileAttribute.LAST_MODIFIED_TIME	||
							attr == BasicFileAttribute.CREATION_TIME		||
							attr == BasicFileAttribute.LAST_ACCESS_TIME) {
						var disp = new DateTimePicker();
						var date = new Date(((FileTime)attr.fetch(FileAttrs)).toMillis());
						var cal  = Calendar.getInstance();
			            	cal.setTime(date);
						
			            LocalDate localdate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			            LocalTime localtime = LocalTime.of(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));	
			            
						disp.getDatePicker().setDate(localdate);
						disp.getTimePicker().setTime(localtime);
						
						DateChangeListener listener;
						
						if(attr == BasicFileAttribute.LAST_MODIFIED_TIME)
							listener = new DateChangeListener() {
								@Override public void dateChanged(DateChangeEvent event) {
									FileTime ft = FileTime.from(disp.getDateTimePermissive().toEpochSecond(null), TimeUnit.SECONDS);
									try {
										Files.setLastModifiedTime(path, ft);
									} catch (IOException e) { e.printStackTrace(); }
								}
							};
						else
							listener = new DateChangeListener() {
								@Override public void dateChanged(DateChangeEvent event) {
									//TODOOOOOOOO
//									FileTime ft = FileTime.from(disp.getDateTimePermissive().toEpochSecond(null), TimeUnit.SECONDS);
//									try {
//										Files.setLastModifiedTime(path, ft);
//									} catch (IOException e) { e.printStackTrace(); }
								}
							};
						disp.getDatePicker().addDateChangeListener(listener);
						
						display = disp;
					}else {
						display = new JLabel(attr.fetch(FileAttrs).toString());
					}
						
					Presentable.genLabledContent(basic_p,
							attr.getTitle(),		//label
							attr.getDescription(),	//tool tip
							x, y++,
							display);
				}
				
			} catch (IOException e) { e.printStackTrace(); }
		
		var posix_p = getTitledPanel("POSIX");
			try {
				PosixFileAttributes FileAttrs = Files.readAttributes(path, PosixFileAttributes.class);
				Presentable.genLabledContent(posix_p,
						"Owner", "",
						x, y++,
						new JLabel(FileAttrs.owner().getName()));
				Presentable.genLabledContent(posix_p,
						"Group", "",
						x, y++,
						new JLabel(FileAttrs.group().getName()));
				
				x = y = 0;
				for(var v : PosixFilePermission.values()) {
					var checkbox = new JCheckBox(v.name().toLowerCase().replace('_', ' '));
						checkbox.setSelected(FileAttrs.permissions().contains(v));
						checkbox.addItemListener(new ItemListener() {
				            @Override public void itemStateChanged(ItemEvent e) {
				            	//update changes
				            	if(e.getStateChange() == ItemEvent.SELECTED) 
				            		FileAttrs.permissions().add(v);
				            	else 
				            		FileAttrs.permissions().remove(v);
				            	
				            	//save changes	
				            	try {
									Files.setPosixFilePermissions(path, FileAttrs.permissions());
								} catch (IOException e1) { e1.printStackTrace(); }
				            }
				        });
						
					posix_p.add(checkbox, Presentable.createGbc(x,y++));
				}
			}catch (UnsupportedOperationException e ) {
				posix_p.add(new JLabel("standard not supported on this system"));
			} 
			catch (IOException e) { e.printStackTrace(); }
		
		x = y = 0;
		thisPanel.add(basic_p, Presentable.createGbc(x, y++));
		thisPanel.add(posix_p, Presentable.createGbc(x, y++));
			
		thisPanel.validate();
	}
	
	private JPanel getTitledPanel(String title) {
		var panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), title, TitledBorder.LEFT, TitledBorder.TOP));
		return panel;
	}
}
