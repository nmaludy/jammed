package com.jammed.app;

import java.awt.Dimension;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.Control;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.NoProcessorException;
import javax.media.Owned;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;

/**
 *
 * @author nmaludy
 */
public class RTPTransmitter implements ControllerListener, Runnable {

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();

	// Input MediaLocator
	// Can be a file or http or capture source
	private MediaLocator locator;
	private String ipAddress;
	private int portBase;
	private Processor processor = null;
	private RTPManager rtpMgrs[];
	private DataSource dataOutput = null;

	public RTPTransmitter(MediaLocator locator, String ipAddress, int portBase) {
		this.locator = locator;
		this.ipAddress = ipAddress;
		this.portBase = portBase;
	}

	/**
	 * Starts the transmission. Returns null if transmission started ok.
	 * Otherwise it returns a string with the reason why the setup failed.
	 */
	public void start() {
		executor.execute(this);
	}

	public void run() {
		createAndConfigureProcessor();
	}
	/**
	 * Stops the transmission if already started
	 */
	public void stop() {
		if (processor != null) {
			processor.stop();
			processor.close();
			processor = null;
			for (int i = 0; i < rtpMgrs.length; i++) {
				rtpMgrs[i].removeTargets("Session ended.");
				rtpMgrs[i].dispose();
			}
		}
	}

	public void controllerUpdate(ControllerEvent ce) {
		if (ce instanceof ConfigureCompleteEvent) {
			setupTracks();
		} else if (ce instanceof RealizeCompleteEvent) {
			setupQuality();
			createTransmitter(); //calls start transmission
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
		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

		Format supported[];
		Format chosen;
		boolean atLeastOneTrack = false;

		// Program the tracks.
		for (int i = 0; i < tracks.length; i++) {
			if (tracks[i].isEnabled()) {
				supported = tracks[i].getSupportedFormats();
				if (supported.length > 0) {
					if (supported[0] instanceof VideoFormat) {
						chosen = checkForVideoSizes(tracks[i].getFormat(), supported[0]);
					} else {
						chosen = supported[0];
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
		int port;

		for (int i = 0; i < pbss.length; i++) {
			try {
				rtpMgrs[i] = RTPManager.newInstance();
				port = portBase + 2 * i;
				ipAddr = InetAddress.getByName(ipAddress);
				localAddr = new SessionAddress(InetAddress.getLocalHost(), port);
				destAddr = new SessionAddress(ipAddr, port);
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
					  : (int) (size.width / 8) * 8);
			height = (size.height % 8 == 0 ? size.height
					  : (int) (size.height / 8) * 8);
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

		Control cs[] = p.getControls();
		QualityControl qc = null;
		VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

		// Loop through the controls to find the Quality control for
		// the JPEG encoder.
		for (int i = 0; i < cs.length; i++) {

			if (cs[i] instanceof QualityControl
					  && cs[i] instanceof Owned) {
				qc = (QualityControl) cs[i];
				qc.setQuality(1.0f);
				//Object owner = ((Owned) cs[i]).getOwner();

				// Check to see if the owner is a Codec.
				// Then check for the output format.
//				if (owner instanceof Codec) {
//					Format fmts[] = ((Codec) owner).getSupportedOutputFormats(null);
//					for (int j = 0; j < fmts.length; j++) {
//						if (fmts[j].matches(jpegFmt)) {
//							qc = (QualityControl) cs[i];
//							qc.setQuality(val);
//							System.err.println("- Setting quality to "
//									  + val + " on " + qc);
//							break;
//						}
//					}
//				}
//				if (qc != null) {
//					break;
//				}
			}
		}
	}
}
