package presentables.presents.serialPoke;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.management.RuntimeErrorException;

import com.fazecast.jSerialComm.SerialPort;

/*
 * acts as abstraction API for what ever might be considered a value of the class [com.fazecast.jSerialComm.SerialPort] 
 */

//var set = Set.of(setting.StopBitOptions.getEnumConstants());
public class SPCSettings {
	public final static EnumSet<SPCSetting> HotSwappableSettings 	= SPCSetting.keepHotSwappables(EnumSet.allOf(SPCSetting.class)); 
	public final static List<SPCSetting> 	AvaliableSettings 		= List.of(SPCSetting.values());
	
	public ConcurrentHashMap<SPCSetting, Object> settings			= new ConcurrentHashMap<>(SPCSetting.values().length);
	protected EnumSet<SPCSetting>			modifiedSettings		= EnumSet.noneOf(SPCSetting.class);
	
	public static SPCSettings getSettings(SerialPort sp) 	{
		System.out.println("SPCSettings>constructor>avaliable settings: " + SPCSettings.AvaliableSettings);
		return new SPCSettings(sp); 
	}
	
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
				throw new RuntimeException("SPCSetting>fetchSettings> failed to find setting: " + setting.toString());
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
		rebase(sp);
	}
	
	public SPCSettings(BufferedReader br) throws IOException {
		rebaseFrom(br);
	}
	
	
	public void rebase(SerialPort sp) {
		AvaliableSettings.parallelStream()
			.forEach(setting -> {
				settings.put(setting, getSetting(setting, sp));
			});
		modifiedSettings.clear();
	}
	
	public void applyModified(SerialPort sp) {
		modifiedSettings.parallelStream()
			.forEach(setting -> {
				if(SPCSetting.isEditable(setting) && settings.get(setting) != null)
					SPCSettings.ApplySetting(setting, sp, this.settings.get(setting));
			});
		modifiedSettings.clear();
	}
	
	public void applyAll(SerialPort sp) {
		modifiedSettings = EnumSet.allOf(SPCSetting.class);
		applyModified(sp);
	}
	
	public boolean setSetting(SPCSetting setting, Object value) {
		System.out.println("SPCSetting>changing value of setting: " + setting + " \t to value: " + value);
		
		assert value.getClass().isInstance(setting.clas) : "bruh";//given [value] foes not match the accepted data type
		
		if(value == null || !SPCSetting.isEditable(setting)) {
			System.out.println("SPCSetting>failed to change value, is readonly:" + setting);
			return false;
		}
		
		settings.put(setting, value);
		modifiedSettings.add(setting);
		
		return true;
	}
	
	public Object getSetting(SPCSetting setting, SerialPort sp) {
		return SPCSettings.fetchSetting(setting, sp);
	}
	
///////////////////
//save & load
///////////////////
	public void writeTo(BufferedWriter bw) {
		AvaliableSettings.stream()
			.forEach(setting -> {
				Object value = settings.get(setting);
				var clas = setting.clas;
				
				try {
					bw.write(setting.name());
					bw.newLine();
					
					//using if-else's because I can't figure out how to make a switch of type [Class]
					//not using instance-of because thats not as specific
					if(
							clas == Boolean	.class	||
							clas == String	.class	||
							clas == Integer	.class) {
						bw.write(value.toString());
					}
					
					bw.newLine();
				} catch (IOException e) {
					System.out.println(clas.toGenericString());
					e.printStackTrace(); 
				}
			});
	}
	
	public void rebaseFrom(BufferedReader br) throws IOException {
		int count = this.AvaliableSettings.size();
		String line;
		SPCSetting loadedSetting = null;
		while((line = br.readLine()) != null && count > 0) {
			count--;
			if(loadedSetting == null) {
				loadedSetting = SPCSetting.valueOf(line);
				
				Object value = null;
				var clas = loadedSetting.clas;
				
				assert parseValue_read_buffered.containsKey(clas) : "SPCSetting contains a unknown class. unable to parse";
				
				value = parseValue_read_buffered.get(clas).apply(br);
				
				settings.put(loadedSetting, value);
			}else {
				Object value = null;
				var clas = loadedSetting.clas;
				
				assert parseValue_read_stringers.containsKey(clas) : "SPCSetting contains a unknown class. unable to parse";
				
				value = parseValue_read_stringers.get(clas).apply(line);
					
				settings.put(loadedSetting, value);
			}
		}
	}
	
	
	public final static Map<Class<? extends Object>, Function<String, Object>> parseValue_read_stringers = Map.of(
			Boolean.class	, Boolean::parseBoolean,
			Integer.class	, Integer::parseInt,
			Double.class	, Double::parseDouble
		);
	public final static Map<Class<? extends Object>, Function<BufferedReader, Object>> parseValue_read_buffered = Map.of(
				
		);
	public final static Set<Class<? extends Object>> parseValue_write_stringers = Set.of(
			Boolean.class	,
			Integer.class	,
			Double.class	
		);
	public final static Map<Class<? extends Object>, Function<BufferedReader, Object>> parseValue_write_buffered = Map.of(
				
		);
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