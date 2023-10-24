package DOMViewer;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class FileViewer extends JTabbedPane{
	protected DOMParser parser;
	
	public FileViewer(DOMParser parser) {
		super();
		setParser(parser);
		initGUI();
	}
	
	private void initGUI() {
		var metaPanel = new JPanel();
			metaPanel.setBackground(Color.PINK);
		var temp = new JTextArea("parser: " + (parser == null ? "null" : parser));
		metaPanel.add(temp);
		
		this.addTab("Meta", metaPanel);
		this.setMnemonicAt(0, KeyEvent.VK_0);
	}

	public DOMParser getParser() {
		return parser;
	}

	public void setParser(DOMParser parser) {
		if(parser != null)
			for(int i = this.getTabCount() - 1; i > 0; i--)
				this.removeTabAt(1);
			
		this.parser = parser;
		
		if(parser == null) return;
		
		parser.ParseFile();
		var tabs = parser.getUITabbs();
		for(var v : tabs.keySet())
			addTab(v.toString(), (JComponent)tabs.get(parser));
	}
}
