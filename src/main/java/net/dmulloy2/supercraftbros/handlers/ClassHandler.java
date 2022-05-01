package net.dmulloy2.supercraftbros.handlers;

import net.dmulloy2.supercraftbros.SuperCraftBros;

/**
 * @author dmulloy2
 */

public class ClassHandler
{
	private final SuperCraftBros plugin;
	public ClassHandler(SuperCraftBros plugin)
	{
		this.plugin = plugin;
	}

	public void generateStockClasses()
	{
		String[] stocks = new String[] { "blaze", "cactus", "creeper", "enderman", "skeleton", "spider", "wither", "zombie" };
		
		for (String stock : stocks)
		{
			plugin.saveResource("classes/" + stock + ".yml", false);
		}
	}
}