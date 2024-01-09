package draggableNodeEditor.serialPoke;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import audinc.gui.MainWin;
import draggableNodeEditor.DraggableNodeEditor;
import draggableNodeEditor.NEntryPoint;
import draggableNodeEditor.NodeSupplier;
import presentables.Presentable;
import presentables.presents.serialPoke.SPCListenerEvent;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NepSPDataListener extends NEntryPoint<SerialPokeCommConnection> implements SerialPortDataListener{
	private static final long serialVersionUID = -3194482443889654939L;
	
	private static final SPCListenerEvent[] listenerEventOptions = SPCListenerEvent.values();
	private static final String
				listenerEventType_eventType 	= "event type (int)",
				listenerEventType_serialPort 	= "serial port (SerialPort)",
				listenerEventType_ReceivedBytes = "received bytes (byte[])";
	
	private ArrayList<NodeSupplier<?>> suppliers = new ArrayList<>();
	
	public NepSPDataListener(SerialPokeCommConnection context) {
		super(context);
		
		initGUI();
	}

	@Override public void initNode(DraggableNodeEditor editor) { 
		
	}
	
	@Override public void initGUI() {
		super.initGUI();
		
		JPanel newListenerDialogContent = new JPanel();
			newListenerDialogContent.setLayout(new BoxLayout(newListenerDialogContent, BoxLayout.Y_AXIS));
			JComboBox<SPCListenerEvent> eventOptions = new JComboBox<>();
				eventOptions.setModel(new DefaultComboBoxModel<>(listenerEventOptions));
			JComboBox<String> listenerOptions = new JComboBox<>();
				listenerOptions.setModel(new DefaultComboBoxModel<>(new String[] {listenerEventType_eventType,listenerEventType_serialPort, listenerEventType_ReceivedBytes}));
				
		newListenerDialogContent.add(eventOptions);
		newListenerDialogContent.add(listenerOptions);
				
		JButton newListener = new JButton(MainWin.getImageIcon("res/build.png", MainWin.stdtabIconSize));
			newListener.setAlignmentX(Component.LEFT_ALIGNMENT);
			newListener.addActionListener(e -> {
				
				int result = JOptionPane.showConfirmDialog(null, newListenerDialogContent, 
			    		"new listener", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					addSupplier((String)listenerOptions.getSelectedItem(), (SPCListenerEvent)eventOptions.getSelectedItem());
				}
			});
			
		add(newListener);
	}

	@Override public String getTitle() {
		return "E.P: Serial Port data listener";
	}

	@Override public JComponent getInspector() {
		return super.getInspector();
	}

	@Override public int getListeningEvents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override public void serialEvent(SerialPortEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	private void addSupplier(String listenerType, SPCListenerEvent event) {
		JPanel content = new JPanel();
		
		var deleteBtn = new JButton("-");
		content.add(deleteBtn);
		content.add(new JLabel(event.toString() + " (" + listenerType +  ")"));
		
		add(content);
		
		revalidate();
	}
}
