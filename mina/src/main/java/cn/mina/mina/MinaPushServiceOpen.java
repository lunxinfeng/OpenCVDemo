package cn.mina.mina;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.google.gson.Gson;

import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.izis.mina.R;
import cn.mina.Mina;
import cn.mina.bean.MinaServicesMessage;
import cn.mina.constant.PublicClass;
import cn.mina.bean.UserScore;
import cn.mina.db.MessageService;
import cn.mina.mina.entity.ChatMsg;
import cn.mina.mina.entity.Message;
import cn.mina.mina.entity.MsgPack;
import cn.mina.util.ConnectionChangeReceiver;
import cn.mina.util.SharedPrefsUtil;

public class MinaPushServiceOpen extends Service implements IMsgCallback {

	// 消息体封装
	private MinaServicesMessage MSM = new MinaServicesMessage();

	private Gson gson = new Gson();
	// 服务广播标识
	public final static String TAG = "izis_MinaPushServiceOpen_yzcd";
	// 默认端口
	protected final static int OPEN_PORT = 8092;
	// 用户编码
	private String userid = "";
	// 动态口令
	private String DynamicKey = "";
	// 信息中转承载
	private String json = "";
	// 用户名称
	private String realname = "";
	// 房间类型
	private int sType;
	// 房间编号
	private int gameid;
	// 段位等级
	private int gameLevel = 0;
	// 启动标识
	private int again = 0;
	// 系统广播
	private ConnectionChangeReceiver myReceiver;
	// 连接封装
	private static MinaConnectManagerOpen manager_open;
	// 端口号
	private static final int myPORT = 8092;
	private MessageService msgService;

	private int otherId;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		DynamicKey = SharedPrefsUtil.getValue(this, "user_dypassword", "-");
		int userId = SharedPrefsUtil.getValue(this, "user_id", 0);
		userid = String.valueOf(userId);
		realname = SharedPrefsUtil.getValue(this, "user_name", "");
		manager_open = MinaConnectManagerOpen.getManager(
				this.getApplicationContext(), this, 0);
		registerReceiver();

		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub

