package uk.co.tstableford.emonnodemanager.log;

public interface LogListener {
	public void logError(String error);
	public void logInfo(String info);
	public void logDebug(String debug);
}
