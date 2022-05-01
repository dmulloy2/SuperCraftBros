package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
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
		this.permission = Permission.CMD_SET_POINT;
		
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		plugin.getArenaCreator(player).setPoint();
	}
}