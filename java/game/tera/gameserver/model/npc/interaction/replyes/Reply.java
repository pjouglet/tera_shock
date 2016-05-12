package tera.gameserver.model.npc.interaction.replyes;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации ответа на нажатие ссылки в НПС диалоге.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public interface Reply
{
	/**
	 * Ответ на нажатие указаной ссылки.
	 * 
	 * @param npc нпс, у которого нажали на ссылку.
	 * @param player игрок, который нажал ссылку.
	 * @param link ссылка, на которую нажали.
	 */
	public void reply(Npc npc, Player player, Link link);
}
