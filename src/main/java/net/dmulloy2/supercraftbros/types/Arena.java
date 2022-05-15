package net.dmulloy2.supercraftbros.types;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.types.LazyLocation;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.ItemUtil;
import net.dmulloy2.util.MaterialUtil;
import net.dmulloy2.util.Util;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Represents a SuperCraftBros arena
 * 
 * @author dmulloy2
 */

@Getter
public class Arena
{
	private static final int MAX_PLAYERS = 4;
	
	public enum Mode
	{
		IDLE, LOBBY, STARTING, INGAME, STOPPING, STOPPED
	}

	private Mode gameMode = Mode.IDLE;

	// Tasks
	private int dropTask;
	private int countdownTask;

	// Fields
	private final ArenaField arenaField;
	private final ArenaField lobbyField;

	// Lists
	private final List<Board> boards;
	private final Map<UUID, ArenaPlayer> active;
	private final Map<UUID, ArenaPlayer> inactive;

	private final String name;
	private final ArenaData data;

	private final SuperCraftBros plugin;

	// ------------------------------------//
	// Constructor
	// ------------------------------------//
	public Arena(SuperCraftBros plugin, ArenaData data)
	{
		this.data = data;
		this.plugin = plugin;

		this.name = data.getName();

		this.arenaField = data.getArena();
		this.lobbyField = data.getLobby();

		this.boards = new ArrayList<>();
		this.active = new ConcurrentHashMap<>();
		this.inactive = new ConcurrentHashMap<>();

		this.gameMode = Mode.LOBBY;

		updateSigns();

		data.setActive(true);
		plugin.addActiveArena(this);
	}

	// ------------------------------------//
	// Add player
	// ------------------------------------//

	public void addPlayer(Player player)
	{
		ArenaPlayer ap = new ArenaPlayer(plugin, player, this, getPlayerCount());

		// Teleport
		teleport(player, data.getLobbySpawn());

		// Save Data
		ap.saveData();

		// Make sure the player is in survival
		player.setGameMode(GameMode.SURVIVAL);

		// Heal up the Player
		player.setFoodLevel(20);
		player.setFireTicks(0);
		player.setHealth(20);

		// Don't allow flight
		player.setAllowFlight(false);
		player.setFlySpeed(0.1F);
		player.setFlying(false);

		// If essentials is found, remove god mode.
		if (plugin.isEssentialsEnabled())
			plugin.getEssentialsHandler().disableGodMode(player);

		ap.clearPotionEffects();

		ap.setActive(true);
		active.put(player.getUniqueId(), ap);

		updateSigns();

		tellPlayers("&a{0} has joined the arena! ({1}/{2})", ap.getName(), active.size(), MAX_PLAYERS);

		if (active.size() == MAX_PLAYERS)
		{
			startCountdown();
		}
	}

	// ------------------------------------//
	// Spawning
	// ------------------------------------//

	public void spawn(ArenaPlayer ap)
	{
		teleport(ap.getPlayer(), getSpawn(ap));

		ap.giveClassItems();
	}

	private LazyLocation getSpawn(ArenaPlayer ap)
	{
		return data.getSpawns().get(ap.getId());
	}

	private void spawnAll()
	{
		plugin.getLogHandler().log("Spawning all players for Arena: {0}", data.getName());

		for (ArenaPlayer ap : active.values())
		{
			spawn(ap);
		}

		setupRandomDrops();
		setupBoards();
		updateSigns();
	}

	// ------------------------------------//
	// Countdown
	// ------------------------------------//

	private int countdown;

	private void startCountdown()
	{
		this.gameMode = Mode.STARTING;

		updateSigns();

		tellPlayers("&6Game starting in 20 seconds!");

		this.countdown = 20;
		this.countdownTask = new CountdownTask().runTaskTimer(plugin, 20L, 20L).getTaskId();

	}

	private void countdown()
	{
		if (countdown == 15 || countdown == 10 || countdown < 6)
		{
			tellPlayers("&6Game starting in {0} seconds!", countdown);
		}
	}

	public class CountdownTask extends BukkitRunnable
	{
		@Override
		public void run()
		{
			countdown--;
			if (countdown <= 0)
			{
				start();
				cancel();
			}
			else
			{
				countdown();
			}
		}
	}

	// ------------------------------------//
	// Starting
	// ------------------------------------//

	private boolean started;

