package com.mjpz.http.generator.view;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class MjpzProgressDialog extends JDialog {

	public MjpzProgressDialog(JFrame frame, boolean mode) {
		super(frame, mode);
	}

	protected JProgressBar progressBar;
	
	public void init(String title, int min, int max) {
		setTitle(title);

		setSize(500, 56);
		setLocationRelativeTo(null);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		// プログレスバー生成
		this.progressBar = new JProgressBar(min, max);

		Container c = getContentPane();
		c.add(progressBar, BorderLayout.PAGE_START);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public void setProgress(int value) {
		progressBar.setValue(value);
	}

}
