package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdDelete extends SuperCraftBrosCommand
{
	public CmdDelete(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "delete";
		this.aliases.add("d");
		this.addRequiredArg("name");
		this.description = "Deletes an arena";
		this.permission = Permission.CMD_DELETE;
		
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		if (! plugin.isValidArena(args[0]))
		{
			err("No arena by that name exists!");
			return;
		}
		
		if (plugin.deleteArena(args[0]))
		{
			sendpMessage("&eSuccessfully deleted arena: {0}", args[0]);
		}
		else
		{
			err("Could not delete arena!");
		}
	}
}