	private void start()
	{
		if (! started)
			return;

		plugin.getLogHandler().log("Starting arena: {0}", data.getName());
		started = true;

		this.gameMode = Mode.INGAME;

		updateSigns();
		spawnAll();
		updateBoards();
		setupRandomDrops();
	}

	// ------------------------------------//
	// Random Item Drops
	// ------------------------------------//

	private void setupRandomDrops()
	{
		if (plugin.getConfig().getBoolean("randomItemDrops.enabled"))
		{
			List<ItemStack> drops = new ArrayList<>();
			for (String i : plugin.getConfig().getStringList("randomItemDrops.items"))
			{
				ItemStack stack = ItemUtil.readItem(i, plugin);
				if (stack != null)
				{
					drops.add(stack);
				}
			}

			this.dropTask = new RandomDropTask(drops).runTaskTimer(plugin, 20L, 120L).getTaskId();
		}
	}

	@AllArgsConstructor
	public class RandomDropTask extends BukkitRunnable
	{
		private List<ItemStack> drops;

		@Override
		public void run()
		{
			if (data.isActive() && gameMode == Mode.INGAME)
			{
				int rand = Util.random(drops.size());
				ItemStack stack = drops.get(rand);
				if (stack != null && stack.getType() != Material.AIR)
				{
					LazyLocation loc = arenaField.getRandomLocation(256);
					loc.getWorld().dropItemNaturally(loc.getLocation(), drops.get(rand));
				}
			}
			else
			{
				cancel();
			}
		}
	}

	// ------------------------------------//
	// Tell players
	// ------------------------------------//

	private void tellPlayers(String string, Object... objects)
	{
		for (ArenaPlayer activePlayer : active.values())
		{
			activePlayer.sendMessage(string, objects);
		}
	}

	// ------------------------------------//
	// Endgame Methods
	// ------------------------------------//

	private boolean stopped;
	
	public void stop()
	{
		if (! stopped)
			return;

		plugin.getLogHandler().log("Stopping arena: {0}", data.getName());
		this.gameMode = Mode.STOPPING;
		this.stopped = true;

		updateSigns();

		for (ArenaPlayer ap : active.values())
		{
			if (ap.isActive())
			{
				leaveArena(ap, ArenaLeaveReason.ENDGAME);
			}
		}

		clearBoards();

		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		scheduler.cancelTask(countdownTask);
		scheduler.cancelTask(dropTask);

		clearEntities();

		this.gameMode = Mode.STOPPED;

		updateSigns();

		data.setActive(false);
		plugin.removeActiveArena(this);
	}

	private void clearEntities()
	{
		for (Entity entity : data.getWorld().getEntities())
		{
			if (entity != null && entity.isValid())
			{
				if (! persistentTypes.contains(entity.getType()))
					entity.remove();
			}
		}
	}

	private static final List<EntityType> persistentTypes = Arrays.asList(
			EntityType.ITEM_FRAME, EntityType.PLAYER, EntityType.VILLAGER
			);

	public void endPlayer(ArenaPlayer ap)
	{
		ap.setLives(0);
		ap.setActive(false);

		ap.clearPotionEffects();
		ap.clearInventory();

		ap.reset();

		teleport(ap.getPlayer(), ap.getSpawnBack());

		updateSigns();
	}

	public void leaveArena(ArenaPlayer ap, ArenaLeaveReason reason)
	{
		if (reason == ArenaLeaveReason.COMMAND)
		{
			ap.sendMessage("&3You have left the arena!");

			endPlayer(ap);

			if (! checkEmpty())
			{
				tellPlayers("&e{0} &3has left the arena! &e{1} &3players remain!", ap.getName(), getPlayerCount());
			}
		}

		if (reason == ArenaLeaveReason.ENDGAME)
		{
			ap.sendMessage("&3Game over!");

			endPlayer(ap);
		}

		if (reason == ArenaLeaveReason.KICK)
		{
			ap.sendMessage("&3You have been kicked from the arena!");

			endPlayer(ap);

			if (! checkEmpty())
			{
				tellPlayers("&e{0} &3has been kicked from the arena! &e{1} &3players remain!", ap.getName(), getPlayerCount());
			}
		}

		if (reason == ArenaLeaveReason.QUIT)
		{
			endPlayer(ap);

			if (! checkEmpty())
			{
				tellPlayers("&e{0} &3has left the arena! &e{1} &3players remain!", ap.getName(), getPlayerCount());
			}
		}

		if (reason == ArenaLeaveReason.LIVES)
		{
			ap.sendMessage("&3You have been eliminated!");

			endPlayer(ap);

			if (! checkEmpty())
			{
				tellPlayers("&e{0} &3has been eliminated! &e{1} &3players remain!", ap.getName(), getPlayerCount());
			}
		}
	}

