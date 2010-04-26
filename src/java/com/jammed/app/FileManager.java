
package com.jammed.app;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileManager {
	
	public final static byte[] getBytesFromFile (final File file) {
		
		InputStream is = null;
		
		try {
			is = new FileInputStream(file);
			
			final long length = file.length();
			
			if (length > Integer.MAX_VALUE) {
				// File is too large
				throw new IOException();
			}
			
			byte[] bytes = new byte[(int)length];
			
			int offset  = 0;
			int numRead = 0;
			
			while (offset < bytes.length &&
				(numRead = is.read(bytes, offset, bytes.length-offset)) >= 0) {
			
				offset += numRead;
			}
			
			if (offset < bytes.length) {
				// Failed to read the file completly
				throw new IOException();
			}
			
			is.close();
			return bytes;
			
		} catch (final Exception e) {
			if (is != null) {
				try {
					is.close();
				} catch (final Exception e1) {}
			}
			
			return new byte[] {-1};
		}
	}
	
	public final static void writeBytesToFile (final byte[] bytes,
		final File file) {
		try {
			final FileOutputStream fos = new FileOutputStream(file);
			
			fos.write(bytes);
			fos.close();
		} catch (final Exception e) { }
	}
	
	public final static void writeBytesToStream (final byte[] bytes,
		final FileOutputStream fos) {
		try {
			fos.write(bytes);
			fos.close();
		} catch (final Exception e) { }
	}

}
