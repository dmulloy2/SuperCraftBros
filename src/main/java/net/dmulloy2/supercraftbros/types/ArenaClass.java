package net.dmulloy2.supercraftbros.types;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.util.ItemUtil;
import net.dmulloy2.util.Util;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ArenaClass
{
	private String name;
	private String extension = ".yml";

	private List<ItemStack> armor;
	private List<ItemStack> weapons;

	private ItemStack icon;

	private final SuperCraftBros plugin;

	// ------------------------------------//
	// Constructor
	// ------------------------------------//
	public ArenaClass(SuperCraftBros plugin, File file)
	{
		this.plugin = plugin;
		this.armor = new ArrayList<>();
		this.weapons = new ArrayList<>();
		this.load(file);
	}

	// ------------------------------------//
	// Load
	// ------------------------------------//
	private final void load(File file)
	{
		try
		{
			YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);

			// ---- Weapons
			if (fc.isSet("weapons"))
			{
				Map<String, Object> values = fc.getConfigurationSection("weapons").getValues(false);
				for (String key : values.keySet())
				{
					String s = fc.getString("weapons." + key);
					if (s.startsWith("head:"))
					{
						s = s.substring(s.indexOf(":"));
						HeadType type = HeadType.toHeadType(s);
						if (type != null)
						{
							ItemStack stack = type.toItemStack();
							if (stack != null)
								weapons.add(stack);
						}
					}
					else
					{
						try
						{
							ItemStack stack = ItemUtil.readItem(s);
							if (stack != null)
								weapons.add(stack);
						}
						catch (Throwable ex)
						{
							plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "parsing item \"" + s + "\""));
						}
					}
				}
			}

			// ---- Armor
			if (fc.isSet("armor.boots"))
			{
				String s = fc.getString("armor.boots");

				try
				{
					ItemStack stack = ItemUtil.readItem(s);
					if (stack != null)
						armor.add(stack);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "parsing item \"" + s + "\""));
				}
			}

			if (fc.isSet("armor.leggings"))
			{
				String s = fc.getString("armor.leggings");

				try
				{
					ItemStack stack = ItemUtil.readItem(s);
					if (stack != null)
						armor.add(stack);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "parsing item \"" + s + "\""));
				}
			}

			if (fc.isSet("armor.chestplate"))
			{
				String s = fc.getString("armor.chestplate");

				try
				{
					ItemStack stack = ItemUtil.readItem(s);
					if (stack != null)
						armor.add(stack);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "parsing item \"" + s + "\""));
				}
			}

			if (fc.isSet("armor.helmet"))
			{
				String s = fc.getString("armor.helmet");
				if (s.startsWith("head:"))
				{
					s = s.substring(s.indexOf(":") + 1);
					HeadType type = HeadType.toHeadType(s);
					if (type != null)
					{
						ItemStack stack = type.toItemStack();
						if (stack != null)
						{
							armor.add(stack);
						}
					}
				}
				else
				{
					ItemStack stack = ItemUtil.readItem(s);
					if (stack != null)
					{
						armor.add(stack);
					}
				}
			}

			// ---- Name
			String name = file.getName().replaceAll(extension, "");
			this.name = name;

			// ---- Icon
			ItemStack icon = null;
			String s = fc.getString("icon");
			if (s.startsWith("head:"))
			{
				s = s.substring(s.indexOf(":") + 1);
				HeadType type = HeadType.toHeadType(s);
				if (type != null)
				{
					icon = type.toItemStack();
				}
			}
			else
			{
				try
				{
					icon = ItemUtil.readItem(s);
				}
				catch (Throwable ex)
				{
					plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "parsing item \"" + s + "\""));
				}
			}

			if (icon != null)
			{
				ItemMeta meta = icon.getItemMeta();

				meta.setDisplayName(WordUtils.capitalize(name));
				meta.setLore(fc.getStringList("description"));

				icon.setItemMeta(meta);
			}

			this.icon = icon;

			// ---- Color
			if (fc.isSet("color"))
			{
				DyeColor dc = DyeColor.valueOf(fc.getString("color"));
				if (dc != null)
				{
					Color color = dc.getColor();
					for (ItemStack stack : armor)
					{
						ItemMeta itemMeta = stack.getItemMeta();
						if (itemMeta instanceof LeatherArmorMeta)
						{
							LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemMeta;
							armorMeta.setColor(color);
							stack.setItemMeta(armorMeta);
						}
					}
				}
			}
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "loading class " + name));
		}
	}

	// ------------------------------------//
	// Getters
	// ------------------------------------//
	public final String getName()
	{
		return name;
	}

	public final List<ItemStack> getArmor()
	{
		return armor;
	}

	public final List<ItemStack> getWeapons()
	{
		return weapons;
	}

	public final ItemStack getIcon()
	{
		return icon;
	}
}