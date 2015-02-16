package uk.co.tstableford.smartwatch;

import java.io.IOException;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import uk.co.tstableford.smartwatch.asyncsocket.ASyncSocket;
import uk.co.tstableford.smartwatch.asyncsocket.SocketEvent;
import uk.co.tstableford.smartwatch.asyncsocket.SocketEventListener;
import uk.co.tstableford.smartwatch.asyncsocket.SocketEventType;
import uk.co.tstableford.smartwatch.log.Log;

public class SWTestProg implements SocketEventListener {
	private ASyncSocket socket = null;
	private HashMap<PacketTypes, PacketHandler> packetHandlers;

	public SWTestProg(String port) {
		try {
			this.socket = new ASyncSocket("192.168.4.1", 1234);
			this.socket.setEventListener(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		this.packetHandlers = new HashMap<PacketTypes, PacketHandler>();
	}

	public void addPacketHandler(PacketTypes t, PacketHandler h) {
		packetHandlers.put(t, h);
	}
	
	public void close() {
		this.socket.close();
		Log.i("Serial port closed");
	}
	
	public void writeBytes(byte[] buffer) {
		socket.send(buffer);
	}

	@Override
	public void socketEvent(SocketEvent arg0) {
		if(arg0.getType() == SocketEventType.RXCHAR && arg0.getEventData() > 1) {
			byte[] in = this.socket.readBytes(2);
			byte[] db = this.socket.readBytes((int)(in[1] & 0xFF));

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
		}
	}
}
