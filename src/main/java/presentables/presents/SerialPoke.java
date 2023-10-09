package presentables.presents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import static java.util.Map.entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpringLayout;
import javax.swing.table.TableModel;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import audinc.gui.MainWin;
import audinc.gui.WrapLayout;
import presentables.Presentable;
import presentables.custom_doTheThingIfNotNull;
import presentables.doTheThing;

public class SerialPoke extends Presentable{
	//editor tab UI
	private JTable commTable;
	private DefaultTableModel commTable_model;
	private JTextField editorTab_portDescriptor_txt;
	protected ArrayList<SerialPokeCommConnection> SPCConnections = new ArrayList<>(1);
	
	@Override protected void start()	{ 	}
	
	@Override protected void init(MainWin mw) 	{
		initGUI(mw);
		onEditorTabSelect();
	}	
	@Override protected void initGUI(MainWin mw){ //good luck trying to figure this out in a timely manner. functions? what are those? 
		JPanel container = new JPanel(new BorderLayout());
			JTabbedPane mainTp = new JTabbedPane();
			
				//editor tab
				JPanel editorTab = new JPanel(new BorderLayout());
					JButton editorTab_newEditor_btn = new JButton("new editor");
						editorTab_newEditor_btn.addActionListener(event -> onNewEditorSelect(mainTp));
					JPanel editorTab_portDescriptor = new JPanel();
						SpringLayout editorTab_portDescriptor_layout = new SpringLayout();
						editorTab_portDescriptor.setLayout(editorTab_portDescriptor_layout);{
							SpringLayout layout = editorTab_portDescriptor_layout;
							
							JLabel label = new JLabel("Port Descriptor : ");
							editorTab_portDescriptor_txt = new JTextField("COM3", 15);
							editorTab_portDescriptor_txt.getDocument().addDocumentListener(new DocumentListener() {
								  public void changedUpdate(DocumentEvent e) 	{
									  String text = editorTab_portDescriptor_txt.getText();
									  
									  if((text.length() == 0) != !editorTab_newEditor_btn.isEnabled()) //System.out.println("flipping enabled to : " + !editorTab_newEditor_btn.isEnabled());
										  editorTab_newEditor_btn.setEnabled(!editorTab_portDescriptor_txt.isEnabled());
								  }
								  public void removeUpdate(DocumentEvent e) 	{
									  String text = editorTab_portDescriptor_txt.getText();
									  
									  if(text.length() == 0 && editorTab_newEditor_btn.isEnabled())
										  editorTab_newEditor_btn.setEnabled(false);	
								  }
								  public void insertUpdate(DocumentEvent e) 	{									  
									  if(!editorTab_newEditor_btn.isEnabled())
										  editorTab_newEditor_btn.setEnabled(true);
								  }
								});
							editorTab_newEditor_btn.setEnabled(editorTab_portDescriptor_txt.getText().length() != 0);
							
					        editorTab_portDescriptor.add(label);
					        editorTab_portDescriptor.add(editorTab_portDescriptor_txt);
					 
					        //label at (5,5).
					        layout.putConstraint(SpringLayout.WEST, label,
					        			5, SpringLayout.WEST, editorTab_portDescriptor);
					        layout.putConstraint(SpringLayout.NORTH, label,
					        			5, SpringLayout.NORTH, editorTab_portDescriptor);
					 
					        //test field at (<label's right edge> + 5, 5).
					        layout.putConstraint(SpringLayout.WEST, editorTab_portDescriptor_txt,
					        			5, SpringLayout.EAST, label);
					        layout.putConstraint(SpringLayout.NORTH, editorTab_portDescriptor_txt,
					        			5, SpringLayout.NORTH, editorTab_portDescriptor);
					 
					        //Adjust constraints for the content pane. text field assumed to be bottom most component
					        layout.putConstraint(SpringLayout.EAST, editorTab_portDescriptor,
					        			1, SpringLayout.EAST, editorTab_portDescriptor_txt);
					        layout.putConstraint(SpringLayout.SOUTH, editorTab_portDescriptor,
					                    5, SpringLayout.SOUTH, editorTab_portDescriptor_txt);
						}
					editorTab.add(editorTab_portDescriptor, BorderLayout.PAGE_START);
					editorTab.add(editorTab_newEditor_btn, BorderLayout.PAGE_END);
					commTable = new JTable() {
						private static final long serialVersionUID = 1L;//eclipse complains
						public boolean isCellEditable(int row, int column) { return false; }; //disables user editing of table
					};
					commTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					commTable.setCellEditor(null);
					commTable_model = (DefaultTableModel) commTable.getModel();
						for(var v : new String[] {"port name", "baud rate"}) commTable_model.addColumn(v);
						commTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
							@Override public void valueChanged(ListSelectionEvent e) {
								editorTab_portDescriptor_txt.setText(commTable.getModel().getValueAt(commTable.getSelectedRow(), 0).toString());
							}
						});
					TableRowSorter<TableModel> importT_srcsTable_sorter = new TableRowSorter<>(commTable_model);
						importT_srcsTable_sorter.setSortKeys(Arrays.asList(
									new RowSorter.SortKey(0, SortOrder.ASCENDING)
								));
					commTable.setRowSorter(importT_srcsTable_sorter);
						JScrollPane commTable_scrollFrame = new JScrollPane(commTable,	
								JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
								commTable_scrollFrame.setAutoscrolls(true);
					editorTab.add(commTable_scrollFrame, BorderLayout.CENTER);
					
						
			mainTp.addTab("editor", MainWin.getImageIcon("res/build.png", MainWin.stdtabIconSize), editorTab, "");
			mainTp.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent changeEvent) {
					JTabbedPane pane = (JTabbedPane) changeEvent.getSource();
					switch(pane.getTitleAt(pane.getSelectedIndex())) {
						case "editor" : onEditorTabSelect(); 
							break;
					}
				}
			});
			
			
			//save/load tab
			
			container.add(mainTp);
		mw.add(container);
	}
	@Override public void quit(){
		for(var v : this.SPCConnections) {
			v.quit();
		}
	}
	

