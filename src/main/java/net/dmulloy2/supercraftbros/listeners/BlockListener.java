package net.dmulloy2.supercraftbros.listeners;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaData;
import net.dmulloy2.supercraftbros.types.ArenaSign;
import net.dmulloy2.supercraftbros.types.Permission;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

/**
 * @author dmulloy2
 */

public class BlockListener implements Listener
{
	private final SuperCraftBros plugin;
	public BlockListener(SuperCraftBros plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) 
	{
		if (event.isCancelled())
			return;
		
		Player player = event.getPlayer();
		if (plugin.isInArena(player))
		{
			player.sendMessage(plugin.getPrefix() +
					FormatUtil.format("&cYou cannot break this!"));

			event.setCancelled(true);
			return;
		}
		
		if (plugin.isInArena(player.getLocation()))
		{
			if (! plugin.getPermissionHandler().hasPermission(player, Permission.BUILD))
			{
				player.sendMessage(plugin.getPrefix() +
						FormatUtil.format("&cYou cannot break this!"));

				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) 
	{
		if (event.isCancelled())
			return;
		
		Player player = event.getPlayer();
		if (plugin.isInArena(player))
		{
			player.sendMessage(plugin.getPrefix() +
					FormatUtil.format("&cYou cannot place this!"));

			event.setCancelled(true);
			return;
		}
		
		if (plugin.isInArena(player.getLocation()))
		{
			if (! plugin.getPermissionHandler().hasPermission(player, Permission.BUILD))
			{
				player.sendMessage(plugin.getPrefix() +
						FormatUtil.format("&cYou cannot place this!"));

				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSignChange(SignChangeEvent event)
	{
		if (event.getLine(0).equalsIgnoreCase("[SCB]"))
		{
			if (plugin.getPermissionHandler().hasPermission(event.getPlayer(), Permission.BUILD))
			{
				if (event.getLine(1).equalsIgnoreCase("Click to join"))
				{
					if (plugin.isValidArena(event.getLine(2)))
					{
						Location location = event.getBlock().getLocation();
						ArenaData data = plugin.getArenaDataHandler().getData(event.getLine(2));
						ArenaSign sign = new ArenaSign(plugin, location, data, plugin.getSignHandler().getFreeId(0));
						plugin.getSignHandler().addSign(sign);
					}
					else
					{
						event.setLine(0, FormatUtil.format("[SCB]"));
						event.setLine(1, FormatUtil.format("&4Invalid Arena"));
						event.setLine(2, "");
						event.setLine(3, "");
					}
				}
			}
			else
			{
				event.setLine(0, FormatUtil.format("[SCB]"));
				event.setLine(1, FormatUtil.format("&4No permission"));
				event.setLine(2, "");
				event.setLine(3, "");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (block.getState() instanceof Sign)
		{
			Sign s = (Sign)block.getState();
			if (s.getLine(0).equalsIgnoreCase("[SCB]"))
			{
				ArenaSign sign = plugin.getSignHandler().getSign(block.getLocation());
				if (sign != null)
				{
					if (plugin.getPermissionHandler().hasPermission(player, Permission.BUILD))
					{
						plugin.getSignHandler().deleteSign(sign);
						player.sendMessage(plugin.getPrefix() +
								FormatUtil.format("&eDeleted sign!"));
						
					}
					else
					{
						event.setCancelled(true);
						player.sendMessage(plugin.getPrefix() +
								FormatUtil.format("&cPermission denied!"));
					}
				}
			}
		}
	}
}