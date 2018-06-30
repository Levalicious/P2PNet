package org.levk.network.commands;

import org.levk.network.messages.Message;
import org.levk.util.Tuple;

public abstract class NetworkCommand {
    public abstract Tuple<Integer, byte[]> handle(Message in);
}
