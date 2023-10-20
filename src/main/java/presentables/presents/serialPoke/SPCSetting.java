package presentables.presents.serialPoke;

public enum SPCSetting {
		SYSTEM_PORT_NAME			("port name", 
										String.class, false, 	"port name as defined by the system"),
		SYSTEM_PORT_PATH 			("port path",
										String.class, false, 	"port path as defined by the system"),
		SYSTEM_PORT_LOCATION		("port location",
										String.class, false, 	"port location as defined by the system"),
		DESCRIPTIVE_PORT_NAME		(String.class, false,		"port name as defined by this program"),
		PORT_DESCRIPTION			(Integer.class, false, 		""),
		PORT_LOCATION				(Integer.class, false, 		""),
		BAUD_RATE					(Integer.class, true,		"baud rate for communication of this clients connection"),
		DEVICE_WRITE_BUFFER_SIZE	(Integer.class, false, 		"write size (bytes) for this device"),
		DEVICE_READ_BUFFER_SIZE		(Integer.class, false,		"read size (bytes) of the this devices buffer"),
		VENDOR_ID					(Integer.class, false,		""),
		DATA_BITS_PER_WORD			(Integer.class, false,		""),
		NUM_STOP_BITS				(Float.class, false,		"", (Object[])(new Float[] {1.5f,1f,2f})),
		PARITY						(Integer.class, false,		""),
	;
	
	public String
		title,
		description;
	public Object[] choosableValues;
	public Boolean 
		allowCustomValues,
		hotSwappable;
	public Class<? extends Object> clas;
	
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
}
