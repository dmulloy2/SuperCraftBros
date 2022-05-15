package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaCreator;
import net.dmulloy2.supercraftbros.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdSetPoint extends SuperCraftBrosCommand
{
	public CmdSetPoint(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "setpoint";
		this.aliases.add("sp");
		this.description = "Sets a point in an arena";
		this.permission = Permission.CMD_CREATE;
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		ArenaCreator ac = plugin.getArenaCreator(player);
		if (ac == null)
		{
			err("You must be creating an arena to do this!");
			return;
		}

		ac.setPoint(player.getLocation());
	}
}
