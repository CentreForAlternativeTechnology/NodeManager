package uk.co.tstableford.smartwatch.log;

import javax.swing.SwingUtilities;

public class Log {
	private static Log instance;
	
	private LogListener listener = null;
	
	private Log() {
		
	}
	
	private static final Log getInstance() {
		if(instance == null) {
			instance = new Log();
		}
		return instance;
	}
	
	public static void setListener(LogListener l) {
		Log.getInstance().listener = l;
	}
	
	public static void e(final String err) {
		System.err.println(err);
		if(Log.getInstance().listener != null) {
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run() {
					Log.getInstance().listener.logError(err);
				}
			});
		}
	}
	
	public static void i(final String info) {
		System.out.println(info);
		if(Log.getInstance().listener != null) {
			SwingUtilities.invokeLater(new Runnable(){
				@Override
				public void run() {
					Log.getInstance().listener.logInfo(info);
				}
			});
		}
	}
	
}
