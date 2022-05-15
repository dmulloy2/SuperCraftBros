/**
 * SuperCraftBros - a bukkit plugin
 * Copyright (C) 2013 - 2014 dmulloy2
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.dmulloy2.supercraftbros;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import net.dmulloy2.SwornAPI;
import net.dmulloy2.SwornPlugin;
import net.dmulloy2.commands.CmdHelp;
import net.dmulloy2.gui.GUIHandler;
import net.dmulloy2.handlers.CommandHandler;
import net.dmulloy2.handlers.LogHandler;
import net.dmulloy2.handlers.PermissionHandler;
import net.dmulloy2.handlers.ResourceHandler;
import net.dmulloy2.integration.VaultHandler;
import net.dmulloy2.supercraftbros.commands.*;
import net.dmulloy2.supercraftbros.handlers.ArenaDataHandler;
import net.dmulloy2.supercraftbros.handlers.ClassHandler;
import net.dmulloy2.supercraftbros.handlers.SignHandler;
import net.dmulloy2.supercraftbros.integration.EssentialsHandler;
import net.dmulloy2.supercraftbros.listeners.BlockListener;
import net.dmulloy2.supercraftbros.listeners.EntityListener;
import net.dmulloy2.supercraftbros.listeners.PlayerListener;
import net.dmulloy2.supercraftbros.types.Arena;
import net.dmulloy2.supercraftbros.types.Arena.Mode;
import net.dmulloy2.supercraftbros.types.ArenaClass;
import net.dmulloy2.supercraftbros.types.ArenaCreator;
import net.dmulloy2.supercraftbros.types.ArenaData;
import net.dmulloy2.supercraftbros.types.ArenaPlayer;
import net.dmulloy2.supercraftbros.types.ArenaSign;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author dmulloy2
 */

public class SuperCraftBros extends SwornPlugin
{
	private @Getter EssentialsHandler essentialsHandler;
	private @Getter ArenaDataHandler arenaDataHandler;
	private @Getter ResourceHandler resourceHandler;
	private @Getter ClassHandler classHandler;
	private @Getter VaultHandler vaultHandler;
	private @Getter SignHandler signHandler;
	private @Getter GUIHandler guiHandler;

	private @Getter Map<UUID, ArenaJoinTask> waiting;

	private Map<String, Arena> activeArenas;
	private @Getter List<ArenaSign> signs;
	private @Getter List<ArenaClass> classes;
	private @Getter Map<UUID, ArenaCreator> creators;

	private final @Getter String prefix = FormatUtil.format("&4[&6&lSCB&4]&r ");

	@Override
	public void onLoad()
	{
		SwornAPI.checkRegistrations();
	}

	@Override
	public void onEnable()
	{
		long start = System.currentTimeMillis();
 
		checkDirectories();

		saveDefaultConfig();
		reloadConfig();

		logHandler = new LogHandler(this);

		File messages = new File(getDataFolder(), "messages.properties");
		if (messages.exists())
			messages.delete();

		resourceHandler = new ResourceHandler(this, getClassLoader());

		commandHandler = new CommandHandler(this);
		arenaDataHandler = new ArenaDataHandler(this);
		permissionHandler = new PermissionHandler(this);

		waiting = new HashMap<>();
		signs = new ArrayList<>();
		classes = new ArrayList<>();
		creators = new ConcurrentHashMap<>();
		activeArenas = new ConcurrentHashMap<>();

		setupIntegration();

		classHandler = new ClassHandler(this);
		signHandler = new SignHandler(this);

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BlockListener(this), this);
		pm.registerEvents(new EntityListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);

		commandHandler.setCommandPrefix("scb");
		commandHandler.registerPrefixedCommand(new CmdAbandon(this));
		commandHandler.registerPrefixedCommand(new CmdCreate(this));
		commandHandler.registerPrefixedCommand(new CmdDelete(this));
		commandHandler.registerPrefixedCommand(new CmdHelp(this));
		commandHandler.registerPrefixedCommand(new CmdJoin(this));
		commandHandler.registerPrefixedCommand(new CmdKick(this));
		commandHandler.registerPrefixedCommand(new CmdLeave(this));
		commandHandler.registerPrefixedCommand(new CmdList(this));
		commandHandler.registerPrefixedCommand(new CmdReload(this));
		commandHandler.registerPrefixedCommand(new CmdSetPoint(this));
		commandHandler.registerPrefixedCommand(new CmdSpawn(this));
		commandHandler.registerPrefixedCommand(new CmdUndo(this));

		loadClasses();

		guiHandler = new GUIHandler(this);

