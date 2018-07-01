package org.levk.p2pnet.network.commands;

import org.levk.p2pnet.network.messages.Message;
import org.levk.p2pnet.util.Tuple;

public abstract class NetworkCommand {
    public abstract Tuple<Integer, byte[]> handle(Message in);
}
