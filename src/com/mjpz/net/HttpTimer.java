/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mjpz.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 雄行
 */
public class HttpTimer {
	private long start;
	private boolean isCanceled;
	protected void setStart(long start) {
		this.start = start;
	}
	protected void cancel() {
		isCanceled = true;
//		System.out.println("cancel");
	}
	protected void check(final SocketChannel ch) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				isCanceled = false;
//				System.out.println("start");
				for (int i = 0; !isCanceled && i < 5000; i++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException ex) {
					}
				}
				if (!isCanceled) {
//					System.out.println("timeouttt");
					try {
						ch.close();
					} catch (IOException ex) {
					}
				}
//					System.out.println("enddddd " + isCanceled);
			}
			
		}).start();
	}
}
