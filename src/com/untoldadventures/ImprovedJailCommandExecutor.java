package com.untoldadventures;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ImprovedJailCommandExecutor implements CommandExecutor
{

	ImprovedJail plugin;

	public ImprovedJailCommandExecutor(ImprovedJail plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		Player player = (Player) sender;
		Server server = player.getServer();
		Player target;

		if (args.length == 0)
		{
			return false;
		}

		if (args.length == 4)
		{
			if (args[0].equalsIgnoreCase("jail"))
			{
				if (player.hasPermission("ij.jail"))
				{
					target = (Bukkit.getPlayer(args[1]));

					if (target != null)
					{
						int x = ImprovedJail.jailConfig.getInt("jail.location.X");
						int y = ImprovedJail.jailConfig.getInt("jail.location.Y");
						int z = ImprovedJail.jailConfig.getInt("jail.location.Z");
						if (x == 0)
						{
							if (y == 0)
							{
								if (z == 0)
								{
									sender.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + "No jail is set!");
									return true;
								}
							}
						}
						
						int returnX = target.getLocation().getBlockX();
						int returnY = target.getLocation().getBlockY();
						int returnZ = target.getLocation().getBlockZ();
						String returnW = target.getLocation().getWorld().getName();
						ImprovedJail.jailConfig.set(target.getName() + ".jail.return.X", returnX);
						ImprovedJail.jailConfig.set(target.getName() + ".jail.return.Y", returnY);
						ImprovedJail.jailConfig.set(target.getName() + ".jail.return.Z", returnZ);
						ImprovedJail.jailConfig.set(target.getName() + ".jail.return.W", returnW);
						World w = server.getWorld(ImprovedJail.jailConfig.getString("jail.location.W"));
						Location jail = new Location(w, x, y, z);
						target.teleport(jail);
						sender.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + args[1] + " has been sentenced to jail for " + args[3] + " minutes.");
						target.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + "You have been jailed by " + sender.getName() + " for " + args[3] + ". Sentenced for " + args[2]);
						ImprovedJail.jailConfig.set(target.getName() + ".jailed", true);
						int jailLength = Integer.parseInt(args[2]);
						ImprovedJail.jailConfig.set(target.getName() + ".jailLength", jailLength);
						target.setGameMode(GameMode.SURVIVAL);
						plugin.saveConfig();
						return true;

					}

				}

				if (args.length != 4)
				{
					return false;
				}

			}

			return false;

		}

		if (args[0].equalsIgnoreCase("setjail"))
		{

			if (player.hasPermission("ij.setjail"))
			{

				int x = player.getLocation().getBlockX();
				int y = player.getLocation().getBlockY();
				int z = player.getLocation().getBlockZ();
				String w = player.getLocation().getWorld().getName();
				ImprovedJail.jailConfig.set("jail.location.X", x);
				ImprovedJail.jailConfig.set("jail.location.Y", y);
				ImprovedJail.jailConfig.set("jail.location.Z", z);
				ImprovedJail.jailConfig.set("jail.location.W", w);

				player.sendMessage(ChatColor.GOLD + "[Jail]" + ChatColor.RED + " Jail Set!");

				return true;

			}

			return false;

		}

		if (args[0].equalsIgnoreCase("unjail"))
		{
			if (player.hasPermission("ij.unjail"))
			{
				if (args.length == 2)
				{
					target = (Bukkit.getPlayer(args[1]));

					if (ImprovedJail.jailConfig.getBoolean(target.getName() + ".jailed") == false)
					{
						sender.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + target.getName() + " is not in jail!");
						return true;
					} else
					{
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

						
						plugin.saveConfig();
						return true;
					}
				}
				if (args.length != 2)
				{
					return false;
				}
			}
			return false;
		}

		if (args[0].equalsIgnoreCase("check"))
		{
			if (args.length == 2)
			{
				target = (Bukkit.getPlayer(args[1]));

				if (ImprovedJail.jailConfig.getBoolean(target.getName() + ".jailed") == false)
				{
					sender.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + target.getName() + " is not in jail!");
					return true;
				}

				int jailLength = ImprovedJail.jailConfig.getInt(target.getName() + ".jailLength");
				int timeElapsed = ImprovedJail.jailConfig.getInt(target.getName() + ".timeElapsed");

				player.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + "Time left in sentence: " + (jailLength - timeElapsed) + " minutes.");

				return true;

			}
			if (args.length == 1)
			{
				target = player;

				if (ImprovedJail.jailConfig.getBoolean(player.getName() + ".jailed") == false)
				{
					sender.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + "Your not in jail!");
					return true;
				}

				int jailLength = ImprovedJail.jailConfig.getInt(player.getName() + ".jailLength");
				int timeElapsed = ImprovedJail.jailConfig.getInt(player.getName() + ".timeElapsed");

				player.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + "Time left in sentence: " + (jailLength - timeElapsed) + " minutes.");

				return true;
			}
		}
		if (args[0].equalsIgnoreCase("pardon"))
		{
			if (player.hasPermission("ij.pardon"))
			{
				if (args.length == 3)
				{
					target = (Bukkit.getPlayer(args[1]));
					if (ImprovedJail.jailConfig.getBoolean(target.getName() + ".jailed") == false)
					{
						sender.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + target.getName() + " is not in jail!");
						return true;
					}

					int pardonTime = Integer.parseInt(args[2]);
					int jailLength = ImprovedJail.jailConfig.getInt(target.getName() + ".jailLength");
					int timeElapsed = ImprovedJail.jailConfig.getInt(target.getName() + ".timeElapsed");
					int remainingTime = ImprovedJail.jailConfig.getInt(target.getName() + ".jailLength") - ImprovedJail.jailConfig.getInt(target.getName() + ".timeElapsed") - Integer.parseInt(args[2]);
					if (timeElapsed - pardonTime >= jailLength)
					{
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

						
						plugin.saveConfig();
					}

					ImprovedJail.jailConfig.set(target.getName() + ".timeElapsed", (jailLength - remainingTime));
					target.sendMessage(ChatColor.GOLD + "[Jail]" + ChatColor.RED + " You have been pardoned for " + pardonTime + " minutes." + " Your have " + remainingTime + " minutes left on your sentence.");
					sender.sendMessage(ChatColor.GOLD + "[Jail] " + ChatColor.RED + target.getName() + " has been pardoned for " + pardonTime + " minutes. They have " + remainingTime + " minutes left on their sentence.");

					return true;

				}
			}

		}
		return false;
	}
}
