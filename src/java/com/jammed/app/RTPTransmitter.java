package com.jammed.app;

import com.jammed.event.RTPTransmissionListener;
import com.jammed.event.StreamEvent;
import com.jammed.event.TransmissionStopEvent;
import java.awt.Dimension;
import java.io.IOException;
import java.net.InetAddress;
import javax.media.ConfigureCompleteEvent;
import javax.media.Control;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.Owned;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.control.BitRateControl;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 *
 * @author nmaludy
 */
public class RTPTransmitter implements ControllerListener, Runnable {

	private EventListenerList streamListeners;
	private MediaLocator locator;
	private String ipAddress;
	private int portBase;
	private int audioPort = -1;
	private int videoPort = -1;
	private Processor processor = null;
	private RTPManager rtpMgrs[];
	private DataSource dataOutput = null;

	private RTPTransmitter(MediaLocator locator, String ipAddress, int portBase) {
		this.streamListeners = new EventListenerList();
		this.locator = locator;
		this.ipAddress = ipAddress;
		this.portBase = portBase;
	}

	public static RTPTransmitter create(MediaLocator locator, String ipAddress, int portBase) {
		return new RTPTransmitter(locator, ipAddress, portBase);
	}

	/**
	 * Starts the transmission. Returns null if transmission started ok.
	 * Otherwise it returns a string with the reason why the setup failed.
	 */
	public void run() {
		createAndConfigureProcessor();
	}

	public void addTransmitterListener(final RTPTransmissionListener l) {
		streamListeners.add(RTPTransmissionListener.class, l);
	}

	public void removeTransmitterListener(final RTPTransmissionListener l) {
		streamListeners.remove(RTPTransmissionListener.class, l);
	}

