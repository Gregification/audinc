package presentables.presents.serialPoke;

import java.util.EnumSet;

public enum SPCSetting {
		//(String:title, Class:object type, Boolean:allowCustomValue, Boolean:isHowSwappable, String:description, Object[]:choosableValues)
		SYSTEM_PORT_NAME			("port name", 
									String.class, false, 	"port name as defined by the system"),
		SYSTEM_PORT_PATH 			("port path",
									String.class, false, 	"port path as defined by the system"),
		SYSTEM_PORT_LOCATION		("port location",
									String.class, false, 	"port location as defined by the system"),
		DESCRIPTIVE_PORT_NAME		(String.class ,	false,		"port name as defined by this program"),
		PORT_DESCRIPTION			(Integer.class, false, 		""),
		PORT_LOCATION				(Integer.class, false, 		""),
		BAUD_RATE					(Integer.class, true ,		"baud rate for communication of this clients connection"),
		DEVICE_WRITE_BUFFER_SIZE	(Integer.class, false, 		"write size (bytes) for this device"),
		DEVICE_READ_BUFFER_SIZE		(Integer.class, false,		"read size (bytes) of the this devices buffer"),
		VENDOR_ID					(Integer.class, false,		""),
		DATA_BITS_PER_WORD			(Integer.class, false,		""),
		NUM_STOP_BITS				(String.class ,	false,		"", (Object[])(stopbitOptions.values())),
		PARITY						(Integer.class, false,		"", (Object[])(parityOptions.values())),
		TIMEOUT_READ				(Integer.class, true ,		"time out in miliseconds before a packet is considered lost"),
		TIMEOUT_WRITE				(Integer.class, true ,		"time out in miliseconds before a write attempt is givenup"),
		FLOWCONTROL_DATA_SET_READY_ENABLED		("DSR enabled",
									Boolean.class, true , false, "Data Set Ready"),
		FLOWCONTROL_DATA_TERMINAL_READY_ENABLED 	("DTR enabled",
									Boolean.class, true , false,"Data Terminal/Transmission Ready"),
		FLOWCONTROL_XIN_ONOFF_ENABLED	("IN XonXoff enabled",
									Boolean.class, true , false,"Input buffer flag signals"),
		FLOWCONTROL_XOUT_ONOFF_ENABLED	("OUT XonXoff enabled",
									Boolean.class, true , false,"Output buffer flag signals"),
		FLOWCONTROL_REQUEST_TO_SEND_ENABLED		("RTS enabled",
									Boolean.class, true , false,"Request To Send"),
		FLOWCONTROL_CLEAR_TO_SEND_ENABLED		("CTS enabled",
									Boolean.class, true , false,"Clear To Send")	
	;
	
	public final Class<? extends Enum> 
		StopBitOptions 	= stopbitOptions.class,
		ParityOptions	= parityOptions.class;
	
	public String
		title				= "default title",
		description			= "default description";
	public Object[] choosableValues;
	public Boolean 
		allowCustomValues	= false,
		isHotSwappable		= false;
	
	public Class<? extends Object> clas;
	
	
	enum stopbitOptions{
		ONE_POINT_FIVE_STOP_BITS	("1.5 stop bits"),
		ONE_STOP_BIT				("1 stop bit"),
		TWO_STOP_BITS				("2 stop bits")
		;
		
		public String title;
		
		private stopbitOptions(String title) 	{ this.title = title; }		
		@Override public String toString() 		{ return title; }
	}
	
	enum parityOptions{
			EVEN_PARITY		("even parity"),
			MARK_PARITY		("mark parity"),
			NO_PARITY		("no parity"),
			ODD_PARITY		("odd parity"),
			SPACE_PARITY	("space parity"),
		;
		
		public String title;
		
		private parityOptions(String title) 	{ this.title = title; }		
		@Override public String toString() 		{ return title; }
	}
	
	private SPCSetting(
				String title,
				Class<? extends Object> clas,
				Boolean allowCustomValues,
				Boolean isHotSwappable,
				String description,
				Object[] choosableValues
			) {
		this(title, clas, allowCustomValues, description, choosableValues);
		this.isHotSwappable 	= isHotSwappable;
	}
	private SPCSetting(
			String title,
			Class<? extends Object> clas,
			Boolean allowCustomValues,
			Boolean isHotSwappable,
			String description
		) {
	this(title, clas, allowCustomValues, isHotSwappable, description, (Object[])(null));
}
	
	
	private SPCSetting(
			String title,
			Class<? extends Object> clas,
			Boolean allowCustomValues,
			String description,
			Object[] choosableValues
		) {
	this.title 				= title;
	this.clas 				= clas;
	this.allowCustomValues	= allowCustomValues;
	this.description 		= description;
	this.choosableValues 	= choosableValues;
}
	
	private SPCSetting(Class<? extends Object> clas, Boolean chooseableValues, String description, Object[] choosableValues) {
		this("", clas, chooseableValues, description, choosableValues);
		this.title = this.name().toLowerCase().replaceAll("_", " ");
	}
	private SPCSetting(Class<? extends Object> clas, Boolean chooseableValues, String description) {
		this("", clas, chooseableValues, description, (Object[])null);
		this.title = this.name().toLowerCase().replaceAll("_", " ");
	}
	private SPCSetting(String title, Class<? extends Object> clas, Boolean chooseableValues, String description) {
		this(title, clas, chooseableValues, description, (Object[])null);
	}
	
	public static EnumSet<SPCSetting> keepHotSwappables(EnumSet<SPCSetting> set){
		for(var v : values())
			if(!v.isHotSwappable) 
				set.remove(v);		
		return set;
	}
}

/*
 * c&p
 * 
 * SPCSettings
SYSTEM_PORT_NAME
SYSTEM_PORT_PATH
SYSTEM_PORT_LOCATION
DESCRIPTIVE_PORT_NAME
PORT_DESCRIPTION
PORT_LOCATION
BAUD_RATE
DEVICE_WRITE_BUFFER_SIZE
DEVICE_READ_BUFFER_SIZE
VENDOR_ID
DATA_BITS_PER_WORD
NUM_STOP_BITS
PARITY
TIMEOUT_READ
TIMEOUT_WRITE
FLOWCONTROL_DATA_SET_READY_ENABLED
FLOWCONTROL_DATA_TERMINAL_READY_ENABLED
FLOWCONTROL_XIN_ONOFF_ENABLED
FLOWCONTROL_XOUT_ONOFF_ENABLED
FLOWCONTROL_REQUEST_TO_SEND_ENABLED
FLOWCONTROL_CLEAR_TO_SEND_ENABLED

 */
