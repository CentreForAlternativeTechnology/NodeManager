package uk.co.tstableford.emonnodemanager;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import uk.co.tstableford.emonnodemanager.log.Log;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class EMonComs implements SerialPortEventListener {
	private SerialPort serialPort = null;
	private HashMap<PacketTypes, PacketHandler> packetHandlers;

	public EMonComs(String port) {
		if(port == null) {
			String[] portNames = SerialPortList.getPortNames();
	        for(int i = 0; i < portNames.length; i++){
	            System.out.println(portNames[i]);
	        }
	        JFrame frame = new JFrame("Input Dialog Example 3");
	        port = (String) JOptionPane.showInputDialog(frame, 
	            "Select a serial port",
	            "Serial Port",
	            JOptionPane.QUESTION_MESSAGE, 
	            null, 
	            portNames, 
	            portNames[0]);
		}
		
		if(port == null) {
			System.out.println("Exiting because user cancelled port selection");
			System.exit(0);
		}
		
		try {
			this.serialPort = this.setupSerialPort(port);
		} catch (SerialPortException e) {
			e.printStackTrace();
			this.serialPort = null;
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
				sc.setParams(SerialPort.BAUDRATE_115200, 
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
		if(arg0.isRXCHAR() && arg0.getEventValue() > 1) {
			try {
				byte[] in = this.serialPort.readBytes(2);
				byte[] db = this.serialPort.readBytes((int)(in[1] & 0xFF));
				
				final Packet p = new Packet(in[0], db);
				final PacketHandler ph = this.packetHandlers.get(p.getPacketType());

				if(ph != null) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							ph.handlePacket(p);
						}	
					});
				}

			} catch (SerialPortException e) {
				Log.e("Could not read serial port.");
			}
		}
	}
}
