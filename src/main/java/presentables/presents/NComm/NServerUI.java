package presentables.presents.NComm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;

public class NServerUI extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private NServer server = null;
	private List<Consumer<NServer>> UiUpdaters = new ArrayList<>();
	
	public NServerUI() {
		
	}
	
	public void initGUI() {
		
	}
	
	public void refreshDisplay() {
		UiUpdaters.stream().forEach(c -> c.accept(server));
	}
	
	public void setServer(NServer server) {
		this.server = server;
	}
	
	public NServer getServer() {
		return server;
	}
}
