
package com.jammed.handlers;

import com.jammed.app.PacketHandler;

import com.jammed.gen.MediaProtos.Playlist;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.jammed.gen.MessageProtos.AddressDeclaration;

public class AddressDeclarationHandler extends PacketHandler<AddressDeclaration> {

	public AddressDeclarationHandler() {
	}

	public boolean isMessageSupported (final MessageLite message) {
		return (message instanceof AddressDeclaration);
	}

	public boolean isMessageSupported (final int type) {
		final AddressDeclaration.Builder builder = AddressDeclaration.newBuilder();

		return builder.getType().ordinal() == type;
	}

	public int type (final MessageLite message) {
		if (!(message instanceof AddressDeclaration)) {
			throw new IllegalArgumentException();
		}

		final AddressDeclaration declaration = (AddressDeclaration)message;

		return  declaration.getType().ordinal();
	}

	public AddressDeclaration mergeFrom (final byte[] data) {
		try {
			final AddressDeclaration.Builder builder = AddressDeclaration.newBuilder();
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

		final AddressDeclaration declaration = (AddressDeclaration)message;

		//Ensure that the declared adderss does not conflict with this clients current transmission address
		//If it does send a AddressRejection message

		return true;
	}

}
