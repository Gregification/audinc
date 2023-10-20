package presentables.presents.serialPoke;

import java.util.EnumSet;
import java.util.Map;

import com.fazecast.jSerialComm.SerialPort;

//var set = Set.of(setting.StopBitOptions.getEnumConstants());
public class SPCSettings {
	public final static EnumSet<SPCSetting> howSwappableSettings = SPCSetting.keepHotSwappables(EnumSet.allOf(SPCSetting.class)); 
	public final SerialPort serialPort;
	public Map<SPCSetting, Object> settings;
	
	public static SPCSettings getSettings(SerialPort sp) 	{ return new SPCSettings(sp); }
	
	public SPCSettings(SerialPort sp) {
		this.serialPort = sp;
	}
	
	public Object getSetting(SPCSetting setting) {
		return SPCSettings.fetchSetting(setting, serialPort);
	}
	
	public void setSetting(SPCSetting setting, SerialPort sp, Object value) {
		assert value.getClass().isInstance(setting.clas) : "trying to assign mismatched objects";
	}
	
	public static Object fetchSetting(SPCSetting setting, SerialPort sp) {		
		var v = Enum.class;
		
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