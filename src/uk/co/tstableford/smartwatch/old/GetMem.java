package uk.co.tstableford.smartwatch.old;

import java.util.Calendar;

import jssc.SerialPort;
import jssc.SerialPortException;

public class GetMem {
	private static SerialPort serialPort = null;
	public GetMem() {
	
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
		System.out.println(Calendar.MONDAY);
		try {
			serialPort.readBytes(serialPort.getInputBufferBytesCount());
		} catch (SerialPortException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("sending request bytes");
		byte[] buffer = {0x08,0x00};
		try {
			serialPort.writeBytes(buffer);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done, awaiting reponse");
	}
}
