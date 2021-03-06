package com.untoldadventures;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ImprovedJail extends JavaPlugin implements Listener
{
	public String Version = "0.2";

	public static File pluginFolder;
	public static File configFile;
	public static FileConfiguration jailConfig;
	public static Permission perms = null;
	public static Economy econ = null;
	public static Chat chat = null;
	private static final Logger log = Logger.getLogger("Minecraft");

	@Override
	public void onEnable()
	{
		this.getLogger().info("Improved Jail|Version " + this.Version + " |Enabling");
		this.getCommand("ij").setExecutor(new ImprovedJailCommandExecutor(this));
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getPluginManager().addPermission(new Permission("ij.jail"));
		this.getServer().getPluginManager().addPermission(new Permission("ij.unjail"));
		this.getServer().getPluginManager().addPermission(new Permission("ij.setjail"));
		this.getServer().getPluginManager().addPermission(new Permission("ij.pardon"));
		// Vault Dependency
		if (ImprovedJail.jailConfig.getBoolean("economy.", true))
		{
			if (!setupEconomy())
			{
				log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}

		setupPermissions();
		setupChat();

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				for (Player target : getServer().getOnlinePlayers())
				{
					if (!jailConfig.getBoolean(target.getName() + ".jailed"))
						continue;

					int jailLength = jailConfig.getInt(target.getName() + ".jailLength");
					int timeElapsed = jailConfig.getInt(target.getName() + ".timeElapsed");

					if (timeElapsed >= jailLength)
					{
						Server server = target.getServer();
						ImprovedJail.jailConfig.set(target.getName() + ".jailed", false);
						target.sendMessage(ChatColor.GOLD + "[Jail]" + ChatColor.RED + " You have been released from jail.");
						int returnX = Integer.parseInt(ImprovedJail.jailConfig.getString(target.getName() + ".jail.return.X"));
						int returnY = Integer.parseInt(ImprovedJail.jailConfig.getString(target.getName() + ".jail.return.Y"));
						int returnZ = Integer.parseInt(ImprovedJail.jailConfig.getString(target.getName() + ".jail.return.Z"));
						World returnW = server.getWorld(ImprovedJail.jailConfig.getString(target.getName() + ".jail.return.W"));
						Location returnPos = new Location(returnW, returnX, returnY, returnZ);
						target.teleport(returnPos);

						ImprovedJail.jailConfig.set(target.getName() + ".jailLength", 0);
						ImprovedJail.jailConfig.set(target.getName() + ".timeElapsed", 0);

						continue;
					}

					timeElapsed++;
					jailConfig.set(target.getName() + ".timeElapsed", timeElapsed);

				}
			}
		}, 1200L, 1200L);

		pluginFolder = getDataFolder();
		configFile = new File(pluginFolder, "jails.yml");
		jailConfig = new YamlConfiguration();
		if (!pluginFolder.exists())
		{
			try
			{
				pluginFolder.mkdir();
			} catch (Exception ex)
			{
			}
		}
		if (!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
			} catch (Exception ex)
			{
			}
		}
		try
		{
			jailConfig.load(configFile);
			ImprovedJail.jailConfig.set("economy.", false);
			ImprovedJail.jailConfig.set("economy.rate", 1000);
		} catch (Exception ex)
		{
		}
	}
	@Override
	public void onDisable()
	{
		this.getLogger().info("Improved Jail|Version " + this.Version + "|Disabling");
		this.saveConfig();
	}

	public void saveConfig()
	{
		try
		{
			jailConfig.save(configFile);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (jailConfig.getBoolean(player.getName() + ".jailed"))
			event.setCancelled(true);

	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (jailConfig.getBoolean(player.getName() + ".jailed"))
			event.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		Server server = player.getServer();
		if (jailConfig.getBoolean(player.getName() + ".jailed"))
		{
			int x = ImprovedJail.jailConfig.getInt("jail.location.X");
			int y = ImprovedJail.jailConfig.getInt("jail.location.Y");
			int z = ImprovedJail.jailConfig.getInt("jail.location.Z");
			World w = server.getWorld(jailConfig.getString("jail.location.W"));
			Location jail = new Location(w, x, y, z);
			event.setRespawnLocation(jail);
		}
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		Player player = event.getPlayer();
		if (jailConfig.getBoolean(player.getName() + ".jailed"))
		{
			event.setCancelled(true);
		}

	}

	// Vault
	private boolean setupEconomy()
	{
		if (getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
		{
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	private boolean setupChat()
	{
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
		chat = rsp.getProvider();
		return chat != null;
	}

	private boolean setupPermissions()
	{
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

}
