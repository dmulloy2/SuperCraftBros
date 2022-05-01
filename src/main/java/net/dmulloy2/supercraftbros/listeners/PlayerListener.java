package net.dmulloy2.supercraftbros.listeners;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.SuperCraftBros.ArenaJoinTask;
import net.dmulloy2.supercraftbros.gui.ClassSelectionGUI;
import net.dmulloy2.supercraftbros.types.Arena;
import net.dmulloy2.supercraftbros.types.ArenaPlayer;
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
			ArenaJoinTask task = plugin.getWaiting().get(player.getName());

			task.cancel();

			plugin.getWaiting().remove(player.getName());

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
			event.getPlayer().sendMessage(plugin.getPrefix() + FormatUtil.format("&cCannot teleport while ingame!"));
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

	private final void onPlayerDisconnect(Player player)
	{
		if (plugin.isInArena(player))
		{
			Arena a = plugin.getArena(player);
			a.endPlayer(plugin.getArenaPlayer(player));
		}

		if (plugin.isWaiting(player))
		{
			plugin.getWaiting().get(player.getName()).cancel();
			plugin.getWaiting().remove(player.getName());
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		if (plugin.isInArena(player))
		{
			Arena a = plugin.getArena(player);
			ArenaPlayer ap = plugin.getArenaPlayer(player);
			a.onPlayerDeath(ap);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSignInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.isInArena(player))
		{
			if (event.hasBlock())
			{
				Block block = event.getClickedBlock();
				if (block.getState() instanceof Sign)
				{
					Sign s = (Sign) block.getState();
					if (s.getLine(0).equalsIgnoreCase("[SCB]"))
					{
						if (s.getLine(1).equalsIgnoreCase("Characters"))
						{
							ClassSelectionGUI csGUI = new ClassSelectionGUI(plugin, player);
							plugin.getGuiHandler().open(player, csGUI);
						}
						else if (s.getLine(1).equalsIgnoreCase("Click to join"))
						{
							plugin.joinArena(player, s.getLine(2));
						}
					}
				}
			}
		}
	}

	// ---- Double Jump

	private List<String> justJumped = new ArrayList<String>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();

		if (plugin.isInArena(player))
		{
			player.setAllowFlight(true);
			justJumped.remove(player.getName());

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
			ap.getArena().spawn(ap);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void setFlyOnJump(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();
		String name = player.getName();
		World world = player.getWorld();

		Vector jump = player.getVelocity().multiply(1).setY(0.17D * 2);
		Vector look = player.getLocation().getDirection().multiply(0.5D);

		if (event.isFlying() && player.getGameMode() != GameMode.CREATIVE)
		{
			if (plugin.isInArena(player))
			{
				if (!justJumped.contains(name))
				{
					player.setFlying(false);

					player.setVelocity(jump.add(look));
					player.setAllowFlight(false);

					player.playSound(player.getLocation(), Sound.ENTITY_IRONGOLEM_ATTACK, 10.0F, -10.0F);

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
				if (!justJumped.contains(player.getName()))
				{
					justJumped.add(player.getName());
				}
			}
			else if (justJumped.contains(player.getName()))
			{
				justJumped.remove(player.getName());
				player.setAllowFlight(true);
				player.setFlying(false);
			}
		}
	}
}