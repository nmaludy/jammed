package com.jammed.app;

import com.jammed.event.RTPReceiverListener;
import com.google.protobuf.MessageLite;
import com.jammed.event.RTPTransmissionListener;
import com.jammed.event.ReceivedStopEvent;
import com.jammed.event.StreamEvent;
import com.jammed.event.TransmissionStopEvent;
import com.jammed.gen.MediaProtos.Media;
import com.jammed.gen.MessageProtos.PlayRequest;
import com.jammed.gen.MessageProtos.PlayResponse;
import com.jammed.gen.ProtoBuffer.Request;
import com.jammed.handlers.PlayRequestHandler;
import com.jammed.handlers.PlayResponseHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.media.MediaLocator;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 *
 * @author nmaludy
 */
public class RTPSessionManager {

	private final ExecutorService transmitters;
	private final List<Integer> transmissionIds;
	private final RTPTransmissionHandler requestHandler;
	private final RTPReceptionHandler responseHandler;
	private final Map<Integer, Request> receiveRequests;
	private final EventListenerList receiveListeners;
	private final SortedSet<Integer> portsInUse;

	private static class RTPSessionManagerHolder {
		static final RTPSessionManager instance = new RTPSessionManager();
	}

	private RTPSessionManager() {
		transmitters = Executors.newCachedThreadPool();
		transmissionIds= Collections.synchronizedList(new ArrayList<Integer>());
		requestHandler = new RTPTransmissionHandler();
		responseHandler = new RTPReceptionHandler();
		receiveRequests = Collections.synchronizedMap(new TreeMap<Integer, Request>());
		receiveListeners = new EventListenerList();
		portsInUse = Collections.synchronizedSortedSet(new TreeSet<Integer>());
		portsInUse.add(Integer.valueOf(2000));//Populate with initial port
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
		playRequest.setMedia(m);
		Request request = RequestPool.getInstance().lease();
		Integer requestId = Integer.valueOf(request.getId());
		receiveRequests.put(requestId, request); //Should always succeed due to ReuqestPool's uniqueness
		playRequest.setRequest(request);
		System.out.println("Sending playRequest " + requestId);
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
	private class RTPTransmissionHandler extends PlayRequestHandler implements RTPTransmissionListener {

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof PlayRequest)) {
				throw new IllegalArgumentException();
			}
			PlayRequest playRequest = (PlayRequest) message;
			Request request = playRequest.getRequest();
			String hostname = request.getOrigin();
			
			if (hostname.equals(Cloud.getInstance().getHostName())) {
				return false; //a request that originated from this system, ignore it
			}

			String mediaHost = playRequest.getMedia().getHostname();
			if (!mediaHost.equals(Cloud.getInstance().getHostName())) {
				return false; //this media isn't present on our system
			}
			System.out.println("Got a request for media on my machine");
			
			synchronized (transmissionIds) {
				if (!transmissionIds.contains(Integer.valueOf(request.getId()))) {
					try {
						Media media = playRequest.getMedia();
						boolean isVideo = MediaUtils.isVideo(media);
						File mediaFile = new File(media.getLocation());
						MediaLocator locator = new MediaLocator(mediaFile.toURI().toURL());
						String address = TransmissionAddressManager.getInstance().getAddress();
						Integer audioPort;

						PlayResponse.Builder builder = PlayResponse.newBuilder();
						builder.setType(builder.getType());
						builder.setAddress(address);
						synchronized(portsInUse) {
							audioPort = Integer.valueOf(portsInUse.last() + 2);
							builder.setAudioPort(audioPort);
							portsInUse.add(audioPort);
							if(isVideo) {
								Integer videoPort = Integer.valueOf(audioPort.intValue() + 2);
								builder.setVideoPort(videoPort);
								portsInUse.add(videoPort);
								System.out.println("Sending video");
							}
						}
						builder.setRequest(request);
						transmissionIds.add(Integer.valueOf(request.getId()));
						RTPTransmitter t = RTPTransmitter.create(locator, address, audioPort);
						t.addTransmitterListener(this);
						transmitters.execute(t);
						System.out.println("Transmitting");
						Cloud.getInstance().send(builder.build(), request.getId());
						System.out.println("Sending Play Response");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			return true;
		}

		public void transmissionStreamUpdate(StreamEvent event) {
			if (event instanceof TransmissionStopEvent) {
				TransmissionStopEvent ts = (TransmissionStopEvent) event;
				RTPTransmitter transmitter = (RTPTransmitter)ts.getSource();
				System.out.println("Killing transmission");
				synchronized (transmissionIds) {
					transmissionIds.remove(Integer.valueOf(transmitter.getAudioPort()));
					transmissionIds.remove(Integer.valueOf(transmitter.getVideoPort()));
				}
			}
		}
	}

	/*
	 * When a play request is sent the client on the other end of the line
	 * sends a PlayResponse telling which address and port to listen for the
	 * media on. This class handles the reponse to the request messages and starts
	 * the receiving of the stream.
	 */
	private class RTPReceptionHandler extends PlayResponseHandler {
		RTPReceiver receiver = null;
		RTPReceiver videoReceiver = null;

		@Override
		public boolean handleMessage(final MessageLite message) {
			if (!(message instanceof PlayResponse)) {
				throw new IllegalArgumentException();
			}

			PlayResponse playResponse = (PlayResponse) message;
			Request request = playResponse.getRequest();
			if (!request.getOrigin().equals(Cloud.getInstance().getHostName())) {
				return false; // not a request from this system
			}			
			Integer id = Integer.valueOf(request.getId());
			synchronized (receiveRequests) {
				if (receiveRequests.containsKey(id)) { //check if this request has been handled yet
					receiveRequests.remove(id);
					String hostname = playResponse.getAddress();
					int port = playResponse.getAudioPort();
					//String hostname = "224.1.1.1";
					//int port = 5000;

					stopReceivers();
					receiver = RTPReceiver.create(hostname, port, false);
					addListenersToReceiver(receiver);
					receiver.start();

					if (playResponse.hasVideoPort()) {
						videoReceiver = RTPReceiver.create(hostname, playResponse.getVideoPort(), true);
						addListenersToReceiver(videoReceiver);
						videoReceiver.start();
					}
				}
			}
			// Handle PlayResponse

			return true;
		}

		private void stopReceivers() {
			if (receiver != null) {
				receiver.stop();
				receiver = null;
			}
			if (videoReceiver != null) {
				videoReceiver.stop();
				videoReceiver = null;
			}
		}
	}
}
