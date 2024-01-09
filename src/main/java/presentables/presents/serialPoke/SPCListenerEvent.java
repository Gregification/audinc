package presentables.presents.serialPoke;

import com.fazecast.jSerialComm.SerialPort;

/**
 * a abstraction for the static listener event constants as defined by the SerialPort.class 
 */
public enum SPCListenerEvent {
	ALL_EVENTS 				("","", Integer.MAX_VALUE),
	DATA_AVAILABLE			("","",SerialPort.LISTENING_EVENT_DATA_AVAILABLE),
	DATA_RECEIVED			("","",SerialPort.LISTENING_EVENT_DATA_RECEIVED),
	DATA_WRITTEN 			("","",SerialPort.LISTENING_EVENT_DATA_WRITTEN),
	PORT_DISCONNECTED 		("","",SerialPort.LISTENING_EVENT_PORT_DISCONNECTED),
	BREAK_INTERRUPT 		("","",SerialPort.LISTENING_EVENT_BREAK_INTERRUPT),
	CTS 					("clear to send", "", SerialPort.LISTENING_EVENT_CTS),
	DSR 					("data set ready","",SerialPort.LISTENING_EVENT_DSR),
	RING_INDICATOR 			("", """ 
									This is an input for DTE devices and an output for DCE devices.
						This signals the DTE device that there is an incoming call.
						This signal is maintained 'Off' at all times except when the 
						DCE receives a ringing signal.
								""",SerialPort.LISTENING_EVENT_RING_INDICATOR),
	FRAMING_ERROR 			("","you are opening the port while the remote device is in the middle of transmitting.",
									SerialPort.LISTENING_EVENT_FRAMING_ERROR),
	FIRMWARE_OVERRUN_ERROR 	("firmware overrun","device driver buffer overrun",SerialPort.LISTENING_EVENT_FIRMWARE_OVERRUN_ERROR),
	SOFTWARE_OVERRUN_ERROR	("sodtware overrun","application buffer overrun",SerialPort.LISTENING_EVENT_SOFTWARE_OVERRUN_ERROR),
	PARITY_ERROR 			("","",SerialPort.LISTENING_EVENT_PARITY_ERROR)
	;
	
	/** user friendly name
	 */
	public final String displayName;
	 
	/** the id is the event id as found by {@code SerialPort.LISTENING_EVENT_ ...} followed by the name of this enum 
	 */
	public final int eventID;
	
	private SPCListenerEvent(String displayName, String description, int listeningEventID) {
		if(displayName == null || displayName.isBlank())	displayName = name().toLowerCase().replaceAll("_", " ");
		
		this.displayName 	= displayName;
		this.eventID		= listeningEventID;
	}
	
	@Override public String toString() { 
		return displayName;
	}
}
