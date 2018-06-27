package org.dilithium.network.messages;

import org.bouncycastle.util.BigIntegers;
import org.dilithium.crypto.ecdsa.ECKey;
import org.dilithium.util.ByteUtil;
import org.dilithium.util.serialization.RLP;
import org.dilithium.util.serialization.RLPElement;
import org.dilithium.util.serialization.RLPItem;
import org.dilithium.util.serialization.RLPList;

import static com.cedarsoftware.util.ArrayUtilities.isEmpty;
import static org.dilithium.crypto.Hash.keccak256;
import static org.dilithium.util.ByteUtil.EMPTY_BYTE_ARRAY;

public class tMessage {
    /* The RLP encoding for this packet */
    private byte[] rlpEncoded;

    /* The raw RLP encoding for this packet
     * (empty r and s values) */
    private byte[] rlpRaw;

    /* Whether the non-encoded fields have
     * had values assigned to them yet */
    private boolean parsed = false;

    /* The target recipient of the payload */
    private byte[] target;

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
    public tMessage(byte[] rlpEncoded) {
        this.rlpEncoded = rlpEncoded;
    }

    /* Construct new packet */
    public tMessage(byte[] target, int messageType, byte[] payload, byte[] privkey) {
        this.target = target;
        this.messageType = messageType;
        this.payload = payload;

        parsed = true;

        this.sign(privkey);

        this.hash = getHash();
    }

    public tMessage(int messageType, byte[] payload, byte[] privkey) {
        this.target = null;
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

            if (packet.size() > 6) throw new RuntimeException("Too many RLP elements");
            for (RLPElement rlpElement : packet) {
                if (!(rlpElement instanceof RLPItem))
                    throw new RuntimeException("tMessage RLP elements shouldn't be lists");
            }

            this.target = packet.get(0).getRLPData();
            this.messageType = ByteUtil.byteArrayToInt(packet.get(1).getRLPData());
            this.payload = packet.get(2).getRLPData();
            this.r = packet.get(3).getRLPData();
            this.s = packet.get(4).getRLPData();
            this.v = (byte) ByteUtil.byteArrayToInt(packet.get(5).getRLPData());
            this.parsed = true;
            this.hash = getHash();
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
    }

    public byte[] getTarget() {
        return target;
    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] getPayload() {
        rlpParse();
        return this.payload;
    }

    public ECKey.ECDSASignature getSig() {
        return ECKey.ECDSASignature.fromComponents(r, s, v);
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

    public byte[] getRawHash() {
        rlpParse();
        byte[] plainMsg = this.getEncodedRaw();
        return keccak256(plainMsg);
    }

    public byte[] getHash() {
        if (!isEmpty(hash)) return hash;

        rlpParse();
        byte[] plainMsg = this.getEncoded();
        return keccak256(plainMsg);
    }

    public byte[] getEncodedRaw() {
        rlpParse();
        if (rlpRaw != null) return rlpRaw;

        byte[] target = RLP.encodeElement(this.target);
        byte[] messageType = RLP.encodeInt(this.messageType);
        byte[] payload = RLP.encodeElement(this.payload);
        byte[] r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
        byte[] s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
        byte[] v = RLP.encodeByte((byte) 0x50);

        rlpRaw = RLP.encodeList(target, messageType, payload, r, s, v);

        return rlpRaw;
    }

    public byte[] getEncoded() {
        if (rlpEncoded != null) return rlpEncoded;

        byte[] target = RLP.encodeElement(this.target);
        byte[] messageType = RLP.encodeInt(this.messageType);
        byte[] payload = RLP.encodeElement(this.payload);
        byte[] r = RLP.encodeElement(this.r);
        byte[] s = RLP.encodeElement(this.s);
        byte[] v = RLP.encodeByte(this.v);

        this.rlpEncoded = RLP.encodeList(target, messageType, payload, r, s, v);

        this.hash = this.getHash();

        return rlpEncoded;
    }
}
