package net.dmulloy2.supercraftbros.types;

import lombok.AllArgsConstructor;
import net.dmulloy2.types.LazyLocation;
import net.dmulloy2.util.Util;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * @author dmulloy2
 */

@AllArgsConstructor
public class ArenaField 
{
	private LazyLocation max;
	private LazyLocation min;

	public boolean isInside(LazyLocation loc)
	{
		if (loc.getX() <= max.getX() && loc.getX() >= min.getX())
		{
			if (loc.getY() <= max.getY() && loc.getY() >= min.getY())
			{
				return loc.getZ() <= max.getZ() && loc.getZ() >= min.getZ();
			}
		}
		
		return false;
	}

	public final boolean isInside(Location loc)
	{
		return isInside(new LazyLocation(loc));
	}

	public final LazyLocation getRandomLocation(int y)
	{
		int length = max.getX() - min.getX();
		int width = max.getZ() - min.getZ();
		
		int randx = Util.random(length);
		int randz = Util.random(width);
		
		return new LazyLocation(max.getWorld(), min.getX() + randx, y, min.getZ() + randz);
	}
	
	public final Block getBlockAt(int xoffset, int yoffset, int zoffset)
	{
		return min.getLocation().add(xoffset, yoffset, zoffset).getBlock();
	}

	public final int getLength()
	{
		return max.getX() - min.getX();
	}
	
	public final int getWidth()
	{
		return max.getZ() - min.getZ();
	}
	
	public final int getHeight()
	{
		return max.getY() - min.getY();
	}
}