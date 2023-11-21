package draggableNodeEditor.serialPoke;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JComponent;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.NodeComponent;
import draggableNodeEditor.NodeConsumer;
import presentables.Presentable;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NNoticeUpdater extends DraggableNode<SerialPokeCommConnection> {
	private static final long serialVersionUID = 1L;
	
	public static final String title = "Notice Updator";
	
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
		
		this.setLayout(new GridBagLayout());
		int i = 0;
		for(var v : this.getNodeComponents()) {
			var c = Presentable.createGbc(0, i++);
			c.anchor = GridBagConstraints.WEST;	//dosen't seem to do anything?
			this.add(v, c);
		}
	}
	
	@Override public String getTitle() { return title + " ("+ index+")"; }

	@Override public void initGUI() {
		
	}

	@Override public void initNode() {
		
	}

	@Override public List<NodeComponent> getNodeComponents() {
		return List.of(c_logger, c_forgroundColor);
	}

	@Override public JComponent getInspector() {
		return null;
	}
	
}
