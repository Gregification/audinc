package presentables.presents.NComm;

import java.io.IOException;
import java.net.Socket;

/**
 * 
 */
public class NClient{
	Socket socket = null;
	
	
	public NClient() {
		
	}
	
	
	public void endConneciton() throws IOException {
		if(socket != null) socket.close();
	}
	
	public NClientEvent makeEvent(Object data) {
		var event = new NClientEvent(this);
			event.data = data;
		return event;
	}
}
