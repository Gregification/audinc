package draggableNodeEditor.serialPoke;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JComponent;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.NodeComponent;
import draggableNodeEditor.NodeConsumer;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NNoticeUpdater extends DraggableNode<SerialPokeCommConnection> {
	private static final long serialVersionUID = 1L;
	
	public static final String title = "Notice Updator";
	public static final GridBagLayout stdLayout;
	
	static {
		stdLayout = new GridBagLayout();
	}
	
	
	public NodeConsumer<Object> c_logger 			= new NodeConsumer<>(
				"logger",
				null
			);
	public NodeConsumer<Color>  c_forgroundColor	= new NodeConsumer<>(
				"forground color",
				Color.black, 
				(o, n) -> n == null ? Color.black : n
			);

	public NNoticeUpdater(SerialPokeCommConnection context) {
		super(context);
		
		this.setOpaque(true);
		
		this.setLayout(stdLayout);
		int i = 0;
		
		for(var v : List.of(c_logger, c_forgroundColor)) {
			var gbc = new GridBagConstraints();
			    gbc.gridx = 0;
			    gbc.gridy = i++;
	
			    gbc.anchor = GridBagConstraints.WEST;
			
			this.add(v, gbc);
			genConnectionPoint(v);
		}
		
		initGUI();	
	}
	
	@Override public String getTitle() { return title + " ("+ index+")"; }

	@Override public void initGUI() {
		
	}

	@Override public void initNode() {
		
	}

	@Override public JComponent getInspector() {
		return null;
	}
}
