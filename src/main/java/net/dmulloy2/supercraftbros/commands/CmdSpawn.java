package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaData;
import net.dmulloy2.supercraftbros.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdSpawn extends SuperCraftBrosCommand
{
	public CmdSpawn(SuperCraftBros plugin) 
	{
		super(plugin);
		this.name = "spawn";
		this.aliases.add("spawnpoint");
		this.addRequiredArg("name");
		this.description = "Teleport to the spawn point of an arena";
		this.permission = Permission.CMD_SPAWN;
		
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
		
		ArenaData data = plugin.getArenaDataHandler().getData(args[0]);
		player.teleport(data.getSpawns().get(0).getLocation());
	}
}