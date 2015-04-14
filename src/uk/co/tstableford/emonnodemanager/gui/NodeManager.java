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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import uk.co.tstableford.emonnodemanager.Packet;
import uk.co.tstableford.emonnodemanager.PacketHandler;
import uk.co.tstableford.emonnodemanager.PacketTypes;
import uk.co.tstableford.emonnodemanager.EMonComs;
import uk.co.tstableford.emonnodemanager.log.Log;
import uk.co.tstableford.emonnodemanager.log.LogListener;

public class NodeManager implements LogListener, ActionListener, PacketHandler {
	private EMonComs swProg;
	private JTextArea logConsole;
	private JTextField start, length, address, value, calibrationValue, baseValue;
	private JCheckBox enableCalibration;
	private SimpleRegression calibration;
	
	public NodeManager(String port) {
		Log.setListener(this);
		
		this.calibration = new SimpleRegression();
		
		JFrame mainFrame = new JFrame();
		mainFrame.setLayout(new GridLayout(1, 2));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				swProg.close();
				System.exit(0);
			}
		});
		
		mainFrame.setTitle("EMon Node Manager");
		
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
		swProg.addPacketHandler(PacketTypes.GETEEPROM, this);
		swProg.addPacketHandler(PacketTypes.PRESSUREREADING, this);
		swProg.addPacketHandler(PacketTypes.DEBUG, this);
		swProg.addPacketHandler(PacketTypes.GETDEPTH, this);
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
		
		//Fetching from EEPROM
		c.gridy++;
		c.gridwidth = 1;
		start = new JTextField("0");
		length = new JTextField("10");
		JButton getEEPROM = new JButton("Get EEPROM");
		getEEPROM.setActionCommand("get_eeprom");
		getEEPROM.addActionListener(this);
		cP.add(new JLabel("Start Address"), c);
		c.gridx = 1;
		cP.add(new JLabel("Length"), c);
		c.gridy++;
		c.gridx = 0;
		cP.add(start, c);
		c.gridx = 1;
		cP.add(length, c);
		c.gridx = 2;
		cP.add(getEEPROM, c);
		c.gridx = 0;
		
		//Setting EEPROM value
		c.gridy++;
		c.gridwidth = 1;
		address = new JTextField();
		value = new JTextField();
		JButton setEEPROM = new JButton("Set EEPROM");
		setEEPROM.addActionListener(this);
		setEEPROM.setActionCommand("set_eeprom");
		cP.add(new JLabel("Address"), c);
		c.gridx = 1;
		cP.add(new JLabel("Value"), c);
		c.gridx = 0;
		c.gridy++;
		cP.add(address, c);
		c.gridx = 1;
		cP.add(value, c);
		c.gridx = 2;
		cP.add(setEEPROM, c);
		c.gridx = 0;
		c.gridwidth = 3;
		
		/* Calibration */
		c.gridy++;
		JButton setBaseLevel = new JButton("Set pressure base");
		setBaseLevel.setActionCommand("set_base_level");
		setBaseLevel.addActionListener(this);
		cP.add(setBaseLevel, c);
		
		c.gridy++;
		c.gridwidth = 2;
		JButton getPressure = new JButton("Get Pressure");
		getPressure.setActionCommand("get_pressure");
		getPressure.addActionListener(this);
		cP.add(getPressure, c);
		
		c.gridx = 2;
		c.gridwidth = 1;
		this.enableCalibration = new JCheckBox("Calibrate?");
		this.enableCalibration.setSelected(false);
		cP.add(this.enableCalibration, c);
		c.gridx = 0;
		c.gridwidth = 3;
		
		c.gridy++;
		c.gridwidth = 1;
		this.calibrationValue = new JTextField();
		cP.add(this.calibrationValue, c);
		c.gridx = 1;
		this.baseValue = new JTextField();
		cP.add(this.baseValue, c);
		
		c.gridx = 2;
		JButton setCalibration = new JButton("Set Calibration");
		setCalibration.setActionCommand("set_calibration");
		setCalibration.addActionListener(this);
		cP.add(setCalibration, c);
		c.gridx = 0;
		c.gridwidth = 3;
		
		c.gridy++;
		JButton getDepth = new JButton("Get Depth");
		getDepth.setActionCommand("get_depth");
		getDepth.addActionListener(this);
		cP.add(getDepth, c);
		
		//End filler
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
	
	@Override
	public void logDebug(String debug) {
		if(logConsole != null) {
			logConsole.append("[DEBUG]" + debug + "\n");
		} else {
			System.err.println("Attempt to print to uninit log: " + debug);
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
			break;
		case "get_eeprom":
			short[] req = new short[2];
			try {
				req[0] = Short.parseShort(this.start.getText());
				req[1] = Short.parseShort(this.length.getText());
				
				if(req[1] != 0 && req[1] < 100 && req[0] < 1024) {
					byte[] buffer3 = { (byte)(PacketTypes.GETEEPROM.getValue() & 0xFF), 0x04 };
					swProg.writeBytes(buffer3);
					byte[] buffer4 = this.shortsToBytes(req);
					swProg.writeBytes(buffer4);
				} else {
					Log.e("Address or length had invalid values");
				}
			} catch (NumberFormatException e) {
				Log.e("Failed to parse address or length");
			}
			break;
		case "set_eeprom":
			short address, value;
			try {
				address = Short.parseShort(this.address.getText());
				value = Short.parseShort(this.value.getText());
				
				this.setEEPROM(address, value);
			} catch (NumberFormatException e) {
				Log.e("Failed to parse address or value");
			}
			break;
		case "get_pressure":
			byte[] p_buffer = { (byte)(PacketTypes.PRESSUREREADING.getValue() & 0xFF), 0x00 };
			swProg.writeBytes(p_buffer);
			break;
		case "set_calibration":
			try {
				float c[] = { Float.parseFloat(this.calibrationValue.getText()) };
				float yi[] = { Float.parseFloat(this.baseValue.getText()) };
				Log.i("Setting calibration gradient to " + c[0]);
				byte buffer5[] =  { (byte)(PacketTypes.SETPRESSUREGRADIENT.getValue() & 0xFF), 0x04 };
				swProg.writeBytes(buffer5);
				swProg.writeBytes(floatsToBytes(c));
				Log.i("Setting y-intercept to " + yi[0]);
				buffer5[0] = (byte)(PacketTypes.SETPRESSURECONSTANT.getValue());
				swProg.writeBytes(buffer5);
				swProg.writeBytes(floatsToBytes(yi));
			} catch (NumberFormatException e) {
				Log.e("Calbration value or intercept is not a number");
			}
			break;
		case "set_base_level":
			byte[] b_buffer = { (byte)(PacketTypes.SETPRESSUREBASE.getValue() & 0xFF), 0x00 };
			swProg.writeBytes(b_buffer);
			break;
		case "get_depth":
			byte[] d_buffer = { (byte)(PacketTypes.GETDEPTH.getValue() & 0xFF), 0x00 };
			swProg.writeBytes(d_buffer);
			break;
		}
	}
	
	private short parseShort(byte b0, byte b1) {
		short b = (short)(b1 & 0xFF);
		b |= (short)(b0 & 0xFF) << 8;
		return b;
	}
	
	private float parseFloat(byte b0, byte b1, byte b2, byte b3) {
		int value = 0;
		value = b0 & 0xFF;
		value |= (int)(b1 & 0xFF) << 8;
		value |= (int)(b2 & 0xFF) << 16;
		value |= (int)(b3 & 0xFF) << 24;
		return Float.intBitsToFloat(value);
	}
	
	private float parseFloat(byte[] data) {
		return parseFloat(data[0], data[1], data[2], data[3]);
	}
	
	private byte[] shortsToBytes(short shorts[]) {
		byte bytes[] = new byte[shorts.length * 2];
		for(int i = 0; i < shorts.length; i++) {
			bytes[(2 * i) + 1] = (byte)((shorts[i]) & 0xFF);
			bytes[2 * i] = (byte)(((shorts[i]) >> 8) & 0xFF);
		}
		return bytes;
	}
	
	private byte[] floatsToBytes(float floats[]) {
		byte bytes[] = new byte[floats.length * 4];
		for(int i = 0; i < floats.length; i++) {
			int bits = Float.floatToIntBits(floats[i]);
			bytes[2 * i] = (byte)(bits & 0xFF);
			bytes[2 * i + 1] = (byte)((bits >> 8) & 0xFF);
			bytes[2 * i + 2] = (byte)((bits >> 16) & 0xFF);
			bytes[2 * i + 3] = (byte)((bits >> 24) & 0xFF);
		}
		return bytes;
	}
	
	private void setEEPROM(short address, short value) {
		if(address < 0 || address > 1023 || value < 0 || value > 255) {
			Log.e("Invalid values for setting EEPROM");
			return;
		}
		
		short[] req = { address, value };
		
		byte[] buffer1 = { (byte)(PacketTypes.SETEEPROM.getValue() & 0xFF), 0x04 };
		swProg.writeBytes(buffer1);
		byte[] buffer2 = this.shortsToBytes(req);
		swProg.writeBytes(buffer2);
	}

	@Override
	public void handlePacket(Packet packet) {
		switch(packet.getPacketType()) {
		case GETMEM:
			short a = parseShort(packet.getData()[0], packet.getData()[1]);
			Log.i(a + " bytes free of 2048 bytes");
			break;
		case GETEEPROM:
			short start = parseShort(packet.getData()[0], packet.getData()[1]);
			short length = parseShort(packet.getData()[2], packet.getData()[3]);
			short eepromValues[] = new short[length];
			StringBuilder str = new StringBuilder("EEPROM at " + start + " for " + length + " = [ ");
			for(int i = 0; i < length; i++) {
				eepromValues[i] = parseShort(packet.getData()[4 + i * 2], packet.getData()[5 + i * 2]);
				str.append(eepromValues[i] + ", ");
			}
			
			str.deleteCharAt(str.length() - 2);
			str.append("]");
			
			Log.i(str.toString());
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
		case PRESSUREREADING:
			short pressure = parseShort(packet.getData()[0], packet.getData()[1]);
			Log.i("Pressure value is " + pressure);
			if(this.enableCalibration.isSelected()) {
				String value = JOptionPane.showInputDialog(null, "Enter depth in meters ie 0.02 for reading " + pressure);
				if(value != null) {
					try {
						float depth = Float.parseFloat(value);
						if(depth >= 0) {
							calibration.addData(pressure, depth);
							if(calibration.getSlope() != Double.NaN) {
								this.calibrationValue.setText(Double.toString(calibration.getSlope()));
								this.baseValue.setText(Double.toString(calibration.getIntercept()));
							}
						} else {
							Log.e("Depth is less than 0, ignoring");
						}
					} catch (NumberFormatException e) {
						Log.e("Invalid depth entry, ignoring.");
					}
				}
			}
			break;
		case DEBUG:
			String str2 = new String(packet.getData());
			Log.d(str2);
			break;
		case GETDEPTH:
			Log.i("Depth from sensor is " + this.parseFloat(packet.getData()) + "m");
			break;
		default:
			Log.e("Unknown response packet type");
			break;
		}
	}
}
