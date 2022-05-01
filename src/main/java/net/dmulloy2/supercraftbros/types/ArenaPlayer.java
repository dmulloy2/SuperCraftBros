package net.dmulloy2.supercraftbros.types;

import lombok.Getter;
import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.Arena.Mode;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

/**
 * @author dmulloy2
 */

@Getter
public class ArenaPlayer
{
	private int id;
	private int lives = 5;

	private boolean active;

	private PlayerData playerData;
	private ArenaClass arenaClass;

	private final String name;
	private final Arena arena;
	private final Player player;
	private final Location spawnBack;
	private final SuperCraftBros plugin;

	// ------------------------------------//
	// Constructor
	// ------------------------------------//

	public ArenaPlayer(SuperCraftBros plugin, Player player, Arena arena, int id)
	{
		this.plugin = plugin;
		this.player = player;
		this.arena = arena;
		this.name = player.getName();
		this.spawnBack = player.getLocation().clone();
		this.id = id;
	}

	// ------------------------------------//
	// Inventory
	// ------------------------------------//

	public final void clearInventory()
	{
		PlayerInventory inventory = player.getInventory();
		inventory.setHelmet(null);
		inventory.setChestplate(null);
		inventory.setLeggings(null);
		inventory.setBoots(null);
		inventory.clear();
	}

	// ------------------------------------//
	// Potions
	// ------------------------------------//

	public final void clearPotionEffects()
	{
		for (PotionEffect effect : player.getActivePotionEffects())
		{
			player.removePotionEffect(effect.getType());
		}
	}

	// ------------------------------------//
	// Setters
	// ------------------------------------//

	public final void setLives(int lives)
	{
		this.lives = lives;
	}

	public final void setActive(boolean active)
	{
		this.active = active;
	}

	// ------------------------------------//
	// Messaging
	// ------------------------------------//

	public final void sendMessage(String string, Object... objects)
	{
		player.sendMessage(plugin.getPrefix() + FormatUtil.format(string, objects));
	}

	// ------------------------------------//
	// Classes
	// ------------------------------------//

	public final void setClass(ArenaClass arenaClass)
	{
		this.arenaClass = arenaClass;

		sendMessage("&aYou will spawn as a(n): {0}", arenaClass.getName());
		giveClassItems();
	}

	public final void giveClassItems()
	{
		if (arena.getGameMode() == Mode.INGAME)
		{
			if (arenaClass == null)
			{
				this.arenaClass = plugin.getClasses().get(0);
			}

			player.getInventory().setContents(arenaClass.getWeapons().toArray(new ItemStack[0]));
			player.getInventory().setArmorContents(arenaClass.getArmor().toArray(new ItemStack[0]));
		}
	}

	public void openClassInventory()
	{
		// TODO
	}

	// ------------------------------------//
	// Classes
	// ------------------------------------//

	public final void saveData()
	{
		this.playerData = new PlayerData(player);
	}

	public final void reset()
	{
		playerData.apply();
	}
}