package com.mjpz.net;

/**
 * HTTPアクセスプログレス
 */
public interface HttpProgress {
	public void initProgress(String title, String note);
	public void finishProgress();
	public void initProgress(String title, String note, int min, int max);
	public void setProgress(int value);
	public void setNote(String note);
}
