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
	
//	static {
//		stdLayout = new GridBagLayout();
//	}
	
	
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
		
		initGUI();	
	}
	
	@Override public String getTitle() { return title + " ("+ index+")"; }

	@Override public void initGUI() {
		for(var v : List.of(c_logger, c_forgroundColor)) {
			v.setBorder(null);
			add(v);
		}
	}

	@Override public void initNode() {
		for(var v : List.of(c_logger, c_forgroundColor)) {
			genConnectionPoint(v);
		}
	}

	@Override public JComponent getInspector() {
		return null;
	}
}
