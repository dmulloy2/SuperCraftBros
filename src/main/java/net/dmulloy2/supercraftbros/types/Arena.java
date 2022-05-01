package net.dmulloy2.supercraftbros.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
		IDLE, LOBBY, STARTING, INGAME, STOPPING, STOPPED;
	}

	private Mode gameMode = Mode.IDLE;

	// Tasks
	private int dropTask;
	private int countdownTask;

	// Fields
	private ArenaField arenaField;
	private ArenaField lobbyField;

	// Lists
	private List<Board> boards;
	private List<ArenaPlayer> active;
	private List<ArenaPlayer> inactive;

	private String name;
	private ArenaData data;

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
		this.active = new ArrayList<>();
		this.inactive = new ArrayList<>();

		this.gameMode = Mode.LOBBY;

		updateSigns();

		data.setActive(true);
		plugin.addActiveArena(this);
	}

	// ------------------------------------//
	// Add player
	// ------------------------------------//

	public final void addPlayer(Player player)
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
		active.add(ap);

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

	public final void spawn(ArenaPlayer ap)
	{
		teleport(ap.getPlayer(), getSpawn(ap));

		ap.giveClassItems();
	}

	private final LazyLocation getSpawn(ArenaPlayer ap)
	{
		return data.getSpawns().get(ap.getId());
	}

	private final void spawnAll()
	{
		plugin.getLogHandler().log("Spawning all players for Arena: {0}", data.getName());

		for (ArenaPlayer ap : active)
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

	private final void startCountdown()
	{
		this.gameMode = Mode.STARTING;

		updateSigns();

		tellPlayers("&6Game starting in 20 seconds!");

		this.countdown = 20;
		this.countdownTask = new CountdownTask().runTaskTimer(plugin, 20L, 20L).getTaskId();

	}

	private final void countdown()
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

	private final void start()
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

	private final void setupRandomDrops()
	{
		if (plugin.getConfig().getBoolean("randomItemDrops.enabled"))
		{
			List<ItemStack> drops = new ArrayList<ItemStack>();
			for (String i : plugin.getConfig().getStringList("randomItemDrops.items"))
			{
				ItemStack stack = ItemUtil.readItem(i);
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
		private final List<ItemStack> drops;

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
					if (loc != null)
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

	private final void tellPlayers(String string, Object... objects)
	{
		for (ArenaPlayer activePlayer : active)
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

		for (ArenaPlayer ap : active)
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

	private final void clearEntities()
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

	public final void endPlayer(ArenaPlayer ap)
	{
		ap.setLives(0);
		ap.setActive(false);

		ap.clearPotionEffects();
		ap.clearInventory();

		ap.reset();

		teleport(ap.getPlayer(), ap.getSpawnBack());

		updateSigns();
	}

	public final void leaveArena(ArenaPlayer ap, ArenaLeaveReason reason)
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

	private final boolean checkEmpty()
	{
		if (getPlayerCount() == 1)
		{
			for (ArenaPlayer ap : active)
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

	public final void onPlayerDeath(ArenaPlayer ap)
	{
		ap.setLives(ap.getLives() -1);

		Player player = ap.getPlayer();
		if (player.getKiller() != null)
		{
			Entity entity = player.getKiller();
			if (entity instanceof Player)
			{
				Player killer = (Player) entity;
				String weapon = getWeapon(killer);

				tellPlayers("&a{0} &fkilled &c{1} &fwith {2}", killer.getName(), player.getName(), weapon);
			}
			else if (entity instanceof LivingEntity)
			{
				LivingEntity killer = (LivingEntity) entity;
				String name = FormatUtil.getFriendlyName(killer.getType());
				String article = FormatUtil.getArticle(name);

				tellPlayers("&a{0} &fwas killed by {1} &c{2}", player.getName(), article, name);
			}
			else
			{
				DamageCause cause = player.getLastDamageCause().getCause();
				String dc = FormatUtil.getFriendlyName(cause.toString());

				tellPlayers("&a{0} &fwas killed by &c{1}", player.getName(), dc);
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

	private final String getWeapon(Player player)
	{
		ItemStack inHand = player.getInventory().getItemInMainHand();
		if (inHand == null || inHand.getType() == Material.AIR)
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

	private final void reward(ArenaPlayer ap)
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
				ItemStack stack = ItemUtil.readItem(s);
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

	public final void updateBoards()
	{
		for (Board board : boards)
		{
			board.update();
		}
	}

	public final void clearBoards()
	{
		for (Board board : boards)
		{
			board.clear();
		}

		boards.clear();
	}

	public final void setupBoards()
	{
		for (ArenaPlayer ap : active)
		{
			boards.add(new Board(this, ap));
		}
	}

	// ------------------------------------//
	// Teleportation
	// ------------------------------------//

	public final void teleport(Player player, LazyLocation lazyLocation)
	{
		teleport(player, lazyLocation.getLocation());

	}

	public final void teleport(Player player, Location location)
	{
		player.teleport(location.clone().add(0.5D, 1.0D, 0.5D));
	}

	// ------------------------------------//
	// Signs
	// ------------------------------------//

	public final void updateSigns()
	{
		plugin.getSignHandler().updateSigns(name);
	}

	// ------------------------------------//
	// Getters
	// ------------------------------------//

	public final int getPlayerCount()
	{
		return active.size();
	}
}