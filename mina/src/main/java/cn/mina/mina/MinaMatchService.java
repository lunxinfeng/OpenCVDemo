package cn.mina.mina;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.google.gson.Gson;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.mina.bean.MinaServicesMessage;
import cn.mina.constant.PublicClass;
import cn.mina.mina.charset.MsgProtocol;
import cn.mina.mina.entity.MsgPack;
import cn.mina.util.PublicUtil;
import cn.mina.util.SharedPrefsUtil;

import static cn.mina.constant.PublicClass.ADDRESS;

public class MinaMatchService extends Service implements IMsgCallback {
	private String userid = "";
	private String DynamicKey = "";
	private String json = "";
	private String realname = "";
	private int sType;
	private int gameid;
	private static String curActivity = ""; // 当前curActivity

	private static NioSocketConnector connector;
	private static InetSocketAddress serverAdd;
	public static IoSession clientSession;
	// private DataReceiver cmdReceiver;
	private ExecutorService service = null;

	private MinaServicesMessage MSM = new MinaServicesMessage();
	private MyBinder myBinder = new MyBinder();
	public final static String TAG = "izis_MinaMatchService_yzcd";
	private boolean CheckDynamicKey = true;
	private boolean sessionCloseding = false;
	public static boolean AgainMina = false; // Mina是否是再次连接
	public static boolean isReconnected = false;
	private Gson gson = new Gson();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		DynamicKey = SharedPrefsUtil.getValue(this, "DynamicKey", "-");
		userid = SharedPrefsUtil.getValue(this, "userid", "");
		realname = SharedPrefsUtil.getValue(this, "realname", "");
		service = Executors.newFixedThreadPool(5);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		int new_sType = 0;
		int new_gameid = 0;

		AgainMina = false;
		isReconnected = false;

