
package com.jammed.app;

import com.google.protobuf.MessageLite;

public interface PacketHandler<E extends MessageLite> {
    public boolean isMessageSupported(final MessageLite message);
    public boolean handleMessage(final E message);
	public int type(final E message);
}
