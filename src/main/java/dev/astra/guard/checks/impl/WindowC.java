package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public final class WindowC implements Check {
    public String name() { return "Window-C"; }
    public void handle(PacketReceiveEvent e) { }
}
