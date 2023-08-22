package presentables.presents;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import audinc.gui.MainWin;
import presentables.Presentable;

public class SerialPoke extends Presentable{
	private ArrayList<SerialPokeCommConnection> comCons = new ArrayList<>();
	private JTable commTable;
	private DefaultTableModel commTable_model;
	
	@Override protected void start()	{ 	}
	
	@Override protected void init(MainWin mw) 	{
		initGUI(mw);
		onEditorTabSelect();
	}	
	@Override protected void initGUI(MainWin mw){
		JPanel container = new JPanel(new BorderLayout());
			JTabbedPane mainTp = new JTabbedPane();
			
				//editor tab
				JPanel editorTab = new JPanel(new BorderLayout());
					JButton editorTab_newEditor_btn = new JButton("new editor");
					editorTab.add(editorTab_newEditor_btn);
					commTable = new JTable() {
						private static final long serialVersionUID = 1L;//eclipse complains
						public boolean isCellEditable(int row, int column) { return false; }; //disables user editing of table
					};
					commTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					commTable.setCellEditor(null);
					commTable_model = (DefaultTableModel) commTable.getModel();
						commTable_model.addColumn("Path");
					TableRowSorter<TableModel> importT_srcsTable_sorter = new TableRowSorter<>(commTable_model);
						importT_srcsTable_sorter.setSortKeys(Arrays.asList(
									new RowSorter.SortKey(0, SortOrder.ASCENDING)
								));
						commTable.setRowSorter(importT_srcsTable_sorter);
						JScrollPane commTable_scrollFrame = new JScrollPane(commTable,	
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
								commTable_scrollFrame.setAutoscrolls(true);
					editorTab.add(commTable_scrollFrame);
					
						
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


///////////////////
//gui
///////////////////
	private void onEditorTabSelect() {
		//update table
		for(var v : SerialPort.getCommPorts()) {
			System.out.println(v.toString());
			commTable_model.addRow(new Object[] {v.toString()});
		}
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "Serial Mapper";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon(""); }
	public static String getDescription() 	{	return "<html>"
			+ "<body> A way to jimmy seral connections into working. please use something like PuTTY for actual communicaitons.<br>"
			+ "possiable use cases<br>"
			+ "<b>the ability to multiplex ports is dependent on host machine</b>"
			+ "<ul><li>force send signal</li>"
			+ "<li>see live port events</li>"
			+ "<li>reroute ports</li>"
			+ "</ul></body>"; }
	
}

class SerialPokeCommConnection{
	public SerialPort sp;
	
	public JPanel SerialPokeCommConnection(SerialPort sp) {
		this.sp = sp;
		JPanel host = new JPanel(new BorderLayout());
		
		sp.addDataListener(new SerialPortDataListener() {
			@Override public int getListeningEvents() { return 0; }

			@Override public void serialEvent(SerialPortEvent spe) {
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
		
		
		return host;
	}
	
	public void setTabIcon(String src) {
		
	}
}