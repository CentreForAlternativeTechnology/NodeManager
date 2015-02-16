package uk.co.tstableford.smartwatch.asyncsocket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

public class ASyncSocket {
	private Socket socket;
	private SocketDispatcher dispatcher;
	private SocketEventListener listener;
	private SocketReader reader;
	private static final long TIMEOUT = 1000;
	
	public ASyncSocket(String host, int port) throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		
		this.listener = null;
		
		if(socket != null) {
			dispatcher = new SocketDispatcher();
			new Thread(dispatcher).start();
			reader = new SocketReader();
			new Thread(reader).start();
		}
	}
	
	public void close() {
		try {
			this.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.socket = null;
	}
	
	public void setEventListener(SocketEventListener e) {
		this.listener = e;
	}
	
	public void flush() {
		reader.flush();
	}
	
	public void send(byte[] data) {
		if(dispatcher != null) {
			dispatcher.send(data);
		}
	}
	
	public byte[] readBytes(int count) {
		long start = System.currentTimeMillis();
		
		while(count > this.reader.buffer.size() && 
				(System.currentTimeMillis() - start) < TIMEOUT) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		byte[] buff = new byte[count];
		for(int i=0; i<count; i++) {
			buff[i] = readByte();
		}
		return buff;
	}
	
	public byte readByte() {
		return this.reader.bufferRemove();
	}
	
	class SocketReader implements Runnable {
		private List<Byte> buffer;
		
		public void flush() {
			try {
				socket.getInputStream().skip(socket.getInputStream().available());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			buffer = new LinkedList<Byte>();
		}
		
		public SocketReader() {
			buffer = new LinkedList<Byte>();
		}
		
		private synchronized void bufferAdd(byte b) {
			buffer.add(b);
		}
		
		public synchronized byte bufferRemove() {
			if(buffer.size() > 0) {
				return buffer.remove(0);
			}
			return -1;
		}
		
		@Override
		public void run() {
			while(socket != null) {
				try {
					if(!socket.isClosed() && socket.getInputStream().available() > 0) {
						this.bufferAdd((byte)(socket.getInputStream().read() & 0xFF));
						if(listener != null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									listener.socketEvent(new SocketEvent(SocketEventType.RXCHAR, buffer.size()));
								}
								
							});
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class SocketDispatcher implements Runnable {
		private List<ByteMessage> senders;
		public SocketDispatcher() {
			senders = new LinkedList<ByteMessage>();
		}
		public synchronized void send(byte[] data) {
			senders.add(new ByteMessage(data));
		}
		private synchronized ByteMessage pop() {
			if(senders.size() > 0) {
				return senders.remove(0);
			}
			return null;
		}
		@Override
		public void run() {
			while(socket != null) {
				ByteMessage data = this.pop();
				if(data != null) {
					try {
						socket.getOutputStream().write(data.getData());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			// TODO Auto-generated method stub
			
		}
		class ByteMessage {
			private byte[] data;
			public ByteMessage(byte[] data) {
				this.data = data;
			}
			public byte[] getData() {
				return this.data;
			}
		}
		
	}
}
