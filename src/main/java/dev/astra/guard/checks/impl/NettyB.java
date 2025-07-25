package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public final class NettyB implements Check {
    public String name() {
        return "Netty-B";
    }

    public void handle(PacketReceiveEvent e) {
    }
}