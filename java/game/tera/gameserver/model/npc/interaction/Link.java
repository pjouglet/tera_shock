package tera.gameserver.model.npc.interaction;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.replyes.Reply;
import tera.gameserver.model.playable.Player;

/**
 * Модель ссылки в диалоге.
 * 
 * @author Ronn
 */
public interface Link
{
	/**
	 * @return ид иконки.
	 */
	public int getIconId();

	/**
	 * @return уникальный ид линка.
	 */
	public int getId();

	/**
	 * @return название ссылки.
	 */
	public String getName();
	
	/**
	 * @return ответ на ссылку.
	 */
	public Reply getReply();

	/**
	 * @return тип ссылки.
	 */
	public LinkType getType();
	
	/**
	 * Обработка ответа на ссылку.
	 * 
	 * @param npc нпс, у которого была нажата ссылка.
	 * @param player игрок, который нажал на ссылку.
	 */
	public void reply(Npc npc, Player player);
	
	/**
	 * @return нужно ли отображать ссылку игроку.
	 */
	public boolean test(Npc npc, Player player);
}
