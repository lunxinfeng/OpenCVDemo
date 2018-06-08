package cn.mina.mina.charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import java.nio.charset.Charset;

public class MsgProtocol implements ProtocolCodecFactory{
	private static final Charset charset=Charset.forName("UTF-8");
  
    @Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {  
        return new MsgProtocolDecoder(charset);
        
    }  
  
    @Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {  
        return new MsgProtocolEncoder(charset);
    }
}
