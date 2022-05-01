/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.supercraftbros.gui;

import java.util.List;

import net.dmulloy2.gui.AbstractGUI;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaClass;
import net.dmulloy2.supercraftbros.types.ArenaPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * @author dmulloy2
 */

public class ClassSelectionGUI extends AbstractGUI
{
	private final SuperCraftBros plugin;
	public ClassSelectionGUI(SuperCraftBros plugin, Player player)
	{
		super(plugin, player);
		this.plugin = plugin;
		this.setup();
	}

	private final List<ArenaClass> getClasses()
	{
		return plugin.getClasses();
	}

	@Override
	public int getSize()
	{
		int size = getClasses().size();
		if (size <= 9)
			return 9;
		else if (size <= 18)
			return 18;
		else if (size <= 27)
			return 27;
		else if (size <= 36)
			return 36;
		else
			return 54;
	}

	@Override
	public String getTitle()
	{
		return "Choose your character!";
	}

	@Override
	public void stock(Inventory inventory)
	{
		for (ArenaClass clazz : getClasses())
		{
			inventory.addItem(clazz.getIcon());
		}
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (plugin.isInArena(player))
		{
			ArenaClass ac = plugin.getClass(event.getCurrentItem());
			ArenaPlayer ap = plugin.getArenaPlayer(player);
			ap.setClass(ac);
		}

		event.setCancelled(true);
		player.closeInventory();
	}
}