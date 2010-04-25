package com.jammed.app;

import com.jammed.event.RTPReceiverListener;
import com.jammed.event.ReceivedStopEvent;
import com.jammed.event.ReceivedStreamEvent;
import com.jammed.event.StreamEvent;
import com.sun.media.rtp.RTPSessionMgr;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 *
 * @author nmaludy
 */
public class RTPReceiver implements ReceiveStreamListener, SessionListener {

	private EventListenerList streamListeners;
	private RTPSessionMgr manager;
	private String host;
	private int port;
	private boolean isVideo;
	private int ttl = 1;

	private RTPReceiver(String h, int p, boolean v) {
		streamListeners = new EventListenerList();
		host = h;
		port = p;
		isVideo = v;
	}

	public static RTPReceiver create(String host, int port, boolean isVideo) {
		return new RTPReceiver(host, port, isVideo);
	}

	public void start() {
		SessionAddress localAddr;
		SessionAddress destAddr;
		InetAddress source;
		try {
			manager = (RTPSessionMgr)RTPManager.newInstance();
			manager.addSessionListener(this);
			manager.addReceiveStreamListener(this);
			source = InetAddress.getByName(host);
			localAddr = new SessionAddress(source, port, ttl);
			destAddr = new SessionAddress(source, port, ttl);
			manager.initialize(localAddr);
			BufferControl bc = (BufferControl) manager.getControl("javax.media.control.BufferControl");
			bc.setBufferLength(350);
			manager.addTarget(destAddr);
			System.out.println("Connecting to " + destAddr.toString());
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		} catch (InvalidSessionAddressException ex) {
			Logger.getLogger(RTPReceiver.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(RTPReceiver.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Close the session manager and event listeners
	 */
	public void stop() {
		if (manager != null) {
			//manager.removeTargets("Removing Receiver Targets");
			manager.dispose();
			manager = null;
		}
		System.out.println("killing RTPReceiver");
		ReceivedStopEvent stopEvent = ReceivedStopEvent.create(this);
		fireReceiverEvent(stopEvent);
	}

	public void addReceiverListener(final RTPReceiverListener l) {
		streamListeners.add(RTPReceiverListener.class, l);
	}

	public void removeReceiverListener(final RTPReceiverListener l) {
		streamListeners.remove(RTPReceiverListener.class, l);
	}

	protected void fireReceiverEvent(final StreamEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (streamListeners == null) {
					return;
				}
				Object[] listeners = streamListeners.getListenerList();
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == RTPReceiverListener.class) {
						((RTPReceiverListener) listeners[i + 1]).receivedStreamUpdate(event);
					}
				}
			}
		});
	}
	/*
	 * SessionListener.
	 */

	@Override
	public void update(SessionEvent evt) {
		if (evt instanceof NewParticipantEvent) {
			Participant p = ((NewParticipantEvent) evt).getParticipant();
			System.err.println("  - A new participant had just joined: " + p.getCNAME());
		}
	}

	/*
	 * ReceiveStreamListener
	 */
	@Override
	public void update(ReceiveStreamEvent evt) {
		System.out.println(evt.getClass().toString());
		Participant participant = evt.getParticipant();	// could be null.
		ReceiveStream stream = evt.getReceiveStream();  // could be null.

		if (evt instanceof RemotePayloadChangeEvent) {
			System.err.println("  - Received an RTP PayloadChangeEvent.");
			System.err.println("Sorry, cannot handle payload change.");
			stop();
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
				ReceivedStreamEvent e = ReceivedStreamEvent.create(this, ds, isVideo);
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
			/*
			ByeEvent bye = (ByeEvent)evt;
			if (bye.getReceiveStream() == null) {
				System.err.println("  - Got \"bye\" from: " + participant.getCNAME() + " they are a passive receiver, no big deal.");
			} else {
				System.err.println("  - Got \"bye\" from: " + participant.getCNAME() + " they are a sender, exiting.");
				stop();
			}
			*/
		}
	}
}
