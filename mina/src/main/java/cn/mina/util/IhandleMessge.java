package cn.mina.util;

import cn.mina.mina.entity.Message;

/**
 * 接收并处理消息的接口
 * @author Administrator
 */
public interface IhandleMessge {
	public void handleMsg(Message msg);
}
