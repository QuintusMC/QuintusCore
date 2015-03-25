package io.quintus;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.quintus.tasks.BungeePlayerListUpdateTask;
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

/**
 * Created by akrill on 3/24/15.
 */
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

        /* Check if we're a Spigot server, since non-Spigot can't do Bungee */
        try {
            server.getClass().getMethod("spigot");
        } catch (NoSuchMethodException ex) {
            logger.log(Level.WARNING, "Not a Spigot server, QuintusCore has no power here.");
            return;
        }

        if (!server.spigot().getConfig().getBoolean("settings.bungeecord")) {
            logger.log(Level.WARNING, "Spigot not configured in BungeeCord mode, QuintusCore has no power here.");
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
            if (in.readUTF() != "ALL") {
                return;
            }
            bungeePlayers = in.readUTF().split(", ");
        }
    }

    public Player getOnlinePlayer() {
        return Iterables.getFirst(server.getOnlinePlayers(), null);
    }

    public List<String> getPlayerNameMatches(String prefix) {
        List<String> matches = new ArrayList<String>();

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
