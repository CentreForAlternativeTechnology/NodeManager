package uk.co.tstableford.emonnodemanager;

import uk.co.tstableford.emonnodemanager.log.Log;

public class Packet {
	private PacketTypes packetType;
	private byte[] data;

	public Packet(byte command, byte[] data) {
		this(PacketTypes.getPacketType((int)(command & 0xFF)), data);
	}
	
	public Packet(PacketTypes p, byte[] data) {
		this.packetType = p;
		this.data = data;
		if(this.packetType == null) {
			Log.e("Unknown packet type");
		}
	}
	
	public PacketTypes getPacketType() {
		return this.packetType;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getBytes() {
		byte[] all = new byte[this.data.length + 2];
		all[0] = (byte)(this.packetType.getValue() & 0xFF);
		all[1] = (byte)(this.data.length & 0xFF);
		for(int i=0; i<this.data.length; i++) {
			all[i + 2] = this.data[i];
		}
		
		return all;
	}
}
