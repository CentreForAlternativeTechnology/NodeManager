package uk.co.tstableford.smartwatch.old;

import uk.co.tstableford.smartwatch.Packet;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class Listener implements SerialPortEventListener {
	public Listener() {
	
	}
	
	
	
	@Override
	public void serialEvent(SerialPortEvent arg0) {
		if(arg0.isRXCHAR() && arg0.getEventValue() >= 1) {
			try {
				byte[] in = GetMem.getSerialPort().readBytes(2);
				byte[] db = GetMem.getSerialPort().readBytes((int)(in[1] & 0xFF));
				
				Packet p = new Packet(in[0], db);
				
				short a = (short)(p.getData()[0]&0xFF);
				a |= (short)(p.getData()[0]&0xFF) << 8;
				System.out.println(a);

			} catch (SerialPortException e) {
				System.err.println("Could not read serial port.");
			}
		}
		try {
			GetMem.getSerialPort().closePort();
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
