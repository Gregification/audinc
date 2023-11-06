package presentables.presents;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout
;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpringLayout;
import javax.swing.table.TableModel;

import com.fazecast.jSerialComm.SerialPort;

import audinc.gui.MainWin;
import presentables.Presentable;
import presentables.presents.serialPoke.SerialPokeCommConnection;

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
		//clear table
		commTable_model.setRowCount(0);
		
		//repopulate table
		for(var v : SerialPort.getCommPorts())
			commTable_model.addRow(new Object[] {v.toString()});

		if(commTable_model.getRowCount() == 0)
			commTable_model.addRow(new Object[] {"no connections found", -1});
	}
	
	private void onNewEditorSelect(JTabbedPane tabbedPane) {
		String title = " " + editorTab_portDescriptor_txt.getText();	//why leading ' ' ? see -> (tldr: is windows reserved file name) -> https://discord.com/channels/648956210850299986/1169668074413232238
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
			+ "<body> "
			+ "<b>intended for RS232</b><br>"
			+ "A way to jimmy seral connections into working. please use something like PuTTY for actual communicaitons.<br>"
			+ "possiable use cases<br>"
			+ "<ul><li>force send signal</li>"
			+ "<li>see live port events</li>"
			+ "<li>reroute ports(wip)</li>"
			+ "</ul>"
			+ "<b>disclaimers!</b>"
			+ "<ul>"
			+ "<li>the ability to multiplex ports is dependent on host machine</li>"
			+ "<li>the com port detector is terriable, some ports may appear under a different name, example: a vitrual COM3 connection may appear as \"vitural port\" but the connection name would be \"COM3\". you have to figure that out yourself</li>"
			+ "<li>if connecting to serial over bluetooth port connection may timeout. try try again</li>"
			+ "</ul"
			+ "<br>Serial Poke? Cereal Pike!"
			+ "</body>"; }
	
}
