package cn.mina.mina;

import org.apache.mina.core.session.IoSession;

import cn.mina.mina.entity.MsgPack;

public interface IMsgCallback {
	public void clientReceiveMsg(MsgPack mp);
	public void clientConnect(String msg, IoSession session);
	public void clientReceiveChessStep(String msg, int method);
}
