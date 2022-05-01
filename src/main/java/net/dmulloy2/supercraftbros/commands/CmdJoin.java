package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;

public class CmdJoin extends SuperCraftBrosCommand
{
	public CmdJoin(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "join";
		this.aliases.add("j");
		this.addRequiredArg("game");
		this.description = "Join an arena";
		
		this.mustBePlayer = true;
	}

	@Override
	public void perform()
	{
		plugin.joinArena(player, args[0]);
	}
}