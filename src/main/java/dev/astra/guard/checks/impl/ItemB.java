package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public final class ItemB implements Check {
    public String name() {
        return "Item-B";
    }

    public void handle(PacketReceiveEvent e) {
    }
}