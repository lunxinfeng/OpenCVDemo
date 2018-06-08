package cn.mina.mina;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.mina.bean.MinaServicesMessage;
import cn.mina.constant.PublicClass;
import cn.mina.mina.charset.MsgProtocol;
import cn.mina.mina.entity.MsgPack;
import cn.mina.mina.exception.CIMSessionDisableException;
import cn.mina.mina.exception.WriteToClosedSessionException;
import cn.mina.util.PublicUtil;
import cn.mina.util.SharedPrefsUtil;

import static cn.mina.constant.PublicClass.ADDRESS;
import static cn.mina.mina.MinaConnectManager.ACTION_SENT_FAILED;

public class MinaConnectManagerOpen {

    private NioSocketConnector connector;
    private ConnectFuture connectFuture;
    private ExecutorService executor;
    private IoSession clientSession;
    public boolean isReconnected = false;

    private boolean CheckDynamicKey = true;
    private boolean sessionCloseding = false;
    private String userid = "";
    private String DynamicKey = "";
    private String json = "";
    private String realname = "";
    private static int myPORT = 8092;
    private int sType;
    private int gameid;
    private MinaServicesMessage MSM = new MinaServicesMessage();
    private Gson gson = new Gson();
    private boolean Connection_Again = true;

    // 服务广播标识
    public final static String TAG = "izis_MinaPushServiceOpen";
    public static final String ACTION_CONNECTION_FAILED = "cn.izis.mina.CONNECTION_FAILED";
    public static final String ACTION_CONNECTION_STATUS = "cn.izis.mina.CONNECTION_STATUS";

    private static MinaConnectManagerOpen manager_open;
    private static IMsgCallback mCallback;
    private Context context;
    private String address;
    private int port;

    private MinaConnectManagerOpen(Context ctx) {
        context = ctx;
        DynamicKey = SharedPrefsUtil.getValue(ctx, "DynamicKey", "-");
        userid = SharedPrefsUtil.getValue(ctx, "userid", "");
        executor = Executors.newFixedThreadPool(3);

        connector = new NioSocketConnector();
        // connector.setConnectTimeoutMillis(60 * 1000);
        connector.setConnectTimeoutMillis(8 * 1000L);
        connector.setConnectTimeoutCheckInterval(3000);
        connector.getSessionConfig().setTcpNoDelay(true);
        connector.getSessionConfig().setReadBufferSize(2048);
        connector.getFilterChain().addFirst("reconnection",
                new IoFilterAdapter() {

                    @Override
                    public void sessionClosed(NextFilter nextFilter,
                                              IoSession ioSession) throws Exception {
                        for (; ; ) {
                            try {
                                Thread.sleep(3000);
                                isReconnected = true;
                                if (sessionCloseding) {
                                    break;
                                }
                                if (PublicClass.CONNECTION_STATE) {
                                    Connection_Again = true;
                                    if (CheckDynamicKey) {
                                        DynamicKeyCheck();
                                    }

                                    try {
                                        if (isConnected()) {
                                            return;
                                        }
                                        InetSocketAddress remoteSocketAddress = new InetSocketAddress(
                                                ADDRESS, myPORT);
                                        connectFuture = connector
                                                .connect(remoteSocketAddress);
                                        connectFuture.awaitUninterruptibly();
                                        clientSession = connectFuture
                                                .getSession();

                                        if (clientSession.isConnected()) {
                                            break;
                                        }

                                    } catch (Exception e) {

                                    }
                                } else {
                                    if (Connection_Again) {
                                        myHandler.sendEmptyMessage(3);
                                        break;
                                    }
                                }
                            } catch (Exception ex) {
                                System.out.println(ex.getMessage());
                            }
                        }
                    }
                });
        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new MsgProtocol()));
        connector.setHandler(iohandler);

    }

    private synchronized void DynamicKeyCheck() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String arg1 = "{\"root\":[{\"userid\":\"" + userid
                            + "\",\"DynamicKey\":\"" + DynamicKey + "\"}]}";
                    Log.i("MinaConnectManagerOpen", arg1);
                    String resStr = PublicUtil.HttpSend(
                            PublicClass.METHOD_NAME, "7300", arg1, userid);
                    Log.i("MinaConnectManagerOpen", userid + "");
                    if (resStr.equals("true")) {
                        myHandler.sendEmptyMessage(1); // 成功获取信息 xixi
                    } else if (resStr.equals("false")) {
                        myHandler.sendEmptyMessage(2);
                    }
                    CheckDynamicKey = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    Handler myHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    if (PublicClass.CONNECTION_STATE) {
                        MSM.setCode(3);
                        json = gson.toJson(MSM);
                        CheckDynamicKey = false;
                        Intent myIntent = new Intent();// 创建Intent对象
                        myIntent.setAction(TAG);
                        myIntent.putExtra("Date", json);
                        context.sendBroadcast(myIntent);
                    }
                    break;
                case 2:
                    if (PublicClass.CONNECTION_STATE) {
                        MSM.setCode(4);
                        json = gson.toJson(MSM);
                        CheckDynamicKey = false;
                        Intent myIntent2 = new Intent();// 创建Intent对象
                        myIntent2.setAction(TAG);
                        myIntent2.putExtra("Date", json);
                        context.sendBroadcast(myIntent2);
                        sessionCloseding = true;
                    }
                    break;
                case 3:
                    Toast.makeText(context, "网络已被断开", Toast.LENGTH_SHORT).show();
                    Connection_Again = false;
                    break;
                case 4:
                    break;
                case 5:
//                    Toast.makeText(context.getApplicationContext(), "消息服务器连接失败", Toast.LENGTH_SHORT).show();
                    connect(address, port);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    IoHandlerAdapter iohandler = new IoHandlerAdapter() {

        private static final String heart = "*";

        @Override
        public void sessionCreated(IoSession session) throws Exception {

            super.sessionCreated(session);
            session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 20);
            System.out.println("sessionCreated~");
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            // TODO Auto-generated method stub
            super.sessionOpened(session);
            if (null != mCallback) {
                mCallback.clientConnect("", session);
            }
            System.out.println("sessionOpened~");
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            // TODO Auto-generated method stub
            super.sessionClosed(session);

            System.out.println("================sessioncoloase~");

        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status)
                throws Exception {
            // TODO Auto-generated method stub
            super.sessionIdle(session, status);
            if (status == IdleStatus.BOTH_IDLE) {

                MsgPack msgPack = new MsgPack();
                msgPack.setMsgLength(heart.getBytes("UTF-8").length);
                msgPack.setMsgMethod(0);
                msgPack.setMsgGroupId(0);
                msgPack.setMsgToID(0);
                msgPack.setMsgPack(heart);
                session.write(msgPack);

            }

        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {

            super.exceptionCaught(session, cause);
            session.close(false);
            System.out
                    .println("exceptionCaught~" + cause.getLocalizedMessage());
        }

        @Override
        public void messageReceived(IoSession session, Object message)
                throws Exception {

            super.messageReceived(session, message);

            MsgPack mp = (MsgPack) message;

            if (mp.getMsgMethod() == 0) {
                return; // 心跳信息，不用处理。保持连接用的。 || mp.getMsgMethod() == 1
            }

            String rcvMsg = mp.getMsgPack();

//            Log.d(TAG, "client receive msg form sever:" + rcvMsg);

            if (null != mCallback) {

                if (mp.getMsgMethod() == 2) {
                    mCallback.clientReceiveMsg(mp);
                } else {
                    mCallback.clientReceiveChessStep(rcvMsg, mp.getMsgMethod());
                }
            }
        }

        @Override
        public void messageSent(IoSession session, Object message)
                throws Exception {

            super.messageSent(session, message);

//            Log.d(TAG, "client send msg to sever:" + message.toString());

            // System.out.println("send msg to server: " + message.toString());
        }

    };

    public synchronized static MinaConnectManagerOpen getManager(Context context,
                                                                 IMsgCallback _mCallback, int PORT) {
        if (manager_open == null) {
            if (PORT != 0) {
                myPORT = PORT;
            }
            manager_open = new MinaConnectManagerOpen(context);
            mCallback = _mCallback;
        }
        return manager_open;

    }

    private synchronized void syncConnection(final String cimServerHost,
                                             final int cimServerPort) {
        try {

            if (isConnected()) {
                return;
            }
            InetSocketAddress remoteSocketAddress = new InetSocketAddress(
                    cimServerHost, cimServerPort);
            connectFuture = connector.connect(remoteSocketAddress);
            connectFuture.awaitUninterruptibly();
            if (connectFuture.getSession() == null && manager_open != null) {
                myHandler.sendEmptyMessage(5);
            }
        } catch (Exception e) {
            if (manager_open != null)
                myHandler.sendEmptyMessage(5);

//            Intent intent = new Intent();
//            intent.setAction(ACTION_CONNECTION_FAILED);
//            intent.putExtra("exception", e);
//            context.sendBroadcast(intent);

//            Log.i(TAG, "******************CIM连接服务器失败  " + cimServerHost + ":"
//                    + cimServerPort);

        }

    }

    public void connect(final String cimServerHost, final int cimServerPort) {

        address = cimServerHost;
        port = cimServerPort;

        if (!netWorkAvailable(context)) {

            Intent intent = new Intent();
            intent.setAction(ACTION_CONNECTION_FAILED);
            intent.putExtra("exception", new Exception());
            context.sendBroadcast(intent);
            return;
        }

        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                syncConnection(cimServerHost, cimServerPort);
            }
        });