	protected void fireTransmitterEvent(final StreamEvent event) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (streamListeners == null) {
					return;
				}
				Object[] listeners = streamListeners.getListenerList();
				System.out.println("Killing RTPTransmitter");
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == RTPTransmissionListener.class) {
						((RTPTransmissionListener) listeners[i + 1]).transmissionStreamUpdate(event);
					}
				}
			}
		});
	}

	public int getAudioPort() {
		return audioPort;
	}

	public int getVideoPort() {
		return videoPort;
	}

	/**
	 * Stops the transmission if already started
	 */
	public void stop() {
		if (processor != null) {
			processor.stop();
			processor.close();
			processor.deallocate();
			processor = null;
		}
		if (rtpMgrs != null) {
			for (int i = 0; i < rtpMgrs.length; i++) {
				rtpMgrs[i].removeTargets("Session ended.");
				System.out.println("Removing target " + i);
				rtpMgrs[i].dispose();
			}
			rtpMgrs = null;
		}
		TransmissionStopEvent stopEvent = TransmissionStopEvent.create(this);
		fireTransmitterEvent(stopEvent);
	}

	public void controllerUpdate(ControllerEvent ce) {
		if (ce instanceof ConfigureCompleteEvent) {
			setupTracks();
		} else if (ce instanceof RealizeCompleteEvent) {
			setupQuality();
			createTransmitter(); //calls start transmission
		} else if (ce instanceof EndOfMediaEvent) {
			stop();
		}
	}

	private void createAndConfigureProcessor() {
		try {
			DataSource ds = Manager.createDataSource(locator);
			processor = Manager.createProcessor(ds);
			processor.addControllerListener(this);
			processor.configure(); //ConfigureCompletedEvent sent to controllerUdpate() when done
		} catch (NoDataSourceException ex) {
			System.err.println("Datasource coudln't be created for locator");
		} catch (NoProcessorException npe) {
			System.err.println("Couldn't create processor");
		} catch (IOException ioe) {
			System.err.println("IOException creating processor");
		}
	}

	private void setupTracks() {
		// Get the tracks from the processor
		TrackControl[] tracks = processor.getTrackControls();
		if (tracks == null || tracks.length < 1) {
			System.err.println("Couldn't find tracks in processor");
			return;
		}
		//Only allow supported RTP payload formats
		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

		Format supported[];
		Format chosen;
		boolean atLeastOneTrack = false;

		double audioQuality = 0.0;
		float videoQuality = 0.0f;
		// Program the tracks.
		for (int i = 0; i < tracks.length; i++) {
			if (tracks[i].isEnabled()) {
				supported = tracks[i].getSupportedFormats();
				if (supported.length > 0) {
					chosen = supported[0]; //default
					for (int j = 0; j < supported.length; j++) {
						if (supported[i] instanceof VideoFormat) {
							//chosen = checkForVideoSizes(tracks[i].getFormat(), supported[0]);
							VideoFormat vf = (VideoFormat) supported[j];
							if (vf.getFrameRate() > videoQuality) {
								videoQuality = vf.getFrameRate();
								chosen = checkForVideoSizes(tracks[i].getFormat(), supported[j]);
							}
						} else if (supported[j] instanceof AudioFormat) {
							AudioFormat af = (AudioFormat) supported[j];
							if (af.getSampleRate() > audioQuality) {
								audioQuality = af.getSampleRate();
								chosen = supported[j];
							}
						}
					}
					tracks[i].setFormat(chosen);
					System.err.println("Track " + i + " is set to transmit as:");
					System.err.println("  " + chosen);
					atLeastOneTrack = true;
				} else {
					tracks[i].setEnabled(false);
				}
			} else {
				tracks[i].setEnabled(false);
			}
		}

		if (!atLeastOneTrack) {
			System.err.println("Couldn't set any of the tracks to a valid RTP format");
			return;
		}
		processor.realize(); //When complete sends RealizeCompleteEvent to controllerUpdate()
	}

	private void setupQuality() {
		setJPEGQuality(processor, 0.5f);
		dataOutput = processor.getDataOutput();
	}

	/**
	 * Use the RTPManager API to create sessions for each media
	 * track of the processor.
	 */
	private void createTransmitter() {

		// Cheated.  Should have checked the type.
		PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
		PushBufferStream pbss[] = pbds.getStreams();

		rtpMgrs = new RTPManager[pbss.length];
		SessionAddress localAddr, destAddr;
		InetAddress ipAddr;
		SendStream sendStream;
		int port = portBase;

		for (int i = 0; i < pbss.length; i++) {
			try {
				if (pbss[i].getFormat() instanceof AudioFormat) {
					port = portBase;
					audioPort = port;
				} else if (pbss[i].getFormat() instanceof VideoFormat) {
					port = portBase + 2;
					videoPort = port;
				}
				rtpMgrs[i] = RTPManager.newInstance();
				ipAddr = InetAddress.getByName(ipAddress);
				localAddr = new SessionAddress(ipAddr, port, 1);
				destAddr = new SessionAddress(ipAddr, port, 1);
				rtpMgrs[i].initialize(localAddr);
				rtpMgrs[i].addTarget(destAddr);
				System.err.println("Created RTP session: " + ipAddress + " " + port);
				sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
				sendStream.start();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return;
			}
		}
		startTransmission();
	}

	private void startTransmission() {
		processor.start();
	}

	/**
	 * For JPEG and H263, we know that they only work for particular
	 * sizes.  So we'll perform extra checking here to make sure they
	 * are of the right sizes.
	 */
	Format checkForVideoSizes(Format original, Format supported) {

		int width, height;
		Dimension size = ((VideoFormat) original).getSize();
		Format jpegFmt = new Format(VideoFormat.JPEG_RTP);
		Format h263Fmt = new Format(VideoFormat.H263_RTP);

		if (supported.matches(jpegFmt)) {
			// For JPEG, make sure width and height are divisible by 8.
			width = (size.width % 8 == 0 ? size.width
					  : (size.width / 8) * 8);
			height = (size.height % 8 == 0 ? size.height
					  : (size.height / 8) * 8);
		} else if (supported.matches(h263Fmt)) {
			// For H.263, we only support some specific sizes.
			if (size.width < 128) {
				width = 128;
				height = 96;
			} else if (size.width < 176) {
				width = 176;
				height = 144;
			} else {
				width = 352;
				height = 288;
			}
		} else {
			// We don't know this particular format.  We'll just
			// leave it alone then.
			return supported;
		}

		return (new VideoFormat(null,
				  new Dimension(width, height),
				  Format.NOT_SPECIFIED,
				  null,
				  Format.NOT_SPECIFIED)).intersects(supported);
	}

	/**
	 * Setting the encoding quality to the specified value on the JPEG encoder.
	 * 0.5 is a good default.
	 */
	void setJPEGQuality(Player p, float val) {
		Control controls[] = p.getControls();
		//VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

		for (int i = 0; i < controls.length; i++) {
			if (controls[i] instanceof QualityControl
					  && controls[i] instanceof Owned) {
				QualityControl qc = (QualityControl) controls[i];
				qc.setQuality(1.0f);
			} else if (controls[i] instanceof BitRateControl) {
				BitRateControl brc = (BitRateControl) controls[i];
				int maxBitRate = brc.getMaxSupportedBitRate();
				brc.setBitRate(maxBitRate);
			}

		}
	}
}
