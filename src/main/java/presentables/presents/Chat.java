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
import presentables.presents.NConn.NServer;

public class Chat extends Presentable{
	private JTable chatsTable;
	private JTextField chatNameInput;
	private List<Integer> serverPorts = Arrays.stream(NServer.defaultListeningPorts).boxed().collect(Collectors.toList());
	
	@Override protected void start()	{
		
	}
	
	@Override protected void init(MainWin mw) 	{ initGUI(mw); }	
	@Override protected void initGUI(MainWin mw){		
		JPanel container = new JPanel(new BorderLayout());
		JTabbedPane mainTp = new JTabbedPane();
		
			//editor tab
			JPanel editorTab = new JPanel(new BorderLayout());
				JButton editorTab_newEditor_btn = new JButton("join chat");
					editorTab_newEditor_btn.addActionListener(event -> onJoinChatGroupClick());
					editorTab_newEditor_btn.setMnemonic(KeyEvent.VK_ENTER);
					editorTab_newEditor_btn.setToolTipText("joins chat if existing, otherwise will host");
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
							
						JLabel label = new JLabel("Chat Name : ");
						chatNameInput = new JTextField("Default", 15);
						chatNameInput.getDocument().addDocumentListener(new DocumentListener() {
							  public void changedUpdate(DocumentEvent e) 	{
								  String text = chatNameInput.getText();
								  
								  if((text.length() == 0) != !editorTab_newEditor_btn.isEnabled()) //System.out.println("flipping enabled to : " + !editorTab_newEditor_btn.isEnabled());
									  editorTab_newEditor_btn.setEnabled(!chatNameInput.isEnabled());
							  }
							  public void removeUpdate(DocumentEvent e) 	{
								  String text = chatNameInput.getText();
								  
								  if(text.length() == 0 && editorTab_newEditor_btn.isEnabled())
									  editorTab_newEditor_btn.setEnabled(false);	
							  }
							  public void insertUpdate(DocumentEvent e) 	{									  
								  if(!editorTab_newEditor_btn.isEnabled())
									  editorTab_newEditor_btn.setEnabled(true);
							  }
							});
						editorTab_newEditor_btn.setEnabled(chatNameInput.getText().length() != 0);
						
						editorTab_portDescriptor.add(refreshTableBtn);
				        editorTab_portDescriptor.add(label);
				        editorTab_portDescriptor.add(chatNameInput);
				 
				        //refresh button at top left
				        layout.putConstraint(SpringLayout.WEST, refreshTableBtn,
			        			3, SpringLayout.WEST, editorTab_portDescriptor);
				        layout.putConstraint(SpringLayout.NORTH, refreshTableBtn,
			        			5, SpringLayout.NORTH, editorTab_portDescriptor);
				        
				        //label at (5,5).
				        layout.putConstraint(SpringLayout.WEST, label,
				        			5, SpringLayout.EAST, refreshTableBtn);
				        layout.putConstraint(SpringLayout.NORTH, label,
				        			5, SpringLayout.NORTH, editorTab_portDescriptor);
				 
				        //test field at (<label's right edge> + 5, 5).
				        layout.putConstraint(SpringLayout.WEST, chatNameInput,
				        			5, SpringLayout.EAST, label);
				        layout.putConstraint(SpringLayout.NORTH, chatNameInput,
				        			5, SpringLayout.NORTH, editorTab_portDescriptor);
				 
				        //Adjust constraints for the content pane. text field assumed to be bottom-most component
				        layout.putConstraint(SpringLayout.EAST, editorTab_portDescriptor,
				        			1, SpringLayout.EAST, chatNameInput);
				        layout.putConstraint(SpringLayout.SOUTH, editorTab_portDescriptor,
				                    5, SpringLayout.SOUTH, chatNameInput);
					}
					
				editorTab.add(editorTab_portDescriptor, BorderLayout.PAGE_START);
				editorTab.add(editorTab_newEditor_btn, BorderLayout.PAGE_END);
				
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
							chatNameInput.setText(chatsTable.getModel().getValueAt(chatsTable.getSelectedRow(), 0).toString());
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
	
	public void onJoinChatGroupClick() {
		
	}
	
	public void updateChatsTable() {
		
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
