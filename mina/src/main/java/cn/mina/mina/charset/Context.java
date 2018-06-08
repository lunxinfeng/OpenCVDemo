package cn.mina.mina.charset;

import org.apache.mina.core.buffer.IoBuffer;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * 记录上下文，因为数据触发没有规模，很可能只收到数据包的一半
 * 所以，需要上下文拼起来才能完整的处理
 */
public class Context {
	private final CharsetDecoder decoder;
	private IoBuffer buf;
	private int matchCount = 0;
	private int overflowPosition = 0;
	
	public Context(Charset charset) {
		decoder = charset.newDecoder();
		buf = IoBuffer.allocate(80).setAutoExpand(true);
	}

	public CharsetDecoder getDecoder() {
		return decoder;
	}

	public IoBuffer getBuffer() {
		return buf;
	}

	public int getOverflowPosition() {
		return overflowPosition;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}

	public void reset() {
		overflowPosition = 0;
		matchCount = 0;
		decoder.reset();
	}

	public void append(IoBuffer in) {
		getBuffer().put(in);
	}
}
