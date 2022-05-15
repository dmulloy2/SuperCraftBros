package net.dmulloy2.supercraftbros.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.Arena;
import net.dmulloy2.supercraftbros.types.ArenaData;

/**
 * @author dmulloy2
 */

public class CmdList extends SuperCraftBrosCommand
{
	public CmdList(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "list";
		this.aliases.add("ls");
		this.description = "List all available games";
		
		this.mustBePlayer = false;
	}
	
	@Override
	public void perform()
	{
		List<String> lines = new ArrayList<>();
		
		StringBuilder line = new StringBuilder();
		line.append("&3====[ &eAvailable Arenas &3]====");
		lines.add(line.toString());
		
		for (Entry<String, ArenaData> data : plugin.getArenaDataHandler().getAllArenaData().entrySet())
		{
			line = new StringBuilder();
			line.append("&b[&e").append(data.getValue().getName()).append(" &eArena&b]    ");
			
			if (data.getValue().isActive())
			{
				Arena a = plugin.getArena(data.getValue().getName());
				switch (a.getGameMode())
				{
					case IDLE -> line.append("&e[IDLE]");
					case INGAME -> {
						line.append("&a[INGAME | ");
						line.append(a.getPlayerCount()).append("]");
					}
					case LOBBY -> {
						line.append("&a[LOBBY | ");
						line.append(a.getPlayerCount()).append("]");
					}
					case STARTING -> line.append("&a[STARTING]");
					case STOPPED -> line.append("&e[STOPPED]");
					case STOPPING -> line.append("&e[STOPPING]");
					default -> {}
				}
			}
			else
			{
				line.append("&e[IDLE]");
			}
			
			lines.add(line.toString());
		}
		
		for (String s : lines)
		{
			sendMessage(s);
		}
	}
}