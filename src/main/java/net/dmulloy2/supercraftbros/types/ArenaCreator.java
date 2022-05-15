package net.dmulloy2.supercraftbros.types;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.types.LazyLocation;
import net.dmulloy2.util.FormatUtil;

import net.dmulloy2.util.InventoryUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaCreator
{
	private int step;
	
	private final Player player;
	
	private final ArenaData data;

	private final SuperCraftBros plugin;

	//------------------------------------//
	// Constructor
	//------------------------------------//
	public ArenaCreator(SuperCraftBros plugin, Player player, String name)
	{
		this.plugin = plugin;
		this.player = player;
		
		this.data = plugin.getArenaDataHandler().newData(name);
		
		data.setName(name);

		this.generateSteps();
		
		start();
	}
	
	private void start()
	{
		InventoryUtil.giveItem(player, new ItemStack(Constants.WAND_TYPE));

		sendMessage("&bYou have started the creation of the arena &e{0}&b!", data.getName());
		sendMessage("&bSet two points for the arena using your &eGold Axe");
		
		this.step = 0;
	}

	//------------------------------------//
	// Point setting
	//------------------------------------//

	public void setPoint(Location location)
	{
		Consumer<LazyLocation> stepFunc = steps.get(step);
		stepFunc.accept(new LazyLocation(location));

		if (++step == steps.size())
		{
			finish();
		}
	}

	public void undo()
	{
		step--;
	}

	private final List<Consumer<LazyLocation>> steps = new ArrayList<>();

	private void generateSteps()
	{
		steps.add(this::setMinLocation);
		steps.add(this::setMaxLocation);
		steps.add(this::setMinLobbyLocation);
		steps.add(this::setMaxLobbyLocation);
		steps.add(this::setLobbySpawn);

		for (int i = 0; i < 4; i++)
		{
			steps.add(this::addArenaSpawn);
		}

		for (int i = 0; i < 4; i++)
		{
			steps.add(this::addBoardMin);
			steps.add(this::addBoardMax);
		}
	}

	private void setMinLocation(LazyLocation location)
	{
		data.setMinArenaLocation(location);
		sendMessage("&aFirst arena point set. Select opposite corner.");
	}

	private void setMaxLocation(LazyLocation loc)
	{
		data.setMaxArenaLocation(loc);
		sendMessage("&aLast arena arena set. Select first lobby point.");
	}

	private void setMinLobbyLocation(LazyLocation location)
	{
		data.setMinLobbyLocation(location);
		sendMessage("&aFirst lobby point set. Select opposite corner next.");
	}

	private void setMaxLobbyLocation(LazyLocation location)
	{
		data.setMaxLobbyLocation(location);
		sendMessage("&aLast lobby point set. Set lobby spawn.");
	}

	private void setLobbySpawn(LazyLocation location)
	{
		data.setLobbySpawn(location);
		sendMessage("&aLobby spawn set. Add 4 arena spawns.");
	}

	private void addArenaSpawn(LazyLocation location)
	{
		List<LazyLocation> spawns = data.getSpawns();
		spawns.add(location);
		sendMessage("&aSet spawn #&e{0}", spawns.size(), data.getName());

		if (spawns.size() == 4)
			sendMessage("&aAdd 4 boards by selecting two corner points.");
	}

	private void addBoardMin(LazyLocation loc)
	{
		data.getBoardMinLocations().add(loc);
		sendMessage("&aBoard point set. Set other corner.");
	}

	private void addBoardMax(LazyLocation loc)
	{
		data.getBoardMaxLocations().add(loc);
		sendMessage("&aBoard completed.");

		if (data.getBoardMaxLocations().size() == 4)
		{
			sendMessage("&aBoards completed");
			finish();
		}
		else
		{
			sendMessage("&aSelect one corner of the next board.");
		}
	}

	private void finish()
	{
		plugin.getArenaDataHandler().save();
		sendMessage("&aYou have completed the creation of arena &e{0}&a!", data.getName());
		cleanUp();
	}
	
	public void abandon()
	{
		plugin.getArenaDataHandler().deleteData(data.getName());
		sendMessage("Arena creation cancelled");
		cleanUp();
	}

	private void cleanUp()
	{
		InventoryUtil.remove(player.getInventory(), Constants.WAND_TYPE, (short) -1, 1);
		plugin.removeArenaCreator(this);
	}
	
	//------------------------------------//
	// Getters
	//------------------------------------//
	public final Player getPlayer()
	{
		return player;
	}
	
	public final String getName()
	{
		return player.getName();
	}

	//------------------------------------//
	// Messaging
	//------------------------------------//
	private void sendMessage(String string, Object... objects)
	{
		player.sendMessage(plugin.getPrefix() + FormatUtil.format(string, objects));
	}
}
