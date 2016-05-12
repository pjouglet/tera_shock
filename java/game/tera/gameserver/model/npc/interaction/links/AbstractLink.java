package tera.gameserver.model.npc.interaction.links;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.playable.Player;

/**
 * Модель ссылки в диалоге.
 * 
 * @author Ronn
 */
public abstract class AbstractLink implements Link
{
	/** название ссылки */
	protected String name;
	/** тип ссылки */
	protected LinkType type;
	/** иконка ссылки */
	protected IconType icon;
	/** ответ */
	protected Reply reply;
	/** кондишен на отображение ссылки */
	protected Condition condition;
	
	/**
	 * @param name название ссылки.
	 * @param type тип ссылки.
	 * @param icon тип иконки.
	 * @param reply ответ на ссылку.
	 * @param condition условие на отображение ссылки.
	 */
	public AbstractLink(String name, LinkType type, IconType icon, Reply reply, Condition condition)
	{
		this.name = name;
		this.type = type;
		this.reply = reply;
		this.icon = icon;
	}

	/**
	 * @return ид иконки.
	 */
	public int getIconId()
	{
		return icon.ordinal();
	}

	@Override
	public int getId()
	{
		return 0;
	}

	/**
	 * @return название ссылки.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return ответ на ссылку.
	 */
	public Reply getReply()
	{
		return reply;
	}
	
	/**
	 * @return тип ссылки.
	 */
	public LinkType getType()
	{
		return type;
	}

	/**
	 * Обработка ответа на ссылку.
	 * 
	 * @param npc нпс, у которого была нажата ссылка.
	 * @param player игрок, который нажал на ссылку.
	 */
	public void reply(Npc npc, Player player)
	{
		reply.reply(npc, player, this);
	}

	@Override
	public boolean test(Npc npc, Player player)
	{
		return false;
	}
}
