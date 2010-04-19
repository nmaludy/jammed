package com.jammed.app;

import com.google.protobuf.MessageLite;
import com.jammed.gen.MessageProtos.AddressDeclaration;
import com.jammed.gen.MessageProtos.AddressRejection;
import com.jammed.gen.ProtoBuffer.Request;
import com.jammed.handlers.AddressDeclarationHandler;
import com.jammed.handlers.AddressRejectionHandler;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author nmaludy
 */
public class TransmissionAddressManager {

	private final Random r = new Random();
	private final ClientDeclarationHandler declarationHandler;
	private final ClientRejectionHandler rejectionHandler;
	private final ReentrantReadWriteLock requestLock = new ReentrantReadWriteLock();
	private final Lock requestWrite = requestLock.writeLock();
	private Request declarationRequest;
	private volatile String address;

	private TransmissionAddressManager() {
		declarationHandler = new ClientDeclarationHandler();
		rejectionHandler = new ClientRejectionHandler();
		Cloud.getInstance().addMessageHandler(declarationHandler);
		Cloud.getInstance().addMessageHandler(rejectionHandler);
		byte[] addr = Cloud.getInstance().getAddress();
		String host = unsignedByteToInt(addr[0]) + ".";
		host += unsignedByteToInt(addr[1]) + ".";
		host += unsignedByteToInt(addr[2]) + ".";
		host += unsignedByteToInt(addr[3]);
		address = generateRandomAddress(host);
		sendDeclarationRequest();
	}
	
	public static int unsignedByteToInt(byte b) {
		return 0x000000FF & ((int) b); //cast b to an int (signed) strip all signage out and preserve value with the AND;
   }

	static class TransmissionAddressManagerHolder {
		static TransmissionAddressManager instance = new TransmissionAddressManager();
	}

	public static TransmissionAddressManager getInstance() {
		return TransmissionAddressManagerHolder.instance;
	}

	public String getAddress() {
		return address;
	}
	
	/*
	 * Takes an IPv4 address in string form (ex 220.123.2.1)
	 * -Splits it into its parts using the '.' as a delimiter (ex 220 123 2 1 all separate strings)
	 * -Saves the last chunk, the lest significant number (ex 1)
	 * -Generates a random set of chunks in the range of valid multicast addresses (224.0.0.0 - 239.255.255.255)
	 * -Assigns the last chunk in the new address to be the same as the last chunk in the input address
	 */

	private String generateRandomAddress(String input) {
		String[] chunks = input.split("\\.");
		String endChunk = chunks[chunks.length - 1];
		String result = String.valueOf(r.nextInt(16) + 224) + ".";
		result += String.valueOf(r.nextInt(256)) + ".";
		result += String.valueOf(r.nextInt(256)) + ".";
		result += endChunk;
		return result;
	}

	private void sendDeclarationRequest() {
		AddressDeclaration.Builder builder = AddressDeclaration.newBuilder();
		builder.setType(builder.getType());
		builder.setAddress(address);
		requestWrite.lock();
		declarationRequest = RequestPool.getInstance().lease();
		builder.setRequest(declarationRequest);
		Cloud.getInstance().send(builder.build(), declarationRequest.getId());
		requestWrite.unlock();
	}

	private class ClientDeclarationHandler extends AddressDeclarationHandler {

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof AddressDeclaration)) {
				throw new IllegalArgumentException();
			}

			final AddressDeclaration declaration = (AddressDeclaration) message;
			Request request = declaration.getRequest();
			String hostname = request.getOrigin();
			if (hostname.equals(Cloud.getInstance().getHostName())) {
				return false; //a request that originated from this system, ignore it
			}

			//Ensure that the declared adderss does not conflict with this clients current transmission address
			//If it does send a AddressRejection message
			if (declaration.getAddress().equals(address)) {
				AddressRejection.Builder builder = AddressRejection.newBuilder();
				builder.setType(builder.getType());
				builder.setRequest(request);
				Cloud.getInstance().send(builder.build(), request.getId());
			}

			return true;
		}
	}
	
	private class ClientRejectionHandler extends AddressRejectionHandler {

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof AddressRejection)) {
				throw new IllegalArgumentException();
			}

			final AddressRejection rejection = (AddressRejection) message;
			Request request = rejection.getRequest();
			String hostname = request.getOrigin();
			if (!hostname.equals(Cloud.getInstance().getHostName())) {
				return false; //a request did not originate from this system, ignore it
			}
			requestWrite.lock();
			if (request.getId() != declarationRequest.getId()) {
				return false; //Not the most recent request that we have sent out
			}
			RequestPool.getInstance().release(declarationRequest);
			requestWrite.unlock();
			
			sendDeclarationRequest();
			return true;
		}
	}
}