	private boolean checkEmpty()
	{
		if (getPlayerCount() == 1)
		{
			for (ArenaPlayer ap : active.values())
			{
				String message = plugin.getPrefix() + FormatUtil.format("&e{0} &3won the game at &e{1}", ap.getName(), data.getName());

				endPlayer(ap);
				reward(ap);

				plugin.getServer().broadcastMessage(message);
			}

			return true;
		}

		if (getPlayerCount() == 0)
		{
			String message = plugin.getPrefix() + FormatUtil.format("&3Game at &e{0} &3ended in a tie!", data.getName());
			plugin.getServer().broadcastMessage(message);
			return true;
		}

		return false;
	}

	// ------------------------------------//
	// Player Death
	// ------------------------------------//

	public void onPlayerDeath(ArenaPlayer ap)
	{
		ap.setLives(ap.getLives() -1);

		Player player = ap.getPlayer();
		if (player.getKiller() != null)
		{
			Player entity = player.getKiller();
			if (entity != null)
			{
				String weapon = getWeapon(entity);

				tellPlayers("&a{0} &fkilled &c{1} &fwith {2}", entity.getName(), player.getName(), weapon);
			}
			else
			{
				EntityDamageEvent damageEvent = player.getLastDamageCause();
				if (damageEvent != null)
				{
					DamageCause cause = damageEvent.getCause();
					String dc = FormatUtil.getFriendlyName(cause.toString());

					tellPlayers("&a{0} &fwas killed by &c{1}", player.getName(), dc);
				}
				else
				{
					tellPlayers("&a{0} died under mysterious circumstances", player.getName());
				}
			}
		}

		if (ap.getLives() == 0)
		{
			leaveArena(ap, ArenaLeaveReason.LIVES);
		}
		else
		{
			spawn(ap);
			ap.sendMessage("&bYou have &e{0} &blives left!", ap.getLives());
		}

		updateBoards();
	}

	private String getWeapon(Player player)
	{
		ItemStack inHand = player.getInventory().getItemInMainHand();
		if (inHand.getType() == Material.AIR)
		{
			return "&chis fists";
		}
		else
		{
			String name = MaterialUtil.getName(inHand);
			String article = FormatUtil.getArticle(name);
			return "&f" + article + " &c" + name;
		}
	}

	// ------------------------------------//
	// Reward
	// ------------------------------------//

	private void reward(ArenaPlayer ap)
	{
		if (plugin.getConfig().getBoolean("rewards.enabled"))
		{
			Player player = ap.getPlayer();

			// Money
			if (plugin.isVaultEnabled())
			{
				Economy eco = plugin.getVaultHandler().getEconomy();
				int money = plugin.getConfig().getInt("rewards.money");
				plugin.getVaultHandler().depositPlayer(player, money);
				ap.sendMessage("&a{0} has been added to your balance!", eco.format(money));
			}

			for (String s : plugin.getConfig().getStringList("rewards.items"))
			{
				ItemStack stack = ItemUtil.readItem(s, plugin);
				if (stack != null)
				{
					player.getInventory().addItem(stack);
				}
			}
		}
	}

	// ------------------------------------//
	// Boards
	// ------------------------------------//

	public void updateBoards()
	{
		for (Board board : boards)
		{
			board.update();
		}
	}

	public void clearBoards()
	{
		for (Board board : boards)
		{
			board.clear();
		}

		boards.clear();
	}

	public void setupBoards()
	{
		for (ArenaPlayer ap : active.values())
		{
			boards.add(new Board(this, ap));
		}
	}

	// ------------------------------------//
	// Teleportation
	// ------------------------------------//

	public void teleport(Player player, LazyLocation lazyLocation)
	{
		teleport(player, lazyLocation.getLocation());

	}

	public void teleport(Player player, Location location)
	{
		player.teleport(location.clone().add(0.5D, 1.0D, 0.5D));
	}

	// ------------------------------------//
	// Signs
	// ------------------------------------//

	public void updateSigns()
	{
		plugin.getSignHandler().updateSigns(name);
	}

	// ------------------------------------//
	// Getters
	// ------------------------------------//

	public int getPlayerCount()
	{
		return active.size();
	}
}