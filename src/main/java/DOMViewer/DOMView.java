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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import audinc.gui.MainWin;

public class DOMView extends JPanel {
	public JPanel eleView;
	public final JTree domTree;
	public final DefaultMutableTreeNode domTree_root;
	
	private JPopupMenu nodeOptionsPopupMenu;
	
	public DOMView() {
		super(new GridBagLayout());
		
		domTree_root = new DefaultMutableTreeNode(MainWin.settingsDir, true);
		
		domTree = new JTree(this.domTree_root);
		initMouseListener();
		
		initGUI();
	}
	
	public DOMView(Object root) {
		super(new GridBagLayout());
		
		domTree_root = new DefaultMutableTreeNode(root, true);
		
		domTree = new JTree(this.domTree_root);
		initMouseListener();
		
		initGUI();
	}
	
	public void parse() {
		
	}
	
	public void setRoot(Object newObj) {
		domTree_root.setUserObject(newObj);
		
		System.out.println("validating");
		
		var v = (DefaultTreeModel)domTree.getModel();
		v.nodeChanged(domTree_root);
	}
	
	
	
///////////////////
//gui
///////////////////	
	protected void nodeOptionsPopupMenu_actionEvent(treeNodeOption option) {
		switch(option) {
			case DELETE :
				nodeOptions_deleteSelected();
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
				nodeOptions_refresh();
				break;
				
			default :
				System.out.println("DOMViewer/DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption) : forgot to impliment a parsing option");
				break;
		}
	}
	private void nodeOptions_deleteSelected() {
		domTree.removeSelectionPaths(domTree.getSelectionPaths());
	}
	private void nodeOptions_refresh() {
		var list = new ArrayList<>(List.of(domTree.getSelectionPaths()));
		 
		/*
		 * only allow BRANCHES that are NOT DECENDENTS of other selected branches.
		 * see diagram for details: https://media.discordapp.net/attachments/1162543720613302293/1162558001211768892/image.png?ex=653c5f82&is=6529ea82&hm=616f0aee041238ed943ab11f215dae8909324701d1815e17129f953892f36dbd&= 
		 * 	- circled 	=> selected paths
		 *  	- red 		=> ignored & filtered out
		 *  	- blue 		=> keep
		 */
		TreePath current, latestParent = list.get(0);
		var itt = list.iterator(); 
			if(itt.hasNext()) itt.next(); //skip first element
			
		while(itt.hasNext()) {
			current = itt.next();
			if(current.isDescendant(latestParent) ||		//if is decendent of another node
				!((DefaultMutableTreeNode)current.getLastPathComponent()).getAllowsChildren()	//if is not a branch
				){
				System.out.println("removing: \t" + current);
				itt.remove();
				continue;
			}
			
			System.out.println("keeping: \t" + current);
			latestParent = current;
		}
		System.out.println("final:");
		list.forEach(System.out::println);
		
	}
	private void nodeOptions_parseSelf() {}
	private void nodeOptions_parseChildren() {}
	
	private void initNodeOptionsPopupMenu() {
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
	
	protected void initGUI() {
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

///////////////////
//MouseListener
///////////////////
	private void initMouseListener() {
		 MouseListener ml = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		    	if (SwingUtilities.isRightMouseButton(e)) {
		    		int row = domTree.getClosestRowForLocation(e.getX(), e.getY());
		 	        domTree.setSelectionRow(row);
		 	        var r = domTree.getPathForRow(row);
		    		if(row != -1) {
			             if(e.getClickCount() == 1) {
			            	 nodeOptionsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			             }
			         }
		 	    }
		     }
		 };
		 domTree.addMouseListener(ml);
	}
}