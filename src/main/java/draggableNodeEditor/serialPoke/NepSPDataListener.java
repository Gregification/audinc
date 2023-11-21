package draggableNodeEditor.serialPoke;

import java.util.List;

import javax.swing.JPanel;

import com.fazecast.jSerialComm.SerialPort;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.NodeComponent;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NepSPDataListener extends DraggableNode<SerialPokeCommConnection> {

	public NepSPDataListener(SerialPokeCommConnection context) {
		super(context);
	}

	@Override
	public void initGUI() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initNode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<NodeComponent> getNodeComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public String getTitle() {
		return "entry point : Serial Port data listener";
	}

	@Override
	public JPanel getInspector() {
		// TODO Auto-generated method stub
		return null;
	}
}
