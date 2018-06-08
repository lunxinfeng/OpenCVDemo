package cn.mina.mina.charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import cn.mina.mina.entity.MsgPack;


/**
 * @see 协议解码
 * @author Herman.Xiong
 * @date 2014????1??16:47:24
 */
public class MsgProtocolDecoder extends CumulativeProtocolDecoder {
	private Charset charset = null;

	public MsgProtocolDecoder() {
		this(Charset.defaultCharset());
	}

	public MsgProtocolDecoder(Charset charset) {
		this.charset = charset;
	}

	public void decode1(IoSession is, IoBuffer buf, ProtocolDecoderOutput out)
			throws Exception {
		buf.order(ByteOrder.LITTLE_ENDIAN);
		MsgPack mp = new MsgPack();
		// 获取消息的内容长??
		mp.setMsgLength(buf.getInt());
		// 获取消息的功能函??
		mp.setMsgMethod(buf.getInt());
		byte[] msg = new byte[mp.getMsgLength()];
		buf.get(msg);
		mp.setMsgPack(new String(msg, charset));
		buf.flip();
		out.write(mp);
	}

	@Override
	public void dispose(IoSession arg0) throws Exception {

	}

	@Override
	public void finishDecode(IoSession arg0, ProtocolDecoderOutput arg1)
			throws Exception {

	}

	public void decode0(IoSession arg0, IoBuffer arg1,
			ProtocolDecoderOutput arg2) throws Exception {
		int limit = arg1.limit();
		byte[] bytes = new byte[limit];
		arg1.get(bytes);
		arg2.write(bytes);
	}

	@Override
	protected boolean doDecode(IoSession session, IoBuffer ioBuffer,
			ProtocolDecoderOutput out) throws Exception {
		ioBuffer.order(ByteOrder.LITTLE_ENDIAN);
		MsgPack mp = (MsgPack) session.getAttribute("nac-msg-pack"); //
		if (null == mp) {
			if (ioBuffer.remaining() >= 20) {
				// 
				int msgLength = ioBuffer.getInt();
				int msgMethod = ioBuffer.getInt();
				int msgGroupId = ioBuffer.getInt();
				int msgToId = ioBuffer.getInt();
				mp = new MsgPack();
				mp.setMsgLength(msgLength);
				mp.setMsgMethod(msgMethod);
				mp.setMsgGroupId(msgGroupId);
				mp.setMsgToID(msgToId);
				
				session.setAttribute("nac-msg-pack", mp);
				return true;
			}
			return false;
		}
		if (ioBuffer.remaining() >= mp.getMsgLength()) {
			byte[] msgPack = new byte[mp.getMsgLength()];
			ioBuffer.get(msgPack);
			mp.setMsgPack(new String(msgPack, charset));
			session.removeAttribute("nac-msg-pack");
			out.write(mp);
			return true;
		}
		return false;
	}

}