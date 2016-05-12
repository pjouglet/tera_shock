package tera.gameserver.model.npc.interaction;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации условия отображения ссылки игроку.
 * 
 * @author Ronn
 */
public interface Condition
{
	public static final Condition[] EMPTY_CONDITIONS = new Condition[0];
	
	/**
	 * Проверка на условия для отображения ссылки.
	 * 
	 * @param npc нпс, у которого находится ссылка.
	 * @param player игрок, запрашиваемый ссылки нпс.
	 * @return нужно ли отображать ссылку.
	 */
	public boolean test(Npc npc, Player player);
}
