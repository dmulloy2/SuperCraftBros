package net.dmulloy2.supercraftbros.integration;

import net.dmulloy2.integration.DependencyProvider;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.util.Util;

import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

/**
 * Handles integration with Essentials.
 * <p>
 * All Essentials integration should go through this handler. Everything is
 * wrapped in a catch-all, since Essentials integration is somewhat buggy and
 * isn't necessary for functioning
 *
 * @author dmulloy2
 */

public class EssentialsHandler extends DependencyProvider<Essentials>
{
	public EssentialsHandler(SuperCraftBros plugin)
	{
		super(plugin, "Essentials");
	}

	/**
	 * Disables Essentials god mode
	 *
	 * @param player {@link Player} to disable god mode for
	 */
	public final void disableGodMode(Player player)
	{
		if (! isEnabled())
			return;

		try
		{
			User user = getEssentialsUser(player);
			if (user != null)
				user.setGodModeEnabled(false);
		}
		catch (Throwable ex)
		{
			handler.getLogHandler().debug(Util.getUsefulStack(ex, "disableGodMode(" + player.getName() + ")"));
		}
	}

	/**
	 * Attempts to get a player's Essentials user
	 *
	 * @param player {@link Player} to get Essentials user for
	 */
	public final User getEssentialsUser(Player player)
	{
		if (! isEnabled())
			return null;

		try
		{
			return getDependency().getUser(player);
		}
		catch (Throwable ex)
		{
			handler.getLogHandler().debug(Util.getUsefulStack(ex, "getEssentialsUser(" + player.getName() + ")"));
		}

		return null;
	}
}