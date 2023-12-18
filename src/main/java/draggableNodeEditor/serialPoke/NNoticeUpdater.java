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
	
	public NodeConsumer<Object> c_logger 			= new NodeConsumer<>(
				Object.class,
				"logger",
				null,
				(o, n) -> {onInput(n); return n;}
			);
	public NodeConsumer<Color>  c_forgroundColor	= new NodeConsumer<>(
				Color.class,
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
		super.initGUI();
		
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
	
	public void onInput(Object obj) {
		System.out.println("NNoticeUpdator logging w/"+c_forgroundColor.getValue()+" : "+ obj.toString());
		this.context.setNoticeText(obj.toString(), c_forgroundColor.getValue());
	}
}
