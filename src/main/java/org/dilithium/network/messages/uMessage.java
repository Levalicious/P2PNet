package org.dilithium.network.messages;

import org.bouncycastle.util.BigIntegers;
import org.dilithium.crypto.ecdsa.ECKey;
import org.dilithium.util.ByteUtil;
import org.dilithium.util.Hex;
import org.dilithium.util.serialization.RLP;
import org.dilithium.util.serialization.RLPElement;
import org.dilithium.util.serialization.RLPItem;
import org.dilithium.util.serialization.RLPList;

import java.math.BigInteger;

import static com.cedarsoftware.util.ArrayUtilities.isEmpty;
import static org.dilithium.crypto.Hash.keccak256;
import static org.dilithium.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.dilithium.util.ByteUtil.concat;

public class uMessage {
    private static final int CHAIN_ID_INC = 35;
    private static final int LOWER_REAL_V = 27;
    private Integer chainId = 40;

    /* The RLP encoding for this packet */
    private byte[] rlpEncoded;

    /* The raw RLP encoding for this packet
     * (empty r and s values) */
    private byte[] rlpRaw;

    /* Whether the non-encoded fields have
     * had values assigned to them yet */
    private boolean parsed = false;

    /* An identifier as to what type of
     * message this packet contains. */
    private int messageType;

    /* The payload of this packet */
    private byte[] payload;

    /* The r value of the signature
     * on this packet */
    private byte[] r;

    /* The s value of the signature
     * on this packet */
    private byte[] s;

    /* The v byte to reconstruct
     * the sender address of this
     * packet */
    private byte v;

    /* Hash of packet. For validation, bloom
     * filters, and spam protection. */
    private byte[] hash;

    /* Reconstruct packet from RLP encoding */
    public uMessage(byte[] rlpEncoded) {
        this.rlpEncoded = rlpEncoded;
    }

    /* Construct new packet */
    public uMessage( int messageType, byte[] payload, byte[] privkey) {
        this.messageType = messageType;
        this.payload = payload;

        parsed = true;

        this.sign(privkey);

        this.hash = getHash();
    }

    /* TODO: Set up constructors to construct based on messageType - Completely ignore payload in certain cases */

    public synchronized void rlpParse() {
        if (parsed) return;
        try {
            RLPList decodedMessageList = RLP.decode2(rlpEncoded);
            RLPList packet = (RLPList) decodedMessageList.get(0);

            if (packet.size() > 5) throw new RuntimeException("Too many RLP elements");
            for (RLPElement rlpElement : packet) {
                if(!(rlpElement instanceof RLPItem)) throw new RuntimeException("uMessage RLP elements shouldn't be lists");
            }

            this.messageType = ByteUtil.byteArrayToInt(packet.get(0).getRLPData());
            this.payload = packet.get(1).getRLPData();
            this.r = packet.get(2).getRLPData();
            this.s = packet.get(3).getRLPData();
            byte[] vData = packet.get(4).getRLPData();
            BigInteger v = ByteUtil.bytesToBigInteger(vData);
            this.v = getRealV(v);
            this.parsed = true;
            this.hash = getHash();
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
    }

    public int getMessageType() {
        rlpParse();
        return messageType;
    }

    public byte[] getPayload() {
        rlpParse();
        return this.payload;
    }

    public ECKey.ECDSASignature getSig() {
        rlpParse();
        return ECKey.ECDSASignature.fromComponents(r,s,v);
    }

    private void sign(byte[] privKeyBytes) {
        this.sign(ECKey.fromPrivate(privKeyBytes));
    }

    private void sign(ECKey key) {
        ECKey.ECDSASignature temp = key.sign(this.getRawHash());
        this.r = BigIntegers.asUnsignedByteArray(temp.r);
        this.s = BigIntegers.asUnsignedByteArray(temp.s);
        this.v = temp.v;
    }

    public byte[] getSender() {
        rlpParse();
        ECKey.ECDSASignature temp = ECKey.ECDSASignature.fromComponents(r, s, v);

        try {
            return ECKey.signatureToAddress(this.getRawHash(), temp);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Sig recovery failed");
            return null;
        }
    }

    public byte[] getRawHash() {
        rlpParse();
        byte[] plainMsg = this.getEncodedRaw();
        return keccak256(plainMsg);
    }

    public byte[] getHash() {
        if(!isEmpty(hash)) return hash;

        rlpParse();
        byte[] plainMsg = this.getEncoded();
        return keccak256(plainMsg);
    }

    public byte[] getEncodedRaw() {
        rlpParse();
        if(rlpRaw != null) return rlpRaw;
        byte[] messageType = RLP.encodeInt(this.messageType);
        byte[] payload = RLP.encodeElement(this.payload);
        byte[] r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
        byte[] s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
        byte[] v = RLP.encodeInt(chainId);

        rlpRaw = RLP.encodeList(messageType, payload, r, s, v);

        return rlpRaw;
    }

    public byte[] getEncoded() {
        if (rlpEncoded != null) return rlpEncoded;

        byte[] messageType = RLP.encodeInt(this.messageType);
        byte[] payload = RLP.encodeElement(this.payload);
        byte[] r = RLP.encodeElement(this.r);
        byte[] s = RLP.encodeElement(this.s);
        int encodeV = this.v - LOWER_REAL_V;
        encodeV += chainId * 2 + CHAIN_ID_INC;
        byte[] v = RLP.encodeInt(encodeV);

        this.rlpEncoded = RLP.encodeList(messageType, payload, r, s, v);

        this.hash = this.getHash();

        return rlpEncoded;
    }

    private byte getRealV(BigInteger bv) {
        if (bv.bitLength() > 31) return 0; // chainId is limited to 31 bits, longer are not valid for now
        long v = bv.longValue();
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) return (byte) v;
        byte realV = LOWER_REAL_V;
        int inc = 0;
        if ((int) v % 2 == 0) inc = 1;
        return (byte) (realV + inc);
    }
}