package tera.gameserver.model.quests;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Интерфейс для реализации квестовых акшенов.
 * 
 * @author Ronn
 */
public interface QuestAction
{
	/**
	 * Применить акшен с учетом события.
	 * 
	 * @param event событие.
	 */
	public void apply(QuestEvent event);
	
	/**
	 * @return тип акшена.
	 */
	public QuestActionType getType();
	
	/**
	 * Проверка на условия для отображения ссылки.
	 * 
	 * @param npc нпс, у которого находится ссылка.
	 * @param player игрок, запрашиваемый ссылки нпс.
	 * @return нужно ли отображать ссылку.
	 */
	public boolean test(Npc npc, Player player);
}
