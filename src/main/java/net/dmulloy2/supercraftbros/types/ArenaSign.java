package net.dmulloy2.supercraftbros.types;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.types.LazyLocation;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * @author dmulloy2
 */

@Getter
public final class ArenaSign implements ConfigurationSerializable
{
	private final int id;
	private final String arenaName;
	private final LazyLocation location;

	private transient Sign sign;
	private transient ArenaData data;
	private transient final SuperCraftBros plugin;

	public ArenaSign(SuperCraftBros plugin, Location location, ArenaData data, int id)
	{
		this.id = id;
		this.arenaName = data.getName();
		this.location = new LazyLocation(location);

		this.getSign();
		this.data = data;
		this.plugin = plugin;
	}

	public ArenaSign(SuperCraftBros plugin, Map<String, Object> args)
	{
		this.id = (int) args.get("id");
		this.arenaName = (String) args.get("arenaName");
		this.location = (LazyLocation) args.get("loc");

		this.getSign();
		this.data = plugin.getArenaDataHandler().getData(arenaName);
		this.plugin = plugin;
	}

	/**
	 * Updates the Sign
	 */
	public void update()
	{
		// Update the sign
		this.getSign();

		// Abort if the sign is null
		if (sign == null)
		{
			plugin.getSignHandler().deleteSign(this);
			return;
		}

		// Abort if the ArenaZone is null
		if (data == null)
		{
			sign.setLine(0, "[SuperCraftBros]");
			sign.setLine(1, FormatUtil.format("&4Null Arena"));
			sign.update();

			plugin.getSignHandler().deleteSign(this);
			return;
		}

		sign.setLine(0, "[SCB]");
		sign.setLine(1, data.getName());

		// Line 3
		StringBuilder line = new StringBuilder();
		if (isActive())
		{
			Arena ar = getArena();
			switch (ar.getGameMode())
			{
				case LOBBY:
					line.append(FormatUtil.format("&aJoin - {0}", ar.getCountdown()));
					break;
				case INGAME:
					line.append(FormatUtil.format("&eIn Game"));
					break;
				case IDLE:
					line.append(FormatUtil.format("&aJoin"));
					break;
				case STOPPING:
					line.append(FormatUtil.format("&eStopping"));
					break;
				default:
					break;
			}
		}
		else
		{
			line.append(FormatUtil.format("&aJoin"));
		}

		sign.setLine(2, line.toString());

		// Line 4
		line = new StringBuilder();
		if (isActive())
		{
			Arena ar = getArena();

			switch (ar.getGameMode())
			{
				case IDLE:
					line.append("IDLE (0/4)");
					break;
				case INGAME:
					line.append("INGAME (");
					line.append(ar.getPlayerCount());
					line.append("/4)");
					break;
				case LOBBY:
					line.append("LOBBY (");
					line.append(ar.getPlayerCount());
					line.append("/4)");
					break;
				case STOPPING:
					line.append("STOPPING (0/4)");
					break;
				default:
					break;
			}
		}
		else
		{
			line.append("IDLE (0/4)");
		}

		sign.setLine(3, line.toString());
		sign.update();
	}

	private final void getSign()
	{
		Block block = location.getLocation().getBlock();
		if (block.getState() instanceof Sign)
		{
			this.sign = (Sign) block.getState();
			return;
		}

		this.sign = null;
	}

	/**
	 * Clears the sign
	 */
	public final void clear()
	{
		// Update the sign
		this.getSign();

		// Abort if the sign is null
		if (sign == null)
		{
			plugin.getSignHandler().deleteSign(this);
			return;
		}

		// Abort if the ArenaZone is null
		if (data == null)
		{
			sign.setLine(0, "[SCB]");
			sign.setLine(1, FormatUtil.format("&4Null Arena"));
			sign.update();

			plugin.getSignHandler().deleteSign(this);
			return;
		}

		sign.setLine(0, "[SCB]");
		sign.setLine(1, data.getName());
		sign.setLine(2, FormatUtil.format("&aJoin"));
		sign.setLine(3, "IDLE (0/4)");
		sign.update();
	}

	private final boolean isActive()
	{
		return getArena() != null;
	}

	private final Arena getArena()
	{
		return plugin.getArena(arenaName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return "ArenaSign { id = " + id + ", name = " + arenaName + ", location = " + location + " }";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new LinkedHashMap<>();

		data.put("id", id);
		data.put("arenaName", arenaName);
		data.put("location", location);

		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		int hash = 301;
		hash *= 1 + id;
		hash *= 1 + arenaName.hashCode();
		hash *= 1 + location.hashCode();
		return hash;
	}
}