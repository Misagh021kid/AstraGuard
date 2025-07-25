package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import dev.astra.guard.checks.Check;

public final class ItemA implements Check {
    public String name() { return "Item-A"; }
    public void handle(PacketReceiveEvent e) { }
}