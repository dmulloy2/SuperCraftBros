package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdAbandon extends SuperCraftBrosCommand
{
	public CmdAbandon(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "abandon";
		this.description = "Abandons the creation of an arena";
		this.permission = Permission.CMD_ABANDON;
		
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		if (! plugin.isCreatingArena(player))
		{
			err("&cYou are not creating an arena!");
			return;
		}
		
		plugin.getArenaCreator(player).abandon();
		
		sendpMessage("&cYou have stopped creating the arena!");
	}
}