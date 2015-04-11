package uk.co.tstableford.emonnodemanager.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import uk.co.tstableford.emonnodemanager.Packet;
import uk.co.tstableford.emonnodemanager.PacketHandler;
import uk.co.tstableford.emonnodemanager.PacketTypes;
import uk.co.tstableford.emonnodemanager.EMonComs;
import uk.co.tstableford.emonnodemanager.log.Log;
import uk.co.tstableford.emonnodemanager.log.LogListener;

public class NodeManager implements LogListener, ActionListener, PacketHandler {
	private EMonComs swProg;
	private JTextArea logConsole;
	
	public NodeManager(String port) {
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
		
		swProg = new EMonComs(port);
		swProg.addPacketHandler(PacketTypes.GETMEM, this);
		swProg.addPacketHandler(PacketTypes.GETCLOCK, this);
	}
	
	private JPanel makeControlPanel() {
		JPanel cP = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.2;
		
		c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
		JButton sync = new JButton("Sync Time");
		sync.setActionCommand("sync_time");
		sync.addActionListener(this);
		cP.add(sync, c);
		
		c.gridy++;
		JButton getMem = new JButton("Get Free Memory");
		getMem.setActionCommand("free_mem");
		getMem.addActionListener(this);
		cP.add(getMem, c);
		
		c.gridy++;
		JButton getClock = new JButton("Get Clock");
		getClock.setActionCommand("get_clock");
		getClock.addActionListener(this);
		cP.add(getClock, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.2; c.gridy++;
		cP.add(Box.createVerticalGlue(), c);
		
		return cP;
	}
	
	public static void main(String[] args) {
		String port = null;
		if(args.length > 0) {
			port = args[0];
		}
		new NodeManager(port);
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
			byte[] buffer = { (byte)(PacketTypes.GETMEM.getValue() & 0xFF), 0x00 };
			swProg.writeBytes(buffer);
			break;
		case "get_clock":
			byte[] buffer2 = { (byte)(PacketTypes.GETCLOCK.getValue() & 0xFF), 0x00 };
			swProg.writeBytes(buffer2);
		}
	}
	
	private short parseShort(byte b0, byte b1) {
		short b = (short)(b1 & 0xFF);
		b |= (short)(b0 & 0xFF) << 8;
		return b;
	}

	@Override
	public void handlePacket(Packet packet) {
		switch(packet.getPacketType()) {
		case GETMEM:
			short a = parseShort(packet.getData()[0], packet.getData()[1]);
			Log.i(a + " bytes free of 2048 bytes");
			break;
		case GETCLOCK:
			Calendar clock = Calendar.getInstance();
			
			clock.set(Calendar.YEAR, (int)(packet.getData()[0]) + 2000);
			clock.set(Calendar.MONTH, (int)(packet.getData()[1]) - 1);
			clock.set(Calendar.DATE, (int)(packet.getData()[2]));
			clock.set(Calendar.HOUR_OF_DAY, (int)(packet.getData()[3]));
			clock.set(Calendar.MINUTE, (int)(packet.getData()[4]));
			clock.set(Calendar.SECOND, (int)(packet.getData()[5]));
			Log.i(clock.getTime().toString());
			break;
		default:
			Log.e("Unknown response packet type");
			break;
		}
	}
}
