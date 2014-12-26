package uk.co.tstableford.smartwatch.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import uk.co.tstableford.smartwatch.Packet;
import uk.co.tstableford.smartwatch.PacketHandler;
import uk.co.tstableford.smartwatch.PacketTypes;
import uk.co.tstableford.smartwatch.SWTestProg;
import uk.co.tstableford.smartwatch.log.Log;
import uk.co.tstableford.smartwatch.log.LogListener;

public class SWTestProgGUI implements LogListener, ActionListener, PacketHandler {
	private static final String SERIAL_PORT = "/dev/rfcomm0";
	private static final byte BUTTON_PRESSED = (byte) 0xc0;
	private static final byte BUTTON_RELEASED = (byte) 0x30;
	private SWTestProg swProg;
	private JTextArea logConsole;
	
	public SWTestProgGUI() {
		Log.setListener(this);
		
		JFrame mainFrame = new JFrame();
		mainFrame.setLayout(new GridLayout(1, 2));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				swProg.close();
				System.exit(0);
			}
		});
		
		mainFrame.setTitle("Smart Watch Test Program");
		
		mainFrame.setSize(800, 600);
		
		logConsole = new JTextArea();
		JScrollPane logPane = new JScrollPane(logConsole);
		logPane.setPreferredSize(new Dimension(400, 600));
		logPane.setBorder(BorderFactory.createTitledBorder("Log"));
		
		mainFrame.add(logPane);
		
		JScrollPane controlScroll = new JScrollPane(this.makeControlPanel());
		controlScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		mainFrame.add(controlScroll);
		
		mainFrame.setVisible(true);
		
		swProg = new SWTestProg(SERIAL_PORT);
		swProg.addPacketHandler(PacketTypes.GETMEM, this);
	}
	
	private JPanel makeControlPanel() {
		JPanel cP = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.2;
		
		JButton buttons[] = new JButton[6];
		final SWTestProgGUI thisListener = this;
		for(int i=0; i<6; i++) {
			final int j = i;
			buttons[i] = new JButton("B" + i);
			buttons[i].addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent arg0) {}
				@Override
				public void mouseEntered(MouseEvent arg0) {}
				@Override
				public void mouseExited(MouseEvent arg0) {}

				@Override
				public void mousePressed(MouseEvent arg0) {
					thisListener.onButton(j, true);
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					thisListener.onButton(j, false);
				}
				
			});
		}
		
		for(int i=0; i<3; i++) {
			c.gridy = 0; c.gridx = i;
			cP.add(buttons[i], c);
			c.gridy = 1;
			cP.add(buttons[i + 3], c);
		}
		
		c.gridx = 0; c.gridy = 2; c.gridwidth = 3;
		JButton sync = new JButton("Sync Time");
		sync.setActionCommand("sync_time");
		sync.addActionListener(this);
		cP.add(sync, c);
		
		c.gridy = 3;
		JButton getMem = new JButton("Get Free Memory");
		getMem.setActionCommand("free_mem");
		getMem.addActionListener(this);
		cP.add(getMem, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.2; c.gridy++;
		cP.add(Box.createVerticalGlue(), c);
		
		return cP;
	}
	
	public static void main(String[] args) {
		new SWTestProgGUI();
	}
	
	public void onButton(int button, boolean pressed) {
		byte[] d = new byte[1];
		d[0] = pressed ? BUTTON_PRESSED : BUTTON_RELEASED;
		d[0] |= (byte)(button & 0x0F);
		Packet p = new Packet(PacketTypes.BUTTON, d);
		swProg.writeBytes(p.getBytes());
		Log.i((pressed ? "Press " : "Release ") + "button " + button);
	}

	@Override
	public void logError(String error) {
		if(logConsole != null) {
			logConsole.append("[ERROR]" + error + "\n");
		} else {
			System.err.println("Attempt to err print to uninit log: " + error);
		}
	}

	@Override
	public void logInfo(String info) {
		if(logConsole != null) {
			logConsole.append("[INFO]" + info + "\n");
		} else {
			System.err.println("Attempt to print to uninit log: " + info);
		}
	}
	
	private void syncTime() {
		byte[] buffer = {0x07,0x07,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
		Calendar now = Calendar.getInstance();
		buffer[2] = (byte)(now.get(Calendar.YEAR) - 2000);
		buffer[3] = (byte)(now.get(Calendar.MONTH) + 1);
		buffer[4] = (byte)(now.get(Calendar.DATE));
		buffer[5] = (byte)(now.get(Calendar.HOUR_OF_DAY));
		buffer[6] = (byte)(now.get(Calendar.MINUTE));
		buffer[7] = (byte)(now.get(Calendar.SECOND));
		buffer[8] = (byte)(now.get(Calendar.DAY_OF_WEEK));
		swProg.writeBytes(buffer);
		Log.i("Device time updated");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		switch(arg0.getActionCommand()) {
		case "sync_time":
			syncTime();
			break;
		case "free_mem":
			byte[] buffer = { 0x08, 0x00 };
			swProg.writeBytes(buffer);
			break;
		}
	}

	@Override
	public void handlePacket(Packet packet) {
		switch(packet.getPacketType()) {
		case GETMEM:
			short a = (short)(packet.getData()[0]&0xFF);
			a |= (short)(packet.getData()[0]&0xFF) << 8;
			Log.i(a + " bytes free of 2048 bytes");
			break;
		default:
			Log.e("Unknown response packet type");
		}
	}
}