		logHandler.log(getMessage("log_enabled"), getDescription().getFullName(), System.currentTimeMillis() - start);
	}

	@Override
	public void onDisable()
	{
		long start = System.currentTimeMillis();

		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);

		Collection<Arena> arenas = activeArenas.values();
		arenas.forEach(Arena::stop);

		arenaDataHandler.save();
		signHandler.onDisable();

		logHandler.log(getMessage("log_disabled"), getDescription().getFullName(), System.currentTimeMillis() - start);
	}

	/**
	 * Check and create directories
	 */
	private void checkDirectories()
	{
		File dataFolder = getDataFolder();
		if (! dataFolder.exists())
		{
			dataFolder.mkdir();
		}

		File arenaFolder = new File(dataFolder, "arenas");
		if (! arenaFolder.exists())
		{
			arenaFolder.mkdir();
		}

		File classFile = new File(dataFolder, "classes");
		if (! classFile.exists())
		{
			classFile.mkdir();
		}
	}

	private void setupIntegration()
	{
		try
		{
			essentialsHandler = new EssentialsHandler(this);
		} catch (Throwable ignored) { }

		try
		{
			vaultHandler = new VaultHandler(this);
		} catch (Throwable ignored) { }
	}

	public boolean isEssentialsEnabled()
	{
		return essentialsHandler != null && essentialsHandler.isEnabled();
	}

	public boolean isVaultEnabled()
	{
		return vaultHandler != null && vaultHandler.isEnabled();
	}

	public String getMessage(String key)
	{
		return resourceHandler.getMessage(key);
	}

	public void addActiveArena(Arena arena)
	{
		activeArenas.put(arena.getName(), arena);
	}

	public void removeActiveArena(Arena arena)
	{
		activeArenas.remove(arena.getName());
	}

	public Arena getArena(String name)
	{
		return activeArenas.get(name);
	}

	public ArenaPlayer getArenaPlayer(Player player)
	{
		UUID playerId = player.getUniqueId();
		for (Arena a : activeArenas.values())
		{
			ArenaPlayer ap = a.getActive().get(playerId);
			if (ap != null)
				return ap;
		}

		return null;
	}

	public boolean isInArena(Player player)
	{
		return getArenaPlayer(player) != null;
	}

	public boolean isValidArena(String name)
	{
		return arenaDataHandler.getData(name) != null;
	}

	public void addCreator(Player player, String name)
	{
		creators.put(player.getUniqueId(), new ArenaCreator(this, player, name));
	}

	public ArenaCreator getArenaCreator(Player player)
	{
		return creators.get(player.getUniqueId());
	}

	public boolean isCreatingArena(Player player)
	{
		return getArenaCreator(player) != null;
	}

	public void removeArenaCreator(ArenaCreator ac)
	{
		creators.remove(ac.getPlayer().getUniqueId());
	}

	public boolean deleteArena(String name)
	{
		return arenaDataHandler.deleteData(name);
	}

	private void loadClasses()
	{
		File folder = new File(getDataFolder(), "classes");
		File[] children = folder.listFiles();
		if (children == null || children.length == 0)
		{
			classHandler.generateStockClasses();
		}

		children = folder.listFiles();

		for (File file : children)
		{
			ArenaClass ac = new ArenaClass(this, file);
			classes.add(ac);
		}
	}

	public ArenaClass getClass(ItemStack stack)
	{
		for (ArenaClass ac : classes)
		{
			if (ac.getIcon().equals(stack))
				return ac;
		}

		return null;
	}

	public boolean isInArena(Location loc)
	{
		for (ArenaData data : arenaDataHandler.getAllArenaData().values())
		{
			if (data.isInside(loc))
				return true;
		}

		return false;
	}

	public void joinArena(Player player, String name)
	{
		if (isInArena(player))
		{
			player.sendMessage(prefix + FormatUtil.format("&cYou are already in an arena!"));
			return;
		}

		if (isCreatingArena(player))
		{
			player.sendMessage(prefix + FormatUtil.format("&cYou cannot join a game while creating an arena!"));
			return;
		}

		Arena a = getArena(name);
		if (a != null)
		{
			if (a.getGameMode() == Mode.LOBBY)
			{
				a.addPlayer(player);
			}
			else
			{
				player.sendMessage(prefix + FormatUtil.format("&cThis arena has already started!"));
			}
		}
		else
		{
			if (isValidArena(name))
			{
				a = new Arena(this, arenaDataHandler.getData(name));

				if (getConfig().getBoolean("joinTimer.enabled"))
				{
					int seconds = getConfig().getInt("joinTimer.seconds");
					player.sendMessage(prefix + FormatUtil.format("&6Please wait for {0} seconds!", seconds));

					new ArenaJoinTask(player, a).runTaskLater(this, seconds * 20L);
				}
				else
				{
					new ArenaJoinTask(player, a).run();
				}
			}
			else
			{
				player.sendMessage(prefix + FormatUtil.format("&cNo arena by that name exists!"));
			}
		}
	}

	public boolean isWaiting(Player player)
	{
		return waiting.containsKey(player.getUniqueId());
	}

	public class ArenaJoinTask extends BukkitRunnable
	{
		private final UUID playerId;
		private final Arena arena;

		public ArenaJoinTask(final Player player, final Arena arena)
		{
			this.playerId = player.getUniqueId();
			this.arena = arena;

			waiting.put(player.getUniqueId(), this);
		}

		@Override
		public void run()
		{
			Player player = Bukkit.getPlayer(playerId);
			if (player != null)
				arena.addPlayer(player);
			waiting.remove(playerId	);
		}
	}

	@Override
	public void reload()
	{
		reloadConfig();
	}
}