package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import dev.astra.guard.Main;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public final class NettyC implements Check {

    private final int MAX_BURST;
    private final int SOFT_BYTES;
    private final int HARD_BYTES;
    private final int MAX_BEES;
    private final int MAX_STRIKES;

    private final int MAX_BOOK_PAGES;
    private final int MAX_CHARS_PER_PAGE;
    private final int MAX_MAP_SIZE;
    private final int MAX_TOTAL_NBT_BYTES;

    private final LoadingCache<UUID, LongAdder> bursts;
    private final LoadingCache<UUID, LongAdder> strikes;

    public NettyC() {
        var cfg = Main.getInstance().getConfigManager();
        MAX_BURST = cfg.getInt("netty.windowClickGuard.maxBurst", 20);
        SOFT_BYTES = cfg.getInt("netty.windowClickGuard.nbtSoft", 65536);
        HARD_BYTES = cfg.getInt("netty.windowClickGuard.nbtHard", 262144);
        MAX_BEES = cfg.getInt("netty.windowClickGuard.maxBees", 30);
        MAX_STRIKES = cfg.getInt("netty.windowClickGuard.maxStrikes", 5);
        MAX_BOOK_PAGES = cfg.getInt("netty.windowClickGuard.book.maxPages", 20);
        MAX_CHARS_PER_PAGE = cfg.getInt("netty.windowClickGuard.book.maxCharsPerPage", 1024);
        MAX_MAP_SIZE = cfg.getInt("netty.windowClickGuard.book.maxMapSize", 50);
        MAX_TOTAL_NBT_BYTES = cfg.getInt("netty.windowClickGuard.book.maxTotalNbtBytes", 200000);

        bursts = CacheBuilder.newBuilder()
                .expireAfterWrite(50, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public LongAdder load(UUID key) {
                        return new LongAdder();
                    }
                });
        strikes = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public LongAdder load(UUID key) {
                        return new LongAdder();
                    }
                });
    }

    @Override
    public String name() {
        return "Netty-C";
    }

    @Override
    public void handle(PacketReceiveEvent ev) {
        if (ev.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) return;
        Player player = ev.getPlayer();
        if (player == null) return;
        UUID uid = player.getUniqueId();

        LongAdder burstCounter = bursts.getUnchecked(uid);
        burstCounter.increment();
        if (burstCounter.intValue() > MAX_BURST) {
            flag(player, ev, "burst>" + MAX_BURST);
            return;
        }

        WrapperPlayClientClickWindow pkt = new WrapperPlayClientClickWindow(ev);
        var stack = pkt.getCarriedItemStack();
        if (stack == null) return;

        NBTCompound tag = stack.getNBT();
        if (tag == null) return;

        NBTList<NBTCompound> pages = tag.getCompoundListTagOrNull("pages");
        int pageCount = (pages != null) ? pages.getTags().size() : 0;
        if (pageCount > MAX_BOOK_PAGES) {
            flag(player, ev, "pages=" + pageCount);
            return;
        }

        int totalBytes = 0;
        for (NBTCompound page : pages.getTags()) {
            String text = page.toString();
            int charLen = text.length();
            if (charLen > MAX_CHARS_PER_PAGE) {
                flag(player, ev, "chars=" + charLen);
                return;
            }
            totalBytes += text.getBytes(StandardCharsets.UTF_8).length;
        }

        Map<Integer, ?> slotMap = pkt.getSlots().orElse(Collections.emptyMap());
        if (slotMap.size() > MAX_MAP_SIZE) {
            flag(player, ev, "map=" + slotMap.size());
            return;
        }

        if (totalBytes > MAX_TOTAL_NBT_BYTES) {
            flag(player, ev, "nbtBytes=" + totalBytes);
            return;
        }

        boolean beeBomb = false;
        NBTCompound block = tag.getCompoundTagOrNull("BlockEntityTag");
        if (block != null) {
            NBTList<NBTCompound> bees = block.getCompoundListTagOrNull("Bees");
            beeBomb = bees != null && bees.getTags().size() > MAX_BEES;
        }

        if (totalBytes >= HARD_BYTES || beeBomb) {
            flag(player, ev, "nbt=" + totalBytes + " bees=" + beeBomb);
            return;
        }

        if (totalBytes >= SOFT_BYTES) {
            LongAdder strikeCounter = strikes.getUnchecked(uid);
            strikeCounter.increment();
            int s = strikeCounter.intValue();
            flag(player, ev, "nbt=" + totalBytes + " [" + s + "/" + MAX_STRIKES + "]");
        }
    }

    private void flag(Player p, PacketReceiveEvent e, String detail) {
        TaskUtil.flag(p, name(), detail);
        e.setCancelled(true);
    }
}
