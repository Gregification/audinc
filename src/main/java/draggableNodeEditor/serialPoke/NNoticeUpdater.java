package draggableNodeEditor.serialPoke;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.DraggableNodeEditor;
import draggableNodeEditor.NodeComponent;
import draggableNodeEditor.NodeConsumer;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NNoticeUpdater extends DraggableNode<SerialPokeCommConnection> {
	private static final long serialVersionUID = 1L;
	
	public static final String title = "Notice Updator";
	
	public NodeConsumer<Object> c_logger 			= new NodeConsumer<>(
				Object.class,
				"logger",
				null
			) { private static final long serialVersionUID = 1L;
			@Override public void accept(PropertyChangeEvent t) {
					onInput(t.getNewValue());
			}};
	public NodeConsumer<Color>  c_forgroundColor	= new NodeConsumer<>(
				Color.class,
				"forground color",
				Color.black
			){ private static final long serialVersionUID = 1L;
			@Override public void accept(PropertyChangeEvent t) {}};

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

	@Override public void initNode(DraggableNodeEditor editor) {
		for(var v : List.of(c_logger, c_forgroundColor)) {
			genConnectionPoint(v);
		}
	}

	@Override public JComponent getInspector() {
		return super.getInspector();
	}
	
	public void onInput(Object obj) {
		System.out.println("NNoticeUpdator logging w/"+c_forgroundColor.getValue()+" : "+ obj.toString());
		
		var color = c_forgroundColor.getValue();
		context.setNoticeText(obj.toString(), color == null ? Color.black : color);
	}
}
