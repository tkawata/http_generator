package com.mjpz.net;

public class Log {

	private boolean debug;
	
	/**
	 * デバッグ
	 * 
	 * <pre></pre>
	 * @param data
	 */
	public void debug(String data) {
		if (debug) {
			info(data);
		}
	}
	
	/**
	 * デバッグ
	 * 
	 * <pre></pre>
	 * @param data
	 */
	public void info(String data) {
		synchronized (this) {
			System.out.println(data);
		}
	}
	
	/**
	 * エラー
	 * 
	 * <pre></pre>
	 * @param data
	 */
	public void error(String data) {
		synchronized (this) {
			System.err.println(data);
		}
	}
	
	/**
	 * エラー
	 * 
	 * <pre></pre>
	 * @param data
	 */
	public void error(String data, Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(data);
		sb.append("\n\n");
		sb.append(parseString(t));
		synchronized (this) {
			error(sb.toString());
		}
	}
	
	public String parseString(Throwable t) {
		StringBuilder sb = new StringBuilder();
		if (t == null) {
			return "";
		}
		sb.append(t.getMessage());
		sb.append("\n");
		StackTraceElement[] elements = t.getStackTrace();
		for (StackTraceElement element : elements) {
			sb.append(element.toString());
			sb.append("\n");
		}
		Throwable cause = t.getCause();
		sb.append(parseString(cause));
		
		return sb.toString();
	}

	/**
	 * debugの取得
	 * <pre></pre>
	 * @return debugを返す。
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * debugの設定
	 * <pre></pre>
	 * @param debug を debug に設定する。
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
}
