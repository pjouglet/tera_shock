package tera.gameserver.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Модель иконки гильдии.
 * 
 * @author Ronn
 */
public class GuildIcon
{
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH_mm_ss");
	
	/** название иконки */
	private String name;
	/** сама иконка */
	private byte[] icon;
	
	public GuildIcon(String name, byte[] icon)
	{
		this.name = name;
		this.icon = icon;
	}
	
	/**
	 * @return the icon
	 */
	public byte[] getIcon()
	{
		return icon;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return есть ли иконка.
	 */
	public boolean hasIcon()
	{
		return icon != null && icon.length > 4;
	}

	/**
	 * @param icon иконка гильдии.
	 */
	public void setIcon(Guild guild, byte[] icon)
	{
		this.icon = icon;
		this.name = "guildlogo_" + guild.getId() + "_" + timeFormat.format(new Date());
	}
}
