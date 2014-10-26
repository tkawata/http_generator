package com.mjpz.net;

import java.io.IOException;

public class HttpException extends IOException {

	/**
	 * type - パラメータエラー：ライブラリ使用時のパラメータに誤りがある
	 */
	public static final int PARAM_ERROR = 1000001;
	/**
	 * type - ランタイムエラー：ライブラリ実行中に異常時発生。（認証中に端末の向きが変更されるなど）
	 */
	public static final int RUNTIME_ERROR = 1000002;
	/**
	 * type - ネットワークエラー：通信異常。リクエストエラーなど
	 */
	public static final int NETWORK_ERROR = 1000401;
	/**
	 * type - リクエストエラー：API実行時のリクエストエラー。HTTTPエラー400番代
	 */
	public static final int REQUEST_ERROR = 2000400;
	/**
	 * type - 認証エラー：API実行時の認証エラー。Sandbox環境でのbasic認証エラー
	 */
	public static final int AUTHORIZE_ERROR = 2000401;
	/**
	 * type - ネットワークエラー：サーバー異常
	 */
	public static final int INTERNAL_SERVER_ERROR = 2000500;
	/**
	 * type - 内部エラー：ライブラリ内部での異常
	 */
	public static final int INTERNAL_ERROR = 5000001;
	/**
	 * エラータイプ
	 * 
	 * @see #PARAM_ERROR
	 * @see #RUNTIME_ERROR
	 * @see #NETWORK_ERROR
	 * @see #REQUEST_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #INTERNAL_SERVER_ERROR
	 * @see #INTERNAL_ERROR
	 */
	private int type;

	/**
	 * コンストラクタ
	 * 
	 * <pre></pre>
	 * 
	 * @param type エラー種別
	 * @param message メッセージ
	 * @param t エラー
	 */
	protected HttpException(int type, String message, Throwable t) {
		super(message, t);
		this.type = type;
	}

	/**
	 * コンストラクタ
	 * 
	 * <pre></pre>
	 * 
	 * @param type エラー種別
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 *
	 */
	protected HttpException(int type) {
		super();
		this.type = type;
	}

	/**
	 * コンストラクタ
	 * 
	 * <pre></pre>
	 * 
	 * @param type エラー種別
	 * @param message メッセージ
	 * @see #PARAM_ERROR
	 * @see #RUNTIME_ERROR
	 * @see #NETWORK_ERROR
	 * @see #REQUEST_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #INTERNAL_SERVER_ERROR
	 * @see #INTERNAL_ERROR
	 */
	protected HttpException(int type, String message) {
		super(message);
		this.type = type;
	}

	/**
	 * エラータイプの取得
	 * 
	 * <pre></pre>
	 * 
	 * @return typeを返す。
	 * @see #PARAM_ERROR
	 * @see #RUNTIME_ERROR
	 * @see #NETWORK_ERROR
	 * @see #REQUEST_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #INTERNAL_SERVER_ERROR
	 * @see #INTERNAL_ERROR
	 */
	public int getType() {
		return type;
	}

	private static final boolean DEBUG = false;
	/** serialVersionUID */
	private static final long serialVersionUID = -2709097549608759993L;
	private int responseCode;
	private String responseState;
	private String request;
	private String header;
	private String body;
	private String dump;
	private String addtion;

	/**
	 * コンストラクタ
	 *
	 * <pre></pre>
	 *
	 * @param responseCode
	 * @param responseState
	 * @param request
	 * @param header
	 * @param body
	 * @param dump
	 */
	protected HttpException(int responseCode, String responseState, String request, String header, String body, String dump) {
		this(getErrorCode(responseCode));
		this.responseCode = responseCode;
		this.responseState = responseState;
		this.request = request;
		this.header = header;
		this.body = body;
		this.dump = dump;
	}

