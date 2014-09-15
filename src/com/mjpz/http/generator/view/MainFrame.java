package com.mjpz.http.generator.view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MainFrame {

	/**
	 * èâä˙âª
	 */
	public JFrame init() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setBounds(10, 10, 500, 800);
	    frame.setTitle("Http Generator");
	    
	    frame.add(getInputPanel());
	    
	    frame.setVisible(true);
	    return frame;
	}
	
	/**
	 * ì¸óÕïîÇÃéÊìæ
	 * @return
	 */
	public JPanel getInputPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		int y = 10;
		final JTextField method = new JTextField(5);
		method.setText("GET");
		method.setBounds(10, y, 80, 30);
		panel.add(method);
		
		final JTextField path = new JTextField(20);
		path.setText("/");
		path.setBounds(90, y, 200, 30);
		panel.add(path);
		
		JLabel httpvl = new JLabel("HTTP/");
		httpvl.setBounds(290, y, 40, 30);
		panel.add(httpvl);

		final JTextField httpv = new JTextField(5);
		httpv.setText("1.1");
		httpv.setBounds(330, y, 80, 30);
		panel.add(httpv);

		y += 30;
		JLabel hostl = new JLabel("host : ");
		hostl.setBounds(10, y, 80, 30);
		panel.add(hostl);
		
		final JTextField host = new JTextField(20);
		host.setText("google.com");
		host.setBounds(90, y, 200, 30);
		panel.add(host);
		
		return panel;
	}
}
