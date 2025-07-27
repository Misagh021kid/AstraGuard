package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTabComplete;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class FloodA implements Check {

    private static final int MAX_LEN = 300;
    private static final int MAX_BURST = 5;

    private static final Cache<UUID, LongAdder> BURST = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .build();

    @Override
    public String name() {
        return "Flood-A";
    }

    @Override
    public void handle(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.TAB_COMPLETE) return;

        var player = (Player) event.getPlayer();
        if (player == null || !player.isOnline()) return;

        var wrapper = new WrapperPlayClientTabComplete(event);
        var cmd = wrapper.getText();

        if (cmd == null || cmd.isBlank()) return;

        if (cmd.length() > MAX_LEN) {
            flag(player, event, "TooLong len=%d".formatted(cmd.length()));
            return;
        }

        var counter = BURST.asMap().computeIfAbsent(player.getUniqueId(), id -> new LongAdder());
        counter.increment();

        if (counter.intValue() > MAX_BURST) {
            flag(player, event, "TooFast burst=%d".formatted(counter.intValue()));
        }
    }

    private static void flag(Player player, PacketReceiveEvent event, String detail) {
        TaskUtil.flag(player, "Flood-A", detail);
        event.setCancelled(true);
    }
}
