package presentables.presents.NConn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * just enough stuff to make the server go, no fancy stuff
 */
public class NServer implements Runnable {
	//ports that the server will TRY to appear on
	public static final int[] defaultListeningPorts = { 59855, 39633, 19015};
	
	public final long 
		creationTime 	= System.currentTimeMillis(),
		Id				= creationTime ^ this.hashCode(); //good enough
	
	ServerSocket serverSocket;
	ArrayList<Socket> clients = new ArrayList<>(2);
	
	ArrayList<Consumer<NEvent>> 		AnyEventListeners = new ArrayList<>(4); 
	ArrayList<Consumer<NClientEvent>> 	ClientEventListeners = new ArrayList<>(4); 
	ArrayList<Consumer<NServerEvent>> 	ServerEventListeners = new ArrayList<>(4); 
	
	ExecutorService handlerService = Executors.newVirtualThreadPerTaskExecutor();
	
	public NServer(int port) throws IOException {
		assert port > 0 && port <= 65535;
		
		serverSocket = new ServerSocket(port);
	}
	
	@Override public void run() {
		
	}
	
	public void registerAnyEventListener(Consumer<NEvent> soyjack) 			{ AnyEventListeners.add(soyjack); }
	public void registerClientEventListener(Consumer<NClientEvent> soyjack) { ClientEventListeners.add(soyjack); }
	public void registerServerEventListener(Consumer<NServerEvent> soyjack) { ServerEventListeners.add(soyjack); }
	
	public boolean unregisterListener(Consumer<? extends NEvent> sj) {
		return
				AnyEventListeners	.remove(sj) ||
				ClientEventListeners.remove(sj) ||
				ServerEventListeners.remove(sj) 
		;
	}
	
	public void sendBlockingly(NClientEvent evnt) {
		
	}
	
	public void sendNonBlockingly() {
		
	}
	
	public void quit() throws IOException {
		serverSocket.close();
	}
}