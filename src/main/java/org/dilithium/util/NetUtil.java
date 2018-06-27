package org.dilithium.util;

import org.dilithium.crypto.ecdsa.ECKey;

import java.security.SignatureException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.dilithium.crypto.Hash.keccak256;
import static org.dilithium.util.ByteUtil.concat;
import static org.dilithium.util.ByteUtil.xor;

public class NetUtil {
    private static int targetedPacketDataSize = 344;
    private static int untargetedPacketDataSize = 378;

    private static byte[] startByte = {(byte)0xFF};
    private static byte[] zero = {(byte)0x00};

    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static String fromBitSet(BitSet set) {
        final StringBuilder buffer = new StringBuilder(set.length());
        IntStream.range(0, set.length()).mapToObj(i -> set.get(i) ? '1' : '0').forEach(buffer::append);
        String temp = buffer.reverse().toString();
        while(temp.length() < 256) {
            temp = 0 + temp;
        }

        return temp;
    }

    private static byte[] removePadding(byte[] data) {
        while(data[0] == (byte) 0x00) {
            data = Arrays.copyOfRange(data, 1, data.length);
        }

        if(data[0] == startByte[0]) {
            data = Arrays.copyOfRange(data, 1, data.length);
        }

        return data;
    }

    public static int packetSize(int initialSize, byte[] payload) {
        return (initialSize - 211) - ((int)Math.floor(log(16, payload.length) - 2));
    }

    private static double log(int base, int num) {
        double numerator = Math.log(num);
        double denominator = Math.log(base);
        return (numerator / denominator);
    }

    public static String calcDist(byte[] from, byte[] to) {
        return fromBitSet(fromByteArray(xor(from, to)));
    }
}
