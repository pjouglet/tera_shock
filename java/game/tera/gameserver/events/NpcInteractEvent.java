package tera.gameserver.events;

import rlib.util.array.Array;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для регистрируемых ивентов
 *
 * @author Ronn
 * @created 11.04.2012
 */
public interface NpcInteractEvent
{
	/**
	 * Добавить ссылки от ивента.
	 *
	 * @param links контейнер ссылок.
	 * @param npc нпс с которым говорит игрок.
	 * @param player игрок
	 */
	public void addLinks(Array<Link> links, Npc npc, Player player);
}
