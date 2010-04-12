
package com.jammed.app;

import com.google.protobuf.MessageLite;

public abstract class PacketHandler<T extends MessageLite>
	implements AbstractHandler<T> {
	
}

