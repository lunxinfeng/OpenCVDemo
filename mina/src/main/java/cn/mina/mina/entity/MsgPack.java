package cn.mina.mina.entity;

import java.io.Serializable;

/**
 *  自定义数据包
 * @author Herman.Xiong
 * @date 2014年6月11日 11:31:45
 */
public class MsgPack implements Serializable {
    /**
     * 序列化和反序列化的版本号
     */
    private static final long serialVersionUID = 1L;
    // 消息长度
    private int msgLength;
    // 消息方法
    private int msgMethod;
    // 讨论组ID
    private int msgGroupID; // 0表示大厅用户
    // 目标用户ID
    private int msgToID; // 0表示组里所有人
    // 消息包内容
    private String msgPack;

    public MsgPack() {
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public int getMsgMethod() {
        return msgMethod;
    }

    public void setMsgMethod(int msgMethod) {
        this.msgMethod = msgMethod;
    }

    public int getMsgGroupId() {
        return msgGroupID;
    }

    public void setMsgGroupId(int msgGroupID) {
        this.msgGroupID = msgGroupID;
    }

    public int getMsgToID() {
        return msgToID;
    }

    public void setMsgToID(int msgToID) {
        this.msgToID = msgToID;
    }

    public String getMsgPack() {
        return msgPack;
    }

    public void setMsgPack(String msgPack) {
        this.msgPack = msgPack;
    }

    public MsgPack(int msgLength, int msgMethod, int msgGroupID, int msgToID,
                   String msgPack) {
        this.msgLength = msgLength;
        this.msgMethod = msgMethod;
        this.msgPack = msgPack;
        this.msgGroupID = msgGroupID;
        this.msgToID = msgToID;
    }

    @Override
    public String toString() {
        return "MsgPack [msgLength=" + msgLength + ", msgMethod=" + msgMethod
                + ", msgGroupID=" + msgGroupID + ", msgToID=" + msgToID
                + ", msgPack=" + msgPack + "]";
    }

}
