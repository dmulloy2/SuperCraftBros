package net.dmulloy2.supercraftbros.listeners;

import net.dmulloy2.supercraftbros.SuperCraftBros;
import net.dmulloy2.supercraftbros.types.Arena;
import net.dmulloy2.supercraftbros.types.Arena.Mode;
import net.dmulloy2.supercraftbros.types.ArenaPlayer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author dmulloy2
 */

public class EntityListener implements Listener
{
	private final SuperCraftBros plugin;
	public EntityListener(SuperCraftBros plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityShootBow(EntityShootBowEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player)
		{
			Player shooter = (Player)entity;
			if (plugin.isInArena(shooter))
			{
				ArenaPlayer ap = plugin.getArenaPlayer(shooter);
				if (ap.getArenaClass().getName().equalsIgnoreCase("wither"))
				{
					event.setCancelled(true);
					
					WitherSkull skull = shooter.launchProjectile(WitherSkull.class);
					skull.setVelocity(event.getProjectile().getVelocity());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		Entity damaged = event.getEntity();
		if (damaged instanceof LivingEntity)
		{
			LivingEntity lentity = (LivingEntity)damaged;
			Entity damager = event.getDamager();
			if (damager instanceof WitherSkull)
			{
				lentity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 10, 1, true));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player)
		{
			Player player = (Player)entity;
			if (plugin.isInArena(player))
			{
				Arena a = plugin.getArena(player);
				if (a.getGameMode() == Mode.LOBBY)
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	// ---- Arena Protection ---- //

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityExplode(EntityExplodeEvent event) 
	{
		if (! event.isCancelled())
		{
			if (plugin.isInArena(event.getLocation()))
			{
				if (! event.blockList().isEmpty())
				{
					event.setCancelled(true);
				}
			}
		}
	}
}