package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdCreate extends SuperCraftBrosCommand
{
	public CmdCreate(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "create";
		this.aliases.add("c");
		this.addRequiredArg("name");
		this.description = "Creates an arena";
		this.permission = Permission.CMD_CREATE;
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		if (plugin.isCreatingArena(player))
		{
			err("&cYou are already creating an arena!");
			return;
		}
		
		if (plugin.isInArena(player))
		{
			err("&cYou cannot create an arena while in-game!");
			return;
		}
		
		plugin.addCreator(player, args[0]);
	}
}