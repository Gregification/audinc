package draggableNodeEditor.serialPoke;

import java.awt.Color;
import java.util.List;

import javax.swing.JComponent;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.NodeComponent;
import draggableNodeEditor.NodeConsumer;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NNoticeUpdater extends DraggableNode<SerialPokeCommConnection> {
	private static final long serialVersionUID = 1L;
	
	public static final String title = "Notice Updator";
	public NodeConsumer<Object> logger 			= new NodeConsumer<>(
				"logger",
				null
			);
	public NodeConsumer<Color>  forgroundColor	= new NodeConsumer<>(
				"forground color",
				Color.black, 
				(o, n) -> n == null ? Color.black : n
			);

	public NNoticeUpdater(SerialPokeCommConnection context) {
		super(context);
		System.out.println("context?!?! -> " + context);
	}
	
	@Override public String getTitle() { return title; }

	@Override public void initGUI() {
		
	}

	@Override public void initNode() {
		
	}

	@Override public List<NodeComponent> getNodeComponents() {
		return null;
	}

	@Override public JComponent getInspector() {
		return null;
	}
	
}
