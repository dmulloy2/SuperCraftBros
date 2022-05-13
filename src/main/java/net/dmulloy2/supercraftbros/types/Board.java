package net.dmulloy2.supercraftbros.types;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * @author dmulloy2
 */

public class Board
{
	private boolean xaxis;

	private final ArenaPlayer ap;
	private final ArenaField field;

	public Board(Arena arena, ArenaPlayer ap)
	{
		ArenaData data = arena.getData();
		this.field = new ArenaField(data.getBoardMaxLocations().get(ap.getId()), data.getBoardMinLocations().get(ap.getId()));
		this.ap = ap;
		this.setup();
	}

	private void setup()
	{
		for (int i = 1; i < 4; i++)
		{
			Material material = field.getBlockAt(i, 0, 0).getType();
			if (material == Material.GLOWSTONE || material == Material.CLAY)
			{
				xaxis = true;
				break;
			}
		}

		if (xaxis)
		{
			for (int i = 0; i < field.getLength(); i++)
			{
				Block block = field.getBlockAt(i, 0, 0);
				block.setType(Material.GLOWSTONE);
				block.getState().update();
			}
		}
		else
		{
			for (int i = 0; i < field.getWidth(); i++)
			{
				Block block = field.getBlockAt(0, 0, i);
				block.setType(Material.GLOWSTONE);
				block.getState().update();
			}
		}
	}

	public final void clear()
	{
		if (xaxis)
		{
			for (int i = 0; i < field.getLength(); i++)
			{
				Block block = field.getBlockAt(i, 0, 0);
				block.setType(Material.CLAY);
				block.getState().update();
			}
		}
		else
		{
			for (int i = 0; i < field.getWidth(); i++)
			{
				Block block = field.getBlockAt(0, 0, i);
				block.setType(Material.CLAY);
				block.getState().update();
			}
		}
	}

	public final void update()
	{
		if (xaxis)
		{
			for (int i = 0; i < ap.getLives(); i++)
			{
				Block block = field.getBlockAt(i, 0, 0);
				block.setType(Material.GLOWSTONE);
				block.getState().update();
			}

			for (int i = ap.getLives(); i < field.getLength(); i++)
			{
				Block block = field.getBlockAt(i, 0, 0);
				block.setType(Material.CLAY);
				block.getState().update();
			}
		}
		else
		{
			for (int i = 0; i < ap.getLives(); i++)
			{
				Block block = field.getBlockAt(0, 0, i);
				block.setType(Material.GLOWSTONE);
				block.getState().update();
			}

			for (int i = ap.getLives(); i < field.getWidth(); i++)
			{
				Block block = field.getBlockAt(0, 0, i);
				block.setType(Material.CLAY);
				block.getState().update();
			}
		}
	}
}