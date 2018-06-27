package org.dilithium.network.commands;

import org.bouncycastle.util.encoders.Hex;
import org.dilithium.network.messages.uMessage;

public class TextCommand extends NetworkCommand {
    @Override
    public byte[] handle(uMessage in) {
        try {
            System.out.println(Hex.toHexString(in.getSender()) + ": " + new String(in.getPayload(), "UTF-8"));
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
