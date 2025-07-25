package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public final class NettyC implements Check {
    public String name() { return "Netty-C"; }
    public void handle(PacketReceiveEvent e) { }
}