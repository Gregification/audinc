package draggableNodeEditor.serialPoke;

import javax.swing.JComponent;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.DraggableNodeEditor;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NepSPDataListener extends DraggableNode<SerialPokeCommConnection> {
	private static final long serialVersionUID = -3194482443889654939L;

	public NepSPDataListener(SerialPokeCommConnection context) {
		super(context);
		
		initGUI();
	}

	@Override public void initNode(DraggableNodeEditor editor) { }

	@Override public String getTitle() {
		return "entry point : Serial Port data listener";
	}

	@Override public JComponent getInspector() {
		return super.getInspector();
	}
}
