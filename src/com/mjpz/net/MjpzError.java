package com.mjpz.net;

/**
 * <b>異常を表すクラス</b><br>
 * <br>
 * <p>
 * 異常発生を表します。
 * </p>
 * <p>
 * {@link #getType()}を使用することにより、異常内容を判別することができます。
 * </p>
 * <pre>
 * public void onError(MjpzError e) {
 * 	int type = e.getType();
 * 	switch(type) {
 * 	case MjpzError.PARAM_ERROR:
 * 		// パラメータエラー：ライブラリ使用時のパラメータに誤りがあるなど
 * 		:
 * 		break;
 * 	case MjpzError.RUNTIME_ERROR:
 * 		// ランタイムエラー：ライブラリ実行中に異常時発生。（認証中に端末の向きが変更されるなど）
 * 		:
 * 		break;
 * 	case MjpzError.NETWORK_ERROR:
 * 		// ネットワークエラー：通信異常など
 * 		:
 * 		break;
 * 	case MjpzError.REQUEST_ERROR:
 * 		// リクエストエラー：API実行時のリクエストエラー。HTTTPエラー400番代
 * 		:
 * 		break;
 * 	case MjpzError.AUTHORIZE_ERROR:
 * 		// 認証エラー：API実行時の認証エラー。Sandbox環境でのbasic認証エラー
 * 		:
 * 		break;
 * 	case MjpzError.INTERNAL_SERVER_ERROR:
 * 		// ネットワークエラー：サーバー異常
 * 		:
 * 		break;
 * 	case MjpzError.INTERNAL_ERROR:
 * 		// 内部エラー：ライブラリ内部での異常
 * 		:
 * 		break;
 * 	default:
 * }
 * </pre>
 * 
 * @author Tekeyuki Kawata
 */
public class MjpzError extends Throwable {
	/** serialVersionUID */
	private static final long serialVersionUID = 5913141888752605893L;

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
	 * <pre></pre>
	 * @param type エラー種別
	 * @param message メッセージ
	 * @param t エラー
	 */
	protected MjpzError(int type, String message, Throwable t) {
		super(message, t);
		this.type = type;
	}

	/**
	 * コンストラクタ
	 * <pre></pre>
	 * @param type エラー種別
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 * @see #AUTHORIZE_ERROR
	 * 
	 */
	protected MjpzError(int type) {
		super();
		this.type = type;
	}

	/**
	 * コンストラクタ
	 * <pre></pre>
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
	protected MjpzError(int type, String message) {
		super(message);
		this.type = type;
	}

	/**
	 * エラータイプの取得
	 * <pre></pre>
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

	/**
	 * メッセージ取得
	 * <pre></pre>
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Error(");
		sb.append(Integer.toString(type));
		sb.append("):");
		sb.append(super.getMessage());
		return sb.toString();
	}
}
