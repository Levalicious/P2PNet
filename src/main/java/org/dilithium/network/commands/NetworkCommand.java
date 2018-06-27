package org.dilithium.network.commands;

import org.dilithium.network.messages.uMessage;

public abstract class NetworkCommand {
    public abstract byte[] handle(uMessage in);
}
