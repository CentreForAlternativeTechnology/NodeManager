package uk.co.tstableford.emonnodemanager;

public enum PacketTypes {
	SETCLOCK(0x07), GETMEM(0x08), BUTTON(0x09), GETFPS(0x0A);
	
	private final int value;
	
	private PacketTypes(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static PacketTypes getPacketType(int val) {
		for(PacketTypes p: PacketTypes.values()) {
			if(p.value == val) {
				return p;
			}
		}
		return null;
	}
}