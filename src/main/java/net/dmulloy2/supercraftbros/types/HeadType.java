package net.dmulloy2.supercraftbros.types;

import lombok.Getter;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * @author dmulloy2
 */

@Getter
public enum HeadType
{
	// Vanilla
	SKELETON(0, "skeleton"),
	WITHER_SKELETON(1, "wither"),
	ZOMBIE(2, "zombie"),
	PLAYER(3, "player"),
	CREEPER(4, "creeper"),

	// Special heads
    SPIDER("MHF_Spider", "spider"),
    ENDERMAN("MHF_Enderman", "enderman"),
    BLAZE("MHF_Blaze", "blaze"),
    HORSE("gavertoso", "horse"),
    SQUID("MHF_Squid", "squid"),
    SILVERFISH("Xzomag", "silverfish"),
    ENDER_DRAGON("KingEndermen", "enderdragon"),
    SLIME("HappyHappyMan", "slime"),
    IRON_GOLEM("MHF_Golem", "irongolem"),
    MUSHROOM_COW("MHF_MushroomCow", "mooshroom"),
    BAT("bozzobrain", "bat"),
    PIG_ZOMBIE("MHF_PigZombie", "zombiepigman"),
    SNOWMAN("Koebasti", "snowman"),
    GHAST("MHF_Ghast", "ghast"),
    PIG("MHF_Pig", "pig"),
    VILLAGER("MHF_Villager", "villager"),
    SHEEP("MHF_Sheep", "sheep"),
    COW("MHF_Cow", "cow"),
    CHICKEN("MHF_Chicken", "chicken"),
    OCELOT("MHF_Ocelot", "ocelot"),
    WITCH("scrafbrothers4", "wolf"),
    MAGMA_CUBE("MHF_LavaSlime", "magmacube"),
    WOLF("Budwolf", "wolf"),
    CAVE_SPIDER("MHF_CaveSpider", "cavespider"),
    ;

	private final short data;
	private final String name;
	private final String configName;

	private HeadType(String name, String configName)
	{
		this.data = (short) 3;
		this.name = name;
		this.configName = configName;
	}

	private HeadType(int data, String configName)
	{
		this.data = (short) data;
		this.name = "";
		this.configName = configName;
	}

	public static HeadType toHeadType(String configName)
	{
		for (HeadType type : HeadType.values())
		{
			if (type.getConfigName().equalsIgnoreCase(configName))
				return type;
		}

		return null;
	}

	public final ItemStack toItemStack()
	{
		ItemStack ret = new ItemStack(Material.SKULL_ITEM, 1, data);

		if (name != "")
		{
			SkullMeta meta = (SkullMeta) ret.getItemMeta();
			meta.setOwner(name);

			String displayName = FormatUtil.getFriendlyName(configName + " Head");
			meta.setDisplayName(displayName);
			ret.setItemMeta(meta);
		}

		return ret;
	}
}