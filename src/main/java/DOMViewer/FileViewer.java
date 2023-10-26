package DOMViewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

public class FileViewer extends JTabbedPane{
	protected DOMParser parser;
	private JPanel metaTab, thisPanel, parserPanel;
	
	public FileViewer(DOMParser parser) {
		super();
		setParser(parser);
		initGUI();
	}
	
	private void initGUI() {
		//meta panels
		metaTab = new JPanel(new GridBagLayout());
			System.out.println("FileViewer>initGUI, background; metapanel:PINK");
			metaTab.setBackground(Color.PINK);
		
		var c = new GridBagConstraints();
			c.weightx = c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
		
		thisPanel = new JPanel(new GridBagLayout());
			thisPanel.setBorder(BorderFactory.createTitledBorder(
					   BorderFactory.createEtchedBorder(), "General", TitledBorder.LEFT, TitledBorder.TOP));
			
		parserPanel = new JPanel(new GridBagLayout());
			parserPanel.setBorder(BorderFactory.createTitledBorder(
					   BorderFactory.createEtchedBorder(), "Parser Specific", TitledBorder.LEFT, TitledBorder.TOP));
		
		metaTab.add(new JSplitPane(SwingConstants.HORIZONTAL, parserPanel, thisPanel), c);
//		metaTab.add(parserPanel, c);
//		
//		metaTab.add(thisPanel,	 c);
		
		addTab("Meta", metaTab);
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
	}
	
	public void updateMeta(Path path) {
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
		
			var comps = new JComponent[][] {
				{new JLabel(""), }
			};
			
			thisPanel.removeAll();
			var c = new GridBagConstraints();
				c.weightx = c.weighty = 1.0;
				c.fill = GridBagConstraints.BOTH;
			for(var v : comps) {
				c.gridy++;
				c.gridx = -1;
				for(var a : v) {
					c.gridx++;
					thisPanel.add(a, c);
				}
			}
				
			System.out.println("creationTime: " + attr.creationTime());
			System.out.println("lastAccessTime: " + attr.lastAccessTime());
			System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

			System.out.println("isDirectory: " + attr.isDirectory());
			System.out.println("isOther: " + attr.isOther());
			System.out.println("isRegularFile: " + attr.isRegularFile());
			System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
			System.out.println("size: " + attr.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
