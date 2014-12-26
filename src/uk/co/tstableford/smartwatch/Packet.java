package uk.co.tstableford.smartwatch;

import uk.co.tstableford.smartwatch.log.Log;

public class Packet {
	private PacketTypes packetType;
	private byte[] data;

	public Packet(byte command, byte[] data) {
		this.data = data;
		this.packetType = PacketTypes.getPacketType((int)(command & 0xFF));

		if(this.packetType == null) {
			Log.e("Unknown packet type " +
					Integer.toHexString((int)(command & 0xFF)));
		}
	}
	
	public PacketTypes getPacketType() {
		return this.packetType;
	}
	
	public byte[] getData() {
		return this.data;
	}
}
