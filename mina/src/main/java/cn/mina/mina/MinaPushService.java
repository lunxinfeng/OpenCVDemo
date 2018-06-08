package cn.mina.mina;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.google.gson.Gson;

import org.apache.mina.core.session.IoSession;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.mina.bean.MinaServicesMessage;
import cn.mina.constant.PublicClass;
import cn.mina.mina.entity.MsgPack;
import cn.mina.util.ConnectionChangeReceiver;
import cn.mina.util.SharedPrefsUtil;

public class MinaPushService extends Service implements IMsgCallback {

	public final static String TAG = "izis_MinaPushService_yzcd";

	protected final static int DEF_CIM_PORT = 8093;
	private String userid = "";
	private String DynamicKey = "";
	private String json = "";
	private String realname = "";
	private int sType;
	private int gameid;
	private int again = 0;

	private MinaServicesMessage MSM = new MinaServicesMessage();
	private Gson gson = new Gson();
	public static MinaConnectManager manager;
	private ConnectionChangeReceiver myReceiver;

	@Override
	public void onCreate() {

		DynamicKey = SharedPrefsUtil.getValue(this, "user_dypassword", "-");
		int userId = SharedPrefsUtil.getValue(this, "user_id", 0);
		userid = String.valueOf(userId);
		realname = SharedPrefsUtil.getValue(this, "user_name", "");
		manager = MinaConnectManager.getManager(this.getApplicationContext(),
				this);
		registerReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 启动服务时务必带此参数
		// String action =
		// intent.getStringExtra(MinaPushManager.SERVICE_ACTION);
		if(intent!=null&&intent.hasExtra("again")){
			again = intent.getIntExtra("again", 0);
		}
		if(again == 0){
			int new_sType = 0;
			int new_gameid = 0;

			if (intent != null) {
				new_sType = intent.getIntExtra("type", 20);
				new_gameid = intent.getIntExtra("gameid", 0);
				
				sType = new_sType;
				gameid = new_gameid;
				}
			if (manager.isConnected()) {
					String msgJson2 = "[{\"room_stype\":\"" + new_sType + "\"}]";
//					PublicUtil.MianSend(107, new_gameid, 0, msgJson2,
//							manager.getCurrentSession());
					sendMsg(107, new_gameid, 0, msgJson2);
				} else {
					manager.connect(PublicClass.ADDRESS, PublicClass.PORT);
			}
		}else{
			if(!manager.isConnected()){
				manager.connect(PublicClass.ADDRESS, PublicClass.PORT);
			}
		}

		return super.onStartCommand(intent, flags, startId);

	}

	
	public static boolean isConnected()
	{
		if(manager==null) return false;
		
		return manager.isConnected();
	}
	
	public static void closeSession() {
		manager.closeSession();
	}

	public static void sendMsg(MsgPack msg) {
		manager.send(msg);
	}
	public static boolean isInit(){
		if(manager==null){
			return false;
		}
		return manager.isInit();
	}
	public static void sendMsg(int Method, int gameid, int otherUser, String msg) {
		manager.send(Method, gameid, otherUser, msg);
	}

	@Override
	public void onDestroy() {
		manager.destroy();
		this.unregisterReceiver(myReceiver);
		manager =null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * @author apple 收到聊天消息
	 */
	@Override
	public void clientReceiveMsg(MsgPack mp) {
		// TODO Auto-generated method stub

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
				+ "\",\"DynamicKey\":\"" + DynamicKey + "\"}]";

		manager.send(1, gameid, 0, msgJson);
		manager.AgainRegister();
	}

	@Override
	public void clientReceiveChessStep(String msg, int method) {

		// TODO Auto-generated method stub
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
	private  void registerReceiver(){
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver=new ConnectionChangeReceiver();
        this.registerReceiver(myReceiver, filter);
    }
}
