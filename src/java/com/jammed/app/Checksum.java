
package com.jammed.app;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
	
	public final static byte[] SHA1 (final byte[] data) {
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-1");
			
			return md.digest(data);
		} catch (final NoSuchAlgorithmException nsae) { }
		
		return new byte[] { -1 };
	}
	
    public final static int fletcher32 (final int[] data) {
        return fletcher32(data, data.length);
    }

    public final static int fletcher32 (final int[] data, final int length) {
        int sum1 = 0xFFFF;
        int sum2 = 0xFFFF;
        int len  = length;
        int i    = 0;

        while (len > 0) {
            int tlen = len > 360 ? 360 : len;
            len -= tlen;

            do {
                sum1 += data[i++];
                sum2 += sum1;
            } while (--tlen > 0);

            sum1 = (sum1 & 0xFFFF) + (sum1 >>> 16);
            sum2 = (sum2 & 0xFFFF) + (sum2 >>> 16);
        }

        sum1 = (sum1 & 0xFFFF) + (sum1 >>> 16);
        sum2 = (sum2 & 0xFFFF) + (sum2 >>> 16);

        return sum2 << 16 | sum1;
    }
	
	public final static int fletcher16 (final byte[] data) {
		return fletcher16(data, data.length);
	}
	
	public final static int fletcher16 (final byte[] data, final int length) {
		int sum1 = 0xFF;
		int sum2 = 0xFF;
		int len  = length;
		int i    = 0;
		
		while (len > 0) {
			int tlen = len > 21 ? 21 : len;
			len -= tlen;
			
			do {
				sum1 += data[i++];
				sum2 += sum1;
			} while (--tlen > 0);
			
			sum1 = (sum1 & 0xFF) + (sum1 >>> 8);
			sum2 = (sum2 & 0xFF) + (sum2 >>> 8);
		}
		
		sum1 = (sum1 & 0xFF) + (sum1 >>> 8);
		sum2 = (sum2 & 0xFF) + (sum2 >>> 8);
		
		return sum2 << 8 | sum1;
	}
	
}
