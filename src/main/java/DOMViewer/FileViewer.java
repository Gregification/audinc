package DOMViewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class FileViewer extends JTabbedPane{
	protected DOMParser parser;
	private JPanel metaTab;
	
	public FileViewer(DOMParser parser) {
		super();
		setParser(parser);
		initGUI();
	}
	
	private void initGUI() {
		metaTab = new JPanel(new GridBagLayout());
			System.out.println("FileViewer>initGUI, background; metapanel:PINK");
			metaTab.setBackground(Color.PINK);
		
		var thisPanel = new JPanel();
		var parserPanel = new JPanel();
		
		var c = new GridBagConstraints();
			c.weightx = c.weighty = 1.0;
			c.gridy = 0;
			c.fill = GridBagConstraints.BOTH;
			
		metaTab.add(parserPanel, c);
		
			c.gridy = 1;
		metaTab.add(thisPanel,	 c);
		
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
}
