package presentables.presents.serialPoke;

import java.util.EnumSet;

import com.fazecast.jSerialComm.SerialPort;

/*
 * if adding a class object type that is formerly unlisted make sure to include it in the file related constructors of [SPCSettings.java]
 * - enums must use their own class! 
 * 
 * questionable enum system but too muchwork to change it. if your reading this and feel like it then go for it
 */
public enum SPCSetting {
		//(String:title, Class:object type, Boolean:allowCustomValue, Boolean:isHowSwappable, String:description, Object[]:choosableValues)
		//PROTOCALL					(String.class, false, false, (Object[])(protocallOptions.values())), //unable to tell if rs485 is being used ???
		SYSTEM_PORT_NAME			("port name", 
									 String.class, 	false, 		"port name as defined by the system"),
		SYSTEM_PORT_PATH 			("port path",
									 String.class, 	false, 		"port path as defined by the system"),
		SYSTEM_PORT_LOCATION		("port location",
									 String.class, 	false, 		"port location as defined by the system"),
		DESCRIPTIVE_PORT_NAME		(String.class ,	false,		"port name as defined by this program"),
		PORT_DESCRIPTION			(String.class, 	false, 		""),
		PORT_LOCATION				(String.class,	false, 		""),
		PROTOCALL					(protocallOptions.class, false, "", (Object[])(protocallOptions.values())),
		BAUD_RATE					(Integer.class, true ,		"baud rate for communication of this clients connection"),
		DEVICE_WRITE_BUFFER_SIZE	(Integer.class, false, 		"write size (bytes) for this device"),
		DEVICE_READ_BUFFER_SIZE		(Integer.class, false,		"read size (bytes) of the this devices buffer"),
		VENDOR_ID					(String.class, 	false,		""),
		DATA_BITS_PER_WORD			(Integer.class, false,		""),
		NUM_STOP_BITS				(stopbitOptions.class,	false,		"", (Object[])(stopbitOptions.values())),
		PARITY						(parityOptions.class , 	false,		"", (Object[])(parityOptions.values())),
		TIMEOUT_READ				(Integer.class, true ,		"time out in miliseconds before a packet is considered lost"),
		TIMEOUT_WRITE				(Integer.class, true ,		"time out in miliseconds before a write attempt is givenup. not that time based write timeouts are only avaliable on windows system and will have no effect otherwise"),
		TIMEOUT						(timeoutOptions.class , 	false,		"", (Object[])(timeoutOptions.values())),
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
	
	public static final protocallOptions defaultProtocol = protocallOptions.RS232;
	
	public String
		title				= "default title",
		description			= "default description";
	public Object[] choosableValues;
	public Boolean 
		allowCustomValues	= false,
		isHotSwappable		= false;
	
	public Class<? extends Object> clas;
	
	public enum protocallOptions{		//j-serial? j-serial-retardation, why can we not tell what protocol is being used(or is it just hidden somewhere)??? there;s a enable rj485 option but no way to check if its in use????  
			RS232	("RS232"),
			RS485	("RS485")
		;
		
		public String title;
		
		private protocallOptions(String title) 	{ this.title = title; }		
		@Override public String toString() 		{ return title; }
	}
	
	public enum stopbitOptions{
			ONE_POINT_FIVE_STOP_BITS	(SerialPort.ONE_POINT_FIVE_STOP_BITS, 	"1.5 stop bits"),
			ONE_STOP_BIT				(SerialPort.ONE_STOP_BIT, 				"1 stop bit"),
			TWO_STOP_BITS				(SerialPort.TWO_STOP_BITS, 				"2 stop bits")
		;
		
		public String title;
		public int SerialPortMask;
		
		private stopbitOptions(int spm, String title) 	{ this.title = title; this.SerialPortMask = spm;}		
		@Override public String toString() 		{ return title; }
		
		public static stopbitOptions getCorresponding(int mask) {
			for(var v : stopbitOptions.values())
				if((v.SerialPortMask & mask) != 0) return v;
			return  null;
		}
	}
	
	public enum parityOptions{
			EVEN_PARITY		(SerialPort.EVEN_PARITY, 	"even parity"),
			MARK_PARITY		(SerialPort.MARK_PARITY, 	"mark parity"),
			NO_PARITY		(SerialPort.NO_PARITY, 		"no parity"),
			ODD_PARITY		(SerialPort.ODD_PARITY, 	"odd parity"),
			SPACE_PARITY	(SerialPort.SPACE_PARITY, 	"space parity"),
		;
		
		public String title;
		public int SerialPortMask;
		
		private parityOptions(int spm, String title) 	{ this.title = title; this.SerialPortMask = spm;}		
		@Override public String toString() 		{ return title; }
		public static parityOptions getCorresponding(int mask) {
			for(var v : parityOptions.values())
				if((v.SerialPortMask & mask) != 0) return v;
			return  null;
		}
	}
	
	public enum timeoutOptions{
			NONBLOCKING			(SerialPort.TIMEOUT_NONBLOCKING),
			READ_BLOCKING		(SerialPort.TIMEOUT_READ_BLOCKING),
			READ_SEMI_BLOCKING	(SerialPort.TIMEOUT_READ_SEMI_BLOCKING),
			SCANNER 			(SerialPort.TIMEOUT_SCANNER),
			WRITE_BLOCKING		(SerialPort.TIMEOUT_WRITE_BLOCKING),
		;
		
		public static timeoutOptions defaultTimeout = timeoutOptions.NONBLOCKING;
		
		public String title;
		public int SerialPortMask;
		
		private timeoutOptions(int spm, String title) 	{ this.title = title; this.SerialPortMask = spm;}
		private timeoutOptions(int spm) 	{ this(spm, ""); this.title = this.name().toLowerCase().replace('_', ' ');}
		@Override public String toString() 		{ return title; }
		public static timeoutOptions getCorresponding(int mask) {
			for(var v : timeoutOptions.values())
				if((v.SerialPortMask & mask) != 0) return v;
			return  null;
		}
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
	private SPCSetting(Class<? extends Object> clas, Boolean chooseableValues, Boolean isHotSwappable, Object[] choosableValues) {
		this("", clas, chooseableValues, "", choosableValues);
		this.isHotSwappable = isHotSwappable;
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
	
	public static boolean isEditable(SPCSetting setting) {
		return (setting.allowCustomValues || (setting.choosableValues != null && setting.choosableValues.length > 1));
	}
	
	public boolean isEditable() {
		return SPCSetting.isEditable(this);
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
