package net.dmulloy2.supercraftbros.commands;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaLeaveReason;
import net.dmulloy2.supercraftbros.types.ArenaPlayer;
import net.dmulloy2.supercraftbros.types.Permission;
import net.dmulloy2.util.Util;

import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class CmdKick extends SuperCraftBrosCommand
{
	public CmdKick(SuperCraftBros plugin)
	{
		super(plugin);
		this.name = "kick";
		this.addRequiredArg("player");
		this.description = "Kick a player from a game";
		this.permission = Permission.CMD_KICK;
		
		this.mustBePlayer = true;
	}
	
	@Override
	public void perform()
	{
		Player target = Util.matchPlayer(args[0]);
		if (player == null)
		{
			err("&cPlayer not found!");
			return;
		}

		ArenaPlayer ap = plugin.getArenaPlayer(target);
		if (ap == null)
		{
			err("&cThis player is not in a game!");
			return;
		}

		ap.getArena().leaveArena(plugin.getArenaPlayer(target), ArenaLeaveReason.KICK);
		
		sendpMessage("&eYou have kicked {0} from the game!", target.getName());
	}
}