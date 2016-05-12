package tera.gameserver.model.items;

/**
 * Перечисление видов расположения итема.
 * 
 * @author Ronn
 */
public enum ItemLocation
{
	INVENTORY,
	EQUIPMENT,
	BANK,
	GUILD_BANK,
	CRYSTAL,
	NONE;
	
	public static final ItemLocation[] VALUES = values();
	
	public static ItemLocation valueOf(int index)
	{
		return VALUES[index];
	}
}
