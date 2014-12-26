package uk.co.tstableford.smartwatch.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import uk.co.tstableford.smartwatch.SWTestProg;
import uk.co.tstableford.smartwatch.log.Log;
import uk.co.tstableford.smartwatch.log.LogListener;

public class SWTestProgGUI implements LogListener{
	private static final String SERIAL_PORT = "/dev/rfcomm0";
	private SWTestProg swProg;
	private JTextArea logConsole;
	
	public SWTestProgGUI() {
		Log.setListener(this);
		
		JFrame mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				swProg.close();
				System.exit(0);
			}
		});
		
		mainFrame.setSize(800, 600);
		
		logConsole = new JTextArea();
		JScrollPane logPane = new JScrollPane(logConsole);
		logPane.setPreferredSize(new Dimension(400, 600));
		logPane.setBorder(BorderFactory.createTitledBorder("Log"));
		
		mainFrame.add(logPane, BorderLayout.WEST);
		
		mainFrame.setVisible(true);
		
		swProg = new SWTestProg(SERIAL_PORT);
	}
	
	public static void main(String[] args) {
		new SWTestProgGUI();
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
}
