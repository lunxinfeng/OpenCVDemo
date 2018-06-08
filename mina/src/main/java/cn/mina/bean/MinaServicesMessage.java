package cn.mina.bean;

import cn.mina.mina.entity.MsgPack;

public class MinaServicesMessage {

	/**
	 * 消息代码：
	 * 1：clientReceiveChessStep 	棋局相关信息
	 * 2:clientReceiveMsg			聊天消息
	 * 3：断线重连后检查动态口令，验证通过
	 * 4：断线重连后检查动态口令，验证不通过，在他处登录。
	 */
	private int code;	
	
	/**
	 * 消息体
	 */
	private MsgPack myPack;
	private String StringMessage;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public MsgPack getMyPack() {
		return myPack;
	}
	public void setMyPack(MsgPack myPack) {
		this.myPack = myPack;
	}
	public String getStringMessage() {
		return StringMessage;
	}
	public void setStringMessage(String stringMessage) {
		StringMessage = stringMessage;
	}
	public MinaServicesMessage(int code, MsgPack myPack) {
		super();
		this.code = code;
		this.myPack = myPack;
	}
	public MinaServicesMessage() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "MinaServicesMessage [code=" + code + ", myPack=" + myPack.toString()
				+ ", StringMessage=" + StringMessage + "]";
	}
	
	
}
