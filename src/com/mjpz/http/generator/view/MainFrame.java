package com.mjpz.http.generator.view;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;

import com.mjpz.net.HttpAgent;
import com.mjpz.net.HttpException;

public class MainFrame {

	/**
	 * ����
	 */
	public JFrame init() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setBounds(10, 10, 1000, 850);
	    frame.setTitle("Http Generator");
	    
	    frame.add(getInputPanel(frame));
	    
	    frame.setVisible(true);
	    return frame;
	}
	
	/**
	 * ��͕��̎擾
	 * @return
	 */
	public JPanel getInputPanel(final JFrame frame) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		
		int y = 10;
		final JTextField method = new JTextField(5);
		method.setText("GET");
		method.setBounds(10, y, 80, 30);
		panel.add(method);
		
		final JTextField path = new JTextField(20);
		path.setText("/");
		path.setBounds(90, y, 600, 30);
		panel.add(path);
		
		JLabel httpvl = new JLabel("HTTP/");
		httpvl.setBounds(690, y, 40, 30);
		panel.add(httpvl);

		final JTextField httpv = new JTextField(5);
		httpv.setText("1.1");
		httpv.setBounds(730, y, 80, 30);
		panel.add(httpv);

		y += 30;
		JLabel hostl = new JLabel("host : ");
		hostl.setBounds(10, y, 80, 30);
		panel.add(hostl);
		
		final JTextField host = new JTextField(20);
		host.setText("www.nokiatheatrelalive.com");
		host.setBounds(90, y, 600, 30);
		panel.add(host);

		y += 30;
		JLabel headerl = new JLabel("header");
		headerl.setBounds(10, y, 80, 30);
		panel.add(headerl);

		y += 30;
		final JTextArea header = new JTextArea(5, 80);
		JScrollPane headerScrollpane = new JScrollPane(header);
		header.setLineWrap(true);
		header.setMargin(new Insets(5, 10, 5, 10));
		headerScrollpane.setBounds(10, y, 950, 200);
		panel.add(headerScrollpane);

		y += 200;
		JLabel bodyl = new JLabel("body");
		bodyl.setBounds(10, y, 80, 30);
		panel.add(bodyl);
		
		y += 30;
		final JTextArea body = new JTextArea(5, 80);
		JScrollPane bodyScrollpane = new JScrollPane(body);
		body.setLineWrap(true);
		body.setMargin(new Insets(5, 10, 5, 10));
		bodyScrollpane.setBounds(10, y, 950, 200);
		panel.add(bodyScrollpane);
		
		y += 200;
		JButton send = new JButton("send");
		send.setBounds(370, y, 80, 30);
		panel.add(send);
		
		JLabel resl = new JLabel("response");
		resl.setBounds(10, y, 80, 30);
		panel.add(resl);

		y += 30;
		final JTextArea result = new JTextArea(5, 80);
		JScrollPane resultScrollpane = new JScrollPane(result);
		result.setLineWrap(true);
		result.setMargin(new Insets(5, 10, 5, 10));
		resultScrollpane.setBounds(10, y, 950, 200);
		panel.add(resultScrollpane);
		
		final Pattern headerPattern = Pattern.compile("^\\s*([^:\\s]+)\\s*:\\s*(\\S+)\\s*$", Pattern.MULTILINE);
		send.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable(){

					@Override
					public void run() {
						result.setText("");
						HttpAgent agent = new HttpAgent(host.getText()){

							private ProgressMonitor pm;
							/**
							 * レスポンスを解析する
							 * 
							 * おばーライド用
							 */
							public void analyzeResponse() {
								result.append(this.getHeader());
								String resString;
								try {
									resString = this.getBodyString("UTF-8");
									System.out.println(resString);
									result.append(resString);
								} catch (HttpException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							/**
							 * プログレス通知用
							 *
							 * 継承したクラスで、オーバーライドして下さい。
							 *
							 * @param nowLoadLength 今回読み込みサイズ
							 * @param loadedLength 読み込み済みサイズ
							 * @param contentLength コンテントレングス。0の場合は不明
							 */
							protected void onProgress(int nowLoadLength, int loadedLength, int contentLength) {
								if (pm == null) {
									pm = new ProgressMonitor(frame, null, null, 0, contentLength);
								}
								pm.setProgress(loadedLength);
							}
						};
						Map<String, String> addtionalHeaders = new HashMap<String, String>();
						String headerString = header.getText();
						Matcher m = headerPattern.matcher(headerString);
						while (m.find()) {
							addtionalHeaders.put(m.group(1), m.group(2));
						}
						try {
							agent.send(method.getText(), path.getText(), httpv.getText(), body.getText(), addtionalHeaders);
						} catch (HttpException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}}).start();
			}
			
		});
		
		return panel;
	}
}
