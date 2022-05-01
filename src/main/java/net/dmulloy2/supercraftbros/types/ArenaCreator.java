package net.dmulloy2.supercraftbros.types;

import java.util.ArrayList;
import java.util.List;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.types.LazyLocation;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class ArenaCreator
{
	private int step;
	
	private Player player;
	
	private ArenaData data;
	
	private List<Location> spawns;
	
	private List<Location> boardMaxLocations;
	private List<Location> boardMinLocations;
	
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
		
		this.spawns = new ArrayList<Location>();
		
		this.boardMaxLocations = new ArrayList<Location>();
		this.boardMinLocations = new ArrayList<Location>();
		
		start();
	}
	
	private void start()
	{
		sendMessage("&bYou have began the creation of the arena &e{0}&b!", data.getName());
		
		sendMessage("&bPlease set two points for the arena. Type &e/scb sp &bwhen done!");
		
		this.step = 1;
	}
	
	//------------------------------------//
	// Steps
	//------------------------------------//
	private void stepUp()
	{
		this.step++;
		
		stepInfo();
	}

	private void stepInfo()
	{
		if (step == 2)
		{
			sendMessage("&ePlease set two points for a lobby!");
		}
		
		if (step == 3)
		{
			sendMessage("&ePlease set the lobby spawnpoint!");
		}
		
		if (step == 4)
		{
			sendMessage("&ePlease set four arena spawnpoints!");
		}
		
		if (step == 5)
		{
			sendMessage("&ePlease set four boards!");
		}
	}
	
	//------------------------------------//
	// Point setting
	//------------------------------------//
	public void setPoint()
	{
		if (! plugin.isWorldEditEnabled())
		{
			sendMessage("&cYou must have WorldEdit installed!");
			return;
		}

		WorldEditPlugin we = plugin.getWorldEditHandler().getDependency();

		if (step == 1)
		{
			Selection sel = we.getSelection(player);
			if (sel == null)
			{
				sendMessage("&cYou must have a valid WorldEdit selection to do this!");
				return;
			}
			
			Location max = sel.getMaximumPoint();
			Location min = sel.getMinimumPoint();
			
			data.setMaxArenaLocation(new LazyLocation(max));
			data.setMinArenaLocation(new LazyLocation(min));
			
			sendMessage("&aArena points set!");
			
			stepUp();
			return;
		}
		
		if (step == 2)
		{
			Selection sel = we.getSelection(player);
			if (sel == null)
			{
				sendMessage("&cYou must have a valid WorldEdit selection to do this!");
				return;
			}
			
			Location max = sel.getMaximumPoint();
			Location min = sel.getMinimumPoint();
			
			data.setMaxLobbyLocation(new LazyLocation(max));
			data.setMinLobbyLocation(new LazyLocation(min));
			
			sendMessage("&aLobby points set!");
			
			stepUp();
			return;
		}
		
		if (step == 3)
		{
			data.setLobbySpawn(new LazyLocation(player));
			
			sendMessage("&aLobby spawn set!");
			
			stepUp();
			return;
		}
		
		if (step == 4)
		{
			spawns.add(player.getLocation());
			
			sendMessage("&aSet spawn &e{0} &ain Arena &e{1}", spawns.size(), data.getName());
			
			if (spawns.size() == 4)
			{
				for (Location spawn : spawns)
				{
					data.getSpawns().add(new LazyLocation(spawn));
				}
				
				sendMessage("&aSpawns set!");
				
				stepUp();
				return;
			}
		}
		
		if (step == 5)
		{
			Selection sel = we.getSelection(player);
			if (sel == null)
			{
				sendMessage("&cYou must have a valid WorldEdit selection to do this!");
				return;
			}
			
			Location max = sel.getMaximumPoint();
			boardMaxLocations.add(max);
			
			Location min = sel.getMinimumPoint();
			boardMinLocations.add(min);
			
			sendMessage("&aSet board &e{0} &ain arena &e{1}", boardMaxLocations.size(), data.getName());
			
			if (boardMaxLocations.size() == 4)
			{
				for (Location boardMax : boardMaxLocations)
				{
					data.getBoardMaxLocations().add(new LazyLocation(boardMax));
				}
				
				for (Location boardMin : boardMinLocations)
				{
					data.getBoardMinLocations().add(new LazyLocation(boardMin));
				}
				
				sendMessage("&aBoard locations set!");
				
				finish();
				return;
			}
		}
	}
	
	private void finish()
	{
		plugin.getArenaDataHandler().save();
		
		plugin.removeArenaCreator(this);
		
		sendMessage("&aYou have completed the creation of arena &e{0}&a!", data.getName());
	}
	
	public void abandon()
	{
		plugin.getArenaDataHandler().deleteData(data.getName());
		
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
	
	public final ArenaData getArenaData()
	{
		return data;
	}
	
	//------------------------------------//
	// Messaging
	//------------------------------------//
	private void sendMessage(String string, Object... objects)
	{
		player.sendMessage(plugin.getPrefix() + FormatUtil.format(string, objects));
	}
}