///////////////////
//gui
///////////////////
	private void onEditorTabSelect() {
		//update table
		for(var v : SerialPort.getCommPorts()) {
			System.out.println(v.toString());
			commTable_model.addRow(new Object[] {v.toString()});
		}
		if(commTable_model.getRowCount() == 0) {
			commTable_model.addRow(new Object[] {"no connections found", -1});
		}
	}
	
	private void onNewEditorSelect(JTabbedPane tabbedPane) {
		String title = tabbedPane.getTabCount() + ". " + editorTab_portDescriptor_txt.getText();
		SerialPort sp = SerialPort.getCommPort(editorTab_portDescriptor_txt.getText());		
		
		SerialPokeCommConnection spcc = new SerialPokeCommConnection(sp, title);
		if(spcc.content == null) return;
		SPCConnections.add(spcc);
		
		int tabI = tabbedPane.getTabCount();
		
		//tab ui
		JPanel tabPanel 	= new JPanel(new GridBagLayout());
		JLabel tabTitle		= new JLabel(title);
		JButton tabClosebtn = new JButton("X");
			tabClosebtn.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(), new EmptyBorder(0,10,0,0)));//border padding: top,left,bottom,right
			tabClosebtn.addActionListener(event -> {spcc.quit(); tabbedPane.remove(spcc.content);});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		
		tabPanel.add(tabTitle, gbc);
		
		gbc.gridx += 5;
		gbc.weightx = 0;
		tabPanel.add(tabClosebtn, gbc);
		
		tabbedPane.addTab(title, spcc.content);
		tabbedPane.setTabComponentAt(tabI, tabPanel);
		
		//select new tab
		tabbedPane.setSelectedIndex(tabI);
	}
	
	public static String[][] getSPGeneralRowInfo(SerialPort sp) {
		return new String[][] {
					{"system port name"	, sp.getSystemPortName()},
					{"baud rate"		, String.valueOf(sp.getBaudRate())}
				};
	}
	public static String[][] getSPFullRowInfo(SerialPort sp) {
		return new String[][] {
					{"system port name"	, sp.getSystemPortName()},
					{"system port path"	, sp.getSystemPortPath()},
					{"system port location"		, sp.getPortLocation()},
					{"descriptive port name"	, sp.getDescriptivePortName()},
					{"baud rate"		, String.valueOf(sp.getBaudRate())},
					{"port decription"	, sp.getPortDescription()},
					{"port location" 	, sp.getPortLocation()},
			    	{"device write buffer size"	, String.valueOf(sp.getDeviceReadBufferSize()) 	+ "bit"},
			    	{"device raed buffer size"	, String.valueOf(sp.getDeviceWriteBufferSize()) + "bit"},
			    	{"vendor ID"		, String.valueOf(sp.getVendorID())},
			    	{"data bits per word"		, String.valueOf(sp.getNumDataBits())},
			    	{"num stop bits"	, switch(sp.getNumStopBits()) {
											case SerialPort.ONE_POINT_FIVE_STOP_BITS 	-> "ONE_POINT_FIVE_STOP_BITS";
											case SerialPort.ONE_STOP_BIT				-> "ONE_STOP_BIT";
											case SerialPort.TWO_STOP_BITS				-> "TWO_STOP_BITS";
											default 									-> "unknown response";}},
			    	{"parity"	, switch(sp.getParity()) {
										case SerialPort.EVEN_PARITY		-> "EVEN_PARITY";
										case SerialPort.MARK_PARITY		-> "MARK_PARITY";
										case SerialPort.NO_PARITY		-> "NO_PARITY";
										case SerialPort.ODD_PARITY		-> "ODD_PARITY";
										case SerialPort.SPACE_PARITY	-> "SPACE_PARITY";
										default 						-> "unknown response";}},
			    	{"read timeout  (milliseconds)"		, String.valueOf(sp.getReadTimeout())},
			    	{"write timeout (milliseconds)"		, String.valueOf(sp.getWriteTimeout())},
			    	{"flow control > data set ready"	, (0 != (SerialPort.FLOW_CONTROL_DSR_ENABLED & sp.getFlowControlSettings()))? "enabled":"disabled"},
			    	{"flow control > data transmission ready",	(0 != (SerialPort.FLOW_CONTROL_DTR_ENABLED & sp.getFlowControlSettings()))? "enabled":"disabled"},
			    	{"flow control > xon/xoff IN"		, (0 != (SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED & sp.getFlowControlSettings()))? "enabled":"disabled"},
			    	{"flow control > xon/xoff OUT"		, (0 != (SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED & sp.getFlowControlSettings()))? "enabled":"disabled"},
			    	{"flow control > request to send"	, (0 != (SerialPort.FLOW_CONTROL_RTS_ENABLED & sp.getFlowControlSettings()))? "enabled":"disabled"},
			    	{"flow control > clear to send"		, (0 != (SerialPort.FLOW_CONTROL_CTS_ENABLED & sp.getFlowControlSettings()))? "enabled":"disabled"}
				};
	}

	
