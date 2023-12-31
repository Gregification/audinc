package draggableNodeEditor.serialPoke;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
				);
	public NodeConsumer<Color>  c_forgroundColor	= new NodeConsumer<>(
				Color.class,
				"forground color",
				Color.black
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
		try {
			this.context.setNoticeText(obj.toString(), c_forgroundColor.getValue().get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
