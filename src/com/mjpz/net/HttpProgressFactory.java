package com.mjpz.net;

public interface HttpProgressFactory {

	public HttpProgress generateProgress(String message, String note);
	public HttpProgress generateProgress(String message, String note, int min, int max);
}
