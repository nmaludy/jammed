
package com.jammed.app;

import com.google.protobuf.MessageLite;

public interface AbstractHandler<T extends MessageLite> {
    public boolean isMessageSupported(final MessageLite message);
    public boolean handleMessage(final MessageLite message);
	public int type(final MessageLite message);
}
