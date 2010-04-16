package com.jammed.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.ControllerListener;
import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.swing.event.EventListenerList;

/**
 *
 * @author nmaludy
 */
public class RTPReceiver implements ReceiveStreamListener, SessionListener {

	private final EventListenerList streamListeners;
	RTPManager manager;
	int ttl = 1;

	public RTPReceiver(String host, int port, RTPReceiverListener listener) {
		SessionAddress localAddr;
		SessionAddress destAddr;
		InetAddress source;
		streamListeners = new EventListenerList();
		addReceiverListener(listener);

		try {
			manager = RTPManager.newInstance();
			manager.addSessionListener(this);
			manager.addReceiveStreamListener(this);
			source = InetAddress.getByName(host);

			if (source.isMulticastAddress()) {
				localAddr = new SessionAddress(source, port, ttl);
				destAddr = new SessionAddress(source, port, ttl);
			} else {
				localAddr = new SessionAddress(InetAddress.getLocalHost(), port);
				destAddr = new SessionAddress(source, port);
			}
			manager.initialize(localAddr);
			BufferControl bc = (BufferControl) manager.getControl("javax.media.control.BufferControl");
			bc.setBufferLength(350);
			manager.addTarget(destAddr);
			
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (InvalidSessionAddressException ex) {
			Logger.getLogger(RTPReceiver.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(RTPReceiver.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Close the players and the session managers.
	 */
	private void close() {
		manager.removeTargets("Closing session from AVReceive2");
		manager.dispose();
		manager = null;
	}


	/**
	 * SessionListener.
	 */
	public void update(SessionEvent evt) {
		if (evt instanceof NewParticipantEvent) {
			Participant p = ((NewParticipantEvent) evt).getParticipant();
			System.err.println("  - A new participant had just joined: " + p.getCNAME());
		}
	}

	/**
	 * ReceiveStreamListener
	 */
	public void update(ReceiveStreamEvent evt) {
		Participant participant = evt.getParticipant();	// could be null.
		ReceiveStream stream = evt.getReceiveStream();  // could be null.

		if (evt instanceof RemotePayloadChangeEvent) {
			System.err.println("  - Received an RTP PayloadChangeEvent.");
			System.err.println("Sorry, cannot handle payload change.");
			System.exit(0);
		} else if (evt instanceof NewReceiveStreamEvent) {
			try {
				stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
				DataSource ds = stream.getDataSource();

				// Find out the formats.
				RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				if (ctl != null) {
					System.err.println("  - Recevied new RTP stream: " + ctl.getFormat());
				} else {
					System.err.println("  - Recevied new RTP stream");
				}

				if (participant == null) {
					System.err.println("      The sender of this stream had yet to be identified.");
				} else {
					System.err.println("      The stream comes from: " + participant.getCNAME());
				}

				StreamReceivedEvent e = StreamReceivedEvent.create(this, ds);
				System.out.println("FireingStreamReceived");
				fireReceiverEvent(e);
				
			} catch (Exception e) {
				System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
				return;
			}

		} else if (evt instanceof StreamMappedEvent) {

			if (stream != null && stream.getDataSource() != null) {
				DataSource ds = stream.getDataSource();
				// Find out the formats.
				RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				System.err.println("  - The previously unidentified stream ");
				if (ctl != null) {
					System.err.println("      " + ctl.getFormat());
				}
				System.err.println("      had now been identified as sent by: " + participant.getCNAME());
			}
		} else if (evt instanceof ByeEvent) {

			System.err.println("  - Got \"bye\" from: " + participant.getCNAME());
		}

	}

	public void addReceiverListener(RTPReceiverListener l) {
		streamListeners.add(RTPReceiverListener.class, l);
	}

	public void removeReceiverListener(RTPReceiverListener l) {
		streamListeners.remove(RTPReceiverListener.class, l);
	}

	protected void fireReceiverEvent(StreamReceivedEvent event) {
		Object[] listeners = streamListeners.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == RTPReceiverListener.class) {
				((RTPReceiverListener) listeners[i + 1]).streamReceived(event);
			}
		}
	}
}
