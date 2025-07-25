package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public final class WindowA implements Check {
    public String name() { return "Window-A"; }
    public void handle(PacketReceiveEvent e) { }
}