	/**
	 * コンストラクタ
	 * 
	 * <pre></pre>
	 * 
	 * @param errorCode
	 * @param description
	 * @param failingUrl
	 */
	protected HttpException(int errorCode, String description, String failingUrl) {
		this(HttpException.INTERNAL_SERVER_ERROR);
		this.responseCode = errorCode;
		this.responseState = description;
		this.request = failingUrl;
	}

	/**
	 * メッセージ取得
	 *
	 * <pre></pre>
	 *
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Error(");
		sb.append(Integer.toString(type));
		sb.append("):");
		if (responseCode > 0) {
			sb.append(responseCode);
			sb.append(" - ");
			sb.append(responseState);
			if (DEBUG) {
				sb.append("\n");
				sb.append("---\n");
				sb.append(request);
				sb.append("---\n");
				sb.append(header);
				sb.append(body);
				if (addtion != null) {
					sb.append("\n");
					sb.append("---\n");
					sb.append(addtion);
				}
			}
		}
		sb.append(super.getMessage());
		return sb.toString();
	}

	/**
	 * responseCodeの取得
	 *
	 * <pre></pre>
	 *
	 * @return responseCodeを返す。
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * responseCodeの設定
	 *
	 * <pre></pre>
	 *
	 * @param responseCode を responseCode に設定する。
	 */
	protected void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * responseStateの取得
	 *
	 * <pre></pre>
	 *
	 * @return responseStateを返す。
	 */
	public String getResponseState() {
		return responseState;
	}

	/**
	 * responseStateの設定
	 *
	 * <pre></pre>
	 *
	 * @param responseState を responseState に設定する。
	 */
	protected void setResponseState(String responseState) {
		this.responseState = responseState;
	}

	/**
	 * requestの取得
	 *
	 * <pre></pre>
	 *
	 * @return requestを返す。
	 */
	public String getRequest() {
		return request;
	}

	/**
	 * requestの設定
	 *
	 * <pre></pre>
	 *
	 * @param request を request に設定する。
	 */
	protected void setRequest(String request) {
		this.request = request;
	}

	/**
	 * headerの取得
	 *
	 * <pre></pre>
	 *
	 * @return headerを返す。
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * headerの設定
	 *
	 * <pre></pre>
	 *
	 * @param header を header に設定する。
	 */
	protected void setHeader(String header) {
		this.header = header;
	}

	/**
	 * bodyの取得
	 *
	 * <pre></pre>
	 *
	 * @return bodyを返す。
	 */
	public String getBody() {
		return body;
	}

	/**
	 * bodyの設定
	 *
	 * <pre></pre>
	 *
	 * @param body を body に設定する。
	 */
	protected void setBody(String body) {
		this.body = body;
	}

	/**
	 * dumpの取得
	 *
	 * <pre></pre>
	 *
	 * @return dumpを返す。
	 */
	public String getDump() {
		return dump;
	}

	/**
	 * dumpの設定
	 *
	 * <pre></pre>
	 *
	 * @param dump を dump に設定する。
	 */
	protected void setDump(String dump) {
		this.dump = dump;
	}

	/**
	 * serialversionuidの取得
	 *
	 * <pre></pre>
	 *
	 * @return serialversionuidを返す。
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * addtionの取得
	 * 
	 * <pre></pre>
	 * 
	 * @return addtionを返す。
	 */
	public String getAddtion() {
		return addtion;
	}

	/**
	 * addtionの設定
	 * 
	 * <pre></pre>
	 * 
	 * @param addtion を addtion に設定する。
	 */
	protected void setAddtion(String addtion) {
		this.addtion = addtion;
	}

	/**
	 * エラーコードの取得
	 * 
	 * <pre></pre>
	 * 
	 * @param responseCode
	 * @return
	 */
	private static int getErrorCode(int responseCode) {
		if (responseCode == 401) {
			return HttpException.AUTHORIZE_ERROR;
		} else if (responseCode < 500) {
			return HttpException.REQUEST_ERROR;
		}
		return HttpException.INTERNAL_SERVER_ERROR;
	}
}
