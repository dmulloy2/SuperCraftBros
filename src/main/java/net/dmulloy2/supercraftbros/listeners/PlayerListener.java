package net.dmulloy2.supercraftbros.listeners;

import java.util.*;

import lombok.AllArgsConstructor;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.SuperCraftBros.ArenaJoinTask;
import net.dmulloy2.supercraftbros.gui.ClassSelectionGUI;
import net.dmulloy2.supercraftbros.types.Arena;
import net.dmulloy2.supercraftbros.types.ArenaCreator;
import net.dmulloy2.supercraftbros.types.ArenaPlayer;
import net.dmulloy2.supercraftbros.types.Constants;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @author dmulloy2
 */

public class PlayerListener implements Listener
{
	private final SuperCraftBros plugin;

	public PlayerListener(SuperCraftBros plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isWaiting(player))
		{
			ArenaJoinTask task = plugin.getWaiting().get(player.getUniqueId());

			task.cancel();

			plugin.getWaiting().remove(player.getUniqueId());

			player.sendMessage(plugin.getPrefix() + FormatUtil.format("&cCancelled!"));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if (!plugin.isInArena(event.getPlayer()))
			return;

		if (event.getCause() == TeleportCause.COMMAND)
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(plugin.getPrefix() + FormatUtil.format("&cCannot teleport while in-game!"));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event)
	{
		if (! event.isCancelled())
		{
			onPlayerDisconnect(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		onPlayerDisconnect(event.getPlayer());
	}

	private void onPlayerDisconnect(Player player)
	{
		ArenaPlayer ap = plugin.getArenaPlayer(player);
		if (ap != null)
		{
			Arena arena = ap.getArena();
			arena.endPlayer(ap);
		}

		if (plugin.isWaiting(player))
		{
			plugin.getWaiting().get(player.getUniqueId()).cancel();
			plugin.getWaiting().remove(player.getUniqueId());
		}

		ArenaCreator ac = plugin.getArenaCreator(player);
		if (ac != null)
		{
			ac.abandon();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		ArenaPlayer ap = plugin.getArenaPlayer(event.getEntity());
		if (ap != null)
			ap.getArena().onPlayerDeath(ap);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();
		if (block == null) return;

		Player player = event.getPlayer();
		if (block.getState() instanceof Sign sign)
		{
			if ("[SCB]".equalsIgnoreCase(sign.getLine(0)))
			{
				String line1 = sign.getLine(1);
				if ("Characters".equalsIgnoreCase(line1))
				{
					if (plugin.isInArena(player))
					{
						ClassSelectionGUI csGUI = new ClassSelectionGUI(plugin, player);
						plugin.getGuiHandler().open(player, csGUI);
					}
				}
				else if ("Click to join".equalsIgnoreCase(line1))
				{
					String arena = sign.getLine(2);
					plugin.joinArena(player, arena);
				}
			}
		}
		else
		{
			ArenaCreator creator = plugin.getArenaCreator(player);
			if (creator != null)
			{
				ItemStack clickedWith = event.getItem();
				if (clickedWith != null && clickedWith.getType() == Constants.WAND_TYPE)
				{
					creator.setPoint(block.getLocation());
				}
			}
		}
	}

	// ---- Double Jump

	private final Set<UUID> justJumped = new HashSet<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();

		if (plugin.isInArena(player))
		{
			player.setAllowFlight(true);
			justJumped.remove(player.getUniqueId());

			new RespawnTask(player).runTaskLater(plugin, 20L);
		}
	}

	@AllArgsConstructor
	public class RespawnTask extends BukkitRunnable
	{
		private final Player player;

		@Override
		public void run()
		{
			ArenaPlayer ap = plugin.getArenaPlayer(player);
			if (ap != null)
				ap.getArena().spawn(ap);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void setFlyOnJump(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();
		World world = player.getWorld();

		Vector jump = player.getVelocity().multiply(1).setY(0.17D * 2);
		Vector look = player.getLocation().getDirection().multiply(0.5D);

		if (event.isFlying() && player.getGameMode() != GameMode.CREATIVE)
		{
			if (plugin.isInArena(player))
			{
				if (!justJumped.contains(player.getUniqueId()))
				{
					player.setFlying(false);

					player.setVelocity(jump.add(look));
					player.setAllowFlight(false);

					player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 10.0F, -10.0F);

					for (int i = 0; i <= 10; i++)
						world.playEffect(player.getLocation(), Effect.SMOKE, i);
				}
				else
				{
					player.setFlying(false);
					player.setAllowFlight(false);
				}

				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove2(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		Block block = loc.add(0.0D, -1.0D, 0.0D).getBlock();

		if (plugin.isInArena(player))
		{
			if (block.getType() == Material.AIR)
			{
				justJumped.add(player.getUniqueId());
			}
			else if (justJumped.contains(player.getUniqueId()))
			{
				justJumped.remove(player.getUniqueId());
				player.setAllowFlight(true);
				player.setFlying(false);
			}
		}
	}
}
