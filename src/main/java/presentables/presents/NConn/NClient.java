package presentables.presents.NConn;

public class NClient {
	public NClientEvent makeEvent(Object data) {
		var event = new NClientEvent(this);
			event.data = data;
		return event;
	}
}
