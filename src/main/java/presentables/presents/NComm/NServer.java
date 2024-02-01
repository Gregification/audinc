package presentables.presents.NComm;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * just enough stuff to make the server go, no fancy stuff.
 * server only hosted on one port
 */
public class NServer implements Runnable {
	//the suggested default listening port
	public static final int defaultListeningPort = 39633;
	
	public final long 
		creationTime 	= System.currentTimeMillis(),
		Id				= creationTime ^ this.hashCode(); //good enough
	
	ServerSocketChannel serverChannel;
	Selector selector;
	
	ArrayList<Consumer<NEvent>> 		AnyEventListeners = new ArrayList<>(4); 
	ArrayList<Consumer<NClientEvent>> 	ClientEventListeners = new ArrayList<>(4); 
	ArrayList<Consumer<NServerEvent>> 	ServerEventListeners = new ArrayList<>(4); 
	
	ExecutorService handlerService = Executors.newCachedThreadPool();
	
	public NServer() {
	}
	
	/**
	 * 
	 * @param endPoint : end point for this server 
	 * @throws IOException
	 */
	public void hostOn(ProtocolFamily protocol, SocketAddress endPoint) throws IOException, SecurityException, IllegalArgumentException {		
		close();

		serverChannel = switch(protocol) { //do something something fancy for network limits
			case null -> ServerSocketChannel.open();
			default -> ServerSocketChannel.open(protocol);
		};
		
		serverChannel.configureBlocking(false);
		
		assert endPoint != null : "bad point, check filter?";
		serverChannel.socket().bind(endPoint);
		
		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	public void close() {
		if(serverChannel != null)
			try {
				serverChannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(selector != null)
			try {
				selector.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			try {
				selector.select(200);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

		    while (keys.hasNext()) {
		        SelectionKey key = keys.next();
		        // remove the key so that we don't process this OPERATION again.
		        keys.remove();

		        // key could be invalid if for example, the client closed the connection.
		        if (!key.isValid()) {
		            continue;
		        }
		        
		        if (key.isAcceptable()) {
		            System.out.println("Accepting connection");
		            
		        }
		        
		        if (key.isWritable()) {
		            System.out.println("Writing...");
		            
		        }
		        
		        if (key.isReadable()) {
		            System.out.println("Reading connection");
		            
		        }
		    }
		}
	}
	
	public synchronized void addNewClient(Socket client) {
		
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
	
	public void quit() {
		close();
	}
}