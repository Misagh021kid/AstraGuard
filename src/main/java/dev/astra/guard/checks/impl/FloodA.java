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

    private static final int MAX_LEN   = 512;
    private static final int MAX_BURST = 3;

    private static final Cache<UUID, LongAdder> BURST =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.SECONDS)
                    .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                    .build();

    @Override public String name() { return "Flood-A"; }

    @Override
    public void handle(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.TAB_COMPLETE) return;

        Player player = (Player) event.getPlayer();
        if (player == null) return;

        WrapperPlayClientTabComplete wrapper = new WrapperPlayClientTabComplete(event);
        String cmd = wrapper.getText();

        if (cmd.length() > MAX_LEN) {
            flag(player, event, "len=" + cmd.length());
            return;
        }

        LongAdder counter = BURST.getIfPresent(player.getUniqueId());
        if (counter == null) { counter = new LongAdder(); BURST.put(player.getUniqueId(), counter); }
        counter.increment();

        if (counter.intValue() > MAX_BURST) {
            flag(player, event, "burst=" + counter.intValue());
        }
    }

    private static void flag(Player p, PacketReceiveEvent e, String detail) {
        TaskUtil.flag(p, "Flood-A", detail);
        e.setCancelled(true);
    }
}
