package cn.mina.mina.charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import cn.mina.mina.entity.MsgPack;


public class MsgProtocolEncoder extends ProtocolEncoderAdapter{
	private Charset charset=null;

    public MsgProtocolEncoder(Charset charset) {
        this.charset = charset;     
    }     

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception { 
        if(message instanceof MsgPack){
        	 MsgPack mp = (MsgPack) message;
        	 IoBuffer buf = IoBuffer.allocate(mp.getMsgLength());
        	 buf.order(ByteOrder.LITTLE_ENDIAN);
             buf.setAutoExpand(true);    
             //消息长度
             buf.putInt(mp.getMsgLength()); 
             //消息方法名
             buf.putInt(mp.getMsgMethod());
             //群的ID
             buf.putInt(mp.getMsgGroupId());
             //目标用户ID
             buf.putInt(mp.getMsgToID());
             if (null != mp.getMsgPack()) {
            	 buf.put(mp.getMsgPack().getBytes(charset));
             }   
             buf.flip();     
             out.write(buf);  
             out.flush();
             buf.free();
        }
    }     
    public void dispose() throws Exception {     
    }
    
	public void encode0(IoSession arg0, Object arg1, ProtocolEncoderOutput arg2)
			throws Exception {
		if (!(arg1 instanceof Serializable)) {
	        throw new NotSerializableException();
	    }
	    IoBuffer buf = IoBuffer.allocate(64);
	    buf.setAutoExpand(true);
	    buf.putObject(arg1);

	    int objectSize = buf.position() - 4;
	    if (objectSize > 1024) {
	        throw new IllegalArgumentException("The encoded object is too big: " + objectSize + " (> " + 1024
	                + ')');
	    }

	    buf.flip();
	    arg2.write(buf);
	}
	
}
