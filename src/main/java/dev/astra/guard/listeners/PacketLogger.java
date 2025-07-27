package dev.astra.guard.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class PacketLogger extends PacketListenerAbstract {

    private final Plugin plugin;

    private final AtomicReference<Double> currentTps = new AtomicReference<>(20.0);
    private final AtomicLong packetSizeThisSecond = new AtomicLong(0);
    private final AtomicBoolean loggingActive = new AtomicBoolean(false);

    private static final double TPS_THRESHOLD = 18.0;
    private static final long HEAVY_PACKET_THRESHOLD = 5_000_000L;
    private static final int TICK_INTERVAL = 20;

    public PacketLogger(Plugin plugin) {
        this.plugin = plugin;
        startTpsAndTrafficMonitor();
    }

    public void register() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }

    private void startTpsAndTrafficMonitor() {
        new BukkitRunnable() {
            int tickCount = 0;
            long lastNano = System.nanoTime();

            @Override
            public void run() {
                tickCount++;
                if (tickCount >= TICK_INTERVAL) {
                    long now = System.nanoTime();
                    double elapsed = (now - lastNano) / 1_000_000_000.0;
                    double tps = Math.min(20.0, TICK_INTERVAL / elapsed * 20);
                    currentTps.set(tps);
                    lastNano = now;
                    tickCount = 0;

                    long totalTraffic = packetSizeThisSecond.getAndSet(0);
                    boolean overload = tps < TPS_THRESHOLD && totalTraffic > HEAVY_PACKET_THRESHOLD;

                    if (overload && !loggingActive.get()) {
                        loggingActive.set(true);
                        Bukkit.getLogger().warning("[AstraGuard] ⚠ Overload detected (TPS: " + String.format("%.2f", tps) + ", Traffic: " + totalTraffic + "B/s). Starting packet logging...");
                    } else if (!overload && loggingActive.get()) {
                        loggingActive.set(false);
                        Bukkit.getLogger().info("[AstraGuard] ✅ Server recovered (TPS: " + String.format("%.2f", tps) + "). Stopped packet logging.");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void countPacketSize(ByteBuf buf) {
        int size = buf.readableBytes();
        packetSizeThisSecond.addAndGet(size);
    }

    private void logPacket(String direction, String playerName, String packetType, int size) {
        System.out.println("[AstraGuard][Overload][" + direction + "] " + playerName + ": " + packetType + " (" + size + " bytes)");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        ByteBuf buf = (ByteBuf) event.getByteBuf();
        int size = buf.readableBytes();
        countPacketSize(buf);

        if (loggingActive.get()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Player player = event.getPlayer();
                logPacket("RECEIVE", player.getName(), event.getPacketType().toString(), size);
            });
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        ByteBuf buf = (ByteBuf) event.getByteBuf();
        int size = buf.readableBytes();
        countPacketSize(buf);

        if (loggingActive.get()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Player player = event.getPlayer();
                logPacket("SEND", player.getName(), event.getPacketType().toString(), size);
            });
        }
    }
}
