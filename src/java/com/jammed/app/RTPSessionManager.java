package com.jammed.app;

import com.google.protobuf.MessageLite;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MessageProtos.PlayRequest;
import com.jammed.gen.MessageProtos.PlayResponse;
import com.jammed.gen.ProtoBuffer.Request;
import com.jammed.handlers.PlayRequestHandler;
import com.jammed.handlers.PlayResponseHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 *
 * @author nmaludy
 */
public class RTPSessionManager {

	private final ExecutorService transmitters;
	private final RequestHandler requestHandler;
	private final RequestHandler responseHandler;
	private final ConcurrentMap<Integer, Request> receiveRequests;
	private final EventListenerList receiveListeners;

	private static class RTPSessionManagerHolder {
		static final RTPSessionManager instance = new RTPSessionManager();
	}

	private RTPSessionManager() {
		transmitters = Executors.newCachedThreadPool();
		requestHandler = new RequestHandler();
		responseHandler = new RequestHandler();
		receiveRequests = new ConcurrentHashMap<Integer, Request>();
		receiveListeners = new EventListenerList();
		Cloud.getInstance().addMessageHandler(requestHandler);
		Cloud.getInstance().addMessageHandler(responseHandler);
	}

	public static RTPSessionManager getInstance() {
		return RTPSessionManagerHolder.instance;
	}

	public void requestReceiveSession(Media m, final RTPReceiverListener l) {
		PlayRequest.Builder playRequest = PlayRequest.newBuilder();
		playRequest.setType(playRequest.getType());
		playRequest.setVideo(MediaUtils.isVideo(m));
		playRequest.setStream(true);
		Request request = RequestPool.getInstance().lease();
		Integer requestId = Integer.valueOf(request.getId());
		receiveRequests.putIfAbsent(requestId, request); //Should always succeed due to ReuqestPool's uniqueness
		playRequest.setRequest(request);
		Cloud.getInstance().send(playRequest.build(), request.getId());
	}

	public void addReceiverListener(final RTPReceiverListener l) {
		receiveListeners.add(RTPReceiverListener.class, l);
	}

	public void removeReceiverListener(final RTPReceiverListener l) {
		receiveListeners.remove(RTPReceiverListener.class, l);
	}

	protected void addListenersToReceiver(final RTPReceiver r) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (receiveListeners == null) {
					return;
				}
				Object[] listeners = receiveListeners.getListenerList();

				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == RTPReceiverListener.class) {
						r.addReceiverListener((RTPReceiverListener) listeners[i + 1]);
					}
				}
			}
		});
	}
	/*

	/*
	 * Waits for other clients to request a Media with a hostname
	 * equal to Cloud.getInstance().getHostname(). Then starts a
	 * RTP transmit session to a new address and sends a Play Response message back.
	 */
	private class RequestHandler extends PlayRequestHandler {

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof PlayRequest)) {
				throw new IllegalArgumentException();
			}
			PlayRequest playRequest = (PlayRequest) message;
			String hostname = playRequest.getMedia().getHostname();
			
			if (!hostname.equals(Cloud.getInstance().getHostName())) {
				return false; //not a request for a media on this sytem
			}

			// Handle PlayRequest

			return true;
		}
	}

	/*
	 * When a play request is sent the client on the other end of the line
	 * sends a PlayResponse telling which address and port to listen for the
	 * media on. This class handles the reponse to the request messages and starts
	 * the receiving of the stream.
	 */
	private class ResponseHandler extends PlayResponseHandler {

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof PlayResponse)) {
				throw new IllegalArgumentException();
			}

			PlayResponse playReponse = (PlayResponse) message;
			Request request = playReponse.getRequest();
			if (!request.getOrigin().equals(Cloud.getInstance().getHostName())) {
				return false; // not a request from this system
			}
			
			Integer id = Integer.valueOf(request.getId());
			if (receiveRequests.remove(id, request)) { //check if this request has been handled yet
				String hostname = playReponse.getAddress();
				int port = playReponse.getAuidoPort();
				RTPReceiver receiver = RTPReceiver.create(hostname, port, false);
				addListenersToReceiver(receiver);
				receiver.start();
				if (playReponse.hasVideoPort()) {
					RTPReceiver videoReceiver = RTPReceiver.create(hostname, port + 2, true);
					addListenersToReceiver(videoReceiver);
					videoReceiver.start();
				}
			}
			// Handle PlayResponse

			return true;
		}
	}
}
