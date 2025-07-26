package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

public class PositionA implements Check {

    @Override
    public String name() {
        return "Position-A";
    }

    @Override
    public void handle(PacketReceiveEvent e) {
        if (e.getPacketType() != PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            return;
        }

        WrapperPlayClientPlayerPositionAndRotation posRot = new WrapperPlayClientPlayerPositionAndRotation(e);
        Location loc = posRot.getLocation();

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        Player player = e.getPlayer();

        if (Math.abs(x) > 30_000_000 || Math.abs(y) > 30_000_000 || Math.abs(z) > 30_000_000) {
            TaskUtil.flag(player, name(), "invalid_coords: x=" + x + ", y=" + y + ", z=" + z);
            e.setCancelled(true);
            return;
        }

        if (yaw < -360 || yaw > 360 || pitch < -90 || pitch > 90) {
            TaskUtil.flag(player, name(), "yaw/pitch: yaw=" + yaw + ", pitch=" + pitch);
            e.setCancelled(true);
        }
    }
}
