package uk.co.tstableford.smartwatch.old;

import java.util.Calendar;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SetTime {
	private static SerialPort serialPort = null;
	public SetTime() {
	
	}

	public static SerialPort getSerialPort() {
		return serialPort;
	}

	public static SerialPort getPort() throws SerialPortException {
			SerialPort sc = new SerialPort("/dev/rfcomm0");
			if(sc.openPort()) {
				System.out.println("Serial port /dev/rfcomm0 opened");
				sc.setParams(SerialPort.BAUDRATE_57600, 
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
				return sc;
			}
			return null;
	}
	public static void main(String[] args) {
		try {
			serialPort = getPort();
		} catch (SerialPortException e) {
			System.err.println("Error connecting serial (1)");
			System.exit(0);
		}
		if(serialPort == null) {
			System.err.println("Error connecting serial (2)");
			System.exit(0);
		}
		try {
			Listener ls = new Listener();
			serialPort.addEventListener(ls);
			//ls.setListener(new CapSense());
		} catch (SerialPortException e) {
			e.printStackTrace();
			System.exit(0);
		}
		try {
			serialPort.readBytes(serialPort.getInputBufferBytesCount());
		} catch (SerialPortException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("sending time bytes");
		byte[] buffer = {0x07,0x07,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
		Calendar now = Calendar.getInstance();
		buffer[2] = (byte)(now.get(Calendar.YEAR) - 2000);
		buffer[3] = (byte)(now.get(Calendar.MONTH) + 1);
		buffer[4] = (byte)(now.get(Calendar.DATE));
		buffer[5] = (byte)(now.get(Calendar.HOUR_OF_DAY));
		buffer[6] = (byte)(now.get(Calendar.MINUTE));
		buffer[7] = (byte)(now.get(Calendar.SECOND));
		buffer[8] = (byte)(now.get(Calendar.DAY_OF_WEEK));
		try {
			serialPort.writeBytes(buffer);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			serialPort.closePort();
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
