package presentables.presents.serialPoke;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.fazecast.jSerialComm.SerialPort;

import DOMViewer.DOMParser;
import presentables.presents.serialPoke.SPCSetting.stopbitOptions;
import presentables.presents.serialPoke.SPCSetting.timeoutOptions;

/**
 * acts as abstraction API for what ever might be considered a value of the class <code>com.fazecast.jSerialComm.SerialPort</code>
 * <p> 
 * Because this is intended for GUI use, there is a bit more overhead for many instances.
 */

//var set = Set.of(setting.StopBitOptions.getEnumConstants());
public class SPCSettings {
	public static final String FileExtension_Settings = "spccS";
	public final static EnumSet<SPCSetting> HotSwappableSettings 	= SPCSetting.keepHotSwappables(EnumSet.allOf(SPCSetting.class)); 
	public final static List<SPCSetting> 	AvaliableSettings 		= List.of(SPCSetting.values());
	
	public ConcurrentHashMap<SPCSetting, Object> settings			= new ConcurrentHashMap<>(SPCSetting.values().length);
	protected volatile EnumSet<SPCSetting>			modifiedSettings		= EnumSet.noneOf(SPCSetting.class);	//from either file or port, whichever was last updated
	
	public static SPCSettings getSettings(SerialPort sp) 	{
		var s = new SPCSettings();
		s.rebase(sp);
		return s;
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
			case NUM_STOP_BITS : 							return stopbitOptions.getCorresponding(sp.getNumStopBits());
			case PARITY : 									return sp.getParity();
			case TIMEOUT_READ : 							return sp.getReadTimeout();
			case TIMEOUT_WRITE : 							return sp.getWriteTimeout();
			case FLOWCONTROL_DATA_SET_READY_ENABLED :		return (SerialPort.FLOW_CONTROL_DSR_ENABLED 		& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_DATA_TERMINAL_READY_ENABLED : 	return (SerialPort.FLOW_CONTROL_DTR_ENABLED 		& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_XIN_ONOFF_ENABLED : 			return (SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED 	& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_XOUT_ONOFF_ENABLED : 			return (SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED & sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_REQUEST_TO_SEND_ENABLED : 		return (SerialPort.FLOW_CONTROL_RTS_ENABLED 		& sp.getFlowControlSettings()) == 1;
			case FLOWCONTROL_CLEAR_TO_SEND_ENABLED : 		return (SerialPort.FLOW_CONTROL_CTS_ENABLED 		& sp.getFlowControlSettings()) == 1;
			case PROTOCALL :								return SPCSetting.defaultProtocol;
			case TIMEOUT_MODE :								return timeoutOptions.defaultTimeout.getValue();
			
			default:
				throw new UnsupportedOperationException("failed to match setting: " + setting.name());
		}
	}
	
	/**
	 * edits the corresponding attribute of a SerialPort (from JSerial) from a given SPCSetting. for Enum's the [value] parameter should be the actual mask integer from the SerialPort class
	 * , use functions such as "[SPCSetting.stopbitOptions].SerialPortMask" to get the integer value. 
	 * @param setting : the chosen setting
	 * @param sp : the target serial port
	 * @param value : the new value of the attribute 
	 * @return TRUE if the value has been successfully changed. FLASE if the given setting is not allowed to be changed, either because - its not a editable setting - or - it is not a hot-swappable setting and the port is open -. 
	 * @throws ClassCastException if the given value does not match the class in [SPCSetting.clas]
	 */
	public static boolean ApplySetting(SPCSetting setting, SerialPort sp, Object value) throws ClassCastException {		
		assert value.getClass().isInstance(setting.clas) : "trying to assign mismatched objects"; //is assert because only in development does the input to this differ.
		
		if(!setting.isEditable() || (sp.isOpen() && !SPCSettings.HotSwappableSettings.contains(setting)))	//if it is not editable
			return false;
		
		if(value instanceof Wrappable) {
			value = ((Wrappable) value).getValue();
		}
		
		switch(setting) {
			case BAUD_RATE : 							sp.setBaudRate((int)value); 		break;
			case DATA_BITS_PER_WORD :					sp.setNumDataBits((int)value);		break;
			case NUM_STOP_BITS :						sp.setNumStopBits((int)value); 		break;
			case PARITY :								sp.setParity((int)value); 			break;
			case TIMEOUT_READ :							sp.setComPortTimeouts((int)fetchSetting(SPCSetting.TIMEOUT_MODE, sp), (int)value, sp.getWriteTimeout()); 	break;
			case TIMEOUT_MODE :							sp.setComPortTimeouts((int)value, sp.getReadTimeout(), sp.getWriteTimeout());								break;
			case TIMEOUT_WRITE :						sp.setComPortTimeouts((int)fetchSetting(SPCSetting.TIMEOUT_MODE, sp), sp.getReadTimeout(), (int)value); 	break;
			case FLOWCONTROL_DATA_SET_READY_ENABLED :		setFLowControl((boolean)value, SerialPort.FLOW_CONTROL_DSR_ENABLED, sp); 		break;	
			case FLOWCONTROL_DATA_TERMINAL_READY_ENABLED:	setFLowControl((boolean)value, SerialPort.FLOW_CONTROL_DTR_ENABLED, sp); 		break;
			case FLOWCONTROL_XIN_ONOFF_ENABLED : 			setFLowControl((boolean)value, SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED, sp); break;
			case FLOWCONTROL_XOUT_ONOFF_ENABLED :			setFLowControl((boolean)value, SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED, sp);break;
			case FLOWCONTROL_REQUEST_TO_SEND_ENABLED :		setFLowControl((boolean)value, SerialPort.FLOW_CONTROL_RTS_ENABLED, sp); 		break;
			case FLOWCONTROL_CLEAR_TO_SEND_ENABLED : 		setFLowControl((boolean)value, SerialPort.FLOW_CONTROL_CTS_ENABLED, sp); 		break;
			case PROTOCALL :							break;
	
			default:
				throw new UnsupportedOperationException("SPCSetting failed to match setting: " + setting.name());
		}
		
		return true;
	}
	
	public SPCSettings(BufferedReader br) throws IOException {
		rebaseFrom(br);
	}
	
	
	public SPCSettings() {
		// TODO Auto-generated constructor stub
	}

	public void rebase(SerialPort sp) {
		AvaliableSettings.parallelStream()
			.forEach(setting -> {
				var v = getSetting(setting, sp);
				if(v != null)
					settings.put(setting, v);
			});
		modifiedSettings.clear();
	}
	
	public void applyModified(SerialPort sp) throws Exception {
		for(var setting : modifiedSettings) {
			try {
				Object value;
				if(SPCSetting.isEditable(setting) && (value = settings.get(setting)) != null) {
					SPCSettings.ApplySetting(setting, sp, value);
				}
			}catch(ClassCastException e) {
				throw new Exception("setting:" + setting.toString() + "\t value:" + this.settings.get(setting), e);  
			}
		};
		modifiedSettings.clear();
	}
	
	public void applyAll(SerialPort sp) {
		modifiedSettings = EnumSet.allOf(SPCSetting.class);
		try { 
			applyModified(sp);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public boolean setSetting(SPCSetting setting, Object value) {		
		assert 	value.getClass().isInstance(setting.clas)||
				value.getClass().equals(setting.clas)
			: "bruh\n expected:" + setting.clas + "\n got:" + value.getClass();//given [value] foes not match the accepted data type
		
		if(value == null || !SPCSetting.isEditable(setting)) {
			System.out.println("SPCSettings> setSetting: failed to change value, is readonly:" + setting);
			return false;
		}
		
		settings.put(setting, value);
		modifiedSettings.add(setting);
		
		return true;
	}
	
	public Object getSetting(SPCSetting setting, SerialPort sp) {
		return SPCSettings.fetchSetting(setting, sp);
	}
	
	public EnumSet<SPCSetting> getModifiedSettings(){
		return modifiedSettings;
	}
	
///////////////////
//save & load
///////////////////
	public void writeTo(BufferedWriter bw) throws IOException{
		modifiedSettings.clear();
		for(var setting : AvaliableSettings) {
			var clas = setting.clas;
			
			if(DOMParser.parseValue_stringers.containsKey(clas)) {
				if(clas.isEnum())
					try {
						String string = ((Enum)settings.get(setting)).name(); //throws exception
						
						bw.write(setting.name());
						bw.newLine();
						
						bw.write(string);
						bw.newLine();
					}catch (ClassCastException cce) { } //invalid... for some reason. idk, not saving it => it should fix itself the next time it loads
				else {
					bw.write(setting.name());
					bw.newLine();
					bw.write(settings.get(setting).toString());
					bw.newLine();
				}
			}
			else {
				try { //if your getting a NullPointerException here check the entry in DOMParser
					DOMParser.writeValue.get(clas).apply(bw);	
				}catch(NullPointerException e) {
					System.out.print(clas.toString());
					throw e;
				}
			}
		}
	}
	
	public void rebaseFrom(BufferedReader br) throws IOException {
		int count = SPCSettings.AvaliableSettings.size();//in case of overrun
		SPCSetting loadedSetting = null;
		modifiedSettings.clear();
		
		for(String line;(line = br.readLine()) != null && count > 0; count--) {
			try {
				loadedSetting = SPCSetting.valueOf(line);
			}catch(ClassCastException | IllegalArgumentException e) {	//skips invalid or blank lines 
				continue;
			}
			
			Object value = null;
			var clas = loadedSetting.clas;
			
			assert 
					DOMParser.parseValue_stringers.containsKey(clas) ||
					DOMParser.parseValue.containsKey(clas) 
				: "SPCSetting contains a unknown class. unable to parse : " + clas;
			try {
				if(DOMParser.parseValue_stringers.containsKey(clas)) {
					String ln = br.readLine();
					if(ln != null && !ln.isBlank())
						value = DOMParser.parseValue_stringers.get(clas).apply(ln);
				}
				else {
					assert DOMParser.parseValue.containsKey(clas) : "class | " + clas + " | is not listed in as a Stringer or given a parsing method";
					
					value = DOMParser.parseValue.get(clas).apply(br);
				}
				
				if(value != null) {
					settings.put(loadedSetting, value);
				}
			}catch(IllegalArgumentException e) {
				System.err.println("SPCSettings > rebase : bad parse, either bad source file or the parser itself. fix in DOMParser or SPCSetting.\n  Given setting: " + loadedSetting + "\n  attempted parse by: " + clas);
			}
		}
	}
	
	private static void setFLowControl(boolean enabled, int mask, SerialPort sp) {
		int fcs = sp.getFlowControlSettings();
		
		if(enabled) 
			fcs |= mask;
		else
			fcs &= ~mask;
		
		sp.setFlowControl(fcs);
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