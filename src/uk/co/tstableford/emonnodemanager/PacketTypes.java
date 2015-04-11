package uk.co.tstableford.emonnodemanager;

public enum PacketTypes {
	GETCLOCK(0x06), SETCLOCK(0x07), GETMEM(0x08);
	
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
