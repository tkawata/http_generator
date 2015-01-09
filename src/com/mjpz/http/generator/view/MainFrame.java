package com.mjpz.http.generator.view;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mjpz.net.HttpAgent;
import com.mjpz.net.HttpException;
import com.mjpz.net.Log;

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
		JLabel urll = new JLabel("URL : ");
		urll.setBounds(10, y, 80, 30);
		panel.add(urll);
		
		final JTextField url = new JTextField(20);
		url.setText("http://google.com/");
		url.setBounds(90, y, 800, 30);
		panel.add(url);

		y += 30;
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
		host.setText("google.com");
		host.setBounds(90, y, 600, 30);
		panel.add(host);
		
		url.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void insertUpdate(DocumentEvent de) {
				String hostStrOrg = url.getText();
				String hostStr = hostStrOrg.replaceFirst("https*://", "");
				int index = hostStr.indexOf('/');
				if (index > 0) {
					path.setText(hostStr.substring(index));
					hostStr = hostStr.substring(0, index);
				}
				if (!hostStrOrg.equals(hostStr)) {
					host.setText(hostStr);
				}
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		JLabel csl = new JLabel("charset : ");
		csl.setBounds(690, y, 80, 30);
		panel.add(csl);
		
		final JTextField charset = new JTextField(20);
		charset.setText("UTF-8");
		charset.setBounds(770, y, 100, 30);
		panel.add(charset);

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
						final MjpzProgressDialog dialog = new MjpzProgressDialog(frame, false);
						result.setText("");
						HttpAgent agent = new HttpAgent(host.getText()){
							/**
							 * レスポンスを解析する
							 * 
							 * おばーライド用
							 */
							public void analyzeResponse() {
								result.append(this.getHeader());
								String resString;
								try {
									resString = this.getBodyString(charset.getText());
									result.append(resString);
								} catch (HttpException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								dialog.setVisible(false);
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
								if (!dialog.isVisible()) {
									dialog.init(host.getText(), 0, contentLength);
									dialog.setVisible(true);
								}
								dialog.setProgress(loadedLength);
							}

						};
                                                
                                                Log log = new Log();
                                                log.setDebug(true);
                                                agent.setLog(log);
                                                
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
							dialog.setVisible(false);
						}
					}}).start();
			}
			
		});
		
		return panel;
	}
}
