package uk.co.tstableford.smartwatch.asyncsocket;

public class SocketEvent {
	public SocketEventType type;
	private int data;
	
	public SocketEvent(SocketEventType type, int data) {
		this.type = type;
		this.data = data;
	}
	
	public int getEventData() {
		return data;
	}
	
	public SocketEventType getType() {
		return this.type;
	}
}
