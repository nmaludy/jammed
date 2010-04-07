
package com.jammed.app;

import com.google.protobuf.MessageLite;

public abstract class PacketHandler<E extends MessageLite>
	implements AbstractHandler<E> {
}