///////////////////
//Presentable statics
///////////////////
	public static String 	getDisplayTitle() 	{ 	return "Serial Poke";	}
	public static ImageIcon getImgIcon()		{	return MainWin.getImageIcon("res/presentIcons/cerealpike.png", new Dimension((int)(MainWin.stbPresentIconSize.width * 1.2),(int)(MainWin.stbPresentIconSize.height * 1.2))); }
	public static String 	getDescription()	{	return "<html>"
			+ "<body> A way to jimmy seral connections into working. please use something like PuTTY for actual communicaitons.<br>"
			+ "possiable use cases<br>"
			+ "<ul><li>force send signal</li>"
			+ "<li>see live port events</li>"
			+ "<li>reroute ports(wip)</li>"
			+ "</ul>"
			+ "<b>the ability to multiplex ports is dependent on host machine</b>"
			+ "<br>Serial Poke? Cereal Pike!"
			+ "</body>"; }
	
}

class SerialPokeCommConnection{
	public String title;
	public SerialPort sp;
	public int logSettings = Integer.MAX_VALUE;//see line ~86 of https://github.com/Fazecast/jSerialComm/blob/master/src/main/java/com/fazecast/jSerialComm/SerialPort.java
	
	//gui
	public JPanel content = null;
	public JTabbedPane content_tabb = new JTabbedPane();
	
	//private
	private boolean
		loggingEnabled = true,
		saveLogTranscript = false;
	private Path logTranscriptPath;
	private BufferedWriter logger = null;
	private JLabel noticeDisplay;
	private JTree ConCateTree;//console category tree
	private DefaultMutableTreeNode ConCateTree_root;
	