		if (intent != null && intent.hasExtra("again")) {
			again = intent.getIntExtra("again", 0);
		}
		if (again == 0) {
			int new_sType = 0;
			int new_gameid = 0;

			if (intent != null) {
				gameLevel = intent.getIntExtra("gamelevel", 0);
				new_sType = intent.getIntExtra("type", 20);
				new_gameid = intent.getIntExtra("gameid", 0);
				sType = new_sType;
				gameid = new_gameid;
				otherId = intent.getIntExtra("otherId",0);
			}
			if (manager_open.isConnected()) {

				String msgJson = "[{\"id\":\"" + gameid + "\",\"userid\":\""
						+ userid + "\",\"stype\":\"" + "I"
						+ "\",\"realName\":\"" + realname + "\",\"gameLevel\":\"" + gameLevel + "\"}]";
				sendMsg(103, gameid, otherId, msgJson); // 目的是为了设置后台MINA连接（SESSION）groupid

				String msgJson2 = "[{\"room_stype\":\"" + new_sType + "\"}]";
				sendMsg(107, new_gameid, otherId, msgJson2);

			} else {
				manager_open.connect(PublicClass.ADDRESS, myPORT);
			}
		} else {
			if (!manager_open.isConnected()) {
				manager_open.connect(PublicClass.ADDRESS, myPORT);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public static void reConnected(){
		manager_open.connect(PublicClass.ADDRESS, myPORT);
	}

	public static boolean isConnected() {
		if (manager_open == null)
			return false;

		return manager_open.isConnected();
	}

	public static boolean isInit() {
		if (manager_open == null) {
			return false;
		}
		return manager_open.isInit();
	}

	public static void closeSession() {
		manager_open.closeSession();
	}

	public static void sendMsg(MsgPack msg) {
		manager_open.send(msg);
	}

	public static void sendMsg(int Method, int gameid, int otherUser, String msg) {
		if (manager_open != null) {
			manager_open.send(Method, gameid, otherUser, msg);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		manager_open.destroy();
		this.unregisterReceiver(myReceiver);
		manager_open = null;
		super.onDestroy();
		System.out.println(Thread.currentThread() + "我是onDestroy！！！");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clientReceiveMsg(MsgPack mp) {
		// TODO Auto-generated method stub
		Message message = ChatMsg.parseJsonToObject(mp
				.getMsgPack());
		message.setGroupId(String.valueOf(mp.getMsgGroupId()));
		message.setToId(String.valueOf(mp.getMsgToID()));

		if (message.getGroupId().equals("0") && !message.getToId().equals("0")) { // 表示私聊
			if (!userid.equals(message.getSend_userid())) { // 表示收到别人发来的消息
				// showNotificationOnWechat(mp.getMsgPack());
				handleChatMsg(message); // 综合上两个if条件，处理收到的别人发的私聊信息。
				return;
			}
		}
		MSM.setCode(2);
		MSM.setStringMessage(gson.toJson(message));
		json = gson.toJson(MSM);
		Intent myIntent = new Intent();// 创建Intent对象
		myIntent.setAction(TAG);
		myIntent.putExtra("Date", json);
		sendBroadcast(myIntent);

	}

	@Override
	public void clientConnect(String msg, IoSession session) {
		// TODO Auto-generated method stub
		String msgJson = "";
		Date now = new Date();
		SimpleDateFormat f1 = new SimpleDateFormat();
		msgJson = "[{\"id\":\"" + userid + "\",\"send_ctn\":\"" + "进入房间"
				+ "\",\"send_date\":\"" + f1.format(now) + "\",\"roomType\":\""
				+ sType + "\",\"send_person\":\"" + realname
				+ "\",\"DynamicKey\":\"" + DynamicKey + "\",\"gameLevel\":\"" + gameLevel + "\"}]";
		if(gameid>0)
		{
			manager_open.send(1, gameid, 0, msgJson);
		}else
		{
			manager_open.send(1, sType, 0, msgJson);
		}
	
		manager_open.AgainRegister();

	}

	/**
	 * @param method
	 *            除聊天指令2之外的其他指令，包括注册指令
	 * @param msg
	 *            : MsgPack主体内容 JOSN格式
	 */
	@Override
	public void clientReceiveChessStep(String msg, int method) {

		// 在做其他事务前，先增加一道判断，对于用户注册消息（指令1）、用户连接关闭消息（指令104）、用户进入房间消息（103）
		// 额外发送广播，通知用户列表刷新。
		try {
			doWithUserState(msg, method);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MsgPack myPack = new MsgPack();
		myPack.setMsgMethod(method);
		myPack.setMsgPack(msg);
		MSM.setCode(1);
		MSM.setMyPack(myPack);
		json = gson.toJson(MSM);
		Intent myIntent = new Intent();// 创建Intent对象
		myIntent.setAction(TAG);
		myIntent.putExtra("Date", json);
		sendBroadcast(myIntent);
	}

	/**
	 * 
	 * @param msgPack
	 *            消息体
	 * @param method
	 *            方法代号
	 */
	private void doWithUserState(String msgPack, int method) throws Exception {
		JSONArray arrays;
		JSONObject JsonObj;

		switch (method) {
		case 1: // 用户注册进入房间

			arrays = new JSONArray(msgPack);
			JsonObj = arrays.getJSONObject(0);
			if (!userid.equals(JsonObj.getString("id"))) {
				UserScore us = new UserScore();
				us.setUserid(Integer.parseInt(JsonObj.getString("id")));
				us.setUsername(JsonObj.getString("send_person"));
				us.setGamelevel(Integer.parseInt(JsonObj.getString("gameLevel")));
				Intent myIntent = new Intent();// 创建Intent对象
				myIntent.setAction("UserList");
				myIntent.putExtra("Date", gson.toJson(us));
				myIntent.putExtra("stype", "1");
				sendBroadcast(myIntent);
			}

			break;
		case 103:
			
			arrays = new JSONArray(msgPack);
			JsonObj = arrays.getJSONObject(0);
			if (!userid.equals(JsonObj.getString("userid"))) {
				UserScore us = new UserScore();
				us.setUserid(Integer.parseInt(JsonObj.getString("id")));
				us.setUsername(JsonObj.getString("send_person"));
				us.setGamelevel(Integer.parseInt(JsonObj.getString("gameLevel")));
				String state=JsonObj.getString("stype");
			
				Intent myIntent = new Intent();// 创建Intent对象
				myIntent.setAction("UserList");
				myIntent.putExtra("Date", gson.toJson(us));
				if(state.equals("I"))
				{
					myIntent.putExtra("stype", "1");
				}else
				{
					myIntent.putExtra("stype", "2");
				}
				sendBroadcast(myIntent);
			}
			break;
		case 104: // 用户连接断开
			arrays = new JSONArray(msgPack);
			JsonObj = arrays.getJSONObject(0);
			UserScore us = new UserScore();
			us.setUserid(Integer.parseInt(JsonObj.getString("id")));
			Intent myIntent = new Intent();// 创建Intent对象
			myIntent.setAction("UserList");
			myIntent.putExtra("Date", gson.toJson(us));
			myIntent.putExtra("stype", "2");
			sendBroadcast(myIntent);

			break;
		default:

		}

	}

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		myReceiver = new ConnectionChangeReceiver();
		this.registerReceiver(myReceiver, filter);
	}

	/**
	 * 处理收到的别人的私聊消息，主要分两种情况（1）当前在私聊窗口中，通过广播方式发出消息即可。（2）不在私聊窗口，里面通过推送通知的方式告知，
	 * 并且存储消息。
	 * 
	 * @param message
	 */
	private void handleChatMsg(Message message) {

		userid = SharedPrefsUtil
				.getValue(getApplicationContext(), "userid", "");

		message.setTopic("mygo" + userid + "To" + message.getSend_userid());

		if (SharedPrefsUtil.getValue(getApplicationContext(), "autologin",
				false)
				&& SharedPrefsUtil.getValue(getApplicationContext(), "isPush",
						true)) {
			String s = getTopActivity(getApplicationContext()); // 当前顶端Component。
			if (!s.equals(Mina.TOP_ACTIVITY)) { // 当前Activity未获得焦点，需要提醒。

				Intent openIntent = new Intent(getApplicationContext(),
						Mina.CHAT_ONE_BY_ONE);
				openIntent.putExtra("touserid", message.getSend_userid());
				PendingIntent pendingIntent = PendingIntent.getActivity(
						getApplicationContext(), 0, openIntent,
						PendingIntent.FLAG_CANCEL_CURRENT);

				String noticeMsg = "";
				String title = "";
				String msgContent = message.getSend_ctn();

				if (msgContent.indexOf(".pcm") > 0) {
					title = "收到一条私聊语音";
					noticeMsg = message.getSend_person() + "：语音消息";

				} else {
					title = "收到一条私聊消息";
					noticeMsg = message.getSend_person() + ":" + msgContent;

				}

				msgService = new MessageService(
						this.getApplicationContext());
				msgService.insertMessage(message);

				Notification notification = new Notification.Builder(
						getApplicationContext()).setContentTitle(title)
						.setContentText(noticeMsg)
						.setSmallIcon(R.drawable.logo_new)
						.setContentIntent(pendingIntent).build();
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				notification.defaults = Notification.DEFAULT_ALL;
				NotificationManager manager = (NotificationManager) getApplicationContext()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.notify(1, notification);

			} else {
				// 无论在在堆栈定端，发送广播
				MSM.setCode(2);
				MSM.setStringMessage(gson.toJson(message));
				json = gson.toJson(MSM);
				Intent myIntent = new Intent();// 创建Intent对象
				myIntent.setAction(TAG);
				myIntent.putExtra("Date", json);
				sendBroadcast(myIntent);
			}
		}

	}

	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null)
			return (runningTaskInfos.get(0).topActivity).toString();
		else
			return null;
	}
}
