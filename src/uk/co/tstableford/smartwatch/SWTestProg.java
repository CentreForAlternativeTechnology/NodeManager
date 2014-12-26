package uk.co.tstableford.smartwatch;

import java.util.HashMap;

import javax.swing.SwingUtilities;

import uk.co.tstableford.smartwatch.log.Log;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class SWTestProg implements SerialPortEventListener {
	private SerialPort serialPort = null;
	private HashMap<PacketTypes, PacketHandler> packetHandlers;

	public SWTestProg(String port) {
		try {
			this.serialPort = this.setupSerialPort(port);
		} catch (SerialPortException e) {
			e.printStackTrace();
			return;
		}
		
		this.packetHandlers = new HashMap<PacketTypes, PacketHandler>();
	}
	
	public void addPacketHandler(PacketTypes t, PacketHandler h) {
		packetHandlers.put(t, h);
	}
	
	public void close() {
		try {
			this.serialPort.closePort();
			Log.i("Serial port closed");
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	private SerialPort setupSerialPort(String port) throws SerialPortException {
			SerialPort sc = new SerialPort(port);
			if(sc.openPort()) {
				Log.i("Serial port " + port + " opened");
				sc.setParams(SerialPort.BAUDRATE_57600, 
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);
				sc.readBytes();
				sc.addEventListener(this);
				return sc;
			}
			return null;
	}
	
	public void writeBytes(byte[] buffer) {
		try {
			serialPort.writeBytes(buffer);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		if(arg0.isRXCHAR() && arg0.getEventValue() >= 1) {
			try {
				byte[] in = this.serialPort.readBytes(2);
				byte[] db = this.serialPort.readBytes((int)(in[1] & 0xFF));
				
				final Packet p = new Packet(in[0], db);
				final PacketHandler ph = this.packetHandlers.get(p.getPacketType());
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ph.handlePacket(p);
					}	
				});

			} catch (SerialPortException e) {
				Log.e("Could not read serial port.");
			}
		}
	}
}