	public SerialPokeCommConnection(SerialPort sp, String title) {
		this.title = title;
		this.sp = sp;
		
		try {
		this.logTranscriptPath = Presentable.getRoot(SerialPoke.class).toAbsolutePath().resolve(this.getDefaultSaveName());
		} catch (java.nio.file.InvalidPathException e) {
			JLabel jl = new JLabel("connection path is invalid. " + e);
			JScrollPane _scroll = new JScrollPane(jl,	
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				_scroll.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/8), (int)MainWin.stdDimension.getHeight()));
				_scroll.setAutoscrolls(true);
			
			JOptionPane.showMessageDialog(content, _scroll);
			return; 
		}
		
		genGUI();
		
		setNoticeText("this connection is not opened untill explicetely told to open", new Color(0x0));
	}
	
	public void quit() {
		if(sp.isOpen()) {
			sp.closePort();
		}
		
		if(logger != null)
			try {
				logger.close();
			} catch (IOException e) { e.printStackTrace(); }
	}

//////////////////////
//UI
//////////////////////
	private void genGUI() {
		content = new JPanel(new BorderLayout());
			content.setLayout(new BorderLayout());
			content.setBackground(MainWin.randColor());
			
			noticeDisplay = new JLabel("notices");
		
		content.add(noticeDisplay, BorderLayout.PAGE_END);
		
		ConCateTree_root = new DefaultMutableTreeNode(new custom_doTheThingIfNotNull<JPanel>() {
				@Override public String toString() { return sp.getSystemPortName(); }
				@Override public void doTheThing(JPanel pan) {}
			});
		
		genUI_tab_editor();
		genUI_tab_settings();
		genUI_tab_liveInfo();
		
		
		content.add(content_tabb, BorderLayout.CENTER);
	}
	
	public void openLoggingDialoug() {		
		JFrame cframe = new JFrame();
        	cframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel logCatagories = new JPanel(new WrapLayout());
        	logCatagories.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Logging catagories", TitledBorder.LEFT, TitledBorder.TOP));
            logCatagories.setEnabled(this.loggingEnabled);
        	{
	        	Field[] fields = SerialPort.class.getDeclaredFields();
	        	String startsWith = "LISTENING_EVENT_";
	        	for(var v : fields) {
	        		String name = v.getName();
	        		if(name.startsWith(startsWith) && v.getType() == int.class) {
	        			if(name.endsWith("TIMED_OUT")) continue;
	        			
	        			int 	val = Integer.MIN_VALUE;
	        			
	        			try { val = v.getInt(null); } 
	        			catch (IllegalArgumentException e) 	{ e.printStackTrace(); } 
	        			catch (IllegalAccessException e) 	{ e.printStackTrace(); }
	        			
	        			if(val == Integer.MIN_VALUE) 		{ new IllegalArgumentException("retreived bad value form serialport. from:" + v.getName()); }
	        			
	        			final int val_f = val;
						JCheckBox jcb = new JCheckBox(name.substring(startsWith.length()), (this.logSettings | val)!=0);
						jcb.addItemListener(il -> {
							 	if(il.getStateChange() == ItemEvent.SELECTED) {
				        			logSettings =  logSettings | val_f;
				        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { //double check because there are other states that can be triggered
				        			int res = logSettings & val_f;
				        			if(res != 0) {
				        				logSettings = logSettings & (~val_f);
				        			}
				        		}
						 	});
						jcb.setSelected((logSettings & val_f) != 0 );
						logCatagories.add(jcb);
	        		}
	        	}
        	}
        
        	
        JCheckBox logToggler =	new JCheckBox("logging enabled", this.loggingEnabled);
        	logToggler.addItemListener(il -> {
        		if(il.getStateChange() == ItemEvent.SELECTED) {
        			this.setLogging(true);
        			logCatagories.setEnabled(true);
        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { //double check because there are other states that can be triggered
        			this.setLogging(false);
        			logCatagories.setEnabled(false);
        		}
        	});
    		
    	JPanel logTranscript_panel = new JPanel();
			SpringLayout editorTab_portDescriptor_layout = new SpringLayout();
			logTranscript_panel.setLayout(editorTab_portDescriptor_layout);{
				SpringLayout layout = editorTab_portDescriptor_layout;
				
				JCheckBox savetoggler =	new JCheckBox("save transcript", isLoggingToTranscript());
		    		savetoggler.addItemListener(il -> {
		        		if(il.getStateChange() == ItemEvent.SELECTED) {
		        			this.saveLogTranscript = true;
		        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { //double check because there are other states that can be triggered
		        			this.saveLogTranscript = false;
		        		}
		        	});
				
		    	JButton saveFilePicker = new JButton("choose file");
		    		saveFilePicker.setBackground(new Color(0f,0f,0f,0f));
		    		saveFilePicker.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		    		saveFilePicker.addActionListener(e -> {
			    			Path pPreferred = Presentable.getRoot(SerialPoke.class);
			    				
			    			JFileChooser fc = new JFileChooser(pPreferred.toAbsolutePath().toString());
			    			
			    			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			    			
			    			switch(fc.showOpenDialog(cframe)) {
			    				case JFileChooser.APPROVE_OPTION : 
				    					try {
				    						var file	= fc.getSelectedFile();
				    						
				    						if(!file.exists()) {
				    							setNoticeText("file cannot be found: " + file.toPath(), new Color(0,0,0));
				    						}
				    						
				    						if(logger != null) logger.close();
				    						
				    						if(file.isDirectory()) {
				    							Files.createDirectories(Paths.get(file.getAbsolutePath()));
				    							logTranscriptPath = logTranscriptPath.resolve(getDefaultSaveName());
				    						}
				    					} catch (IOException e1) { e1.printStackTrace(); }
			    					break;
			    				case JFileChooser.CANCEL_OPTION : 
			    					break;
			    			}
		    			});
		    		
				JTextField input = new JTextField(logTranscriptPath.toAbsolutePath().toString());
//				input.getDocument().addDocumentListener(new DocumentListener() {
//					  public void changedUpdate(DocumentEvent e) 	{}
//					  public void removeUpdate(DocumentEvent e) 	{}
//					  public void insertUpdate(DocumentEvent e) 	{}
//					});
				
				logTranscript_panel.add(savetoggler);
				logTranscript_panel.add(input);
				logTranscript_panel.add(saveFilePicker);
		 
		        //toggler
		        layout.putConstraint(SpringLayout.WEST, savetoggler,
		        			5, SpringLayout.WEST, logTranscript_panel);
		        layout.putConstraint(SpringLayout.NORTH, savetoggler,
		        			5, SpringLayout.NORTH, logTranscript_panel);
		 
		        //file selector button
		        layout.putConstraint(SpringLayout.EAST, saveFilePicker,
		        		-5, SpringLayout.EAST, logTranscript_panel);
		        layout.putConstraint(SpringLayout.NORTH, saveFilePicker,
		        		3, SpringLayout.NORTH, logTranscript_panel);
		        layout.putConstraint(SpringLayout.SOUTH, saveFilePicker,
		        		-3, SpringLayout.NORTH, input);
		        
		        //text field .
		        layout.putConstraint(SpringLayout.NORTH, input,
		        			5, SpringLayout.SOUTH, savetoggler);
		        
		        //Adjust constraints for the content pane. text field assumed to be bottom most component
		        layout.putConstraint(SpringLayout.EAST, logTranscript_panel,
		        			1, SpringLayout.EAST, input);
		        layout.putConstraint(SpringLayout.SOUTH, logTranscript_panel,
		                    5, SpringLayout.SOUTH, input);
			}
    		
        
        JComponent[] objs = {
        		logToggler,
	        	logCatagories,
	        	logTranscript_panel
        	};
        JPanel wrapper = new JPanel();
        	wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        for(var v : objs) {v.setAlignmentX(JComponent.LEFT_ALIGNMENT); wrapper.add(v);}
        
        JScrollPane wrapper_scroll = new JScrollPane(wrapper,
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        	wrapper_scroll.setPreferredSize(new Dimension((int)(300*MainWin.stdDimensionScale), (int)(150*MainWin.stdDimensionScale)));
        	wrapper_scroll.setBorder(BorderFactory.createEmptyBorder());
        JOptionPane.showMessageDialog(cframe, wrapper_scroll, "logging", JOptionPane.PLAIN_MESSAGE);
        
	}
	public void openInfoDialoug() {
		JOptionPane.showMessageDialog(content, new Object[] {this.getInfoScrollFrame()}, "info: " +title, JOptionPane.PLAIN_MESSAGE);
	}	
	
	private JComponent getInfoScrollFrame() {
		JLabel spInfo = new JLabel();{
			 StringBuilder sb = new StringBuilder("<html>");
			 sb.append("<table>"
			 		+ "<tr>"
			 		+ "<th>field</th>"
			 		+ "<th>value</th>"
			 		+ "</tr>");
			 
			 for(String[] s : SerialPoke.getSPFullRowInfo(sp)) {
				 sb.append("<tr>"
				 		+ "<td>"+s[0]+"</td>"
				 		+ "<td>"+s[1]+"</td>"
				 		+ "</tr>");
			 }
			 
			 sb.append("</table>");
			 
			 spInfo.setText(sb.toString());
		}
		JScrollPane spInfo_scroll = new JScrollPane(spInfo,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return spInfo_scroll;
	}
	
	public void setNoticeText(String text) {
		this.noticeDisplay.setText(text);
	}
	public void setNoticeText(String text, Color color) {
		this.noticeDisplay.setForeground(color);
		setNoticeText(text);
	}
	public void appendNoticeText(String text, Color color) {
		setNoticeText(this.noticeDisplay.getText() + text);
	}
	private boolean isLogging() {
		return this.loggingEnabled;
	}
	private void setLogging(boolean enabled) {
		if(this.isLogging() == enabled) return;
		this.loggingEnabled = enabled;
	}
	public boolean isLoggingToTranscript() {
		return this.saveLogTranscript;
	}
	public void setLoggingTranscript(boolean enabled) {
		if(this.isLoggingToTranscript() == enabled) return;
	}
	public boolean setLoggingTranscript(boolean enabled, String setTo) {
		if(!this.setLoggingTranscript(setTo)) return false;
		
		this.setLogging(enabled);
		return true;
	}
	public boolean setLoggingTranscript(String setTo) {
		setLoggingTranscript(this.loggingEnabled, setTo);
		return false;
	}
	public String getDefaultSaveName() {
		return title + ".log";
	}
	private void log(String toLog) {
		if(!isLoggingToTranscript()) return;
		if(logger == null)
			try {
				logger = Files.newBufferedWriter(logTranscriptPath);
			} catch (IOException e) { e.printStackTrace(); }
		
		try {
			logger.write(toLog);
			logger.newLine();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	private void genUI_tab_liveInfo() {
		JPanel tab_cont = new JPanel(new BorderLayout());
		
		var emptyBorder = BorderFactory.createEmptyBorder();
		
		//tool bar
		JToolBar toolbar = new JToolBar();
			JButton tb_infobtn = new JButton(MainWin.getImageIcon("res/info.png", MainWin.stdtabIconSize));
				tb_infobtn.setBorder(emptyBorder);
				tb_infobtn.setToolTipText("info");
				tb_infobtn.addActionListener(e -> openInfoDialoug());
			JButton tb_logbtn = new JButton(MainWin.getImageIcon("res/log.png", MainWin.stdtabIconSize));
				tb_logbtn.setBorder(emptyBorder);
				tb_logbtn.setToolTipText("log settings");
				tb_logbtn.addActionListener(e -> openLoggingDialoug());
				
		toolbar.add(tb_infobtn);
		toolbar.add(tb_logbtn);
		
		tab_cont.add(toolbar, BorderLayout.PAGE_START);
		
		//text area
		JTextPane logout = new JTextPane();
			logout.setContentType("text/html");
			logout.setEditable(false);
			logout.setText("<table>"
			 		+ "<tr>"
			 		+ "<th>time</th>"
			 		+ "<th>event</th>"
			 		+ "<th>data received</th>"
			 		+ "</tr>");
			SimpleAttributeSet logout_sas = new SimpleAttributeSet();
			JScrollPane logout_scroll = new JScrollPane(logout,	
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tab_cont.add(logout_scroll, BorderLayout.CENTER);
		
		content_tabb.addTab("live info", MainWin.getImageIcon("res/info1.png", MainWin.stdtabIconSize), tab_cont, "event driven info about the connection. read only");
		
		sp.addDataListener(new SerialPortDataListener() {
			StyledDocument logout_doc = logout.getStyledDocument();
			int endLength = ("</html>").length();
			
			@Override public int getListeningEvents() { return 0; }
				
			@Override public void serialEvent(SerialPortEvent spe) {
				//if event is to be logged
				if(loggingEnabled && ((logSettings & spe.getEventType()) != 0)) {
					try {
						long time_mil = (int)(System.nanoTime()/Math.pow(10,6)); 
						String 
							serialPortEventName_s 	= spe.toString(),
							receivedData_s			= Arrays.toString(spe.getReceivedData()); 
						StringBuilder sb = new StringBuilder();
							sb.append("<tr><td>");
							sb.append(time_mil);
							sb.append("</td><td>");
							sb.append(serialPortEventName_s);
							sb.append("</td><td>");
							sb.append(receivedData_s);
							sb.append("</td></tr>");
						
						logout_doc.insertString(logout_doc.getLength() - endLength, sb.toString(), logout_sas);
						
						if(saveLogTranscript) {
							String line = String.format("%-12d:%30s:%d\n",
									time_mil,
									serialPortEventName_s,
									receivedData_s);
							log(line);
							
						}
					} catch (BadLocationException e) { e.printStackTrace(); }
				}
				
				switch(spe.getEventType()) {
					case SerialPort.LISTENING_EVENT_DATA_AVAILABLE :
						break;
					case SerialPort.LISTENING_EVENT_DATA_RECEIVED :
						break;
					case SerialPort.LISTENING_EVENT_DATA_WRITTEN :
						break;
					case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED :
						break;
					case SerialPort.LISTENING_EVENT_BREAK_INTERRUPT :
						break;
					/* clear to send */
					case SerialPort.LISTENING_EVENT_CTS :
						break;
					/* data set ready */
					case SerialPort.LISTENING_EVENT_DSR : 
						break;
					/* This is an input for DTE devices and an output for DCE devices. 
					 * This signals the DTE device that there is an incoming call.
					 * This signal is maintained "Off" at all times except when the 
					 * DCE receives a ringing signal. */
					case SerialPort.LISTENING_EVENT_RING_INDICATOR :
						break;
					/* you are opening the port while the remote device is in the middle of transmitting. */
					case SerialPort.LISTENING_EVENT_FRAMING_ERROR :
						break;
					/* device driver buffer overrun */
					case SerialPort.LISTENING_EVENT_FIRMWARE_OVERRUN_ERROR :
						break;
					/* application buffer overrun */
					case SerialPort.LISTENING_EVENT_SOFTWARE_OVERRUN_ERROR :
						break;
					/* parity error */
					case SerialPort.LISTENING_EVENT_PARITY_ERROR :
						break;
				}
			}
		});
	}
	
	private void genUI_tab_editor() {
		JPanel tab_cont = new JPanel();
		
		JCheckBox jcb_toggleport = new JCheckBox("port enabled", sp.isOpen());
			jcb_toggleport.setToolTipText("open/close port. ");
			jcb_toggleport.addItemListener(il -> {
				 	if(il.getStateChange() == ItemEvent.SELECTED) {
				 		int delay = 200;
	        			setNoticeText("opening port ("+delay+"mil delay)... ", Color.black);
	        			appendNoticeText((sp.openPort(delay) ? "success" : "failed" ), Color.black);
	        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { 
	        			setNoticeText("closing port ... " , Color.black);
	        			appendNoticeText((sp.closePort() ? "success" : "failed" ), Color.black);
	        		}
			 	});
		tab_cont.add(jcb_toggleport);
		
		content_tabb.addTab("editor", MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize), tab_cont, "general manager");
	}
	
	private void genUI_tab_settings() {
		JPanel tab_cont = new JPanel(new BorderLayout());
		
		//screen right
		JPanel cards = new JPanel();
			cards.setLayout(new BoxLayout(cards, BoxLayout.LINE_AXIS));
			cards.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		//screen left
		JPanel console = new JPanel();
			console.setLayout(new BoxLayout(console, BoxLayout.Y_AXIS));
			//populating ConCateTree_root
			{
				//category session
				DefaultMutableTreeNode cat_session = new DefaultMutableTreeNode(new custom_doTheThingIfNotNull<JPanel>() {
					@Override public String toString() { return "session"; }
					@Override public void doTheThing(JPanel pan) {
						
					}
					}, true);
				
				//category connection
				DefaultMutableTreeNode cat_connection = new DefaultMutableTreeNode(new custom_doTheThingIfNotNull<JPanel>() {
					@Override public String toString() { return "connection"; }
					@Override public void doTheThing(JPanel pan) {
						
					}
					}, true);
					//sub category flow control
					DefaultMutableTreeNode cat_connection_flowControl = new DefaultMutableTreeNode(new custom_doTheThingIfNotNull<JPanel>() {
						@Override public String toString() { return "flow control"; }
						@Override public void doTheThing(JPanel pan) {
							pan.add(new JLabel("connection>flow control"));
						}
						}, true);
					cat_connection.add(cat_connection_flowControl);
				
				//top level categories
				ConCateTree_root.add(cat_session);
				ConCateTree_root.add(cat_connection);
			}
			ConCateTree = new JTree(ConCateTree_root);
				ConCateTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				ConCateTree.addTreeSelectionListener(new TreeSelectionListener() {
					@Override public void valueChanged(TreeSelectionEvent e) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)ConCateTree.getLastSelectedPathComponent();
						cards.removeAll();
						
						//go through the current node and all sub nodes.also doubles as dollar store bench mark for the garbage collector
						Stack<JPanel> cardStack = new Stack<JPanel>();
							cardStack.push(cards);
						genUI_tab_settings_loadCardUI(node, cardStack);
						
						cards.validate();
					}
				});
			ConCateTree.setAlignmentX(Component.LEFT_ALIGNMENT);
			console.add(ConCateTree);
		
		//scroll for screen right
		JScrollPane cards_scroll = new JScrollPane(cards,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			cards_scroll.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/8), (int)MainWin.stdDimension.getHeight()));
			cards_scroll.setAutoscrolls(true);
			
		//scroll for screen left
		JScrollPane console_scroll = new JScrollPane(console,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			console_scroll.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/5), (int)MainWin.stdDimension.getHeight()));
			console_scroll.setPreferredSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/4), (int)MainWin.stdDimension.getHeight()));
		
		//join screens with split pane
		tab_cont.add(new JSplitPane(SwingConstants.VERTICAL,console_scroll, cards_scroll), BorderLayout.CENTER);
		
		//set default selection as top level
		Stack<JPanel> cardStack = new Stack<JPanel>();
			cardStack.push(cards);
		genUI_tab_settings_loadCardUI(ConCateTree_root, cardStack);
		cards_scroll.validate();
		
		content_tabb.addTab("settings", MainWin.getImageIcon("res/note.png", MainWin.stdtabIconSize), tab_cont, "seral port settings and info");
	}
	private void genUI_tab_settings_loadCardUI(DefaultMutableTreeNode currNode, Stack<JPanel> cardStack) {
		try {
//			System.out.println("currNode:"+currNode.getUserObject().toString());
			//current node goes first
			custom_doTheThingIfNotNull<JPanel> currNodeTask = (custom_doTheThingIfNotNull<JPanel>)(currNode.getUserObject());
				currNodeTask.doTheThing(cardStack.peek());
			
			// if has children, make & use a new bordered panel
			if(currNode.getChildCount() > 0) {
//				System.out.println("children found with:"+ currNodeTask.toString());
				
				//set up container panel w/ border
				JPanel np = new JPanel();
					np.setLayout(new BoxLayout(np, BoxLayout.Y_AXIS));
					np.setAlignmentX(Component.LEFT_ALIGNMENT);
					np.setBorder(BorderFactory.createTitledBorder(
							BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
							currNodeTask.toString(), 
							TitledBorder.LEFT, 
							TitledBorder.TOP
						));
				cardStack.peek().add(np);
				cardStack.push(np);
				
				//waterfall through all children & build the UI
				var v = currNode.children().asIterator();
				while(v.hasNext()) {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)(v.next());
					custom_doTheThingIfNotNull<JPanel> dtt = (custom_doTheThingIfNotNull<JPanel>)(dmtn.getUserObject());
					
//					System.out.println("\tdtt: "+currNodeTask.toString()+" has child: "+dtt.toString());
					
					//child does the thing
					dtt.doTheThing(cardStack.peek());
					
					//cascade down
					if(dmtn.getChildCount() > 0) genUI_tab_settings_loadCardUI(dmtn, cardStack);
				}
				
				//remove self
				cardStack.pop();
			}
			
		}catch(Exception e1) {
			System.err.println("SerialPoke.java>SerialPokeCommConnection.genGUI()>ConCateTree | :3 oh no!, something exploded when clicked!\n\tmost likely caused by a DefaultMutableTreeNode that did not have its object declared as a [custom_doTheThingIfNotNull<JPanel>] lambda");
			e1.printStackTrace();
		}
	}

	private void getAsRow(JComponent ...comp) {
		
	}
}