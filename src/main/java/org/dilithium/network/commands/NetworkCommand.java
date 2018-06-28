package org.dilithium.network.commands;

import org.dilithium.network.messages.uMessage;
import org.dilithium.util.Tuple;

public abstract class NetworkCommand {
    public abstract Tuple<Integer, byte[]> handle(uMessage in);
}
