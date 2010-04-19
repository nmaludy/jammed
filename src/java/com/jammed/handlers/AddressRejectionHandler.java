
package com.jammed.handlers;

import com.jammed.app.PacketHandler;

import com.jammed.gen.MediaProtos.Playlist;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.jammed.gen.MessageProtos.AddressDeclaration;
import com.jammed.gen.MessageProtos.AddressRejection;

public class AddressRejectionHandler extends PacketHandler<AddressRejection> {

	public AddressRejectionHandler() {
	}

	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof AddressRejection);
	}

	public boolean isMessageSupported (final int type) {
		final AddressRejection.Builder builder = AddressRejection.newBuilder();

		return builder.getType().ordinal() == type;
	}

	public int type (final MessageLite message) {
		if (!(message instanceof AddressRejection)) {
			throw new IllegalArgumentException();
		}

		final AddressRejection rejection = (AddressRejection)message;

		return rejection.getType().ordinal();
	}

	public AddressRejection mergeFrom (final byte[] data) {
		try {
			final AddressRejection.Builder builder = AddressRejection.newBuilder();
			builder.mergeFrom(data);

			return builder.build();
		} catch (final InvalidProtocolBufferException ipbe) {
			return null;
		}
	}

	public boolean handleMessage (final MessageLite message) {
		if (!(message instanceof AddressDeclaration)) {
			throw new IllegalArgumentException();
		}

		final AddressRejection rejection = (AddressRejection)message;

		//The address declaration message you just send was rejected by another host
		// 1) Change your transmission address
		// 2) Send another AddressDeclaration to ensure that you are not conflicting with another
		//		hosts transmission space

		return true;
	}

}
