package net.dmulloy2.supercraftbros.types;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.dmulloy2.types.LazyLocation;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * @author dmulloy2
 */

@Data
public class ArenaData implements ConfigurationSerializable
{
	private String name;

	private LazyLocation maxArenaLocation;
	private LazyLocation minArenaLocation;

	private LazyLocation maxLobbyLocation;
	private LazyLocation minLobbyLocation;

	private List<LazyLocation> spawns = new ArrayList<>();

	private List<LazyLocation> boardMaxLocations = new ArrayList<>();
	private List<LazyLocation> boardMinLocations = new ArrayList<>();

	private LazyLocation lobbySpawn;

	private transient ArenaField lobby;
	private transient ArenaField arena;

	private transient boolean active;

	@Setter(AccessLevel.NONE)
	private Map<String, Object> data = new HashMap<>();

	public ArenaData() { }

	public ArenaData(Map<String, Object> args)
	{
		for (Entry<String, Object> entry : args.entrySet())
		{
			try
			{
				for (Field field : getClass().getDeclaredFields())
				{
					if (field.getName().equals(entry.getKey()))
					{
						boolean accessible = field.isAccessible();
						if (! accessible)
							field.setAccessible(true);

						field.set(this, entry.getValue());

						if (! accessible)
							field.setAccessible(false);
					}
				}
			} catch (Throwable ignored) { }
		}
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new HashMap<>();

		for (Field field : getClass().getDeclaredFields())
		{
			if (Modifier.isTransient(field.getModifiers()))
				continue;

			try
			{
				boolean accessible = field.isAccessible();
				field.setAccessible(true);

				if (field.getType().equals(Integer.TYPE))
				{
					if (field.getInt(this) != 0)
						data.put(field.getName(), field.getInt(this));
				}
				else if (field.getType().equals(Long.TYPE))
				{
					if (field.getLong(this) != 0)
						data.put(field.getName(), field.getLong(this));
				}
				else if (field.getType().equals(Boolean.TYPE))
				{
					if (field.getBoolean(this))
						data.put(field.getName(), field.getBoolean(this));
				}
				else if (field.getType().isAssignableFrom(Collection.class))
				{
					if (! ((Collection<?>) field.get(this)).isEmpty())
						data.put(field.getName(), field.get(this));
				}
				else if (field.getType().isAssignableFrom(String.class))
				{
					if (((String) field.get(this)) != null)
						data.put(field.getName(), field.get(this));
				}
				else if (field.getType().isAssignableFrom(Map.class))
				{
					if (! ((Map<?, ?>) field.get(this)).isEmpty())
						data.put(field.getName(), field.get(this));
				}
				else
				{
					if (field.get(this) != null)
						data.put(field.getName(), field.get(this));
				}

				field.setAccessible(accessible);
			} catch (Throwable ignored) { }
		}

		return data;
	}

	public final World getWorld()
	{
		return maxArenaLocation.getWorld();
	}

	public final boolean isInside(Location loc)
	{
		return arena.isInside(loc) || lobby.isInside(loc);
	}
}