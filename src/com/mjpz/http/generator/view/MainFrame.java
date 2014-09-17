package com.mjpz.http.generator.view;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.mjpz.http.generator.net.GenHttpAgent;
import com.mjpz.net.HttpAgent;

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

		JButton header = new JButton("header");
		header.setBounds(290, y, 80, 30);
		panel.add(header);

		JButton send = new JButton("send");
		send.setBounds(370, y, 80, 30);
		panel.add(send);

		y += 30;
		final JTextArea body = new JTextArea(5, 80);
		JScrollPane bodyScrollpane = new JScrollPane(body);
		body.setLineWrap(true);
		body.setMargin(new Insets(5, 10, 5, 10));
		bodyScrollpane.setBounds(10, y, 450, 200);
		panel.add(bodyScrollpane);
		

		y += 200;
		final JTextArea result = new JTextArea(5, 80);
		JScrollPane resultScrollpane = new JScrollPane(result);
		result.setLineWrap(true);
		result.setMargin(new Insets(5, 10, 5, 10));
		resultScrollpane.setBounds(10, y, 450, 200);
		panel.add(resultScrollpane);
		
		send.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				HttpAgent agent = new GenHttpAgent(host.getText());
				
			}
			
		});
		
		return panel;
	}
}
