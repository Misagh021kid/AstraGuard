package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public final class BundleA implements Check {
    public String name() { return "Bundle-A"; }
    public void handle(PacketReceiveEvent e) { }
}