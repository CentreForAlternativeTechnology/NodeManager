package uk.co.tstableford.smartwatch.log;

public interface LogListener {
	public void logError(String error);
	public void logInfo(String info);
}
