package draggableNodeEditor;

import java.awt.Graphics;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JComponent;

public class NodeConnectionPanel extends JComponent{
	private static final long serialVersionUID = 80412434145358352L;
	
	public List<NodeConnection<?>> connections = List.of();
	public ReentrantLock connectionLock = new ReentrantLock(true);
	
	public NodeConnectionPanel() {
		this.setOpaque(true);
	}
	
	public void addConnection(NodeConnection<?> conn) {
		connectionLock.lock();
		connections.add(conn);
		connectionLock.unlock();
	}
	
	public void removeConnection(NodeConnection<?> conn) {
		connectionLock.lock();
		connections.remove(conn);
		connectionLock.unlock();
	}
	
	public List<NodeConnection<?>> removeLooseConnections(){
		List<NodeConnection<?>> looseConns = List.of();
		
		for(var conn : this.connections) {
			boolean isLoose = true;
			for(var tp : conn) {
				if(tp.targetComponent != null) {
					isLoose = false;
					break;
				}
			}
			
			if(isLoose) {
				looseConns.add(conn);
			}
		}
		
		this.connections.remove(looseConns);
		
		return looseConns;
	}
	
	@Override public void paint(Graphics g) {
		super.paint(g);
		
		if(connections != null)
			for(var conn : this.connections)
				conn.paint(g);
	}
}
