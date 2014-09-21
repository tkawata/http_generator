package com.mjpz.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpAgent {

	private static final boolean DEBUG = false;

	private static final String CRLF = "\r\n";
	private Pattern PATTERN_RESPONSE_FIRST_LINE = Pattern.compile("^HTTP/[0-9]+\\.[0-9]+ ([0-9]+) ([^\\n]+)", Pattern.MULTILINE);

	/** タイムアウト */
	protected static final int TIME_OUT = 30 * 1000;
	/** ソケット */
	private Socket socket;
	/** チャネル */
	private SocketChannel ch;
	/** ホスト名 */
	private String host;
	/** ユーザーエージェント */
	private String userAgent;
	/** リクエスト文字列のバッファ */
	private StringBuilder requestBuffer = new StringBuilder();
	/** レスポンスコード */
	private int responseCode;
	/** レスポンスステータス */
	private String responseState;
	/** コンテントレングス */
	private int contentLength;
	/** レスポンスヘッダー */
	private String header;
	/** リクエストボディ */
	private byte[] body;
	/** ダウンロード先 */
	private FileChannel outputChanel;
	/** キャンセル */
	private boolean isCancelled;

	/**
	 * コンストラクタ
	 * 
	 * <pre></pre>
	 * 
	 * @param host ホスト名
	 */
	protected HttpAgent(String host) {
		this.host = host;
	}

	/**
	 * コンストラクタ
	 * 
	 * <pre></pre>
	 * 
	 * @param host ホスト名
	 * @param userAgent ユーザーエージェント
	 */
	protected HttpAgent(String host, String userAgent) {
		this.host = host;
		this.userAgent = userAgent;
	}

	/**
	 * HTTPレスポンスコードの取得
	 * 
	 * @return
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * HTTPレスポンスの取得ステータスの取得
	 * 
	 * @return
	 */
	public String getResponseState() {
		return responseState;
	}

	/**
	 * HTTPヘッダーの取得
	 * 
	 * @return
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * HTTPバディの取得
	 * 
	 * @return
	 */
	public byte[] getBody() {
		return body;
	}

	/**
	 * ホスト名の取得
	 * 
	 * <pre></pre>
	 * 
	 * @return
	 */
	public String getHost() {
		return this.host;
	}

	/**
	 * リクエスト送信
	 * 
	 * @throws IOException
	 */
	public void send(String method, String path, String version, String body, Map<String, String> addtionalHeaders) throws MjpzError {
		try {
			this.connect();
			if (DEBUG) {
				System.out.println("====================");
			}
			this.pushRequest(method, path, version, body, addtionalHeaders);
			if (DEBUG) {
				System.out.println(getRequest());
				System.out.println("--------------------");
			}
			this.pullResponse();
			this.analyzeResponse();
		} catch (IOException e) {
			throw new MjpzError(MjpzError.NETWORK_ERROR, "Network Error:" + e.getMessage(), e);
		} finally {
			this.close();
		}
	}

	/**
	 * リクエストを送る
	 */
	protected void pushRequest(String method, String path, String version, String body, Map<String, String> addtionalHeaders) throws MjpzError, IOException {
		push(method);
		push(" ");
		push(path);
		push(" HTTP/");
		push(version);
		newLine();

		push("Host:");
		push(getHost());
		newLine();

		if (addtionalHeaders != null) {
			Set<String> keys = addtionalHeaders.keySet();
			for (String key : keys) {
				push(key);
				push(":");
				push(addtionalHeaders.get(key));
				newLine();
			}
		}

		if ((addtionalHeaders == null || !addtionalHeaders.containsKey("User-Agent")) && userAgent != null) {
			push("User-Agent:");
			push(userAgent);
			newLine();
		}

//		if (body != null && body.length() > 0) {
//			push("Content-Type:");
//			push("application/json");
//			newLine();
//		}

		push("Content-Length: ");
		push(Integer.toString(body.getBytes().length));
		newLine();

		newLine();

		push(body);
	}

	/**
	 * レスポンスを解析する
	 * 
	 * おばーライド用
	 */
	public void analyzeResponse() {
	}

	/**
	 * コネクションを貼る
	 * 
	 * @throws IOException
	 */
	private void connect() throws IOException {
		SocketAddress endpoint = new InetSocketAddress(getHost(), 80);
		socket = new Socket();
		ch = SocketChannel.open(endpoint);
	}

	/**
	 * レスポンスを引っ張る
	 * 
	 * @throws IOException
	 */
	private void pullResponse() throws IOException, MjpzError {
		ByteBuffer bodybuf = null;
		ByteBuffer wkBuf = ByteBuffer.allocate(128 * 1024);
		int loadedLength = 0;
		long length = 0;
		long bodyPos = 0;
		do {
			if (isCancelled) {
				break;
			}
			length = ch.read(wkBuf);
			wkBuf.flip();
			while (bodyPos == 0 && wkBuf.position() < wkBuf.limit()) {
				byte b = wkBuf.get();
				int pos = wkBuf.position();
				if (b == 0x0a && wkBuf.get(pos - 1) == 0x0a && wkBuf.get(pos - 2) == 0x0d && wkBuf.get(pos - 3) == 0x0a) {
					// headerの終わり
					byte[] dst = new byte[pos];
					wkBuf.position(0);
					wkBuf.get(dst, 0, pos);
					header = new String(dst);
					if (DEBUG) {
						System.out.println(header);
					}
					wkBuf.position(pos);
					bodyPos = pos;
					break;
				}
			}
			if (bodyPos != 0) {
				if (responseCode == 0) {
					Matcher matcher = PATTERN_RESPONSE_FIRST_LINE.matcher(header);
					if (matcher.find()) {
						String responseCodeString = matcher.group(1);
						responseCode = Integer.parseInt(responseCodeString);

						responseState = matcher.group(2);
						if (DEBUG) {
							System.out.printf("responseCode=%d, responseState=%s\n", responseCode, responseState);
						}
					}
				}
				if (contentLength == 0) {
					Matcher matcher = Pattern.compile("Content-Length[\\s]*:[\\s]*([0-9]+)").matcher(header);
					if (matcher.find()) {
						String contentLengthString = matcher.group(1);
						contentLength = Integer.parseInt(contentLengthString);
						bodybuf = ByteBuffer.allocate(contentLength);
						if (DEBUG) {
							System.out.printf("contentLength=%d\n", contentLength);
						}
					} else {
						bodybuf = ByteBuffer.allocate(128 * 1024 * 10);
					}
				}

				int nowLoadLength = wkBuf.limit() - wkBuf.position();
				if (responseCode == 200 && outputChanel != null) {
					outputChanel.write(wkBuf);
				} else {
					bodybuf.put(wkBuf);
				}
				// プログレス
				loadedLength += nowLoadLength;
				onProgress(nowLoadLength, loadedLength, contentLength);
			}
			wkBuf.clear();
		} while (length > 0);
		
		if (bodybuf != null) {
			bodybuf.flip();
			body = new byte[bodybuf.limit()];
			bodybuf.get(body);
		}

		if (DEBUG) {
			System.out.println(getResponseDump());
			if (body != null) {
				System.out.println(new String(body));
			}
		}

		if (responseCode != 200) {
			throw new HttpError(responseCode, responseState, getRequest(), header, body != null ? new String(body) : "", getResponseDump());
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
	}

	/**
	 * 文字列をサーバーに送る
	 * 
	 * @param str
	 * @throws IOException
	 */
	protected void push(String str) throws IOException {
		requestBuffer.append(str);
		ch.write(ByteBuffer.wrap(str.getBytes()));
	}

	/**
	 * 改行コードを送る
	 * 
	 * @throws IOException
	 */
	protected void newLine() throws IOException {
		requestBuffer.append(CRLF);
		ch.write(ByteBuffer.wrap(CRLF.getBytes()));
	}

	/**
	 * リクエストを取得する
	 * 
	 * <pre></pre>
	 * 
	 * @return
	 */
	public String getRequest() {
		return requestBuffer.toString();
	}

	/**
	 * レスポンスのダンプを取得する
	 * 
	 * <pre></pre>
	 * 
	 * @return
	 */
	public String getResponseDump() {
		if (body == null) {
			return "";
		}
		return getDump(body);
	}

	/**
	 * ダンプ取得
	 * 
	 * <pre></pre>
	 * 
	 * @param bytes
	 * @return
	 */
	private String getDump(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		final String indexLabel = " %s\n %05d ";
		final String chipLabel = "%02x ";
		int index = 0;
		sb.append(String.format(indexLabel, "", index));
		ByteBuffer lineBuffer = ByteBuffer.allocate(8);
		for (byte b : bytes) {
			lineBuffer.put(b);
			sb.append(String.format(chipLabel, b));
			if (index++ > 0 && index % 8 == 0) {
				lineBuffer.flip();
				sb.append(String.format(indexLabel, new String(lineBuffer.array()), index));
				lineBuffer.clear();
			}
		}
		sb.append(String.format(indexLabel, "", index));
		return sb.toString();
	}

	/**
	 * ディスクリプタを解放
	 */
	public void close() {
		if (ch != null) {
			try {
				ch.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * userAgentの取得
	 * 
	 * <pre></pre>
	 * 
	 * @return userAgentを返す。
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * userAgentの設定
	 * 
	 * <pre></pre>
	 * 
	 * @param userAgent を userAgent に設定する。
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * ファイルチャネル登録
	 * 
	 * <pre></pre>
	 * 
	 * @param outputFile
	 * @throws MjpzError
	 */
	public void setOutputFile(File outputFile) throws MjpzError {
		try {
			@SuppressWarnings("resource")
			RandomAccessFile rwFile = new RandomAccessFile(outputFile, "rw");
			outputChanel = rwFile.getChannel();
		} catch (FileNotFoundException e) {
			throw new MjpzError(MjpzError.PARAM_ERROR, "No Exists:" + e.getMessage(), e);
		}
	}

	/**
	 * contentLengthの取得
	 * 
	 * <pre></pre>
	 * 
	 * @return contentLengthを返す。
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * contentLengthの設定
	 * 
	 * <pre></pre>
	 * 
	 * @param contentLength を contentLength に設定する。
	 */
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * isCanceledの取得
	 * <pre></pre>
	 * @return isCanceledを返す。
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * isCanceledの設定
	 * <pre></pre>
	 * @param isCancelled を isCanceled に設定する。
	 */
	public void cancel() {
		this.isCancelled = true;
	}

}

