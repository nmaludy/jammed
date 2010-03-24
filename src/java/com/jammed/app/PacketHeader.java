
package com.jammed.app;

public class PacketHeader implements Comparable<PacketHeader> {

    private static final int FIN = 1;
	private static final int CHK = 2;
    
    private static final int INT_SIZE  = 3;
    private static final int BYTE_SIZE = INT_SIZE << 2;

    private int type;
    private int sequence;
    private int request;
    private int source;
    private int options;
    private int checksum;

    public static int getSizeInBytes() {
        return BYTE_SIZE;
    }

    public PacketHeader() {
        
    }

    public PacketHeader(final byte[] data) {
        byte[] currentInt = new byte[4];
        int[] header      = new int[INT_SIZE];
        int[] check       = new int[2];
        int calculated    = -1;

        for (int i = 0; i < INT_SIZE; i++) {
            System.arraycopy(data, i * 4, currentInt, 0, 4);
            header[i] = byteArrayToInteger(currentInt);
        }

        request  = header[0] & 0x3FF;
        sequence = (header[0] >>> 10) & 0x3FF;
        type     = (header[0] >>> 24) & 0xFF;

        options  = (header[1] & 0xFFFF);
        source   = (header[1] >>> 16) & 0xFFFF;

        checksum = header[2];

        check[0]   = header[0] & 0xFFFF;
        check[1]   = (header[0] >>> 16) & 0xFFFF;
        calculated = Checksum.fletcher32(check);

        if (checksum != calculated) {
            throw new RuntimeException("Packet failed checksum");
        }

    }

    public byte[] build() {
        byte[] result = new byte[BYTE_SIZE];
        int[] check   = new int[2];
        int[] ints    = buildIntegerArray();
        int bitIndex  = 0;

        check[0] = ints[0] & 0xFFFF;
        check[1] = (ints[0] >>> 16) & 0xFFFF;

        checksum = Checksum.fletcher32(check);
        ints[2]  = checksum;
        
        for (int i = 0; i < ints.length; i++) {
            byte[] bits = integerToByteArray(ints[i]);
            
            for (int j = 0; j < bits.length; j++) {
                result[bitIndex++] = bits[j];
            }
        }
        
        return result;
    }

    public void setType (final int type) {
        if (type > 255) { // 8 bits
            throw new IllegalArgumentException("Field too large: " + type);
        }

        this.type = type;
    }

    public void setSequence (final int sequence) {
        if (sequence > 1023) { // 10 bits
            throw new IllegalArgumentException("Field too large: " + sequence);
        }

        this.sequence = sequence;
    }

    public void setRequest (final int request) {
        if (request > 1023) { // 10 bits
             throw new IllegalArgumentException("Field too large: " + request);
        } else if (request == 0) {
			throw new IllegalArgumentException("Request cannot be zero");
		}

        this.request = request;
    }

    public void setSource (final int source) {
        if (source > 65535) { // 16 bits
            throw new IllegalArgumentException("Field too large: " + source);
        }

        this.source = source;
    }

    public void setFinished (final boolean finished) {
		setBit(finished, FIN);
    }
	
	public void setChunk (final boolean chunk) {
		setBit(chunk, CHK);
	}

    public int getType() {
        return type;
    }

    public int getSequence() {
        return sequence;
    }

    public int getRequest() {
        return request;
    }

    public int getSource() {
        return source;
    }

    public boolean isFinished () {
        return isBitSet(FIN);
    }
	
	public boolean isChunk() {
		return isBitSet(CHK);
	}

    public int getChecksum() {
        return checksum;
    }

    protected int[] buildIntegerArray() {
        int[] result = new int[INT_SIZE];

        result[0] = request & 0x3FF;
        result[0] += (sequence & 0x3FF) << 10;
        result[0] += (type & 0xFF) << 24;

        result[1] = options & 0xFFFF;
        result[1] += (source & 0xFFFF) << 16;
        
        return result;
    }
	
	private final void setBit(final boolean bool, final int bit) {
		if (bool) {
			setBit(bit);
		} else {
			clearBit(bit);
		}
	}

    private final boolean isBitSet(final int bit) {
        return (options & bit) > 0;
    }

    private final void setBit(final int bit) {
        options |= bit;
    }

    private final void clearBit(final int bit) {
        options &= ~bit;
    }
    
    private final static byte[] integerToByteArray(final int integer) {
        return new byte[] {
            (byte) (integer >>> 24),
            (byte) (integer >>> 16),
            (byte) (integer >>> 8),
            (byte) (integer)
        };
    }

    private final static int byteArrayToInteger(final byte[] bytes) {
        return (
                ((bytes[0] << 24)) +
                ((bytes[1] & 0xFF) << 16) +
                ((bytes[2] & 0xFF) << 8) +
                ((bytes[3] & 0xFF)));
    }
    
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		
		sb.append('[');
		sb.append("{type = 0x");
		sb.append(Integer.toHexString(type).toUpperCase());
		sb.append("}, ");
		sb.append("{sequence = 0x");
		sb.append(Integer.toHexString(sequence).toUpperCase());
		sb.append("}, ");
		sb.append("{request = 0x");
		sb.append(Integer.toHexString(request).toUpperCase());
		sb.append("}, ");
		sb.append("{source = 0x");
		sb.append(Integer.toHexString(source).toUpperCase());
		sb.append("}, ");
		sb.append("{options = 0x");
		sb.append(Integer.toHexString(options).toUpperCase());
		sb.append("}, ");
		sb.append("{checksum = 0x");
		sb.append(Integer.toHexString(checksum).toUpperCase());
		sb.append("}");
		sb.append(']');
		
		return sb.toString();
	}
	
	public int compareTo (final PacketHeader other) {
		return ((Integer)this.getSequence()).compareTo(other.getSequence());
	}
}

