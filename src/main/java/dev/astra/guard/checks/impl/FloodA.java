package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public class FloodA implements Check {
    @Override
    public String name() {
        return "Flood-A";
    }

    @Override
    public void handle(PacketReceiveEvent e) {

    }
}
