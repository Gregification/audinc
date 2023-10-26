package DOMViewer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import FileMetadata.BasicFileAttribute;
import presentables.Presentable;

public class FileViewer extends JTabbedPane{
	protected DOMParser parser;
	private JPanel metaTab, thisPanel, parserPanel;
	
	public FileViewer(DOMParser parser) {
		super();
		setParser(parser);
		initGUI();
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
		updateMeta(parser.getPath());
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
			
		metaTab.add(
				new JSplitPane(SwingConstants.HORIZONTAL,
						new JScrollPane(parserPanel,
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
						new JScrollPane(thisPanel,
								JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
								JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)),
				c);
		
		addTab("Meta", metaTab);
	}
	
	public void updateMeta(Path path) {
		thisPanel.removeAll();	
		
		try {
			BasicFileAttributes FileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
			
			int x = 0, y = 1;
				
			for(var attr : BasicFileAttribute.values()) {
				var c = Presentable.createGbc(x, y);
				
				var label = new JLabel(attr.getTitle() + " :");
					label.setToolTipText(attr.getDescription());
				var display = new JLabel();
					display.setText(attr.fetch(FileAttrs).toString());
					
				thisPanel.add(label, 	Presentable.createGbc(x, y));
				thisPanel.add(display,	Presentable.createGbc(x+1, y));
				y++;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		thisPanel.validate();
	}
}
