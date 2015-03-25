package io.quintus.quintuscore;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.quintus.quintuscore.tasks.BungeePlayerListUpdateTask;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuintusCore extends JavaPlugin implements Listener, PluginMessageListener {

    private Server server;
    private Logger logger;
    private Messenger messenger;
    private BukkitTask playerListTask;

    public String[] bungeePlayers;

    @Override
    public void onEnable() {
        server = getServer();
        logger = getLogger();
        messenger = this.server.getMessenger();

        if (!isBungeeEnabled()) {
            logger.log(Level.WARNING, "Either not Spigot or not configured in BungeeCord mode. Functionality limited.");
            return;
        }

        messenger.registerOutgoingPluginChannel(this, "BungeeCord");
        messenger.registerIncomingPluginChannel(this, "BungeeCord", this);

        playerListTask = new BungeePlayerListUpdateTask(this).runTaskTimer(this, 20L, 20L);

    }

    @Override
    public void onDisable() {
        if (playerListTask != null) {
            playerListTask.cancel();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("PlayerList")) {
            if (!in.readUTF().equals("ALL")) {
                return;
            }
            bungeePlayers = in.readUTF().split(", ");
        }
    }

    public boolean isSpigot() {
        try {
            server.getClass().getMethod("spigot");
            return true;
        } catch (NoSuchMethodException ex) {
            logger.log(Level.WARNING, "Not a Spigot server, QuintusCore has no power here.");
            return false;
        }
    }

    public boolean isBungeeEnabled() {
        return isSpigot() && server.spigot().getConfig().getBoolean("settings.bungeecord");
    }

    public Player getOnlinePlayer() {
        return Iterables.getFirst(server.getOnlinePlayers(), null);
    }

    public List<String> getPlayerNameMatches(String prefix) {
        List<String> matches = new ArrayList<>();

        if (bungeePlayers != null) {
            for (String playerName : bungeePlayers) {
                if (playerName.startsWith(prefix)) {
                    matches.add(playerName);
                }
            }
        } else {
            for (Player player : server.getOnlinePlayers()) {
                if (player.getName().startsWith(prefix)) {
                    matches.add(player.getName());
                }
            }
        }

        Collections.sort(matches);

        return matches;
    }

    public List<String> getPlayerNameMatches(String prefix, String[] exclusions) {
        List<String> matches = getPlayerNameMatches(prefix);
        for (String exclusion : exclusions) {
            if (matches.contains(exclusion)) {
                matches.remove(exclusion);
            }
        }
        return matches;
    }

}
