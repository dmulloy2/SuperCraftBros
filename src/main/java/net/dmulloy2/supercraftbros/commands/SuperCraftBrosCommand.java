/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.commands.Command;
import net.dmulloy2.supercraftbros.SuperCraftBros;

/**
 * @author dmulloy2
 */

public abstract class SuperCraftBrosCommand extends Command
{
	protected final SuperCraftBros plugin;
	public SuperCraftBrosCommand(SuperCraftBros plugin)
	{
		super(plugin);
		this.plugin = plugin;
		this.usesPrefix = true;
	}
}