package net.dmulloy2.supercraftbros.types;

import lombok.Getter;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Stores data on players
 *
 * @author dmulloy2
 */

@Getter
public final class PlayerData
{
	private final boolean allowFlight;
	private final float exhaustion;
	private final float exp;
	private final int fireTicks;
	private final float flySpeed;
	private final boolean flying;
	private final int foodLevel;
	private final GameMode gameMode;
	private final double health;
	private final ItemStack[] armorContents;
	private final ItemStack[] contents;
	private final int level;
	private final float saturation;
	private final int totalExperience;

	private final Player player;

	public PlayerData(Player player)
	{
		this.allowFlight = player.getAllowFlight();
		this.exhaustion = player.getExhaustion();
		this.exp = player.getExp();
		this.fireTicks = player.getFireTicks();
		this.flySpeed = player.getFlySpeed();
		this.flying = player.isFlying();
		this.foodLevel = player.getFoodLevel();
		this.gameMode = player.getGameMode();
		this.health = player.getHealth();
		this.armorContents = player.getInventory().getArmorContents();
		this.contents = player.getInventory().getContents();
		this.level = player.getLevel();
		this.saturation = player.getSaturation();
		this.totalExperience = player.getTotalExperience();
		this.player = player;
	}

	public void apply()
	{
		player.setAllowFlight(allowFlight);
		player.setExhaustion(exhaustion);
		player.setExp(exp);
		player.setFireTicks(fireTicks);
		player.setFlySpeed(flySpeed);
		player.setFlying(flying);
		player.setFoodLevel(foodLevel);
		player.setGameMode(gameMode);
		player.setHealth(health);
		player.getInventory().setArmorContents(armorContents);
		player.getInventory().setContents(contents);
		player.setLevel(level);
		player.setSaturation(saturation);
		player.setTotalExperience(totalExperience);
	}
}