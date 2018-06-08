package cn.mina.mina;

import android.util.Log;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import cn.mina.mina.entity.MsgPack;

public class ClientMinaHandler extends IoHandlerAdapter {

	public static final String TAG = "ClientMinaHandler";

	private IMsgCallback mCallback;

	private static final String heart = "*";

	public ClientMinaHandler(IMsgCallback callback) {

		mCallback = callback;
	}

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

			mCallback.clientConnect("",session);

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
		// session.close(true);

		System.out.println("杩涘叆绌洪棽鐘舵�");

		//

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
		System.out.println("exceptionCaught~" + cause.getLocalizedMessage());
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {

		super.messageReceived(session, message);

		MsgPack mp = (MsgPack) message;

		if (mp.getMsgMethod() == 0 ) {
			return; // 蹇冭烦淇℃伅锛屼笉鐢ㄥ鐞嗐�淇濇寔杩炴帴鐢ㄧ殑銆�|| mp.getMsgMethod() == 1
		}

		String rcvMsg = mp.getMsgPack();

		Log.d(TAG, "client receive msg form sever:" + rcvMsg);

		if (null != mCallback) {

			if (mp.getMsgMethod() == 2) {
				mCallback.clientReceiveMsg(mp);
			} else {
				mCallback.clientReceiveChessStep(rcvMsg, mp.getMsgMethod());
			}
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {

		super.messageSent(session, message);

		Log.d(TAG, "client send msg to sever:" + message.toString());

		// System.out.println("send msg to server: " + message.toString());
	}

}
