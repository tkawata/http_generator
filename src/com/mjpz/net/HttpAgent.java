package com.mjpz.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class HttpAgent {

	private Log log;
	private static final String CRLF = "\r\n";
	private Pattern PATTERN_RESPONSE_FIRST_LINE = Pattern.compile("^HTTP/[0-9]+\\.[0-9]+ ([0-9]+) ([^\\n]+)", Pattern.MULTILINE);
	private Pattern PATTERN_LOCATION = Pattern.compile("Location\\s*:\\s*https?://([^/]+)(\\S+)", Pattern.MULTILINE);
	private Pattern PATTERN_GZIP = Pattern.compile(".*gzip.*");
	/** タイムアウト */
	protected static final int TIME_OUT = 30 * 1000;
	/**
	 * 302 Found時に遷移するか。デフォルトtrue
	 */
	private boolean movedTemporarily = true;
	/**
	 * Gzipであるか
	 */
	private boolean gzipFlag;
	/** ソケット */
	private Socket socket;
	/** チャネル */
	private SocketChannel ch;
	/** ホスト名 */
	private String host;
	/** メソッド */
	private String method;
	/** バージョン */
	private String version;
	private Map<String, String> addtionalHeaders;
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
	private ByteBuffer body;
	/** ダウンロード先 */
	private FileChannel outputChanel;
	/** キャンセル */
	private boolean isCancelled;
	/** ProgressMonitor on/off */
	private boolean enableProgressMonitor;

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
	
	public void purge() {
		if (body == null) {
			return;
		}
		if (log != null) {
			log.debug(String.format("--purge--%s", this.body.toString()));
		}
		body.clear();
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
		
		body.flip();
		byte[] dst = new byte[body.limit()];
		body.get(dst);
		if (log != null) {
			log.debug(String.format("--getBody--%s", this.body.toString()));
		}
		
		return dst;
	}

	/**
	 * HTTPバディの取得
	 *
	 * @return
	 * @throws HttpException 
	 */
	public String getBodyString(String encode) throws HttpException {
		byte[] dst = getBody();
		String result;
		try {
			result = new String(dst, encode);
			this.purge();
			return result;
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(HttpException.INTERNAL_ERROR, "encode", e);
		}
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
	 * 初期化
	 * 
	 * <pre></pre>
	 */
	private void init() {
		this.purge();
		this.gzipFlag = false;
		this.requestBuffer = new StringBuilder();
		this.contentLength = 0;
		this.responseCode = 0;
		this.responseState = null;
		this.header = null;
	}

	/**
	 * リクエスト送信
	 *
	 * @throws IOException
	 */
	public void send(String method, String path, String version, String body, Map<String, String> addtionalHeaders) throws HttpException {
		this.method = method;
		this.version = version;
		this.addtionalHeaders = addtionalHeaders;
		this.init();
		try {
			this.connect();
			if (log != null) {
				log.debug("====================");
			}
			this.pushRequest(method, path, version, body, addtionalHeaders);
			if (log != null) {
				log.debug(getRequest());
				log.debug("--------------------");
			}
			this.pullResponse();
			this.analyzeResponse();
		} catch (IOException e) {
			throw new HttpException(HttpException.NETWORK_ERROR, 
					String.format("Network Error:%s, host:%s, path:%s", e.getMessage(), this.host, path), e);
		} finally {
			this.close();
		}
	}

	/**
	 * リクエストを送る
	 */
	protected void pushRequest(String method, String path, String version, String body, Map<String, String> addtionalHeaders) throws HttpException, IOException {
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
				String value = addtionalHeaders.get(key);
				if (outputChanel == null && key.equalsIgnoreCase("Accept-Encoding") && PATTERN_GZIP.matcher(value).matches()) {
					this.gzipFlag = true;
				}
				push(key);
				push(":");
				push(value);
				newLine();
			}
		}
		if ((addtionalHeaders == null || !addtionalHeaders.containsKey("User-Agent")) && userAgent != null) {
			push("User-Agent:");
			push(userAgent);
			newLine();
		}
// if (body != null && body.length() > 0) {
// push("Content-Type:");
// push("application/json");
// newLine();
// }
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
	private void pullResponse() throws IOException, HttpException {
		ByteBuffer wkBuf = ByteBuffer.allocate(128 * 1024);
		int loadedLength = 0;
		long length = 0;
		long bodyPos = 0;
		File temp = null;
		FileChannel output = null;
		if (outputChanel != null) {
			output = outputChanel;
		} else if (this.gzipFlag) {
			temp = File.createTempFile(String.format("%s_%d_%s", "hazip", System.nanoTime(), Thread.currentThread().getName()), ".zip");
			if (log != null) {
				log.debug(temp.getAbsolutePath());
				log.debug("-----");
			}
			output = new RandomAccessFile(temp, "rw").getChannel();
		}
		
		try {
			HttpTimer timer = new HttpTimer();
		do {
			if (isCancelled) {
				break;
			}
			// タイマー
			timer.check(ch);
			try {
			length = ch.read(wkBuf);
			} catch (java.nio.channels.AsynchronousCloseException e) {
				break;
			}
			timer.cancel();
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
					if (log != null) {
						log.debug(header);
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
						if (log != null) {
							log.debug(String.format("responseCode=%d, responseState=%s", responseCode, responseState));
						}
					}
				}
				if (contentLength == 0) {
					Matcher matcher = Pattern.compile("Content-Length[\\s]*:[\\s]*([0-9]+)").matcher(header);
					if (matcher.find()) {
						String contentLengthString = matcher.group(1);
						contentLength = Integer.parseInt(contentLengthString);
						body = ByteBuffer.allocate(contentLength);
						if (log != null) {
							log.debug(String.format("contentLength=%d", contentLength));
						}
					} else {
						contentLength =128 * 1024 * 10;
						body = ByteBuffer.allocate(contentLength);
					}
				}
				int nowLoadLength = wkBuf.limit() - wkBuf.position();
				if (responseCode == 200 && output != null) {
					output.write(wkBuf);
				} else {
					body.put(wkBuf);
				}
// プログレス
				loadedLength += nowLoadLength;
				onProgress(nowLoadLength, loadedLength, contentLength);
				if ((contentLength > 0 && loadedLength >= contentLength)) {
					break;
				}
			}
			if (log != null) {
				log.debug("length=" + length);
				log.debug("isConnected=" + ch.isConnected());
			}
			wkBuf.clear();
		} while (length > 0);
		if (log != null && contentLength < 1024 && !this.gzipFlag) {
//			log.debug(getResponseDump());
			if (body != null) {
				log.debug(new String(getBody()));
				log.debug("body=" + body);
				log.debug("loadedLength=" + loadedLength);
			}
		}
		if (this.movedTemporarily && 300 <= responseCode && responseCode < 400) {
			Matcher m = PATTERN_LOCATION.matcher(header);
			if (m.find()) {
				String host = m.group(1);
				String path = m.group(2);
				this.host = host;
				this.purge();
				this.send(method, path, version, "", addtionalHeaders);
			}
			return;
		}
		if (responseCode != 200) {
			throw new HttpException(responseCode, responseState, getRequest(), header, body != null ? new String(getBody()) : "", getResponseDump());
		}
		if (temp != null) {
			// gzip解凍
			output.close();
			ReadableByteChannel gzch = Channels.newChannel(new BufferedInputStream(new GZIPInputStream(new FileInputStream(temp))));
			try {
				this.body = ByteBuffer.allocate(128 * 1024 * 10);
				do {
					length = gzch.read(wkBuf);
					wkBuf.flip();
					this.body.put(wkBuf);
					wkBuf.clear();
				} while (length > 0);
			} finally {
				gzch.close();
			}
		}
		} finally {
			if (outputChanel != null) {
				outputChanel.close();
			}
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
		return getDump(getBody());
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
				try {
					sb.append(String.format(indexLabel, new String(lineBuffer.array(), "UTF-8"), index));
				} catch (UnsupportedEncodingException e) {
					sb.append(String.format(indexLabel, new String(lineBuffer.array()), index));
				}
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
	 * @throws HttpException
	 */
	public void setOutputFile(File outputFile) throws HttpException {
		try {
			@SuppressWarnings("resource")
			RandomAccessFile rwFile = new RandomAccessFile(outputFile, "rw");
			outputChanel = rwFile.getChannel();
		} catch (FileNotFoundException e) {
			throw new HttpException(HttpException.PARAM_ERROR, "No Exists:" + e.getMessage(), e);
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
	 * 
	 * <pre></pre>
	 * 
	 * @return isCanceledを返す。
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * isCanceledの設定
	 * 
	 * <pre></pre>
	 * 
	 * @param isCancelled を isCanceled に設定する。
	 */
	public void cancel() {
		this.isCancelled = true;
	}

	/**
	 * logの設定
	 * <pre></pre>
	 * @param log を log に設定する。
	 */
	public void setLog(Log log) {
		this.log = log;
	}

	public boolean isMovedTemporarily() {
		return movedTemporarily;
	}

	public void setMovedTemporarily(boolean movedTemporarily) {
		this.movedTemporarily = movedTemporarily;
	}

}
