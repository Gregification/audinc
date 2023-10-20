package presentables.presents.serialPoke;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.fazecast.jSerialComm.SerialPort;

/*
 * acts as abstraction API for what ever might be considered a value of the class [com.fazecast.jSerialComm.SerialPort] 
 */

//var set = Set.of(setting.StopBitOptions.getEnumConstants());
public class SPCSettings {
	public final static EnumSet<SPCSetting> HotSwappableSettings 	= SPCSetting.keepHotSwappables(EnumSet.allOf(SPCSetting.class)); 
	public final static List<SPCSetting> 	AvaliableSettings 		= List.of(SPCSetting.values());
	
	public SerialPort serialPort;
	public ConcurrentHashMap<SPCSetting, Object> settings			= new ConcurrentHashMap<>(SPCSetting.values().length);
	protected EnumSet<SPCSetting>			modifiedSettings		= EnumSet.noneOf(SPCSetting.class);
	
	public static SPCSettings getSettings(SerialPort sp) 	{ return new SPCSettings(sp); }
	
	public static Object fetchSetting(SPCSetting setting, SerialPort sp) {			
		switch(setting) {
			case SYSTEM_PORT_NAME : 						return sp.getSystemPortName();
			case SYSTEM_PORT_PATH : 						return sp.getSystemPortPath();
			case SYSTEM_PORT_LOCATION : 					return sp.getPortLocation();
			case DESCRIPTIVE_PORT_NAME : 					return sp.getDescriptivePortName();
			case PORT_DESCRIPTION : 						return sp.getPortDescription();
			case PORT_LOCATION : 							return sp.getPortLocation();
			case BAUD_RATE : 								return sp.getBaudRate();
			case DEVICE_WRITE_BUFFER_SIZE : 				return sp.getDeviceWriteBufferSize();
			case DEVICE_READ_BUFFER_SIZE : 					return sp.getDeviceReadBufferSize();
			case VENDOR_ID : 								return sp.getVendorID();
			case DATA_BITS_PER_WORD : 						return sp.getNumDataBits();
			case NUM_STOP_BITS : 							return sp.getNumStopBits();
			case PARITY : 									return sp.getParity();
			case TIMEOUT_READ : 							return sp.getReadTimeout();
			case TIMEOUT_WRITE : 							return sp.getWriteTimeout();
			case FLOWCONTROL_DATA_SET_READY_ENABLED :		return (SerialPort.FLOW_CONTROL_DSR_ENABLED 		& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_DATA_TERMINAL_READY_ENABLED : 	return (SerialPort.FLOW_CONTROL_DTR_ENABLED 		& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_XIN_ONOFF_ENABLED : 			return (SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED 	& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_XOUT_ONOFF_ENABLED : 			return (SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED & sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_REQUEST_TO_SEND_ENABLED : 		return (SerialPort.FLOW_CONTROL_RTS_ENABLED 		& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_CLEAR_TO_SEND_ENABLED : 		return (SerialPort.FLOW_CONTROL_CTS_ENABLED 		& sp.getFlowControlSettings()) == 1;

			default:
				System.out.println("SPCSetting>fetchSetting : failed to find setting" + setting.toString());
				return null;
		}
	}
	
	public static boolean ApplySetting(SPCSetting setting, SerialPort sp, Object rawValue) {
		System.out.println("applying setting changes");
		assert rawValue.getClass().isInstance(setting.clas) : "trying to assign mismatched objects";
		
		if(!setting.isEditable() || (sp.isOpen() && !SPCSettings.HotSwappableSettings.contains(setting)))
			return false; 
		
		var value = setting.clas.cast(rawValue);
		
		switch(setting) {
			case SYSTEM_PORT_PATH : 
				break;
			case SYSTEM_PORT_LOCATION : 
				break;
			case DESCRIPTIVE_PORT_NAME :
				break;
			case PORT_DESCRIPTION :
				break;
			case PORT_LOCATION : 
				break;
			case BAUD_RATE : 
				break;
			case DEVICE_WRITE_BUFFER_SIZE : 
				break;
			case DEVICE_READ_BUFFER_SIZE : 
				break;
			case VENDOR_ID : 
				break;
			case DATA_BITS_PER_WORD : 
				break;
			case NUM_STOP_BITS : 
				break;
			case PARITY : 
				break;
			case TIMEOUT_READ : 
				break;
			case TIMEOUT_WRITE : 
				break;
			case FLOWCONTROL_DATA_SET_READY_ENABLED : 
				break;
			case FLOWCONTROL_DATA_TERMINAL_READY_ENABLED : 
				break;
			case FLOWCONTROL_XIN_ONOFF_ENABLED : 
				break;
			case FLOWCONTROL_XOUT_ONOFF_ENABLED : 
				break;
			case FLOWCONTROL_REQUEST_TO_SEND_ENABLED : 
				break;
			case FLOWCONTROL_CLEAR_TO_SEND_ENABLED : 
				break;
	
			default:
				System.out.println("SPCSetting>function : failed to match setting" + setting.toString());
				break;
		}
		
		return true;
	}
	
	public SPCSettings(SerialPort sp) {
		this.serialPort 		= sp;
		
		rebase();
	}
	
	public SPCSettings(BufferedReader is) {
		rebaseFrom(is);
	}
	
	
	
	public void rebase() {
		AvaliableSettings.parallelStream()
			.forEach(setting -> {
				settings.put(setting, getSetting(setting));
			});
		modifiedSettings.clear();
	}
	
	public void apply() {
		AvaliableSettings.parallelStream()
			.forEach(setting -> {
				SPCSettings.ApplySetting(setting, serialPort, this.settings.get(setting));
			});
		modifiedSettings.clear();
	}
	
	public boolean setSetting(SPCSetting setting, Object value) {
		System.out.println("making a setting change");
		
		assert value.getClass().isInstance(setting.clas) : "bruh";//given [value] foes not match the accepted data type
		
		if(value == null || !SPCSetting.isEditable(setting)) return false;
		
		settings.put(setting, value);
		modifiedSettings.add(setting);
		
		return true;
	}
	
	public Object getSetting(SPCSetting setting) {
		return SPCSettings.fetchSetting(setting, serialPort);
	}
	
///////////////////
//save & load
///////////////////
	public void writeTo(BufferedWriter bw) {
		AvaliableSettings.stream()
			.forEach(setting -> {
				Object value = getSetting(setting);
				
				
			});
	}
	
	public void rebaseFrom(BufferedReader br) {
		
	}
}

/*
 switch(setting) {
			case SYSTEM_PORT_NAME : 
				return null;
			case SYSTEM_PORT_PATH : 
				return null;
			case SYSTEM_PORT_LOCATION : 
				return null;
			case DESCRIPTIVE_PORT_NAME : 
				return null;
			case PORT_DESCRIPTION : 
				return null;
			case PORT_LOCATION : 
				return null;
			case BAUD_RATE : 
				return null;
			case DEVICE_WRITE_BUFFER_SIZE : 
				return null;
			case DEVICE_READ_BUFFER_SIZE : 
				return null;
			case VENDOR_ID : 
				return null;
			case DATA_BITS_PER_WORD : 
				return null;
			case NUM_STOP_BITS : 
				return null;
			case PARITY : 
				return null;
			case TIMEOUT_READ : 
				return null;
			case TIMEOUT_WRITE : 
				return null;
			case FLOWCONTROL_DATA_SET_READY_ENABLED : 
				return null;
			case FLOWCONTROL_DATA_TERMINAL_READY_ENABLED : 
				return null;
			case FLOWCONTROL_XIN_ONOFF_ENABLED : 
				return null;
			case FLOWCONTROL_XOUT_ONOFF_ENABLED : 
				return null;
			case FLOWCONTROL_REQUEST_TO_SEND_ENABLED : 
				return null;
			case FLOWCONTROL_CLEAR_TO_SEND_ENABLED : 
				return null;

			default:
				System.out.println("SPCSetting>function : failed to match setting" + setting.toString());
				return null;
		}
 * /
 */