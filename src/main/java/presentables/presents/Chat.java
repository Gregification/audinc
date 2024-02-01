package presentables.presents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import audinc.gui.MainWin;
import presentables.Presentable;
import presentables.presents.NComm.NServer;
import presentables.presents.NComm.NServerUI;

public class Chat extends Presentable {
	private static final String defaultChatName = "localhost";
	
	private JTable chatsTable;
	private JTextField addressInput, portInput;
	
	@Override protected void start()	{	
	}
	
	@Override protected void init(MainWin mw) 	{ initGUI(mw); }	
	@Override protected void initGUI(MainWin mw){		
		JPanel container = new JPanel(new BorderLayout());
		JTabbedPane mainTp = new JTabbedPane();
		
			//editor tab
			JPanel editorTab = new JPanel(new BorderLayout());
				JButton joinChat_btn = new JButton("join chat");
					joinChat_btn.addActionListener(event -> onJoinChatGroupClick(mainTp));
					joinChat_btn.setMnemonic(KeyEvent.VK_ENTER);
					joinChat_btn.setToolTipText("joins chat if existing, otherwise will host");
				JPanel editorTab_portDescriptor = new JPanel();
					SpringLayout editorTab_portDescriptor_layout = new SpringLayout();
					editorTab_portDescriptor.setLayout(editorTab_portDescriptor_layout);{
						SpringLayout layout = editorTab_portDescriptor_layout;
						
						var refreshTableBtn = new JButton(MainWin.getImageIcon("res/refresh.png", MainWin.stdtabIconSize));
							refreshTableBtn.addActionListener(e -> updateChatsTable());
							refreshTableBtn.setToolTipText("refresh the connections table bellow");
							refreshTableBtn.setBorder(null);
							refreshTableBtn.setOpaque(false);
							refreshTableBtn.setBackground(new Color(0,0,0,0));
							
						JLabel
							addresslabel = new JLabel("address"),
							portlabel = new JLabel("port");
//						InetSocketAddress.createUnresolved
						
						addressInput = new JTextField("localhost", 20);
						portInput = new JTextField(NServer.defaultListeningPort + "", 5);
							portInput.setToolTipText("will attempt connection on these ports. comma seperators allowed.");
							
						addressInput.getDocument().addDocumentListener(new DocumentListener() {
							  public void changedUpdate(DocumentEvent e) 	{
								  String text = addressInput.getText();
								  
								  if((text.length() == 0) != !joinChat_btn.isEnabled())
									  joinChat_btn.setEnabled(!addressInput.isEnabled());
							  }
							  public void removeUpdate(DocumentEvent e) 	{
								  String text = addressInput.getText();
								  
								  if(text.length() == 0 && joinChat_btn.isEnabled())
									  joinChat_btn.setEnabled(false);	
							  }
							  public void insertUpdate(DocumentEvent e) 	{									  
								  if(!joinChat_btn.isEnabled())
									  joinChat_btn.setEnabled(true);
							  }
							});
						joinChat_btn.setEnabled(addressInput.getText().length() != 0);
						
						editorTab_portDescriptor.add(refreshTableBtn);
				        editorTab_portDescriptor.add(addresslabel);
				        editorTab_portDescriptor.add(addressInput);
				        editorTab_portDescriptor.add(portInput);
				        editorTab_portDescriptor.add(portlabel);
				 
				        //refresh button at top left
				        layout.putConstraint(SpringLayout.WEST, refreshTableBtn,
			        			3, SpringLayout.WEST, editorTab_portDescriptor);
				        
				        //address label
				        layout.putConstraint(SpringLayout.WEST, addresslabel,
				        			5, SpringLayout.EAST, refreshTableBtn);
				 
				        //address input
				        layout.putConstraint(SpringLayout.WEST, addressInput,
				        			5, SpringLayout.EAST, addresslabel);
				        
				        //port label
				        layout.putConstraint(SpringLayout.WEST, portlabel,
				        		5, SpringLayout.EAST, addressInput);
				        
				        //port input
				        layout.putConstraint(SpringLayout.WEST, portInput,
				        		5, SpringLayout.EAST, portlabel);
				        
				        //Adjust constraints for the content pane. text field assumed to be bottom-most component
				        layout.putConstraint(SpringLayout.EAST, editorTab_portDescriptor,
				        			1, SpringLayout.EAST, portInput);
				        layout.putConstraint(SpringLayout.SOUTH, editorTab_portDescriptor,
				                    5, SpringLayout.SOUTH, addressInput);
					}
					
				editorTab.add(editorTab_portDescriptor, BorderLayout.PAGE_START);
				editorTab.add(joinChat_btn, BorderLayout.PAGE_END);
				
				chatsTable = new JTable() {
					private static final long serialVersionUID = 1L;//eclipse complains
					public boolean isCellEditable(int row, int column) { return false; }; //disables user editing of table
				};
				chatsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				chatsTable.setCellEditor(null);
				var chatsTModel = (DefaultTableModel) chatsTable.getModel();
					for(var v : new String[] {"name", "users"}) chatsTModel.addColumn(v);
					chatsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
						@Override public void valueChanged(ListSelectionEvent e) {
							addressInput.setText(chatsTable.getModel().getValueAt(chatsTable.getSelectedRow(), 0).toString());
						}
					});
				TableRowSorter<TableModel> importT_srcsTable_sorter = new TableRowSorter<>(chatsTModel);
					importT_srcsTable_sorter.setSortKeys(Arrays.asList(
								new RowSorter.SortKey(0, SortOrder.ASCENDING)
							));
				chatsTable.setRowSorter(importT_srcsTable_sorter);
					JScrollPane commTable_scrollFrame = new JScrollPane(chatsTable,	
							JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							commTable_scrollFrame.setAutoscrolls(true);
				editorTab.add(commTable_scrollFrame, BorderLayout.CENTER);
				
					
		mainTp.addTab("Nav", MainWin.getImageIcon("res/build.png", MainWin.stdtabIconSize), editorTab, "");		
		
		//save/load tab
		
		container.add(mainTp);
		mw.add(container);
	}
	
	public void onJoinChatGroupClick(JTabbedPane tabbedPane) {
		 String title = addressInput.getText();
		 if(title == null || title.isBlank()) title = defaultChatName;
		 
		 NServerUI server = new NServerUI();
	}
	
	public void updateChatsTable() {
		//if server dne, make server
		
		//join server as client
	}
	
	List<Integer> getSelectedPorts() {
		assert portInput != null : "failed to init";
		
		List<Integer> ret =  Arrays.stream(portInput.getText().split(","))
				.map(str -> {
					try {
						return Integer.parseInt(str);
					}catch(NumberFormatException e) {
						return -1;
					}
				})
				.filter(i -> i > NServer.minPort && i <= NServer.maxPort)
				.distinct()
				.collect(Collectors.toList());
		if(!ret.contains(NServer.defaultListeningPort))
			ret.add(NServer.defaultListeningPort);
		
		return ret;
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "chat";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("https://cataas.com/cat"); }
	public static String getDescription() 	{	return "<html>"
			+ "<body>a network chat<br>"
			+ "</body>"; }
	
	@Override public void quit() {}
}
