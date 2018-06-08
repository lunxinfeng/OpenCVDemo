package cn.mina.mina;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import cn.mina.mina.entity.MsgPack;

public class MinaPushManager {

	static String ACTION_ACTIVATE_PUSH_SERVICE = "ACTION_ACTIVATE_PUSH_SERVICE";

	static String ACTION_CONNECTION = "ACTION_CONNECTION";

	static String ACTION_CONNECTION_STATUS = "ACTION_CONNECTION_STATUS";

	static String ACTION_SENDREQUEST = "ACTION_SENDREQUEST";

	static String ACTION_DISCONNECTION = "ACTION_DISSENDREQUEST";

	static String ACTION_DESTORY = "ACTION_DESTORY";

	static String SERVICE_ACTION = "SERVICE_ACTION";

	static String KEY_SEND_BODY = "KEY_SEND_BODY";

	static String KEY_CIM_CONNECTION_STATUS = "KEY_CIM_CONNECTION_STATUS";

	/**
	 * 初始化,连接服务端，在程序启动页或者 在Application里调用
	 * 
	 * @param context
	 * @param ip
	 * @param port
	 */
	public static void init(Context context, String ip, int port) {

		Intent serviceIntent = new Intent(context, MinaPushService.class);
		serviceIntent.putExtra(SERVICE_ACTION, ACTION_CONNECTION);
		context.startService(serviceIntent);

	}

	protected static void init(Context context) {

		boolean isManualStop = CIMCacheTools.getBoolean(context,
				CIMCacheTools.KEY_MANUAL_STOP);
		boolean isManualDestory = CIMCacheTools.getBoolean(context,
				CIMCacheTools.KEY_CIM_DESTORYED);

		if (isManualStop || isManualDestory) {
			return;
		}

		String host = CIMCacheTools.getString(context,
				CIMCacheTools.KEY_CIM_SERVIER_HOST);
		int port = CIMCacheTools.getInt(context,
				CIMCacheTools.KEY_CIM_SERVIER_PORT);

		init(context, host, port);

	}

	/**
	 * 设置一个账号登录到服务端
	 * 
	 * @param account
	 *            用户唯一ID
	 */
	public static void bindAccount(Context context, String account) {

		boolean isManualDestory = CIMCacheTools.getBoolean(context,
				CIMCacheTools.KEY_CIM_DESTORYED);
		if (isManualDestory || account == null || account.trim().length() == 0) {
			return;
		}

		CIMCacheTools.putBoolean(context, CIMCacheTools.KEY_MANUAL_STOP, false);
		CIMCacheTools.putString(context, CIMCacheTools.KEY_ACCOUNT, account);

	}

	protected static void bindAccount(Context context) {

		String account = CIMCacheTools.getString(context,
				CIMCacheTools.KEY_ACCOUNT);
		bindAccount(context, account);
	}

	/**
	 * 发送一个CIM请求
	 * 
	 * @param context
	 * @body
	 */
	public static void sendRequest(Context context, MsgPack body) {

		boolean isManualStop = CIMCacheTools.getBoolean(context,
				CIMCacheTools.KEY_MANUAL_STOP);
		boolean isManualDestory = CIMCacheTools.getBoolean(context,
				CIMCacheTools.KEY_CIM_DESTORYED);

		if (isManualStop || isManualDestory) {
			return;
		}

		Intent serviceIntent = new Intent(context, MinaPushService.class);
		serviceIntent.putExtra(KEY_SEND_BODY, body);
		serviceIntent.putExtra(SERVICE_ACTION, ACTION_SENDREQUEST);
		context.startService(serviceIntent);

	}

	/**
	 * 停止接受推送，将会退出当前账号登录，端口与服务端的连接
	 * 
	 * @param context
	 */
	public static void stop(Context context) {

		boolean isManualDestory = CIMCacheTools.getBoolean(context,
				CIMCacheTools.KEY_CIM_DESTORYED);
		if (isManualDestory) {
			return;
		}

		CIMCacheTools.putBoolean(context, CIMCacheTools.KEY_MANUAL_STOP, true);

		Intent serviceIntent = new Intent(context, MinaPushService.class);
		serviceIntent.putExtra(SERVICE_ACTION, ACTION_DISCONNECTION);
		context.startService(serviceIntent);

	}

	/**
	 * 完全销毁CIM，一般用于完全退出程序，调用resume将不能恢复
	 * 
	 * @param context
	 */
	public static void destory(Context context) {

		CIMCacheTools
				.putBoolean(context, CIMCacheTools.KEY_CIM_DESTORYED, true);
		CIMCacheTools.putString(context, CIMCacheTools.KEY_ACCOUNT, null);

		Intent serviceIntent = new Intent(context, MinaPushService.class);
		serviceIntent.putExtra(SERVICE_ACTION, ACTION_DESTORY);
		context.startService(serviceIntent);

	}

	/**
	 * 重新恢复接收推送，重新连接服务端，并登录当前账号
	 * 
	 * @param context
	 */
	public static void resume(Context context) {

		boolean isManualDestory = CIMCacheTools.getBoolean(context,
				CIMCacheTools.KEY_CIM_DESTORYED);
		if (isManualDestory) {
			return;
		}

		bindAccount(context);
	}

	public static void detectIsConnected(Context context) {
		Intent serviceIntent = new Intent(context, MinaPushService.class);
		serviceIntent.putExtra(SERVICE_ACTION, ACTION_CONNECTION_STATUS);
		context.startService(serviceIntent);
	}

	private static String getVersionName(Context context) {
		String versionName = null;
		try {
			PackageInfo mPackageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			versionName = mPackageInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		return versionName;
	}

}