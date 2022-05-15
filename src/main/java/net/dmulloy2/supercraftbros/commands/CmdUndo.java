package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaCreator;
import net.dmulloy2.supercraftbros.types.Permission;

public class CmdUndo extends SuperCraftBrosCommand
{
	public CmdUndo(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "undo";
		this.description = "Undoes last creation step";
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

		ac.undo();
		sendpMessage("Last step undone.");
	}
}
