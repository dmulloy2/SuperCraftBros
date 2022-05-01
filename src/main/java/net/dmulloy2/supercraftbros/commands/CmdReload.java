package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.Permission;
import net.dmulloy2.types.Reloadable;

/**
 * @author dmulloy2
 */

public class CmdReload extends SuperCraftBrosCommand implements Reloadable
{
	public CmdReload(SuperCraftBros plugin) 
	{
		super(plugin);
		this.name = "reload";
		this.aliases.add("rl");
		this.description = "Reloads the configuration";
		this.permission = Permission.CMD_RELOAD;
	}

	@Override
	public void perform()
	{
		reload();
	}

	@Override
	public void reload()
	{
		plugin.reload();

		sendpMessage("&aSuperCraftBros has been reloaded!");
	}
}