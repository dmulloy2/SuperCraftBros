package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaLeaveReason;
import net.dmulloy2.supercraftbros.types.ArenaPlayer;

/**
 * @author dmulloy2
 */

public class CmdLeave extends SuperCraftBrosCommand
{
	public CmdLeave(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "leave";
		this.aliases.add("l");
		this.description = "Leave an arena";
		
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		ArenaPlayer ap = plugin.getArenaPlayer(player);
		if (ap == null)
		{
			err("You are not in an arena!");
			return;
		}
		
		ap.getArena().leaveArena(plugin.getArenaPlayer(player), ArenaLeaveReason.COMMAND);
	}
}