		if (intent != null) {
			new_sType = intent.getIntExtra("type", 0);
			new_gameid = intent.getIntExtra("gameid", 0);
		}
		if (clientSession == null) {
			sType = new_sType;
			gameid = new_gameid;
			connectServer();
		} else {

			if (clientSession.isConnected()) {
				if (sType != new_sType) {
					sType = new_sType;
					String msgJson2 = "[{\"room_stype\":\"" + sType + "\"}]";
					PublicUtil
							.MianSend(107, gameid, 0, msgJson2, clientSession);
				}
			} else {
				sType = new_sType;
				gameid = new_gameid;
				connectServer();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// if (cmdReceiver != null) {
		// this.unregisterReceiver(cmdReceiver);
		// }
		if (null != clientSession && !clientSession.isClosing()) {
			clientSession.getFilterChain().remove("reconnection");
			clientSession.close(false);
			connector.dispose();
			clientSession = null;
			connector = null;
			serverAdd = null;
		}
		super.onDestroy();
	}

	@Override
	public void clientReceiveMsg(MsgPack mp) {

	}

	@Override
	public void clientConnect(String msg, IoSession session) {
		clientSession = session;
		String msgJson = "";
		Date now = new Date();
		SimpleDateFormat f1 = new SimpleDateFormat();
		msgJson = "[{\"id\":\"" + userid + "\",\"send_ctn\":\"" + "进入房间"
				+ "\",\"send_date\":\"" + f1.format(now) + "\",\"roomType\":\""
				+ sType + "\",\"send_person\":\"" + realname
				+ "\",\"DynamicKey\":\"" + DynamicKey + "\"}]";
		PublicUtil.MianSend(1, gameid, 0, msgJson, session);

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
		CheckDynamicKey = true;
		Intent myIntent = new Intent();// 创建Intent对象
		myIntent.setAction(TAG);
		myIntent.putExtra("Date", json);
		sendBroadcast(myIntent);

	}

	public void connectServer() {

		if (service == null) {

			return;
		}
		service.submit(new Runnable() {

			@Override
			public void run() {

				connectToServer();
			}
		});

	}

	private void connectToServer() {

		if (clientSession != null && !clientSession.isClosing()) {

			clientSession.getFilterChain().remove("reconnection"); //
			clientSession.close(true);
			clientSession = null;

			connector.dispose();
			connector = null;

		}

		if (null == connector) {
			// 创建非阻塞的server端的Socket连接
			connector = new NioSocketConnector();
		} else {

			try {

				ConnectFuture cf = connector.connect(serverAdd);
				cf.awaitUninterruptibly();
				clientSession = cf.getSession();

				return;

			} catch (RuntimeIoException e) {
				System.out.println(e.getMessage().toString());
			}

		}

		serverAdd = new InetSocketAddress(ADDRESS, PublicClass.PORT);
		connector.setConnectTimeoutMillis(60000L);
		connector.setConnectTimeoutCheckInterval(10000);
		DefaultIoFilterChainBuilder chain = connector.getFilterChain();

		// 先清理掉 已有IoFilterAdapter
		if (chain.contains("reconnection")) {
			chain.remove("reconnection");
		}
		if (chain.contains("codec")) {
			chain.remove("codec");
		}

		chain.addFirst("reconnection", new IoFilterAdapter() {
			@Override
			public void sessionClosed(NextFilter nextFilter, IoSession ioSession)
					throws Exception {
				for (;;) {
					try {

						Thread.sleep(3000);
						isReconnected = true;
						if (CheckDynamicKey) {
							DynamicKeyCheck();
						}
						if (sessionCloseding) {
							break;
						}
						ConnectFuture future = connector.connect(serverAdd);
						future.awaitUninterruptibly();// 等待连接创建成功
						clientSession = future.getSession();// 获取会话
						if (clientSession.isConnected()) {
							AgainMina = true;
							break;
						}
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
						// logger.info("重连服务器登录失败,3秒再连接一次:" + ex.getMessage());
					}
				}
			}
		});

		chain.addLast("codec", new ProtocolCodecFilter(new MsgProtocol()));

		if (connector.getHandler() == null) {
			connector.setHandler(new ClientMinaHandler(this));
		}

		try {

			ConnectFuture cf = connector.connect(serverAdd);
			cf.awaitUninterruptibly();
			clientSession = cf.getSession();

		} catch (RuntimeIoException e) {
			System.out.println(e.getMessage().toString());
		}

	}

	private void DynamicKeyCheck() {
		new Thread() {
			@Override
			public void run() {
				try {
					String arg1 = "{\"root\":[{\"userid\":\"" + userid
							+ "\",\"DynamicKey\":\"" + DynamicKey + "\"}]}";
					String resStr = PublicUtil.HttpSend(
							PublicClass.METHOD_NAME, "7300", arg1, userid);
					if (resStr.equals("true")) {
						myHandler.sendEmptyMessage(1); // 成功获取信息 xixi
					} else {
						myHandler.sendEmptyMessage(2);
					}
					CheckDynamicKey = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return myBinder;
	}

	public class MyBinder extends Binder {

		public MinaMatchService getService1() {
			return MinaMatchService.this;
		}
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				break;
			case 1:

				MSM.setCode(3);
				json = gson.toJson(MSM);
				CheckDynamicKey = false;
				Intent myIntent = new Intent();// 创建Intent对象
				myIntent.setAction(TAG);
				myIntent.putExtra("Date", json);
				sendBroadcast(myIntent);
				break;
			case 2:
				MSM.setCode(4);
				json = gson.toJson(MSM);
				CheckDynamicKey = false;
				Intent myIntent2 = new Intent();// 创建Intent对象
				myIntent2.setAction(TAG);
				myIntent2.putExtra("Date", json);
				sendBroadcast(myIntent2);
				sessionCloseding = true;
				break;
			case 3:
				break;
			case 4:
				break;
			}
			super.handleMessage(msg);
		}
	};

}
