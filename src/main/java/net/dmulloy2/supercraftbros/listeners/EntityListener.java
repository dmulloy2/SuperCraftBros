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
		if (entity instanceof Player shooter)
		{
			ArenaPlayer ap = plugin.getArenaPlayer(shooter);
			if (ap != null)
			{
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
		if (event.getEntity() instanceof LivingEntity damaged)
		{
			Entity damager = event.getDamager();
			if (damager instanceof WitherSkull)
			{
				damaged.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 10, 1, true));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player player)
		{
			ArenaPlayer ap = plugin.getArenaPlayer(player);
			if (ap != null)
			{
				Arena a = ap.getArena();
				if (a.getGameMode() == Mode.LOBBY)
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	// ---- Arena Protection ---- //

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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