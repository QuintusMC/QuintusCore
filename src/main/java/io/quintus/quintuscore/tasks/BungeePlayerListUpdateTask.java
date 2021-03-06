package io.quintus.quintuscore.tasks;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.quintus.quintuscore.QuintusCore;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BungeePlayerListUpdateTask extends BukkitRunnable {

    private final QuintusCore plugin;

    public BungeePlayerListUpdateTask(QuintusCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Player sender = plugin.getOnlinePlayer();
        if (sender == null) {
            return;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerList");
        out.writeUTF("ALL");
        sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
