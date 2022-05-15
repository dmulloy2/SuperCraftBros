package net.dmulloy2.supercraftbros.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dmulloy2.types.IPermission;

/**
 * @author dmulloy2
 */

@Getter
@AllArgsConstructor
public enum Permission implements IPermission
{
	CMD_CREATE("create"),
	CMD_DELETE("delete"),
	CMD_KICK("kick"),
	CMD_RELOAD("reload"),
	CMD_SPAWN("spawn"),

	BUILD("build"),
	JOIN("join"),
	;
	
	private final String node;
}