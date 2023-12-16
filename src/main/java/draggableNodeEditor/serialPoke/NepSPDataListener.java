package draggableNodeEditor.serialPoke;

import javax.swing.JPanel;

import draggableNodeEditor.DraggableNode;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NepSPDataListener extends DraggableNode<SerialPokeCommConnection> {
	private static final long serialVersionUID = -3194482443889654939L;

	public NepSPDataListener(SerialPokeCommConnection context) {
		super(context);
		
		initGUI();
	}

	@Override public void initNode() {
		
	}

	@Override public String getTitle() {
		return "entry point : Serial Port data listener";
	}

	@Override public JPanel getInspector() {
		return null;
	}
}
