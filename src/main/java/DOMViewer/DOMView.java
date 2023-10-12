package DOMViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import audinc.gui.MainWin;

public class DOMView extends JPanel {
	public JPanel eleView;
	public final JTree domTree;
	public final DefaultMutableTreeNode domTree_root;
	
	private JPopupMenu nodeOptionsPopupMenu;
	
	public DOMView(String defaultName) {
		super(new GridBagLayout());
		
		this.domTree_root = new DefaultMutableTreeNode(defaultName);
		this.domTree = new JTree(this.domTree_root);
		initMouseListener();
		
		initGUI();
	}
	
	
	public void parse(InputStream is) {
		
	}
	
///////////////////
//gui
///////////////////
	public void initGUI() {
		this.eleView 	= new JPanel();
		
		JScrollPane tv_sp = new JScrollPane(domTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tv_sp.setPreferredSize(new Dimension(MainWin.stdDimension.width/5, 30));
		
		JScrollPane ev_sp = new JScrollPane(eleView,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		var c = new GridBagConstraints();
		c.weightx = c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		
		this.setBackground(Color.red);
		this.add(new JSplitPane(SwingConstants.VERTICAL, tv_sp, ev_sp), c);
		
		initNodeOptionsPopupMenu();
	}
	
	public void initNodeOptionsPopupMenu() {
		this.nodeOptionsPopupMenu = new JPopupMenu("popup menu");
		for(var v : treeNodeOption.values()) {
			JMenuItem mi = new JMenuItem(v.toString());
			this.nodeOptionsPopupMenu.add(mi);
			
			mi.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					nodeOptionsPopupMenu_actionEvent(v);
				}});
		}
	}
	
	public void nodeOptionsPopupMenu_actionEvent(treeNodeOption option) {
		switch(option) {
			case DELETE :
				break;
			case PARSE :
				nodeOptions_parseSelf();
				nodeOptions_parseChildren();
				break;
			case PARSE_SELF : 
				nodeOptions_parseSelf();
				break;
			case PARSE_CHILDREN : 
				nodeOptions_parseChildren();
				break;
			case REFRESH : 
				break;
			default :
				System.out.println("DOMViewer/DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption) : forgot to impliment a parsing option");
				break;
		}
	}
	private void nodeOptions_parseSelf() {}
	private void nodeOptions_parseChildren() {}

///////////////////
//MouseListener interface
///////////////////
	private void initMouseListener() {
		 MouseListener ml = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		    	if (SwingUtilities.isRightMouseButton(e)) {
		    		int row = domTree.getClosestRowForLocation(e.getX(), e.getY());
		 	        domTree.setSelectionRow(row);
		    		if(row != -1) {
			             if(e.getClickCount() == 1) {
			            	 nodeOptionsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			             }
			             else if(e.getClickCount() == 2) {
			                 //double click option
			             }
			         }
		 	    }
		     }
		 };
		 domTree.addMouseListener(ml);
	}
}