//		try {
//
//			if (future.get(8000, TimeUnit.MILLISECONDS)!= null) {
//				connect(cimServerHost, cimServerPort);
//			}
//		} catch (Exception e) {
//
//			future.cancel(true);
////			connect(cimServerHost, cimServerPort);
//			Toast.makeText(context, "消息服务器连接异常！", Toast.LENGTH_SHORT).show();
//			e.printStackTrace();
//		}

    }

    public void send(int Method, int gameid, int otherUser, String msg) {
        MsgPack msgPack = new MsgPack();
        try {
            msgPack.setMsgLength(msg.getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        msgPack.setMsgMethod(Method); // 主动认输
        msgPack.setMsgGroupId(gameid); // 默认群聊
        msgPack.setMsgToID(otherUser); // 默认群聊
        msgPack.setMsgPack(msg);

        // 启动线程发送消息
        send(msgPack);
    }

    public void send(final MsgPack body) {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                IoSession session = getCurrentSession();
                if (session != null && session.isConnected()) {
                    WriteFuture wf = session.write(body);
                    // 消息发送超时 10秒
//                     wf.awaitUninterruptibly(5, TimeUnit.SECONDS);
//                     if (!wf.isWritten()) {
//                     Intent intent = new Intent();
//                     intent.setAction(ACTION_SENT_FAILED);
//                     intent.putExtra("exception",
//                     new WriteToClosedSessionException());
//                     intent.putExtra("sentBody", body);
//                     context.sendBroadcast(intent);
//                     }
                } else {

//                     Intent intent = new Intent();
//                     intent.setAction(ACTION_SENT_FAILED);
//                     intent.putExtra("exception",
//                     new CIMSessionDisableException());
//                     intent.putExtra("sentBody", body);
//                     context.sendBroadcast(intent);
                }
            }
        });
    }

    public void destroy() {
        IoSession session = getCurrentSession();
        if (session != null) {
            session.getFilterChain().remove("reconnection");
            session.close(false);
        }

        if (connector != null && !connector.isDisposed()) {
            connector.dispose();
        }
        manager_open = null;
        myHandler.removeCallbacksAndMessages(null);
    }

    public boolean isConnected() {
        IoSession session = getCurrentSession();
        if (session == null) {
            return false;
        }
        return session.isConnected();
    }

    public boolean isInit() {
        if (manager_open != null) {
            return true;
        } else {
            return false;
        }
    }

    public void deliverIsConnected() {
        Intent intent = new Intent();
        intent.setAction(ACTION_CONNECTION_STATUS);
        intent.putExtra("KEY_CIM_CONNECTION_STATUS", isConnected());
        context.sendBroadcast(intent);
    }

    public void AgainRegister() {
        CheckDynamicKey = true;
        Connection_Again = true;
    }

    public void closeSession() {
        IoSession session = getCurrentSession();
        if (session != null) {
            session.getFilterChain().remove("reconnection");
            session.close(false);
        }
    }

    public IoSession getCurrentSession() {
        if (connector.getManagedSessionCount() > 0) {
            for (Long key : connector.getManagedSessions().keySet()) {
                return connector.getManagedSessions().get(key);
            }
        }

        return null;
    }

    public static boolean netWorkAvailable(Context context) {
        try {
            ConnectivityManager nw = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = nw.getActiveNetworkInfo();
            return networkInfo != null;
        } catch (Exception e) {
        }

        return false;
    }
}
