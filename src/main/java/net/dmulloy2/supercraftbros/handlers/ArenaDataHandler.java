package net.dmulloy2.supercraftbros.handlers;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.dmulloy2.io.FileSerialization;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.ArenaData;
import net.dmulloy2.supercraftbros.types.ArenaField;
import net.dmulloy2.util.Util;

public class ArenaDataHandler
{
	private final File folder;
	private final String extension = ".dat";
	private final String folderName = "arenas";

	private final HashMap<String, ArenaData> data;

	private final SuperCraftBros plugin;

	public ArenaDataHandler(final SuperCraftBros plugin)
	{
		this.folder = new File(plugin.getDataFolder(), folderName);
		if (! folder.exists())
			folder.mkdir();

		this.data = new HashMap<>();
		this.plugin = plugin;
		this.loadAllData();
	}

	public ArenaData newData(String key)
	{
		ArenaData value = new ArenaData();
		data.put(key, value);
		return value;
	}

	public ArenaData getData(String key)
	{
		ArenaData value = data.get(key);
		if (value == null)
		{
			File file = new File(folder, getFileName(key));
			if (file.exists())
			{
				value = loadData(key);
				data.put(key, value);
			}
		}

		return value;
	}

	public ArenaData loadData(String key)
	{
		try
		{
			File file = new File(folder, getFileName(key));
	
			ArenaData data = FileSerialization.load(file, ArenaData.class);
			ArenaField lobby = new ArenaField(data.getMaxLobbyLocation(), data.getMinLobbyLocation());
			data.setLobby(lobby);
	
			ArenaField arena = new ArenaField(data.getMaxArenaLocation(), data.getMinArenaLocation());
			data.setArena(arena);
			return data;
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "loading arena {0}", key));
			return null;
		}
	}

	private String getFileName(String key)
	{
		return key + extension;
	}

	public Map<String, ArenaData> getAllLoadedData()
	{
		return Collections.unmodifiableMap(data);
	}

	public void save()
	{
		plugin.getLogHandler().log("Saving {0} to disk...", folderName);
		long start = System.currentTimeMillis();
		for (Entry<String, ArenaData> entry : getAllLoadedData().entrySet())
		{
			try
			{
				File file = new File(folder, getFileName(entry.getKey()));
				FileSerialization.save(entry.getValue(), file);
			}
			catch (Throwable ex)
			{
				plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "saving arena {0}", entry.getKey()));
			}
		}

		plugin.getLogHandler().log("{0} saved! [{1} ms]", folderName, (System.currentTimeMillis() - start));
	}

	public boolean deleteData(String key)
	{
		ArenaData data = getData(key);
		if (data != null)
		{
			this.data.remove(data.getName());

			File file = new File(folder, getFileName(data.getName()));
			return file.delete();
		}

		return false;
	}

	public Map<String, ArenaData> getAllArenaData()
	{
		Map<String, ArenaData> data = new HashMap<>(this.data);
		File[] files = folder.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.getName().contains(extension))
				{
					String fileName = file.getName().replaceAll(extension, "");
					if (! isFileAlreadyLoaded(fileName, data))
						data.put(fileName, loadData(fileName));
				}
			}
		}

		return Collections.unmodifiableMap(data);
	}

	public void loadAllData()
	{
		long start = System.currentTimeMillis();

		plugin.getLogHandler().log("Loading {0} from disk...", folderName);

		File[] files = folder.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.getName().contains(extension))
				{
					String fileName = file.getName().replaceAll(extension, "");
					if (! isFileAlreadyLoaded(fileName, data))
						data.put(fileName, loadData(fileName));
				}
			}
		}

		plugin.getLogHandler().log("{0} loaded! [{1}ms]", folderName, System.currentTimeMillis() - start);
	}

	private boolean isFileAlreadyLoaded(final String fileName, final Map<String, ArenaData> map)
	{
		for (String key : map.keySet())
		{
			if (key.equals(fileName))
				return true;
		}

		return false;
	}
}