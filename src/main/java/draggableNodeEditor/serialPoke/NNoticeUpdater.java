package draggableNodeEditor.serialPoke;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.JComponent;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.DraggableNodeEditor;
import draggableNodeEditor.NodeConsumer;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public class NNoticeUpdater extends DraggableNode<SerialPokeCommConnection> {
	private static final long serialVersionUID = 1L;
	
	public static final String title = "Notice Updator";
	
	public NodeConsumer<Object> c_logger 			= new NodeConsumer<>(
				Object.class,
				"logger",
				"c_logger initial text"
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
		var color = c_forgroundColor.getValueNow();
		
		System.out.println("NNoticeUpdator > onInput; \n\t>color:"+color+"\n\t>text:"+ obj.toString());
		
		context.setNoticeText(obj.toString(), color == null ? Color.black : color);
	}
}
